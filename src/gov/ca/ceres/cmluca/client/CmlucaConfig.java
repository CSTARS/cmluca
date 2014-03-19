package gov.ca.ceres.cmluca.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

import edu.ucdavis.gwt.gis.client.config.GadgetConfig;
import edu.ucdavis.gwt.gis.client.config.LayerConfig;

public class CmlucaConfig extends GadgetConfig {
	
	protected CmlucaConfig() {}
	
	public final native PolyStyleConfig getLargeRadiusStyle() /*-{
	    if( this.largeRadiusStyle ) return this.largeRadiusStyle;
	    return {};
	}-*/;
	
	public final native PolyStyleConfig getSmallRadiusStyle() /*-{
       if( this.smallRadiusStyle ) return this.smallRadiusStyle;
       return {};
    }-*/;

}
