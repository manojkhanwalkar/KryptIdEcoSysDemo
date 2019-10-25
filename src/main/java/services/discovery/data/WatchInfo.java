package services.discovery.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WatchInfo {


    @JsonProperty
String serviceType;


    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @JsonProperty
    String callbackURL;

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }
}


