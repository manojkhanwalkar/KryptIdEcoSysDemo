package util;


import com.codahale.metrics.health.HealthCheck;

public class AppHealthCheck extends HealthCheck
{

    protected com.codahale.metrics.health.HealthCheck.Result check() throws Exception
    {

        return com.codahale.metrics.health.HealthCheck.Result.healthy();
    }

}