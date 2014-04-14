package gov.ca.ceres.cmluca.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;

import edu.ucdavis.cstars.client.ESRI;
import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.callback.BufferCallback;
import edu.ucdavis.cstars.client.callback.QueryTaskCountCallback;
import edu.ucdavis.cstars.client.dojo.Color;
import edu.ucdavis.cstars.client.event.ClickHandler;
import edu.ucdavis.cstars.client.event.MouseEvent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.tasks.BufferParameters;
import edu.ucdavis.cstars.client.tasks.GeometryService;
import edu.ucdavis.cstars.client.tasks.GeometryService.UnitType;
import edu.ucdavis.cstars.client.tasks.Query;
import edu.ucdavis.cstars.client.tasks.QueryTask;
import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.gwt.gis.client.AppManager;

public class CmlucaQuery {

    private GeometryService gService = null;
    
    private QueryTask airSpaceQuery = QueryTask.create("http://atlas.resources.ca.gov/ArcGIS/rest/services/Military/SUA_in_WGA_states/MapServer/0");
    private QueryTask flightPathsQuery = QueryTask.create("http://atlas.resources.ca.gov/ArcGIS/rest/services/Military/MTR_corridor_in_WGA_states/MapServer/0");
    private QueryTask militaryBasesQuery = QueryTask.create("http://atlas.resources.ca.gov/ArcGIS/rest/services/Military/DOD_Inst_Ranges/MapServer/0");
    //private QueryTask countyQuery = QueryTask.create("http://atlas.resources.ca.gov/ArcGIS/rest/services/Boundaries/CountyBoundaries/MapServer/0");
    
    private CmlucaQueryPopup popup = new CmlucaQueryPopup();

    private int queryCount = 0;
    private Graphic radiusGraphicSmall = null;
    private Graphic radiusGraphicLarge = null;
    private int radiusShort = 1000;
    private int radiusLong = 4000;
    
    private MapWidget map = null;

    public CmlucaQuery(MapWidget mw) {
            map = mw;
            
            map.addControl(popup);
            
            gService = GeometryService.create(AppManager.INSTANCE.getConfig().getGeometryServer());

            //identify proxy page to use if the toJson payload to the geometry service is greater than 2000 characters.
            //If this null or not available the buffer operation will not work.  Otherwise it will do a http post to the proxy.
            //ESRI.setProxyUrl("http://cmluca.projects.atlas.ca.gov/esri/proxy.php");
            //ESRI.alwaysUseProxy(false);

            map.addClickHandler(new ClickHandler(){
                @Override
                public void onClick(MouseEvent event) {
                    if( event.getGraphic() != null ) {
                        popup.show();
                    } else {
                        Point pt = event.getMapPoint();
                        pt = (Point) Geometry.webMercatorToGeographic(pt);
                        query("Map Point: "+format(pt.getY())+", "+format(pt.getX()), event.getMapPoint());
                    }
                    
                }
            });
    }
    
    private final native double format(double num) /*-{
        return num.toFixed(4);
    }-*/;
    
    public void query(String name, Geometry geom) {
        reset();
        getBuffer(geom);
        
        popup.loading(name);
        popup.show();
    }
    
    private void getBuffer(Geometry geom) {
        BufferParameters params = BufferParameters.create();
        params.setDistances(new int[] {radiusShort, radiusLong });
        params.setUnit(UnitType.UNIT_FOOT);
        params.setGeometries(new Geometry[] { geom });
        params.setOutSpatialRefernce(map.getSpatialReference());
        
        // get a buffered geometry
        gService.buffer(params, new BufferCallback(){
                @Override
                public void onBufferComplete(JsArray<Geometry> geometries) {
                        Polygon p1 = (Polygon) geometries.get(0);
                        Polygon p2 = (Polygon) geometries.get(1);
                        
                        radiusGraphicSmall = createSmallRadiusGraphic(p1);
                        radiusGraphicLarge = createLargeRadiusGraphic(p2);
                        
                        map.getGraphics().add(radiusGraphicLarge);
                        map.getGraphics().add(radiusGraphicSmall);

                        popup.setMap(radiusGraphicSmall, radiusGraphicLarge);
                        execute(p1, false);
                        execute(p2, true);
                }
                @Override
                public void onError(Error error) {
                    Window.alert("Error getting the buffered geometry");
                }
        });
    }
    
    private void reset() {
        if( radiusGraphicSmall != null ) map.getGraphics().remove(radiusGraphicSmall);
        if( radiusGraphicLarge != null ) map.getGraphics().remove(radiusGraphicLarge);
        
        queryCount = 6;
    }
    
    private void execute(Geometry geom, final boolean isLarge) {
            Query q = Query.create();
            q.setGeometry(geom);

            airSpaceQuery.executeForCount(q, new QueryTaskCountCallback(){
                    @Override
                    public void onExecuteForCountComplete(int count) {
                        if( isLarge ) popup.setAirSpace4000(count > 0);
                        else popup.setAirSpace1000(count > 0);
                        queryCount--;
                    }
                    @Override
                    public void onError(Error error) {
                        queryCount--;
                    }
            });
            
            flightPathsQuery.executeForCount(q, new QueryTaskCountCallback(){
                    @Override
                    public void onExecuteForCountComplete(int count) {
                        if( isLarge ) popup.setFlightPath4000(count > 0);
                        else popup.setFlightPath1000(count > 0);
                        queryCount--;
                    }
                    @Override
                    public void onError(Error error) {
                        queryCount--;
                    }
            });
            
            militaryBasesQuery.executeForCount(q, new QueryTaskCountCallback(){
                    @Override
                    public void onExecuteForCountComplete(int count) {
                        if( isLarge ) popup.setMilitaryBase4000(count > 0);
                        else popup.setMilitaryBase1000(count > 0);
                        queryCount--;
                    }
                    @Override
                    public void onError(Error error) {
                        queryCount--;
                    }
            });
            
    }
    
    private Graphic createSmallRadiusGraphic(Polygon p){
            PolyStyleConfig style = ((CmlucaConfig) AppManager.INSTANCE.getConfig()).getSmallRadiusStyle();

            SimpleFillSymbol fill = SimpleFillSymbol.create(
                            SimpleFillSymbol.StyleType.STYLE_SOLID,
                            SimpleLineSymbol.create(
                                            SimpleLineSymbol.StyleType.STYLE_SOLID,
                                            Color.create(style.getOutlineColor()),
                                            1
                            ),
                            Color.create(style.getFillColor())
            );
            return Graphic.create(p, fill);
    }
    
    private Graphic createLargeRadiusGraphic(Polygon p){
            PolyStyleConfig style = ((CmlucaConfig) AppManager.INSTANCE.getConfig()).getLargeRadiusStyle();
            SimpleFillSymbol fill = SimpleFillSymbol.create(
                            SimpleFillSymbol.StyleType.STYLE_SOLID,
                            SimpleLineSymbol.create(
                                            SimpleLineSymbol.StyleType.STYLE_SOLID,
                                            Color.create(style.getOutlineColor()),
                                            1
                            ),
                            Color.create(style.getFillColor())
            );              
            return Graphic.create(p, fill);
    }

}
