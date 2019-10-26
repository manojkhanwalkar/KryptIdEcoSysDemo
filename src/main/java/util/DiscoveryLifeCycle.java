package util;

import constants.Constants;
import io.dropwizard.lifecycle.Managed;
import services.client.DiscoveryClient;


public class DiscoveryLifeCycle implements Managed {


    private DiscoveryClient discoveryClient ;

    private final String serviceUrl ;
    private final String serviceType;
    private final String healthUrl;

    public DiscoveryLifeCycle(String serviceURL , String serviceType, String healthUrl)
    {
        this.serviceType= serviceType;
        this.serviceUrl=serviceURL;
        this.healthUrl = healthUrl;
    }


    @Override
    public void start()  {
        // Do something to start the object



        System.out.println("Started the discovery lifecycle ");
        try {
            discoveryClient = new DiscoveryClient(Constants.discoveryServiceUrl);
            discoveryClient.register(serviceUrl,serviceType,healthUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop()  {
        // Do something to stop the object

        System.out.println("Stopped the discovery lifecycle");
        try {

            discoveryClient = new DiscoveryClient(Constants.discoveryServiceUrl);

            discoveryClient.deregister(Constants.INOServiceUrl,Constants.INOServiceType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
