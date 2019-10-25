package services.discovery;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class DiscoveryApplication extends Application<DiscoveryConfiguration> {
    public static void main(String[] args) throws Exception {
        new DiscoveryApplication().run(args);
    }

    @Override
    public String getName() {
        return "Mitek Application";
    }

    @Override
    public void initialize(Bootstrap<DiscoveryConfiguration> bootstrap) {

    //    IDStatusPollManager.getInstance().start();

        // nothing to do yet
    }

    @Override
    public void run(DiscoveryConfiguration configuration,
                    Environment environment) {
        final DiscoveryResource resource = new DiscoveryResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );
        environment.jersey().register(resource);
    }

}