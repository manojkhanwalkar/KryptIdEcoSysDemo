package util;

import constants.Constants;
import io.dropwizard.lifecycle.Managed;
import services.client.DiscoveryClient;


public class DiscoveryLifeCycle implements Managed {


    DiscoveryClient discoveryClient ;

    final String serviceUrl ;
    final String serviceType;

    public DiscoveryLifeCycle(String serviceURL , String serviceType)
    {
        this.serviceType= serviceType;
        this.serviceUrl=serviceURL;
    }


    @Override
    public void start() throws Exception {
        // Do something to start the object



        System.out.println("Started the discovery lifecycle ");
        try {
            discoveryClient = new DiscoveryClient(Constants.discoveryServiceUrl);
            discoveryClient.register(serviceUrl,serviceType);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop() throws Exception {
        // Do something to stop the object

        System.out.println("Stopped the discovery lifecycle");
        try {

            discoveryClient.deregister(Constants.INOServiceUrl,Constants.INOServiceType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
