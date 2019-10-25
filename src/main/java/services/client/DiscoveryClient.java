package services.client;

import com.fasterxml.jackson.core.type.TypeReference;
import services.discovery.data.ServiceInfo;
import services.discovery.data.ServiceStatus;
import services.discovery.data.ServiceStatusInfo;
import services.discovery.data.WatchInfo;
import util.JSONUtil;

import java.util.HashMap;

import static util.ClaimValidator.convert;

public class DiscoveryClient {



    private Connection discovery;




    public DiscoveryClient(String discoveryUrl)
    {

         discovery = new Connection(discoveryUrl);

    }




    public void register(String url,String type) throws Exception
    {
        ServiceInfo serviceInfo= new ServiceInfo();
        serviceInfo.setUrl(url);
        serviceInfo.setServiceType(type);

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

        TypeReference<HashMap<String, ServiceStatus.Status>> typeRef
                = new TypeReference<HashMap<String, ServiceStatus.Status>>() {};

        return (ServiceStatusInfo) JSONUtil.fromJSON(response,ServiceStatusInfo.class);

    }

    public ServiceStatusInfo watch(String serviceType , String callbackURL) throws Exception
    {
        WatchInfo watchInfo= new WatchInfo();
        watchInfo.setServiceType(serviceType);
        watchInfo.setCallbackURL(callbackURL);



        String response =  discovery.sendSimple(JSONUtil.toJSON(watchInfo),"watch");

        System.out.println(response);

        return (ServiceStatusInfo) JSONUtil.fromJSON(response,ServiceStatusInfo.class);


    }











    public static void main(String[] args) throws Exception {


        DiscoveryClient verifier = new DiscoveryClient("https://172.17.0.1:8580/");


        verifier.register("https://172.17.0.1:8180/","INO");

        verifier.service("INO");

        verifier.watch("INO","TO BE IMPLEMENTED");

        verifier.deregister("https://172.17.0.1:8180/","INO");


    }





}
