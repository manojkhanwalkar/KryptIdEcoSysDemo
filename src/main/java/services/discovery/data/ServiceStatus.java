package services.discovery.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//TODO - cache storing this status object should return a deep copy

public class ServiceStatus implements Cloneable{

    public void add(String url, String healthUrl) {

        ServiceStatusHealthCheckTuple serviceStatusHealthCheckTuple = new ServiceStatusHealthCheckTuple();
        serviceStatusHealthCheckTuple.setHealthCheckURL(healthUrl);
        serviceStatusHealthCheckTuple.setServiceStatus(Status.Up);

        servicesStatus.put(url,serviceStatusHealthCheckTuple);

    }

    public void remove(String url) {

        servicesStatus.remove(url);
    }

    public void updateStatus(String url, Status status)
    {
        ServiceStatusHealthCheckTuple serviceStatusHealthCheckTuple = servicesStatus.get(url);
        serviceStatusHealthCheckTuple.setServiceStatus(status);

    }

    public  enum Status  { Up, Down};


 /*   @JsonProperty
    String serviceType; */

    Map<String,ServiceStatusHealthCheckTuple> servicesStatus = new HashMap<>();


  /*  public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }*/


    // to be used by the cache while returning the object
    public ServiceStatusInfo clone()
    {
        ServiceStatusInfo serviceStatusInfo = new ServiceStatusInfo();
        //serviceStatusInfo.serviceType = this.serviceType;

        servicesStatus.entrySet().stream().forEach(entry-> {
           serviceStatusInfo.servicesStatus.put(entry.getKey(),entry.getValue().getServiceStatus());
        });
        return serviceStatusInfo;
    }




}


class ServiceStatusHealthCheckTuple
{
    String healthCheckURL ;
    ServiceStatus.Status serviceStatus;

    public String getHealthCheckURL() {
        return healthCheckURL;
    }

    public void setHealthCheckURL(String healthCheckURL) {
        this.healthCheckURL = healthCheckURL;
    }

    public ServiceStatus.Status getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus.Status serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
}


