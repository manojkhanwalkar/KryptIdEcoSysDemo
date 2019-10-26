package services.discovery;

import com.codahale.metrics.annotation.Timed;
import data.*;
import services.discovery.data.ServiceInfo;
import services.discovery.data.ServiceStatus;
import services.discovery.data.ServiceStatusInfo;
import services.discovery.data.WatchInfo;
import util.CryptUtil;
import util.JSONUtil;
import util.KeyExchangeManager;
import util.RSAKeyHolder;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static util.ClaimValidator.convert;
import static util.ClaimValidator.isValidClaim;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    private final String template;
    private final String defaultName;


    private final ServiceStatusCache serviceStatusCache;

    public DiscoveryResource(String template, String defaultName, ServiceStatusCache serviceStatusCache) {
        this.template = template;
        this.defaultName = defaultName;

        this.serviceStatusCache=serviceStatusCache;

    }


    @POST
    @Timed
    @Path("/watch")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceStatusInfo watch(WatchInfo watchInfo) {

        //TODO - add watch features
        ServiceStatusInfo serviceStatusInfo = serviceStatusCache.getStatus(watchInfo.getServiceType()) ;
        serviceStatusInfo.setServiceType(watchInfo.getServiceType());
        return serviceStatusInfo;


    }


    @POST
    @Timed
    @Path("/service")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceStatusInfo service(String serviceType) {


        ServiceStatusInfo serviceStatusInfo= serviceStatusCache.getStatus(serviceType) ;
        serviceStatusInfo.setServiceType(serviceType);
        return serviceStatusInfo;

    }


    @POST
    @Timed
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public String register(ServiceInfo serviceInfo) {

        System.out.println("Resource has been registered " + serviceInfo);
        serviceStatusCache.register(serviceInfo);
        return "Service has been registered";

    }




    @POST
    @Timed
    @Path("/deregister")
    @Produces(MediaType.APPLICATION_JSON)
    public String deregister(ServiceInfo serviceInfo) {

        serviceStatusCache.deregister(serviceInfo);

     return "Service has been deregistered" ;


    }









}
