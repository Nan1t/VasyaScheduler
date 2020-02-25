package ru.nanit.vasyascheduler.web;

import fi.iki.elonen.NanoHTTPD;
import ru.nanit.vasyascheduler.api.storage.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebPanel extends NanoHTTPD {

    private Map<String, String> accounts = new ConcurrentHashMap<>();

    public WebPanel(Configuration conf){
        super(conf.get().getNode("webpanel", "port").getInt());
    }

}
