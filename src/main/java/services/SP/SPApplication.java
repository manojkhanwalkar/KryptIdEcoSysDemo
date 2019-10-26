package services.SP;

import constants.Constants;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import util.DiscoveryLifeCycle;


public class SPApplication extends Application<SPConfiguration> {
    public static void main(String[] args) throws Exception {
        new SPApplication().run(args);
    }

    @Override
    public String getName() {
        return "Mitek Application";
    }

    @Override
    public void initialize(Bootstrap<SPConfiguration> bootstrap) {

    //    IDStatusPollManager.getInstance().start();

        // nothing to do yet
    }

    @Override
    public void run(SPConfiguration configuration,
                    Environment environment) {
        final SPResource resource = new SPResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );

        DiscoveryLifeCycle myManagedObject = new DiscoveryLifeCycle(Constants.SPServiceUrl,Constants.SPServiceType,Constants.SPServiceHealthUrl);
        environment.lifecycle().manage(myManagedObject);

        environment.jersey().register(resource);
    }

}