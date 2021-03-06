package gov.ca.ceres.cmluca.client;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.MapWidget.BaseMap;
import edu.ucdavis.cstars.client.control.Control;
import edu.ucdavis.cstars.client.control.Position;
import edu.ucdavis.cstars.client.event.MapLoadHandler;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.restful.RestfulLayerInfo;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.canvas.CanvasGeometry;
import edu.ucdavis.gwt.gis.client.canvas.CanvasMap;
import edu.ucdavis.gwt.gis.client.canvas.CanvasPoint;
import edu.ucdavis.gwt.gis.client.canvas.CanvasPolygon;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;
import edu.ucdavis.gwt.gis.client.toolbar.button.ToolbarItem;

import gov.ca.ceres.cmluca.client.Print.PrintTaskComplete;

public class CmlucaQueryPopup extends Control  {

    private static CmlucaQueryPopupUiBinder uiBinder = GWT.create(CmlucaQueryPopupUiBinder.class);
    interface CmlucaQueryPopupUiBinder extends  UiBinder<Widget, CmlucaQueryPopup> {}

    private Widget panel = null;
    
    @UiField HTML airSpace1000Icon;
    @UiField HTML airSpace1000Text;
    @UiField HTML airSpace4000Icon;
    @UiField HTML airSpace4000Text;
    @UiField HTML flightPath1000Icon;
    @UiField HTML flightPath1000Text;
    @UiField HTML flightPath4000Icon;
    @UiField HTML flightPath4000Text;
    @UiField HTML militaryBase1000Icon;
    @UiField HTML militaryBase1000Text;
    @UiField HTML militaryBase4000Icon;
    @UiField HTML militaryBase4000Text;
    @UiField HTML location;
    @UiField HTML smallBufferLegend;
    @UiField HTML largeBufferLegend;
    
    @UiField SimplePanel map;
    @UiField Anchor closeBtn;
    
    private int mapWidth = 75;
    private int mapHeight = 75;
    private String baselayer = "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer";
    
    private HTML[] textFields = null;
    private HTML[] iconFields = null;
    
    private FlowPanel footer = new FlowPanel();
    private Button print = new Button("<i class='icon-print'></i> Print Report");
    private Button toolbarPrint = new Button("<i class='icon-print'></i> <span class=\"visible-desktop\">Print Report</span>");
    private Button close = new Button("Close");
    
    private boolean printDisabled = true;
    private boolean requiresAction = false;
    private int actionCount = 0;
    
    private HashMap<String, String> intersectText = new HashMap<String, String>();
    
    private SimplePanel root = new SimplePanel();
    
    public CmlucaQueryPopup() {
        panel = uiBinder.createAndBindUi(this);
        textFields = new HTML[] {airSpace1000Text, airSpace4000Text, flightPath1000Text, flightPath4000Text,
                militaryBase1000Text, militaryBase4000Text};
        iconFields = new HTML[] {airSpace1000Icon, airSpace4000Icon, flightPath1000Icon, flightPath4000Icon,
                militaryBase1000Icon, militaryBase4000Icon};
        
        print.addStyleName("btn");
        print.addStyleName("btn-primary");
        print.addStyleName("disabled");
        print.addClickHandler(onPrintClicked);
        
        toolbarPrint.addStyleName("btn");
        toolbarPrint.addStyleName("disabled");
        toolbarPrint.addClickHandler(onPrintClicked);
        
        AppManager.INSTANCE.getClient().getToolbar().addToolbarItem(toolbarPrint);
        
        smallBufferLegend.setHTML("1000ft Buffer: <span style='width:15px;height:15px;margin:5px;display:inline-block;background-color:"+
                cssFromObject(((CmlucaConfig) AppManager.INSTANCE.getConfig()).getSmallRadiusStyle().getFillColor())+";border:1px solid "+
                cssFromObject(((CmlucaConfig) AppManager.INSTANCE.getConfig()).getSmallRadiusStyle().getOutlineColor())+"'>&nbsp;</span>");
        
        largeBufferLegend.setHTML("4000ft Buffer: <span style='width:15px;height:15px;margin:5px;display:inline-block;background-color:"+
                cssFromObject(((CmlucaConfig) AppManager.INSTANCE.getConfig()).getLargeRadiusStyle().getFillColor())+";border:1px solid "+
                cssFromObject(((CmlucaConfig) AppManager.INSTANCE.getConfig()).getLargeRadiusStyle().getOutlineColor())+"'>&nbsp;</span>");
        
        closeBtn.setHTML("<i class=\"icon-remove\"></i>");
        closeBtn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        close.addStyleName("btn");
        close.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        footer.add(print);
        footer.add(close);
        
        root.add(panel);
        initWidget(root);
        
        setStyleName("cmluca-popup");
        
        
    }
    
