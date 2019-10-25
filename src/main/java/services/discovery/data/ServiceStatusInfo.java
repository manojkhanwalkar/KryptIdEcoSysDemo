package services.discovery.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;


//TODO - cache storing this status object should return a deep copy

public final class ServiceStatusInfo {


    @JsonProperty
    protected String serviceType;


    @JsonProperty
    protected HashMap<String, ServiceStatus.Status> servicesStatus = new HashMap<>();


    public String getServiceType() {
        return serviceType;
    }


    public Map<String, ServiceStatus.Status> getServicesStatus() {
        return servicesStatus;
    }

    @Override
    public String toString() {
        return "ServiceStatusInfo{" +
                "serviceType='" + serviceType + '\'' +
                ", servicesStatus=" + servicesStatus +
                '}';
    }
}


