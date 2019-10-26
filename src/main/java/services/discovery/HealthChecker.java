package services.discovery;

import io.dropwizard.lifecycle.Managed;

public class HealthChecker implements Managed {

    ServiceStatusCache serviceStatusCache;

    public HealthChecker(ServiceStatusCache serviceStatusCache)
    {
        this.serviceStatusCache = serviceStatusCache;
    }


    @Override
    public void start() throws Exception {

        System.out.println("Health checker started");

    }

    @Override
    public void stop() throws Exception {

        System.out.println("Health checker stopped");

    }


    // TODO - start a scheduledthread executor .  get list of services from the cache and then call their health checks and update the status of the services .

}
