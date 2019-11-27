package services.discovery;

import services.discovery.data.ServiceInfo;
import services.discovery.data.ServiceStatus;
import services.discovery.data.ServiceStatusInfo;
import util.JSONUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ServiceStatusCache {

    static class Holder
    {
        public static ServiceStatusCache serviceStatusCache = new ServiceStatusCache();
    }

    public static ServiceStatusCache getInstance()
    {
        return Holder.serviceStatusCache;
    }


    private ServiceStatusCache()
    {

    }

    List<CacheObserver> observers = new CopyOnWriteArrayList<>();


    public void addObserver(CacheObserver cacheObserver)
    {
        observers.add(cacheObserver);
    }

    HashMap<String, ServiceStatus>  serviceStatusMap = new HashMap<>();


    private void updateObservers(String str)
    {
        observers.stream().forEach(observer-> observer.notify(str));
    }

    public synchronized void register(ServiceInfo serviceInfo)
    {
        System.out.println("Registering service " + serviceInfo);

        ServiceStatus serviceStatus = serviceStatusMap.get(serviceInfo.getServiceType());
        if (serviceStatus==null)
        {
            serviceStatus = new ServiceStatus();
            serviceStatusMap.put(serviceInfo.getServiceType(),serviceStatus);
           // serviceStatus.setServiceType(serviceInfo.getServiceType());

        }

        serviceStatus.add(serviceInfo.getUrl(),serviceInfo.getHealthCheckUrl());

        updateObservers("Registered service " + JSONUtil.toJSON(serviceInfo));


        System.out.println("Service status after registration " + serviceStatusMap);


    }

    public synchronized void deregister(ServiceInfo serviceInfo)
    {
        System.out.println("Deregistering service " + serviceInfo);
        ServiceStatus serviceStatus = serviceStatusMap.get(serviceInfo.getServiceType());
        if (serviceStatus!=null)
        {
            serviceStatus.remove(serviceInfo.getUrl());

        }

        updateObservers("De-registered service " + JSONUtil.toJSON(serviceInfo));

        System.out.println("Service status after deregistration " + serviceStatusMap);
    }

    //TODO - deep clone the map before returning

    public synchronized HashMap<String, ServiceStatus> getServiceStatusMap()
    {
        return serviceStatusMap;
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


    protected List<ServiceStatus> getServices()
    {
        List<ServiceStatus> services = serviceStatusMap.entrySet().stream().map(entry->entry.getValue()).collect(Collectors.toList());
        return services;
    }



}
