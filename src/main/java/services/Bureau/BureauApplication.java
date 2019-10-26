package services.Bureau;

import constants.Constants;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import util.DiscoveryLifeCycle;


public class BureauApplication extends Application<BureauConfiguration> {
    public static void main(String[] args) throws Exception {
        new BureauApplication().run(args);
    }

    @Override
    public String getName() {
        return "Mitek Application";
    }

    @Override
    public void initialize(Bootstrap<BureauConfiguration> bootstrap) {

    //    IDStatusPollManager.getInstance().start();

        // nothing to do yet
    }

    @Override
    public void run(BureauConfiguration configuration,
                    Environment environment) {
        final BureauResource resource = new BureauResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );
        DiscoveryLifeCycle myManagedObject = new DiscoveryLifeCycle(Constants.BureauServiceUrl,Constants.BureauServiceType, Constants.BureauServiceHealthUrl);
        environment.lifecycle().manage(myManagedObject);

        environment.jersey().register(resource);
    }

}