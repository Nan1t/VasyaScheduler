package ru.nanit.vasyascheduler.web;

import fi.iki.elonen.NanoHTTPD;
import ru.nanit.vasyascheduler.api.network.WebHookHandler;

public class HookIndex implements WebHookHandler {

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        // TODO
        return null;
    }
}
