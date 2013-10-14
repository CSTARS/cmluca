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
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;
import edu.ucdavis.gwt.gis.client.toolbar.button.ToolbarItem;

public class Print {

    public static final Print INSTANCE = new Print();
    
    private static PrintTask printTask;
    private MenuButton menuButton = new MenuButton();
    
    private static final String AUTHOR_TEMPLATE = "Thank you for using the CMLUCA system.";
    
    public interface PrintTaskComplete {
        public void onComplete();
    }
    
    protected Print() {}
    
    public void setServer(String server) {
        ESRI.addCorsEnabledServers(server.replaceAll(".*:\\/\\/","").replaceAll("\\/.*",""));
        printTask = PrintTask.create(server);
    }
    
    public static void exec() {
        exec(null);
    }
    
    public static void exec(final PrintTaskComplete callback) {
        PrintParameters params = PrintParameters.create();
        params.setMap(AppManager.INSTANCE.getMap());
        
        PrintTemplate template = PrintTemplate.create();
        template.setFormat(Format.PNG_32);
        
        PrintTemplate.LayoutOptions layoutOptions = PrintTemplate.LayoutOptions.create();
        layoutOptions.setTitleText("CMLUCA Export");
        layoutOptions.setScalebarUnit("Meters");
        //layoutOptions.setLegendLayers(getLegends());
        layoutOptions.setCopyrightText("");
        layoutOptions.setAuthorText(AUTHOR_TEMPLATE);
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
            exec();
        }

        @Override
        public void onAdd(Toolbar toolbar) {}
        
    }
}
