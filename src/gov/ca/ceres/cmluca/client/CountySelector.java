package gov.ca.ceres.cmluca.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.callback.QueryTaskCallback;
import edu.ucdavis.cstars.client.tasks.FeatureSet;
import edu.ucdavis.cstars.client.tasks.Query;

import gov.ca.ceres.cmluca.client.SearchBox.CmlucaQueryTask;
import gov.ca.ceres.cmluca.client.SearchBox.SearchResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CountySelector extends FlowPanel {
    
    private SearchBox searchBox;
    
    private ListBox list = new ListBox();
    private HTML loading = new HTML();

    private List<String> COUNTIES = Arrays.asList("Select County","Alameda","Alpine","Amador","Butte","Calaveras","Colusa",
            "Contra Costa","Del Norte","El Dorado","Fresno","Glenn","Humboldt","Imperial","Inyo","Kern","Kings",
            "Lake","Lassen","Los Angeles","Maderav","Marin","Mariposa","Mendocino","Merced","Modoc","Mono",
            "Monterey","Napa","Nevada","Orange","Placer","Plumas","Riverside","Sacramento","San Benito",
            "San Bernardino","San Diego","San Francisco","San Joaquin","San Luis Obispo","San Mateo",
            "Santa Barbara","Santa Clara","Santa Cruz","Shasta","Sierra","Siskiyou","Solano","Sonoma",
            "Stanislaus","Sutter","Tehama","Trinity","Tulare","Tuolumne","Ventura","Yolo","Yuba");

    public CountySelector(SearchBox searchBox) {
        this.searchBox = searchBox;
        
        searchBox.setSearchHandler(new Runnable(){
            @Override
            public void run() {
                reset();
            } 
        });
        
        for( String c: COUNTIES ) list.addItem(c);
 
        list.setStyleName("county-select");
        list.addChangeHandler(changeHandler);
                
        loading.setStyleName("county-select");
        loading.addStyleName("loading");
        loading.setVisible(false);
        
        add(list);
        add(loading);
    }
    
    private ChangeHandler changeHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
            String value = list.getValue(list.getSelectedIndex());
            searchBox.setText("");
            if( value.equals(COUNTIES.get(0)) ) return;
            
            onSelect(value);
        }
    };
    
    private void onSelect(String county) {
        for( CmlucaQueryTask qt:  SearchBox.getQueries() ){
            // HACK
            if( qt.getUrl().matches(".*CaCountyBound.*") ) {
                query(qt, county);
            }
        }
    }
    
    private void query(final CmlucaQueryTask qt, String county) {
        loading.setVisible(true);
        list.setVisible(false);
        loading.setHTML("<i class=\"icon-spinner icon-spin\"></i> "+county);
       
        Query q = Query.create();
        q.setOutFields(new String[]{"*"});
        q.setReturnGeometry(false);
        q.setWhere("UPPER("+qt.getParameter()+") like '"+county.toUpperCase()+"%'");
        qt.execute(q, new QueryTaskCallback(){
            @Override
            public void onComplete(FeatureSet featureSet) {
                if( featureSet.getFeatures().length() > 0) {
                    Graphic.Attributes attrs = featureSet.getFeatures().get(0).getAttributes();
                    SearchResult sr = new SearchResult(qt.format(attrs), null, qt.getUrl(), attrs.getStringForced(qt.getParameter()));
                    searchBox.getGeometry(sr, new Runnable(){
                        @Override
                        public void run() {
                            loading.setVisible(false);
                            list.setVisible(true);
                        }
                    });
                } else {                    
                    Window.alert("Error retrieving county");
                    loading.setVisible(false);
                    list.setVisible(true);
                }
                
                
            }
            @Override
            public void onError(Error error) {
                Window.alert("Error retrieving county");
                
                loading.setVisible(false);
                list.setVisible(true);
            }
        });
    }
    
    public void reset() {
        list.setSelectedIndex(0);
    }

}
