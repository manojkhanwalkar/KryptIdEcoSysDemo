package services.discovery;

import io.dropwizard.lifecycle.Managed;
import services.client.Connection;
import services.discovery.data.ServiceStatus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HealthChecker implements Managed {

    ServiceStatusCache serviceStatusCache;

    ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    public HealthChecker(ServiceStatusCache serviceStatusCache)
    {
        this.serviceStatusCache = serviceStatusCache;
    }


    @Override
    public void start() throws Exception {

        System.out.println("Health checker started");

        HealthTimerTask timerTask = new HealthTimerTask(serviceStatusCache);

        ses.scheduleWithFixedDelay(timerTask,10,10, TimeUnit.SECONDS);

    }

    @Override
    public void stop() throws Exception {

        ses.shutdownNow();

        System.out.println("Health checker stopped");

    }


    // TODO - start a scheduledthread executor .  get list of services from the cache and then call their health checks and update the status of the services .


    /*



        Runnable task1 = () -> {
            count++;
            System.out.println("Running...task1 - count : " + count);
        };

        // init Delay = 5, repeat the task every 1 second
        ScheduledFuture<?> scheduledFuture = ses.scheduleAtFixedRate(task1, 5, 1, TimeUnit.SECONDS);
     */

}

class HealthTimerTask implements Runnable
{

    ServiceStatusCache serviceStatusCache;
    public  HealthTimerTask(ServiceStatusCache serviceStatusCache)
    {
        this.serviceStatusCache = serviceStatusCache;

    }


    public void run()
    {
        serviceStatusCache.serviceStatusMap.values().parallelStream().forEach(serviceStatus -> {

            serviceStatus.serviceInstanceStatus.values().parallelStream().forEach(instance-> {

                Connection conn = new Connection(instance.getHealthCheckURL());
                String response = null;
                try {
                    response = conn.get("healthcheck");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (response.toLowerCase().contains("false"))
                {
                    instance.setServiceStatus(ServiceStatus.Status.Down);
                }
                else
                {
                    instance.setServiceStatus(ServiceStatus.Status.Up);
                }

            });


        });

    }

}
