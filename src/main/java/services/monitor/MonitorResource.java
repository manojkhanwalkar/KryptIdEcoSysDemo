package services.monitor;

import com.codahale.metrics.annotation.Timed;
import services.discovery.ServiceStatusCache;
import services.discovery.data.ServiceInfo;
import services.discovery.data.ServiceStatusInfo;
import services.discovery.data.WatchInfo;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static util.CertUtil.getCertificate;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class MonitorResource {
    private final String template;
    private final String defaultName;




    public MonitorResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;


    }


    @POST
    @Timed
    @Path("/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public String watch(String str) {

        System.out.println(str);


        return "Received watch callback" ;

    }



    @POST
    @Timed
    @Path("/certtest")
    @Produces(MediaType.APPLICATION_JSON)
    public String certtest(String str) {

        byte[] bytes =  Base64.getDecoder().decode(str.getBytes());

        X509Certificate certificate1 = getCertificate(bytes);

        System.out.println(certificate1);


       // System.out.println(str);


        return "Received cert" ;

    }


















}
