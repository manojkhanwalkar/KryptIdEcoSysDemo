package services.monitor;

import constants.Constants;
import services.client.Connection;
import services.discovery.data.ServiceInfo;
import services.discovery.data.ServiceStatusInfo;
import services.discovery.data.WatchCallbackInfo;
import services.discovery.data.WatchInfo;
import util.CertUtil;
import util.JSONUtil;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static java.util.Base64.getEncoder;

public class CertTestClient {



    private Connection certtest;




    public CertTestClient(String monitorUrl)
    {

        certtest = new Connection(monitorUrl);

    }




    public void certtest(String cert) throws Exception
    {

       String response =  certtest.sendSimple(cert,"certtest");

       System.out.println(response);
    }















    public static void main(String[] args) throws Exception {


        String certFile = "/home/manoj/IdeaProjects/securitytest/src/main/resources/certificate.pem";

        X509Certificate certificate = CertUtil.getCertificate(certFile);

        PublicKey key = certificate.getPublicKey();

        String certStr = getEncoder().encodeToString(certificate.getEncoded());

        CertTestClient certTestClient = new CertTestClient(Constants.MonitorServiceUrl);

        certTestClient.certtest(certStr);



    }


}
