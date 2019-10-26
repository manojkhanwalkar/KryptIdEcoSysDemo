package services.discovery;

import services.discovery.data.ServiceInfo;
import services.discovery.data.ServiceStatus;
import services.discovery.data.ServiceStatusInfo;

import java.util.HashMap;
import java.util.Map;

public class ServiceStatusCache {




    Map<String, ServiceStatus>  serviceStatusMap = new HashMap<>();

    public synchronized void register(ServiceInfo serviceInfo)
    {
        ServiceStatus serviceStatus = serviceStatusMap.get(serviceInfo.getServiceType());
        if (serviceStatus==null)
        {
            serviceStatus = new ServiceStatus();
            serviceStatusMap.put(serviceInfo.getServiceType(),serviceStatus);
           // serviceStatus.setServiceType(serviceInfo.getServiceType());

        }

        serviceStatus.add(serviceInfo.getUrl(),serviceInfo.getHealthCheckUrl());


    }

    public synchronized void deregister(ServiceInfo serviceInfo)
    {
        ServiceStatus serviceStatus = serviceStatusMap.get(serviceInfo.getServiceType());
        if (serviceStatus!=null)
        {
            serviceStatus.remove(serviceInfo.getUrl());

        }



    }

    public synchronized ServiceStatusInfo getStatus(String serviceType)
    {
        ServiceStatus serviceStatus = serviceStatusMap.get(serviceType);
        if (serviceStatus!=null)
        {
            return serviceStatus.clone();
        }
        else
            return null;

    }




}
