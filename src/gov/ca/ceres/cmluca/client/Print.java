package gov.ca.ceres.cmluca.client;

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
    
    private static final String AUTHOR_TEMPLATE_NO_INTERSECT = "Your project location does not intersect with any military bases, special use airspaces, or low level flight paths. If you are submitting a project permit application, please provide the above information to";
    private static final String COPYRIGHT_TEMPLATE_NO_INTERSECT = "your local planning agency as part of your permit application. A copy of your permit application for this project does not have to be sent to the U.S. Military, per Government Codes 65352, 65940, and 65944.";
    
    private static final String AUTHOR_TEMPLATE_INTERSECT = "Your project location intersects with the above military layers. Please provide the above information to your local planning agency as part of your permit application.";
    private static final String COPYRIGHT_TEMPLATE_INTERSECT = "A copy of your permit application must be sent by the city/county to the appropriate branch(es) of the U.S. Military, per Government Codes 65352, 65940, and 65944.";
    
    public interface PrintTaskComplete {
        public void onComplete();
    }
    
    protected Print() {}
    
    private static String title = "";
    private static boolean requiresAction;
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
        exec(title, requiresAction, null);
    }
    
    public static void exec(String t, boolean ra, final PrintTaskComplete callback) {
        title = t;
        requiresAction = ra;
        
        PrintParameters params = PrintParameters.create();
        params.setMap(AppManager.INSTANCE.getMap());
        
        PrintTemplate template = PrintTemplate.create();
        template.setFormat(Format.PNG_32);
        
        PrintTemplate.LayoutOptions layoutOptions = PrintTemplate.LayoutOptions.create();
        layoutOptions.setTitleText("CMLUCA Report - "+(requiresAction ? "Requires Action" : "No Action Required")+" \n \n\n"+title);
        layoutOptions.setScalebarUnit("Meters");
        //layoutOptions.setLegendLayers(getLegends());
        
        if( requiresAction ) {
            layoutOptions.setAuthorText(AUTHOR_TEMPLATE_INTERSECT);
            layoutOptions.setCopyrightText(COPYRIGHT_TEMPLATE_INTERSECT);
        } else {
            layoutOptions.setAuthorText(AUTHOR_TEMPLATE_NO_INTERSECT);
            layoutOptions.setCopyrightText(COPYRIGHT_TEMPLATE_NO_INTERSECT);
        }
        
        template.setLayoutOptions(layoutOptions);
        
        template.setLayout(Layout.A4_PORTRAIT);
        template.setLabel("What is this");
        
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
