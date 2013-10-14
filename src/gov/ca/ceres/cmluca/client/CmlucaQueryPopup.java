package gov.ca.ceres.cmluca.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import gov.ca.ceres.cmluca.client.Print.PrintTaskComplete;

public class CmlucaQueryPopup extends BootstrapModalLayout {

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
    
    private HTML[] textFields = null;
    private HTML[] iconFields = null;
    
    private FlowPanel footer = new FlowPanel();
    private Button print = new Button("<i class='icon-print'></i> Print Report");
    private Button close = new Button("Close");
    private boolean printDisabled = false;
    
    public CmlucaQueryPopup() {
        panel = uiBinder.createAndBindUi(this);
        textFields = new HTML[] {airSpace1000Text, airSpace4000Text, flightPath1000Text, flightPath4000Text,
                militaryBase1000Text, militaryBase4000Text};
        iconFields = new HTML[] {airSpace1000Icon, airSpace4000Icon, flightPath1000Icon, flightPath4000Icon,
                militaryBase1000Icon, militaryBase4000Icon};
        
        print.addStyleName("btn");
        print.addStyleName("btn-primary");
        print.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                if( printDisabled ) return;
                
                print.addStyleName("disabled");
                print.setHTML("<i class='icon-spinner icon-spin'></i> Generating Report...");
                Print.exec(new PrintTaskComplete(){
                    @Override
                    public void onComplete() {
                        print.setHTML("<i class='icon-print'></i> Print Report");
                        print.removeStyleName("disabled");
                    }
                });
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
        
    }

    @Override
    public String getTitle() {
        return "Results";
    }

    @Override
    public Widget getBody() {
        return panel;
    }

    @Override
    public Widget getFooter() {
        return footer;
    }

    public void loading(String location) {
        this.location.setHTML(location);
        for( int i = 0; i < textFields.length; i++ ) {
            textFields[i].setHTML("");
        
        }
        for( int i = 0; i < iconFields.length; i++ ) iconFields[i].setHTML("<i class='icon-spinner icon-spin'></i>");
    }
    
    public void setAirSpace1000(boolean inside) {
        set(inside, 1000, airSpace1000Text, airSpace1000Icon);
    }
    
    public void setAirSpace4000(boolean inside) {
        set(inside, 4000, airSpace4000Text, airSpace4000Icon);
    }
    
    public void setFlightPath1000(boolean inside) {
        set(inside, 1000, flightPath1000Text, flightPath1000Icon);
    }
    
    public void setFlightPath4000(boolean inside) {
        set(inside, 4000, flightPath4000Text, flightPath4000Icon);
    }
    
    public void setMilitaryBase1000(boolean inside) {
        set(inside, 1000, militaryBase1000Text, militaryBase1000Icon);
    }
    
    public void setMilitaryBase4000(boolean inside) {
        set(inside, 4000, militaryBase4000Text, militaryBase4000Icon);
    }
    
    private void set(boolean inside, int distance, HTML text, HTML icon) {
        if( inside ) {
            text.setHTML(" <b>is inside "+distance+"ft</b>");
            icon.setHTML(" <i class='icon-ok' style='color:green'></i>");
        } else {
            text.setHTML(" is outside "+distance+"ft");
            icon.setHTML(" <i class='icon-remove' style='color:red'></i>");
        }
    }
    
}
