package services.discovery.data;

public class ServiceStatusHealthCheckTuple
{
    String healthCheckURL ;
    ServiceStatus.Status serviceStatus;

    public String getHealthCheckURL() {
        return healthCheckURL;
    }

    public void setHealthCheckURL(String healthCheckURL) {
        this.healthCheckURL = healthCheckURL;
    }

    public ServiceStatus.Status getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus.Status serviceStatus) {
        this.serviceStatus = serviceStatus;
    }


    @Override
    public String toString() {
        return "ServiceStatusHealthCheckTuple{" +
                "healthCheckURL='" + healthCheckURL + '\'' +
                ", serviceStatus=" + serviceStatus +
                '}';
    }
}
