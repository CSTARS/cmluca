package gov.ca.ceres.cmluca.client;

import java.util.HashMap;
import java.util.LinkedList;

import com.google.gwt.user.client.Window;

import edu.ucdavis.cstars.client.ESRI;
import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.callback.PrintTaskCallback;
import edu.ucdavis.cstars.client.tasks.PrintParameters;
import edu.ucdavis.cstars.client.tasks.PrintTask;
import edu.ucdavis.cstars.client.tasks.PrintTemplate;
import edu.ucdavis.cstars.client.tasks.PrintTask.PrintResult;
import edu.ucdavis.cstars.client.tasks.PrintTemplate.Format;
import edu.ucdavis.cstars.client.tasks.PrintTemplate.Layout;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;
import edu.ucdavis.gwt.gis.client.toolbar.button.ToolbarItem;

public class Print {

    public static final Print INSTANCE = new Print();
    
    private static PrintTask printTask;
    private MenuButton menuButton = new MenuButton();
    
    private static final String NO_INTERSECT = "Your project location does not intersect with any military bases, special use airspaces, or low level flight paths.  " +
    		"If you are submitting a project permit application, please provide this information to your local planning agency as part of your permit application.  " +
    		"A copy of your permit application for this project does not have to be sent to the U.S. Military, per Government Codes 65352, 65940, and 65944.";
    
    private static final String INTERSECT = "Your project location intersects with the identified layers.  Please provide this information to your local planning agency " +
    		"as part of your permit application.   A copy of your permit application must be sent by the city/county to the appropriate branch(es) of the U.S. Military, per " +
    		"Government Codes 65352, 65940, and 65944.";
    
    public interface PrintTaskComplete {
        public void onComplete();
    }
    
    protected Print() {}
    
    private static String title = "";
    private static boolean requiresAction;
    private static HashMap<String, String> requiresActionText;
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setServer(String server) {
        Debugger.INSTANCE.log("CMLUCA: 1.1: "+server);
        ESRI.addCorsEnabledServers(server.replaceAll(".*:\\/\\/","").replaceAll("\\/.*",""));
        Debugger.INSTANCE.log("CMLUCA: 1.2");
        printTask = PrintTask.create(server);
        Debugger.INSTANCE.log("CMLUCA: 1.3");
    }
    
    public static void exec(String title, boolean requiresAction) {
        exec(title, requiresAction, requiresActionText, null);
    }
    
    public static void exec(String t, boolean ra, HashMap<String, String> raText, final PrintTaskComplete callback) {
        title = t;
        requiresAction = ra;
        requiresActionText = raText;
        
        PrintParameters params = PrintParameters.create();
        params.setMap(AppManager.INSTANCE.getMap());
        
        
        PrintTemplate template = PrintTemplate.create();
        //template.setFormat(Format.PNG_32);
        
        PrintTemplate.LayoutOptions layoutOptions = PrintTemplate.LayoutOptions.create();
        
        layoutOptions.setTitleText("CMLUCA Report - "+(requiresAction ? "Requires Action" : "No Action Required")+" \n \n\n"+title);
        //layoutOptions.setScalebarUnit("Meters");
        
        
        //layoutOptions.setLegendLayers(getLegends());
        
        /*[300,300],"dpi":96},"layoutOptions":{"titleText":"CMLUCA Report ­ Requires Action \n \nLat: 3901139.7299006963

\nLng:­12976575.483712692","authorText":"Your project location intersects with the above military layers. \nPlease provide the above

information to your local planning agency \nas part of your permit application.","copyrightText":"A copy of your permit application must be sent by

the city/county to the appropriate branch(es) of the U.S. Military, per Government Codes 65352, 65940, and 65944.","customTextElements":

[{"AirSpace1":"Input for AirSpace1"},{"AirSpace2":"Input for AirSpace2"},{"FlightPath1":"Input for FlightPath1"},{"FlightPath2":"Input for

FlightPath2"},{"MilitaryBase1":"Input for MilitaryBase1"},{"MilitaryBase2":"Input for MilitaryBase2"},{"CMLUCAtext":"Your project location

intersects with the above military layers. \nPlease provide the above information to your local planning agency \nas part of your permit

application.  \n \nA copy of your permit application must be sent by the city/county to \nthe appropriate branch(es) of the U.S. Military, per

Government \nCodes 65352, 65940, and 65944."}],"scaleBarOptions":*/
        
        if( requiresAction ) {
            layoutOptions.setCustomTextElement("CMLUCAtext", INTERSECT);
        } else {
            layoutOptions.setCustomTextElement("CMLUCAtext", NO_INTERSECT);
        }
        
        for( String key: requiresActionText.keySet() ) {
            layoutOptions.setCustomTextElement(key, requiresActionText.get(key));
        }
        
        template.setFormat(Format.PDF);
        template.setLayoutOptions(layoutOptions);
        
        template.setLayout(Layout.UNKNOWN);

        
        
        template.setExportOptions(PrintTemplate.ExportOptions.create(300, 300, 96));
        params.setPrintTemplate(template);
        
        printTask.execute(params, 
            new PrintTaskCallback(){
                @Override
                public void onComplete(PrintResult result) {
                    if( callback != null ) callback.onComplete();
                    Window.open(result.getUrl(), "_blank", "");
                }
                @Override
                public void onError(Error error) {
                    if( callback != null ) callback.onComplete();
                    Window.alert("Error: "+error.getMessage());
                }
        });
    }
    
    public static String[] getLegends() {
        LinkedList<DataLayer> layers = AppManager.INSTANCE.getDataLayers();
        String[] legends = new String[layers.size()];
        for( int i = 0; i < layers.size(); i++ ) legends[i] = layers.get(i).getId();
        return legends;
    }
    
    public ToolbarItem getToolbarItem() {
        return menuButton;
    }
    
    public static class MenuButton extends ToolbarItem {

        @Override
        public String getIcon() {
            return "";
        }

        @Override
        public String getText() {
            return "Print Report";
        }

        @Override
        public void onClick() {
            exec(title, requiresAction);
        }

        @Override
        public void onAdd(Toolbar toolbar) {}
        
    }
}
