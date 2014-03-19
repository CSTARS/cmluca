package gov.ca.ceres.cmluca.client;

import com.google.gwt.core.client.EntryPoint;

import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.GisClient.GisClientLoadHandler;
import edu.ucdavis.gwt.gis.client.toolbar.menu.BasemapMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.HelpMenu;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CMLUCA implements EntryPoint {

    private GisClient mapClient = null;
    private SearchBox searchBox = null;
    
    public void onModuleLoad() {
        
        /*GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){
            @Override
            public void onUncaughtException(Throwable e) {
                String stackTrace = "";
                for( int i = 0; i < e.getStackTrace().length; i++ ) {
                    StackTraceElement ele = e.getStackTrace()[i];
                    stackTrace += ele.toString()+"<br />";
                }
                Debugger.INSTANCE.catchException(e, "Search", "Uncaught exception in CalAtlasMaps<br /><b>Trace</b><br>"+stackTrace);
            }
        });*/
        
        injectMobileMetaTag();
        

        mapClient = new GisClient();
                
        mapClient.load(new GisClientLoadHandler(){
            @Override
            public void onLoad() {
                onClientReady();
            }
        });

        
    }
    

        
    public void onClientReady() {

        Debugger.INSTANCE.log("CMLUCA: onClientReady()");
        
        mapClient.getToolbar().addToolbarMenu(new BasemapMenu());
        
        //ExportMenu export = new ExportMenu();
        Debugger.INSTANCE.log("CMLUCA: 1");

        Print.INSTANCE.setServer(AppManager.INSTANCE.getConfig().getPrintConfig().getServer());
        //export.addItem(Print.INSTANCE.getToolbarItem());
        
        Debugger.INSTANCE.log("CMLUCA: 2");
        //mapClient.getToolbar().addToolbarMenu(export);
        
        
        mapClient.getToolbar().addToolbarMenu(new HelpMenu());
        
        Debugger.INSTANCE.log("CMLUCA: 3");

        if( GisClient.isIE7() || GisClient.isIE8() ) {
            mapClient.getRootPanel().getElement().getStyle().setProperty("border", "1px solid #aaaaaa");
        }
        
        Debugger.INSTANCE.log("CMLUCA: 4");
        
        searchBox = new SearchBox();
        mapClient.getToolbar().setSearchBox(searchBox);
        searchBox.setMap(mapClient.getMapWidget());
        
        Debugger.INSTANCE.log("CMLUCA: onClientReady()");

        mapClient.expandLegends(true);
    }
    
    
    private native void injectMobileMetaTag() /*-{
        try {
            var head = $wnd.document.getElementsByTagName('head')[0];
            
            var meta = $wnd.document.createElement('meta');
            meta.setAttribute('name', 'viewport');
            meta.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=1.0; user-scalable=0;');
            head.appendChild(meta);
            
            meta = $wnd.document.createElement('meta');
            meta.setAttribute('name', 'viewport');
            meta.setAttribute('content', 'width=device-width');
            head.appendChild(meta);
            
            meta = $wnd.document.createElement('meta');
            meta.setAttribute('name', 'HandheldFriendly');
            meta.setAttribute('content', 'True');
            head.appendChild(meta);
        } catch (e) {}
    }-*/;

}
