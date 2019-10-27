package services.client;

import com.fasterxml.jackson.core.type.TypeReference;
import constants.Constants;
import services.discovery.data.*;
import util.JSONUtil;

import java.util.HashMap;

import static util.ClaimValidator.convert;

public class DiscoveryClient {



    private Connection discovery;




    public DiscoveryClient(String discoveryUrl)
    {

         discovery = new Connection(discoveryUrl);

    }




    public void register(String url,String type, String healthUrl) throws Exception
    {
        ServiceInfo serviceInfo= new ServiceInfo();
        serviceInfo.setUrl(url);
        serviceInfo.setServiceType(type);
        serviceInfo.setHealthCheckUrl(healthUrl);

       String response =  discovery.sendSimple(JSONUtil.toJSON(serviceInfo),"register");

       System.out.println(response);
    }

    public void deregister(String url, String type) throws Exception
    {
        ServiceInfo serviceInfo= new ServiceInfo();
        serviceInfo.setUrl(url);
        serviceInfo.setServiceType(type);

        String response =  discovery.sendSimple(JSONUtil.toJSON(serviceInfo),"deregister");

        System.out.println(response);
    }


    public ServiceStatusInfo service(String serviceType) throws Exception
    {

        String response =  discovery.sendSimple(serviceType,"service");

        System.out.println(response);


        return (ServiceStatusInfo) JSONUtil.fromJSON(response,ServiceStatusInfo.class);

    }

    public WatchCallbackInfo watch( String callbackURL) throws Exception
    {
        WatchInfo watchInfo= new WatchInfo();
      //  watchInfo.setServiceType(serviceType);
        watchInfo.setCallbackURL(callbackURL);



        String response =  discovery.sendSimple(JSONUtil.toJSON(watchInfo),"watch");

        System.out.println(response);

        return (WatchCallbackInfo) JSONUtil.fromJSON(response, WatchCallbackInfo.class);


    }











    public static void main(String[] args) throws Exception {


        DiscoveryClient verifier = new DiscoveryClient(Constants.discoveryServiceUrl);


        verifier.register(Constants.INOServiceUrl,"INO", Constants.INOServiceHealthUrl);

        verifier.service("INO");

       // verifier.watch("INO","TO BE IMPLEMENTED");

        verifier.deregister(Constants.INOServiceUrl,"INO");


    }





}
