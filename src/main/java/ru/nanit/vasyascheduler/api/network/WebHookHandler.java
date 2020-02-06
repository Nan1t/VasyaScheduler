package ru.nanit.vasyascheduler.api.network;

import fi.iki.elonen.NanoHTTPD;

public interface WebHookHandler {

    NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session);

}
