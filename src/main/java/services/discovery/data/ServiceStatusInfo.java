package services.discovery.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//TODO - cache storing this status object should return a deep copy

public final class ServiceStatusInfo {


    @JsonProperty
    protected String serviceType;


    @JsonProperty
    protected HashMap<String, ServiceStatus.Status> servicesStatus = new HashMap<>();


    @JsonIgnore
    public String getServiceUrl()
    {
        List<String> serviceUrls = servicesStatus.entrySet().stream().filter(e -> e.getValue().equals(ServiceStatus.Status.Up)).limit(1).map(e->e.getKey()).collect(Collectors.toList());
        return serviceUrls.get(0);
    }


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


