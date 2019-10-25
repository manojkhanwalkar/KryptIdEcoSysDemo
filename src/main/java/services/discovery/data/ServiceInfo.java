package services.discovery.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceInfo {


    @JsonProperty
String serviceType;

@JsonProperty
String url;

@JsonProperty
String healthCheckUrl;


    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public String toString() {
        return "ServiceInfo{" +
                "serviceType='" + serviceType + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}