    private void hide() {
        setVisible(false);
    }
    
    public void show() {
        setVisible(true);
        setPosition(135, 10, Position.TOP_LEFT);
    }
    
    private ClickHandler onPrintClicked = new ClickHandler(){
        @Override
        public void onClick(ClickEvent event) {
            if( printDisabled ) return;
            
            disablePrint(true);
            
            print.setHTML("<i class='icon-spinner icon-spin'></i> Generating Report...");
            toolbarPrint.setHTML("<i class='icon-spinner icon-spin'></i> Generating...");
            Print.exec(location.getText().replace("Map Point", "LatLng"), requiresAction, intersectText, new PrintTaskComplete(){
                @Override
                public void onComplete() {
                    print.setHTML("<i class='icon-print'></i> Print Report");
                    toolbarPrint.setHTML("<i class='icon-print'></i> <span class=\"visible-desktop\">Print Report</span>");
                    disablePrint(false);
                }
            });
        }
        
    };
    
    private void disablePrint(boolean disabled) {
        if( disabled ) {
            print.addStyleName("disabled");
            toolbarPrint.addStyleName("disabled");
        } else {
            print.removeStyleName("disabled");
            toolbarPrint.removeStyleName("disabled");
        }
        printDisabled = disabled;
    }
    
    private final native String cssFromObject(JavaScriptObject jso) /*-{
        return "rgba("+jso.r+","+jso.g+","+jso.b+","+jso.a+")";
    }-*/;


    public void loading(String location) {
        disablePrint(true);
        requiresAction = false;
        actionCount = 0;
        
        intersectText = new HashMap<String, String>();
        this.location.setHTML(location);
        for( int i = 0; i < textFields.length; i++ ) {
            textFields[i].setHTML("");
        
        }
        for( int i = 0; i < iconFields.length; i++ ) iconFields[i].setHTML("<i class='icon-spinner icon-spin'></i>");
        map.clear();
        map.add(new HTML("Buffering geometry..."));
    }
    
    public void setMap(final Graphic b1, final Graphic b2) {
        map.clear();
        CanvasMap canvas = new CanvasMap(mapHeight, mapWidth);
        canvas.getCanvas().getElement().getStyle().setProperty("border", "1px solid #ccc");
        
        Extent ext = ((Polygon) b2.getGeometry()).getExtent();
        
        SimpleFillSymbol fill = (SimpleFillSymbol) b1.getSymbol();
        Polygon poly1 = (Polygon) Geometry.toScreenGeometry(
                ext, mapWidth, mapHeight, b1.getGeometry());
        CanvasPolygon cg1 = new CanvasPolygon(poly1, 
                fill.getOutline().getColor().toCss(true), fill.getColor().toCss(true));
        cg1.setLineWidth(1);
        
        fill = (SimpleFillSymbol) b2.getSymbol();
        Polygon poly2 = (Polygon) Geometry.toScreenGeometry(
                ext, mapWidth, mapHeight, b2.getGeometry());
        CanvasPolygon cg2 = new CanvasPolygon(poly2, 
                fill.getOutline().getColor().toCss(true), fill.getColor().toCss(true));
        cg2.setLineWidth(1);
        
        int wkid = ext.getSpatialReference().getWkid();
        String sr = "";
        if( wkid > 0 ) sr = String.valueOf(wkid);
        String url = baselayer+"/export?bbox="+getBboxForExtent(ext, mapWidth, mapHeight) + 
                "&format=png&transparent=true&f=image&imageSR="+sr+"&bboxSR="+sr+"&size="+mapWidth+","+mapHeight;
        CanvasPoint basemap = new CanvasPoint(0, 0, url);
        
        //canvas.addGeometry(basemap);
        
        canvas.addGeometry(cg2);
        canvas.addGeometry(cg1);
        
        map.add(canvas.getCanvas());
        canvas.redraw();
    }
    
