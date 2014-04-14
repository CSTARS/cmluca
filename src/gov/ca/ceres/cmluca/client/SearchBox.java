package gov.ca.ceres.cmluca.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.callback.AddressToLocationsCallback;
import edu.ucdavis.cstars.client.callback.QueryTaskCallback;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.geometry.Polygon;
import edu.ucdavis.cstars.client.layers.GraphicsLayer;
import edu.ucdavis.cstars.client.tasks.Address;
import edu.ucdavis.cstars.client.tasks.AddressCandidate;
import edu.ucdavis.cstars.client.tasks.FeatureSet;
import edu.ucdavis.cstars.client.tasks.Locator;
import edu.ucdavis.cstars.client.tasks.Query;
import edu.ucdavis.cstars.client.tasks.QueryTask;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.config.SearchServiceConfig;
import edu.ucdavis.gwt.gis.client.toolbar.GeocodeResultsPanel;

public class SearchBox extends TextBox {
    
    private static LinkedList<Locator> locators = new LinkedList<Locator>();
    private static LinkedList<CmlucaQueryTask> queries = new LinkedList<CmlucaQueryTask>();
    
    private GraphicsLayer graphicsLayer;
    private Graphic marker = null;
    
    //private VerticalPanel panel = new VerticalPanel();
    private GeocodeResultsPanel resultsPopup = new GeocodeResultsPanel(this);
    //private TextBox tb = new TextBox();
        
    private LinkedList<AddressCandidate> locs = null;
    
    private AddressCandidate currentPlace = null;
    private QueryAll currentQuery = null;
    private MapWidget map = null;
    private CmlucaQuery cmlucaQuery = null;
    
    private Runnable searchHandler;
    
