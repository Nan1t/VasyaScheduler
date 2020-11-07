package ru.nanit.vasyascheduler.services;

import fi.iki.elonen.NanoHTTPD;
import ru.nanit.vasyascheduler.api.network.WebHookHandler;
import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.api.util.Logger;

import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public final class WebHookManager extends NanoHTTPD {

    private static int port;
    private Map<String, WebHookHandler> handlers = new HashMap<>();

    public WebHookManager(Configuration conf) throws Exception {
        super(conf.get().getNode("http", "port").getInt());
        port = conf.get().getNode("http", "port").getInt();

        Path keystore = Paths.get(conf.get().getNode("http", "ssl", "keystore").getString());
        String password = conf.get().getNode("http", "ssl", "password").getString();

        setupSSL(keystore, password);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String[] params = session.getUri().split("/");

        if(params.length > 0){
            String hookId = params[1];
            WebHookHandler handler = handlers.get(hookId.toLowerCase());

            if(handler != null){
                Response response = handler.handle(session);

                if(response != null){
                    return response;
                }
            }
        }

        return newFixedLengthResponse("BIP-BIIIIIP. Error 404 here");
    }

    public void registerHook(String key, WebHookHandler handler){
        handlers.put(key, handler);
    }

    public void start() {
        try{
            super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            Logger.info("HTTP server started on port " + port);
        } catch (IOException e){
            Logger.error("Error while starting HTTP server: ", e);
        }
    }

    public void stop(){
        super.stop();
        Logger.info("HTTP server successfully stopped");
    }

    private void setupSSL(Path file, String password){
        try{
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = Files.newInputStream(file);

            keystore.load(keystoreStream, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, password.toCharArray());

            makeSecure(makeSSLSocketFactory(keystore, keyManagerFactory), null);
        } catch (Exception e){
            Logger.error("Error while installing ssl certificate. Check keystore path and entered password in config.conf");
        }
    }
}