    private String getBboxForExtent(Extent extent, int width, int height) {
        double x = extent.getXMin() + ((extent.getXMax() - extent.getXMin()) / 2);
        double y = extent.getYMin() + ((extent.getYMax() - extent.getYMin()) / 2);
        
        double s1 = (extent.getXMax() - extent.getXMin()) / width;
        double s2 = (extent.getYMax() - extent.getYMin()) / height;
        
      //  double newWidth = s1 * (width * 0.000254);
      //  double newHeight = s2 * (height * 0.000254);

        
        /*double xMin = x - (newWidth / 2);
        double xMax = x + (newWidth / 2);
        double yMin = y - (newHeight / 2);
        double yMax = y + (newHeight / 2);*/
        
        double xMin = x - (s1 / 2);
        double xMax = x + (s1 / 2);
        double yMin = y - (s2 / 2);
        double yMax = y + (s2 / 2);
        
        return String.valueOf(xMin) + "," + yMin + "," + String.valueOf(xMax) + "," + yMax;
    }
    
    public void setAirSpace1000(boolean inside) {
        set(inside, 1000, airSpace1000Text, airSpace1000Icon);
        if( inside ) {
            intersectText.put("AirSpace1", "Within 1000ft");
        } else {
            intersectText.put("AirSpace1", "Outside 1000ft");
        }
    }
    
    public void setAirSpace4000(boolean inside) {
        set(inside, 4000, airSpace4000Text, airSpace4000Icon);
        if( inside ) {
            intersectText.put("AirSpace2", "Within 4000ft");
        } else {
            intersectText.put("AirSpace2", "Outside 4000ft");
        }
    }
    
    public void setFlightPath1000(boolean inside) {
        set(inside, 1000, flightPath1000Text, flightPath1000Icon);
        if( inside ) {
            intersectText.put("FlightPath1", "Within 4000ft");
        } else {
            intersectText.put("FlightPath1", "Outside 1000ft");
        }
    }
    
    public void setFlightPath4000(boolean inside) {
        set(inside, 4000, flightPath4000Text, flightPath4000Icon);
        if( inside ) {
            intersectText.put("FlightPath2", "Within 4000ft");
        } else {
            intersectText.put("FlightPath2", "Outside 4000ft");
        }
    }
    
    public void setMilitaryBase1000(boolean inside) {
        set(inside, 1000, militaryBase1000Text, militaryBase1000Icon);
        if( inside ) {
            intersectText.put("MilitaryBase1", "Within 1000ft");
        } else {
            intersectText.put("MilitaryBase1", "Outside 1000ft");
        }
    }
    
    public void setMilitaryBase4000(boolean inside) {
        set(inside, 4000, militaryBase4000Text, militaryBase4000Icon);
        if( inside ) {
            intersectText.put("MilitaryBase2", "Within 4000ft");
        } else {
            intersectText.put("MilitaryBase2", "Outside 4000ft");
        }
    }
    
    private void set(boolean inside, int distance, HTML text, HTML icon) {
        actionCount++;
        
        if( inside ) {
            requiresAction = true;
            text.setHTML(" <b>is inside "+distance+"ft</b>");
            icon.setHTML(" <i class='icon-ok' style='color:green'></i>");
        } else {
            text.setHTML(" is outside "+distance+"ft");
            icon.setHTML(" <i class='icon-remove' style='color:red'></i>");
        }
        
        if( actionCount == 6) disablePrint(false);
    }

    @Override
    public void init(MapWidget mapWidget) {
        hide();
    }
    
    
}
