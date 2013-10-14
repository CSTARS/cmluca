package gov.ca.ceres.cmluca.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceConfig extends JavaScriptObject {
    
    protected ServiceConfig() {}
    
    public final native String getUrl() /*-{
        if( this.url ) return this.url;
        return "";
    }-*/;
    
    public final native String getType() /*-{
        if( this.type ) return this.type;
        return "";
    }-*/;
    
    public final native String getParameter() /*-{
        if( this.parameter ) return this.parameter;
        return "NAME";
    }-*/;
    
    public final native JavaScriptObject getFormatter() /*-{
        return this.format;
    }-*/;

}
