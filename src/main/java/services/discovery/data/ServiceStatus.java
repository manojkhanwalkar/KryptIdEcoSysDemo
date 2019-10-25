package services.discovery.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//TODO - cache storing this status object should return a deep copy

public class ServiceStatus implements Cloneable{

    public void add(String url) {

        servicesStatus.put(url,Status.Up);

    }

    public void remove(String url) {

        servicesStatus.remove(url);
    }

    public void updateStatus(String url, Status status)
    {
        servicesStatus.put(url,status);
    }

    public  enum Status  { Up, Down};


    @JsonProperty
    String serviceType;

    Map<String,Status> servicesStatus = new HashMap<>();


    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }


    // to be used by the cache while returning the object
    public ServiceStatusInfo clone()
    {
        ServiceStatusInfo serviceStatusInfo = new ServiceStatusInfo();
        serviceStatusInfo.serviceType = this.serviceType;
        serviceStatusInfo.servicesStatus.putAll(this.servicesStatus);
        return serviceStatusInfo;
    }




}