    public SearchBox() {
        Debugger.INSTANCE.log("CMLUCA: 4.1");
        JsArray<SearchServiceConfig> searchServices = ((CmlucaConfig) AppManager.INSTANCE.getConfig()).getSearchServices();
        for( int i = 0; i < searchServices.length(); i++ ) {
            if( searchServices.get(i).getType().equals("geocoder") ) {
                locators.add(Locator.create(searchServices.get(i).getUrl()));
            } else {
                CmlucaQueryTask cqt = (CmlucaQueryTask) QueryTask.create(searchServices.get(i).getUrl());
                cqt.setParameter(searchServices.get(i).getParameter());
                cqt.setFormatter(searchServices.get(i).getFormatter());
                queries.add(cqt);
            }
        }

        Debugger.INSTANCE.log("CMLUCA: 4.2");
        getElement().setAttribute("placeholder", "Enter your Location");
        setStyleName("search-query");

        addKeyUpHandler(new KeyUpHandler(){
            public void onKeyUp(KeyUpEvent event) {
                if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ){
                    String searchTxt = getText().toLowerCase();
                    resultsPopup.clear();
                    search(searchTxt);
                }
            }
        });
        Debugger.INSTANCE.log("CMLUCA: 4.3");
        Window.addResizeHandler(new ResizeHandler(){
            @Override
            public void onResize(ResizeEvent event) {
                resize();
            }
        });
    }
    
    public static LinkedList<CmlucaQueryTask> getQueries() {
        return queries;
    }
    
    public void setMap(MapWidget map) {
        Debugger.INSTANCE.log("CMLUCA: 4 setMap - 1");
        cmlucaQuery = new CmlucaQuery(map);
        this.map = map;
        resize();
        Debugger.INSTANCE.log("CMLUCA: 4 setMap - 2");
    }
    
    
    public native void debug(JavaScriptObject jso) /*-{
        console.log(jso);
    }-*/;
    
    public native void debug(String jso) /*-{
        console.log(jso);
    }-*/;
    
    private void resize() {
        if( AppManager.INSTANCE.getClient().isPhoneMode() ) setWidth("100px");
        else setWidth("150px");
    }

    public void init(MapWidget mapWidget) {     
        map = mapWidget;
        graphicsLayer = map.getGraphics();
    }
    
    public void setCounty(String county) {
        if( county.length() == 0 ) return;
        setText(county);
    }
    
    public void setSearchHandler(Runnable searchHandler) {
        this.searchHandler = searchHandler;
    }

    private void search(String searchTxt) {
        searchHandler.run();
        
        resultsPopup.add(new HTML("<i class='icon-spinner icon-spin'></i> Searching '"+searchTxt+"'... "));
        resultsPopup.show();
        if( currentQuery != null ) currentQuery.cancel();
        
        currentQuery = new QueryAll(searchTxt, new QueryAll.QueryComplete(){
            public void onComplete(LinkedList<SearchResult> results) {
                currentQuery = null;
                
                // sort alphabetically
                Collections.sort(results, new Comparator<SearchResult>(){
                    @Override
                    public int compare(SearchResult o1, SearchResult o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
                
                resultsPopup.clear();
                for( SearchResult r: results ) {
                    resultsPopup.add(createResultBtn(r)); 
                }
                
            }
        });
    }
    
    private HTML createResultBtn(final SearchResult r) {
        HTML btn = new HTML("<div class='cmluca-search-result'>"+
                (r.center == null ? "<i class='icon-check-empty'></i> " : "<i class='icon-map-marker'></i> ")+
                r.name+" <span>"+
                (r.center == null ? "Boundary" : "Point")
                +"</span></div>"); 
        
        btn.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                if( r.center != null ) {
                    cmlucaQuery.query(r.name+" (Centroid)", r.center);
                    map.centerAndZoom(r.center, 12);
                } else {
                    getGeometry(r);
                    resultsPopup.hide();
                }
            }
        });
        
        return btn;
    }
    
    public void getGeometry(SearchResult sr) {
        getGeometry(sr, null);
    }
    
    public void getGeometry(final SearchResult sr,final Runnable callback) {
        CmlucaQueryTask qt = null;
        for( CmlucaQueryTask cqt: queries ) {
            if( cqt.getUrl().contentEquals(sr.url) ) {
                qt = cqt;
                break;
            }
        }
        
        Query q = Query.create();
        q.setOutFields(new String[]{"*"});
        q.setReturnGeometry(true);
        q.setWhere(qt.getParameter()+" = '"+sr.id+"'");
        qt.execute(q, new QueryTaskCallback(){
            @Override
            public void onComplete(FeatureSet featureSet) {
                if( callback != null ) callback.run();
                
                if( featureSet.getFeatures().length() == 0 ) {
                    Window.alert("Failed to fetch intersect geometry");
                    return;
                }
                cmlucaQuery.query(sr.name+" (Boundary)", featureSet.getFeatures().get(0).getGeometry());
                map.setExtent(((Polygon) featureSet.getFeatures().get(0).getGeometry()).getExtent(), true);
            }
            @Override
            public void onError(Error error) {
                if( callback != null ) callback.run();
                debug(error);
                //Window.alert("Failed to fetch intersect geometry");
            }
        });
        
    }
    
    private static class QueryAll {
        int queriesRemaining = 0;
        String searchTxt = "";
        LinkedList<SearchResult> resultList = new LinkedList<SearchResult>();
        boolean cancelled = false;
        
        interface QueryComplete {
            public void onComplete(LinkedList<SearchResult> results);
        }
        private QueryComplete callback = null;
        
        public QueryAll(String searchTxt, QueryComplete callback) {
            queriesRemaining = locators.size()+queries.size();
            this.searchTxt = searchTxt;
            this.callback = callback;
            
            
            for( Locator l: locators ) locate(l);
            for( CmlucaQueryTask qt: queries ) query(qt);
        }
        
        private void locate(final Locator l) {
            
            Address addr = Address.create();
            
            // add california if not already there

            
            addr.setSingleLineInput(searchTxt);
            
            Locator.Parameters params = Locator.Parameters.create();
            params.setAddress(addr);
            params.setOutFields(new String[] {"*"});
            
            l.addressToLocations(params, new AddressToLocationsCallback(){
                @Override
                public void onAddressToLocationsComplete(JsArray<AddressCandidate> candidates) {
                    LinkedList<AddressCandidate> topLocs = getTopLocations(candidates);
                    for( AddressCandidate loc: topLocs ) {
                        SearchResult sr = new SearchResult(loc.getAddressAsString(), loc.getLocation(), l.getUrl(), "");
                        resultList.add(sr);
                    }
                    checkDone();
                }
                @Override
                public void onError(Error error) {
                    checkDone();
                }
            });
        }
        
        private void query(final CmlucaQueryTask qt) {
            
            Query q = Query.create();
            q.setOutFields(new String[]{"*"});
            q.setReturnGeometry(false);
            q.setWhere("UPPER("+qt.getParameter()+") like '"+searchTxt.toUpperCase()+"%'");
            qt.execute(q, new QueryTaskCallback(){
                @Override
                public void onComplete(FeatureSet featureSet) {
                    int c = 0;
                    for( int i = 0; i < featureSet.getFeatures().length(); i++ ) {
                        Graphic.Attributes attrs = featureSet.getFeatures().get(i).getAttributes();
                        resultList.add(new SearchResult(qt.format(attrs), null, qt.getUrl(), attrs.getStringForced(qt.getParameter())));
                        c++;
                        if( c == 5 ) break;
                    }
                    checkDone();
                }
                @Override
                public void onError(Error error) {
                    checkDone();
                }
            });
        }
        
        private void checkDone() {
            queriesRemaining--;
            if( queriesRemaining <= 0 && !cancelled ) callback.onComplete(resultList);
        }
        
        private LinkedList<AddressCandidate> getTopLocations(JsArray<AddressCandidate> list ){
            LinkedList<AddressCandidate> tmpList = new LinkedList<AddressCandidate>();
            LinkedList<AddressCandidate> top = new LinkedList<AddressCandidate>();
            
            // remove all duplicates, but keep the highest score
            for( int i = 0; i < list.length(); i++ ){
                if( !list.get(i).getAddressAsString().matches(".*California.*") &&
                    !list.get(i).getAddressAsString().matches(".*CA.*") ) continue;
                
                AddressCandidate addr = list.get(i);
                boolean found = false;
                for( int j = 0; j < tmpList.size(); j++ ){
                    if( addr.getAddressAsString().contentEquals(tmpList.get(j).getAddressAsString()) ){
                        if( addr.getScore() > tmpList.get(j).getScore() ){
                            tmpList.remove(j);
                            tmpList.add(j, addr);
                        }
                        found = true;
                        break;
                    }
                }
                if( !found ) tmpList.add(addr);
            }
            
            // now grab the top 5 scores
            for( int i = 0; i < tmpList.size(); i++ ){
                AddressCandidate addr = tmpList.get(i);
                boolean found = false;
                for( int j = 0; j < top.size(); j++ ){
                    if( addr.getScore() > top.get(j).getScore() ){
                        top.add(j, addr);
                        found = true;
                        break;
                    }
                }
                if( !found && top.size() <= 5 ) top.add(addr);
                if( top.size() > 5 ) top.removeLast();
            }
            
            return top;
        }
        
        public void cancel() {
            cancelled = true;
        }
    }
    
    public static class SearchResult {
        String name = "";
        Point center = null;
        String url = "";
        String id = "";
        public SearchResult(String name, Point center, String url, String id){
            this.name = name;
            this.center = center;
            this.url = url;
            this.id = id;
        }
    }
    
    public static class CmlucaQueryTask extends QueryTask {
        
        protected CmlucaQueryTask() {}
        
        public final native String getParameter() /*-{
            return this.__cqt_parameter;
        }-*/;
        
        public final native void setParameter(String param) /*-{
            this.__cqt_parameter = param;
        }-*/;
        
        public final String format(Graphic.Attributes attrs) {
            if( hasFormatter() ) return _format(attrs);
            return attrs.getStringForced(getParameter());
        }
        
        private final native String _format(JavaScriptObject attrs) /*-{
            return this.__cqt_formatter(attrs);
        }-*/;
        
        private final native boolean hasFormatter() /*-{
            if( this.__cqt_formatter != null ) return true;
            return false;
        }-*/;
        
        public final native void setFormatter(JavaScriptObject formatter) /*-{
            this.__cqt_formatter = formatter;
        }-*/;
       
    }

}
