package services.discovery.data;

import com.amazonaws.services.kinesis.model.HashKeyRange;
import com.amazonaws.services.medialive.model.HlsAkamaiHttpTransferMode;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class WatchCallbackInfo {


    HashMap<String,ServiceStatus>  serviceStatusSnapShot ;

    public HashMap<String, ServiceStatus> getServiceStatusSnapShot() {
        return serviceStatusSnapShot;
    }

    public void setServiceStatusSnapShot(HashMap<String, ServiceStatus> serviceStatusSnapShot) {
        this.serviceStatusSnapShot = serviceStatusSnapShot;
    }


    @Override
    public String toString() {
        return "WatchCallbackInfo{" +
                "serviceStatusSnapShot=" + serviceStatusSnapShot +
                '}';
    }
}


