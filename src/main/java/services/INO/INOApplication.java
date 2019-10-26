package services.INO;

import constants.Constants;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import services.client.DiscoveryClient;
import util.DiscoveryLifeCycle;


public class INOApplication extends Application<INOConfiguration> {
    public static void main(String[] args) throws Exception {
        new INOApplication().run(args);
    }



    @Override
    public String getName() {
        return "Mitek Application";
    }

    @Override
    public void initialize(Bootstrap<INOConfiguration> bootstrap) {



        //    IDStatusPollManager.getInstance().start();

        // nothing to do yet
    }

    @Override
    public void run(INOConfiguration configuration,
                    Environment environment) {
        final INOResource resource = new INOResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );

        DiscoveryLifeCycle myManagedObject = new DiscoveryLifeCycle(Constants.INOServiceUrl,Constants.INOServiceType, Constants.INOServiceHealthUrl);
        environment.lifecycle().manage(myManagedObject);
        environment.jersey().register(resource);


    }

}