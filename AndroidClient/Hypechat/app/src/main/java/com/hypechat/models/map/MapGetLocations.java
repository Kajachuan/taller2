package com.hypechat.models.map;

import java.util.Map;

public class MapGetLocations {

    private Map<String,Map<String,String>> locations;

    public MapGetLocations (Map<String,Map<String,String>>  locations){
        this.locations = locations;
    }

    public Map<String,Map<String,String>>  getLocations() {
        return locations;
    }
}
