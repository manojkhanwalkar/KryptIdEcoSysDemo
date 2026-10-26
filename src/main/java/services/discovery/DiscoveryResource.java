package services.discovery;

import com.codahale.metrics.annotation.Timed;
import data.*;
import services.discovery.data.*;
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
    public WatchCallbackInfo watch(WatchInfo watchInfo) {

        WatchCallbackInfo watchCallbackInfo =  new WatchCallbackInfo();
        watchCallbackInfo.setServiceStatusSnapShot(serviceStatusCache.getServiceStatusMap());

        WatchUpdater updater = new WatchUpdater(watchInfo);
        serviceStatusCache.addObserver(updater);

        return watchCallbackInfo;



    }


    @POST
    @Timed
    @Path("/service")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceStatusInfo service(String serviceType) {


        ServiceStatusInfo serviceStatusInfo= serviceStatusCache.getStatus(serviceType) ;
        if (serviceStatusInfo == null)
            throw new javax.ws.rs.NotFoundException("Unknown service type");
        serviceStatusInfo.setServiceType(serviceType);
        return serviceStatusInfo;

    }


    /*
     * Registration is unauthenticated, so without a check anyone who can reach this port could register a
     * rogue INO/SP/Bureau (receiving claims and PII) or make the health checker call arbitrary URLs (SSRF).
     * Until registrations are authenticated, only https URLs on allow-listed hosts are accepted.
     * Hosts come from KRYPTID_DISCOVERY_ALLOWED_HOSTS (comma separated); the default is the demo's docker host.
     */
    static final java.util.Set<String> ALLOWED_HOSTS = allowedHosts();

    static java.util.Set<String> allowedHosts()
    {
        String configured = System.getenv("KRYPTID_DISCOVERY_ALLOWED_HOSTS");
        if (configured == null || configured.trim().isEmpty())
            configured = "172.17.0.1,localhost,127.0.0.1";
        java.util.Set<String> hosts = new java.util.HashSet<>();
        for (String h : configured.split(","))
            if (!h.trim().isEmpty())
                hosts.add(h.trim().toLowerCase(java.util.Locale.ROOT));
        return hosts;
    }

    public static boolean isAllowedUrl(String url)
    {
        if (url == null)
            return false;
        try {
            java.net.URI uri = new java.net.URI(url);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && uri.getUserInfo() == null
                    && ALLOWED_HOSTS.contains(uri.getHost().toLowerCase(java.util.Locale.ROOT));
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }

    private static void requireAllowedService(ServiceInfo serviceInfo, boolean needHealthUrl)
    {
        if (serviceInfo == null || serviceInfo.getServiceType() == null || !isAllowedUrl(serviceInfo.getUrl())
                || (needHealthUrl && !isAllowedUrl(serviceInfo.getHealthCheckUrl()))) {
            System.out.println("Rejected service (de)registration for a non allow-listed URL");
            throw new javax.ws.rs.WebApplicationException("Service URL not allowed", javax.ws.rs.core.Response.Status.FORBIDDEN);
        }
    }

    @POST
    @Timed
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public String register(ServiceInfo serviceInfo) {

        requireAllowedService(serviceInfo, true);
        serviceStatusCache.register(serviceInfo);
        return "Service has been registered";

    }




    @POST
    @Timed
    @Path("/deregister")
    @Produces(MediaType.APPLICATION_JSON)
    public String deregister(ServiceInfo serviceInfo) {

        requireAllowedService(serviceInfo, false);

        serviceStatusCache.deregister(serviceInfo);

     return "Service has been de-registered" ;


    }









}
