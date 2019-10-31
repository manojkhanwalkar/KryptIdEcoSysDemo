package services.clientproxy;

import constants.Constants;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import util.AppHealthCheck;
import util.DiscoveryLifeCycle;


public class ClientProxyApplication extends Application<ClientProxyConfiguration> {
    public static void main(String[] args) throws Exception {
        new ClientProxyApplication().run(args);
    }

    @Override
    public String getName() {
        return "Mitek Application";
    }

    @Override
    public void initialize(Bootstrap<ClientProxyConfiguration> bootstrap) {

    //    IDStatusPollManager.getInstance().start();

        // nothing to do yet
    }

    @Override
    public void run(ClientProxyConfiguration configuration,
                    Environment environment) {
        final ClientProxyResource resource = new ClientProxyResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );
    //    DiscoveryLifeCycle myManagedObject = new DiscoveryLifeCycle(Constants.BureauServiceUrl,Constants.BureauServiceType, Constants.BureauServiceHealthUrl);
    //    environment.lifecycle().manage(myManagedObject);

        environment.jersey().register(resource);
        environment.healthChecks().register("APIHealthCheck", new AppHealthCheck());

    }

}