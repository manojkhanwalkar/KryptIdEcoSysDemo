package services.monitor;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class MonitorApplication extends Application<MonitorConfiguration> {
    public static void main(String[] args) throws Exception {
        new MonitorApplication().run(args);
    }

    @Override
    public String getName() {
        return "Mitek Application";
    }

    @Override
    public void initialize(Bootstrap<MonitorConfiguration> bootstrap) {

    //    IDStatusPollManager.getInstance().start();

        // nothing to do yet
    }

    @Override
    public void run(MonitorConfiguration configuration,
                    Environment environment) {
        final MonitorResource resource = new MonitorResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );

        environment.lifecycle().manage(new ServiceWatcher());
        environment.jersey().register(resource);
    }

}