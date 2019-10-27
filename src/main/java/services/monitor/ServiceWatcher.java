package services.monitor;

import constants.Constants;
import io.dropwizard.lifecycle.Managed;
import services.client.DiscoveryClient;

public class ServiceWatcher implements Managed {

    @Override
    public void start() throws Exception {
        DiscoveryClient client = new DiscoveryClient(Constants.discoveryServiceUrl);

        System.out.println(client.watch(Constants.MonitorServiceUrl));

    }

    @Override
    public void stop() throws Exception {

    }
}
