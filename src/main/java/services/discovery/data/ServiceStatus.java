package services.discovery.data;

import java.util.HashMap;
import java.util.Map;


//TODO - cache storing this status object should return a deep copy

public class ServiceStatus implements Cloneable{

    public synchronized void add(String url, String healthUrl) {

        ServiceStatusHealthCheckTuple serviceStatusHealthCheckTuple = new ServiceStatusHealthCheckTuple();
        serviceStatusHealthCheckTuple.setHealthCheckURL(healthUrl);
        serviceStatusHealthCheckTuple.setServiceStatus(Status.Up);

        serviceInstanceStatus.put(url,serviceStatusHealthCheckTuple);

    }

    public synchronized void remove(String url) {

        serviceInstanceStatus.remove(url);
    }

    public synchronized void updateStatus(String url, Status status)
    {
        ServiceStatusHealthCheckTuple serviceStatusHealthCheckTuple = serviceInstanceStatus.get(url);
        serviceStatusHealthCheckTuple.setServiceStatus(status);

    }

    public  enum Status  { Up, Down};


 /*   @JsonProperty
    String serviceType; */

    public Map<String,ServiceStatusHealthCheckTuple> serviceInstanceStatus = new HashMap<>();


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

        serviceInstanceStatus.entrySet().stream().forEach(entry-> {
           serviceStatusInfo.servicesStatus.put(entry.getKey(),entry.getValue().getServiceStatus());
        });
        return serviceStatusInfo;
    }

    @Override
    public String toString() {
        return "ServiceStatus{" +
                "serviceInstanceStatus=" + serviceInstanceStatus +
                '}';
    }
}


