package services.discovery;

import constants.Constants;
import services.client.Connection;
import services.discovery.data.WatchInfo;

import java.util.concurrent.CompletableFuture;

public class WatchUpdater implements CacheObserver {

    String callbackUrl ;
    public WatchUpdater(WatchInfo watchInfo)
    {
        callbackUrl = watchInfo.getCallbackURL();
    }


    @Override
    public void notify(String str) {


        CompletableFuture<Void> future = CompletableFuture.runAsync(()->{

        Connection connection = new Connection(Constants.MonitorServiceUrl);
        try {
            connection.sendSimple(str,"callback");
        } catch (Exception e) {
            e.printStackTrace();
        }

        });
    }
}
