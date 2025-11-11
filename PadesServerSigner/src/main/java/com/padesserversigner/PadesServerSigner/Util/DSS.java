package com.padesserversigner.PadesServerSigner.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.padesserversigner.PadesServerSigner.PadesServerSignerApplication;
import com.padesserversigner.PadesServerSigner.Util.CmdSoap.SoapClient;
import eu.europa.esig.dss.cades.signature.CAdESTimestampParameters;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.*;
import eu.europa.esig.dss.model.Policy;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.*;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pdf.IPdfObjFactory;
import eu.europa.esig.dss.service.SecureRandomNonceSource;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.spi.client.http.Protocol;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificatePool;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPSource;
import eu.europa.esig.dss.validation.*;
import eu.europa.esig.dss.validation.timestamp.TimestampOCSPSource;
import org.apache.commons.codec.binary.Base64;
import eu.europa.esig.dss.pdf.pdfbox.*;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pdf.openpdf.ITextDefaultPdfObjFactory;

import javax.xml.soap.SOAPException;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.padesserversigner.PadesServerSigner.Util.CmdSoap.SoapClient.*;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

@Component
public class DSS {

    @Value("${application.id}")
    private String propertiesApplicationId;

    // Static field that can be accessed by static methods
    private static String applicationId;

    // PostConstruct method, called after the bean is initialized
    @PostConstruct
    public void init() {
        applicationId = propertiesApplicationId;
    }

    private static final String filesPADES = System.getProperty("user.home") + "/Documents/PADES";

    public static SignatureFieldParameters createSignatureFieldParameters(String name){
        SignatureFieldParameters signatureFieldParameters = new SignatureFieldParameters();
        signatureFieldParameters.setName(name);
        signatureFieldParameters.setPage(0);

        signatureFieldParameters.setOriginX(200);
        signatureFieldParameters.setOriginY(600);
        signatureFieldParameters.setWidth(400);
        signatureFieldParameters.setHeight(80);

        return signatureFieldParameters;
    }

    public static SignatureImageParameters createSignatureImageParameters(byte[] image, int xAxis, int yAxis, int width,int height, String imageText){
        SignatureImageParameters signatureImageParameters = new SignatureImageParameters();


        try (FileOutputStream fos = new FileOutputStream(filesPADES + "/image.png")) {
            fos.write(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(filesPADES + "/image.png");
        signatureImageParameters.setImage(new FileDocument(file));

        signatureImageParameters.setPage(1);

        signatureImageParameters.setBackgroundColor(Color.BLACK);
        signatureImageParameters.setxAxis(xAxis);
        signatureImageParameters.setyAxis(yAxis);
        signatureImageParameters.setWidth(width);
        signatureImageParameters.setHeight(height);

        signatureImageParameters.setAlignmentHorizontal(SignatureImageParameters.VisualSignatureAlignmentHorizontal.CENTER);

        return signatureImageParameters;
    }


    public static SignatureImageParameters createSignatureTextImageParameters(String imageText){
        SignatureImageParameters signatureImageParameters = new SignatureImageParameters();
        signatureImageParameters.setPage(1);

        signatureImageParameters.setBackgroundColor(Color.BLACK);
        signatureImageParameters.setxAxis(200);
        signatureImageParameters.setyAxis(600);
        signatureImageParameters.setWidth(400);
        signatureImageParameters.setHeight(80);

        signatureImageParameters.setAlignmentHorizontal(SignatureImageParameters.VisualSignatureAlignmentHorizontal.CENTER);

        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
        // Defines the text content
        textParameters.setText(imageText);
        // Specifies the text size value (the default font size is 12pt)
        textParameters.setSize(14);
        // Defines the color of the characters
        textParameters.setTextColor(Color.BLACK);
        // Defines the background color for the area filled out by the text
        textParameters.setBackgroundColor(Color.YELLOW);
        // Defines a padding between the text and a border of its bounding area
        textParameters.setPadding(20);
        // Set textParameters to a SignatureImageParameters object
        signatureImageParameters.setTextParameters(textParameters);

        textParameters.setSignerTextPosition(SignatureImageTextParameters.SignerTextPosition.LEFT);
        // Specifies a horizontal alignment of a text with respect to its area
        textParameters.setSignerTextHorizontalAlignment(SignatureImageTextParameters.SignerTextHorizontalAlignment.RIGHT);
        // Specifies a vertical alignment of a text block with respect to a signature field area
        textParameters.setSignerTextVerticalAlignment(SignatureImageTextParameters.SignerTextVerticalAlignment.TOP);

        return signatureImageParameters;
    }

    public static Policy getSignaturePolicy(){
        Policy signaturePolicy = new Policy();

        File policyFile = new File(filesPADES + "/Policies/POL#16.PolAssQual_signed_signed.pdf");
        DSSDocument policyDSSDocument = new FileDocument(policyFile);
        String policyDigest = policyDSSDocument.getDigest(DigestAlgorithm.SHA1);

        signaturePolicy.setId("2.16.620.2.1.2.2");

        signaturePolicy.setDescription("Política CMD de assinatura qualificada");

        signaturePolicy.setDigestAlgorithm(DigestAlgorithm.SHA1);

        signaturePolicy.setDigestValue(Base64.decodeBase64(policyDigest));

        signaturePolicy.setSpuri("https://apps.autenticacao.gov.pt/documents/10179/615532/POL%2316.PolAssQual_signed_signed.pdf");

        signaturePolicy.setQualifier("OIDAsURN");

        return signaturePolicy;
    }

    public static PAdESSignatureParameters createPAdESSignatureParametersBlevel(String signatureField,String signatureLevel, String reason, String location,String contactInfo){
        PAdESSignatureParameters pAdESSignatureParameters = new PAdESSignatureParameters();
        pAdESSignatureParameters.setContactInfo(contactInfo);  //nao aparece
        pAdESSignatureParameters.setReason(reason);
        pAdESSignatureParameters.setLocation(location);
        pAdESSignatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        pAdESSignatureParameters.setContentSize(50000);
        pAdESSignatureParameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        switch (signatureLevel){
            case ("pades_t"):
                pAdESSignatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
                break;
            case ("pades_lt"):
                pAdESSignatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LT);
                break;
            case ("pades_lta"):
                pAdESSignatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
                break;
            default:
                pAdESSignatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
                break;
        }

        pAdESSignatureParameters.bLevel().setSignaturePolicy(getSignaturePolicy());

        return pAdESSignatureParameters;
    }


    public static HashMap<String,X509Certificate> getCert(String phoneNumber) throws Exception {
        HashMap<String,X509Certificate> certChain = new HashMap<String,X509Certificate>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode getCertificateParameters = mapper.createObjectNode();

        ((ObjectNode) getCertificateParameters).put("phoneNumber",phoneNumber);
        ((ObjectNode) getCertificateParameters).put("applicationId",applicationId);

        String certChainString = SoapClient.getCertificate(getCertificateParameters);

        CertificateFactory fact = CertificateFactory.getInstance("X.509");

        InputStream signingCert = new ByteArrayInputStream(certChainString.split("\\n\\n")[0].getBytes());
        X509Certificate signingCertX509 = (X509Certificate) fact.generateCertificate(signingCert);

        certChain.put("signingCert",signingCertX509);

        InputStream certsChain = new ByteArrayInputStream(certChainString.split("\\n\\n")[1].getBytes());
        Collection<X509Certificate> certsChainCollection = (Collection<X509Certificate>) fact.generateCertificates(certsChain);

        for (X509Certificate cert : certsChainCollection) {
            switch (cert.getSerialNumber().toString()) {
                case "87357526987899817111535713719133838329":
                    certChain.put("ecEstado",cert);
                    break;
                case "4888358685188713580":
                    certChain.put("subEcEstado",cert);
                    break;
                default:
                    throw new Exception("Certificados erados");
            }
        }

        return certChain;
    }

    public static JsonNode padesLTASignCMD(String phoneNumber, String pin, String docName, byte[] fileToSign,String signatureLevel, byte[] image, int xAxis, int yAxis, int width,int height,String imageText,String reason, String location,String contactInfo) throws Exception {
        PAdESSignatureParameters pAdESSignatureParameters = createPAdESSignatureParametersBlevel("PSSSignatureField",signatureLevel,reason,location,contactInfo);

        HashMap<String,X509Certificate> certChain = getCert(phoneNumber);

        CertificateToken certificateSignerToken = new CertificateToken(certChain.get("signingCert"));
        pAdESSignatureParameters.setSigningCertificate(certificateSignerToken);

        pAdESSignatureParameters.setCertificateChain(certificateSignerToken);
        pAdESSignatureParameters.setCertificateChain(new CertificateToken(certChain.get("subEcEstado")));
        pAdESSignatureParameters.setCertificateChain(new CertificateToken(certChain.get("ecEstado")));

        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);

        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(ocspDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);


        File certfile6 = new File(filesPADES + "/certs/timestamp/cc0018.pem");

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        FileInputStream is6 = new FileInputStream (certfile6);
        X509Certificate cer6 = (X509Certificate) fact.generateCertificate(is6);

        CertificateToken certest6 = new CertificateToken(cer6);

        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

        CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource();
        commonTrustedCertificateSource.addCertificate(certest6);

        commonCertificateVerifier.setTrustedCertSource(commonTrustedCertificateSource);

        PAdESService pAdESService = new PAdESService(commonCertificateVerifier);
        pAdESService.setPdfObjFactory(new ITextDefaultPdfObjFactory());

        OnlineTSPSource tspSource =  new OnlineTSPSource("http://ts.cartaodecidadao.pt/tsa/server");
        tspSource.setDataLoader(new TimestampDataLoader());

        pAdESService.setTspSource(tspSource);

        //Hash do documento para assinar
        DSSDocument fileDocument = new InMemoryDocument(fileToSign);

        if(image!=null && !imageText.isEmpty()){
            pAdESSignatureParameters.setImageParameters(createSignatureImageParameters(image,xAxis,yAxis,width,height,imageText));
        } else if(!imageText.isEmpty()){
            pAdESSignatureParameters.setImageParameters(createSignatureTextImageParameters(imageText));
        }

        InputStream inputStream = fileDocument.openStream();

        byte[] hash = pAdESService.getDataToSign(fileDocument, pAdESSignatureParameters).getBytes();

        //insere prefixo na assinatura
        String base64ToSignString = Base64.encodeBase64String(hash);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNodeAuthorize = mapper.createObjectNode();

        ((ObjectNode) rootNodeAuthorize).put("phoneNumber",phoneNumber);
        ((ObjectNode) rootNodeAuthorize).put("pin",pin);
        ((ObjectNode) rootNodeAuthorize).put("docName",docName);
        ((ObjectNode) rootNodeAuthorize).put("hash",base64ToSignString);

        String processId = SoapClient.signRequest(rootNodeAuthorize);

        byte[] toDatabase = inputStream.readAllBytes();
        inputStream.close();
        String stream = Base64.encodeBase64String(toDatabase);

        JsonNode valuesToSave = mapper.createObjectNode();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        ((ObjectNode) valuesToSave).put("date",dateFormat.format(pAdESSignatureParameters.getSigningDate()));

        ((ObjectNode) valuesToSave).put("document",stream);
        ((ObjectNode) valuesToSave).put("processId",processId);

        return valuesToSave;
    }


    public static String padesLTAsendOTP(String dateString, String phoneNumber, String processId, byte[] document, String otp,String signatureLevel, byte[] image,int xAxis, int yAxis, int width,int height,String imageText,String reason, String location,String contactInfo) throws Exception {
        PAdESSignatureParameters pAdESSignatureParameters1 = createPAdESSignatureParametersBlevel("PSSSignatureField",signatureLevel,reason,location,contactInfo);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        Date date = dateFormat.parse(dateString);
        pAdESSignatureParameters1.bLevel().setSigningDate(date);

        HashMap<String, X509Certificate> certChain = getCert(phoneNumber);

        CertificateToken certificateSignerToken = new CertificateToken(certChain.get("signingCert"));
        pAdESSignatureParameters1.setSigningCertificate(certificateSignerToken);

        pAdESSignatureParameters1.setCertificateChain(certificateSignerToken);
        pAdESSignatureParameters1.setCertificateChain(new CertificateToken(certChain.get("subEcEstado")));
        pAdESSignatureParameters1.setCertificateChain(new CertificateToken(certChain.get("ecEstado")));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNodeSendOtp = mapper.createObjectNode();

        ((ObjectNode) rootNodeSendOtp).put("SAD",processId);
        ((ObjectNode) rootNodeSendOtp).put("applicationId",applicationId);
        ((ObjectNode) rootNodeSendOtp).put("code",otp);

        String signaturebase64 = SoapClient.validateOtp(rootNodeSendOtp);
        byte[] signature = Base64.decodeBase64(signaturebase64.getBytes("UTF-8"));

        //cria um signatureValue
        SignatureValue signatureValue = new SignatureValue(SignatureAlgorithm.RSA_SHA256,signature);

        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();


        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);

        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(ocspDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);


        File certfile6 = new File(filesPADES +"/certs/timestamp/cc0018.pem");

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        FileInputStream is6 = new FileInputStream (certfile6);
        X509Certificate cer6 = (X509Certificate) fact.generateCertificate(is6);

        CertificateToken certest6 = new CertificateToken(cer6);


        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

        CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource();
        commonTrustedCertificateSource.addCertificate(certest6);

        commonCertificateVerifier.setTrustedCertSource(commonTrustedCertificateSource);


        PAdESService pAdESService = new PAdESService(commonCertificateVerifier);

        OnlineTSPSource tspSource =  new OnlineTSPSource("http://ts.cartaodecidadao.pt/tsa/server");
        tspSource.setDataLoader(new TimestampDataLoader());

        pAdESService.setTspSource(tspSource);

        if(image!=null && !imageText.isEmpty()){
            pAdESSignatureParameters1.setImageParameters(createSignatureImageParameters(image,xAxis,yAxis,width,height,imageText));
        } else if(!imageText.isEmpty()){
            pAdESSignatureParameters1.setImageParameters(createSignatureTextImageParameters(imageText));
        }

        DSSDocument dssDocument1 = new InMemoryDocument(document);
        System.out.println("EStou dentro do docuemnto");

        //assina e grava no pdf sign_result
        dssDocument1 = pAdESService.signDocument(dssDocument1, pAdESSignatureParameters1, signatureValue);

        System.out.println(dssDocument1.getName());
        try {
            dssDocument1.save(filesPADES + "/" + dssDocument1.getName());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return "success";
    }

    public static JsonNode padesLTASignCMDCC(List<X509Certificate> certificates, String docName, byte[] fileToSign, String signatureLevel, byte[] image, int xAxis, int yAxis, int width, int height, String imageText, String reason, String location, String contactInfo) throws Exception {
        PAdESSignatureParameters pAdESSignatureParameters = createPAdESSignatureParametersBlevel("PSSSignatureField",signatureLevel,reason,location,contactInfo);

        HashMap<String,X509Certificate> certChain = new HashMap<String,X509Certificate>();

        certChain.put("signingCert",certificates.get(0));
        certChain.put("subEcEstado",certificates.get(1));
        certChain.put("ecEstado",certificates.get(2));
        certChain.put("ecRaizEstado",certificates.get(3));


        CertificateToken certificateSignerToken = new CertificateToken(certChain.get("signingCert"));
        pAdESSignatureParameters.setSigningCertificate(certificateSignerToken);

        pAdESSignatureParameters.setCertificateChain(certificateSignerToken);
        pAdESSignatureParameters.setCertificateChain(new CertificateToken(certChain.get("subEcEstado")));
        pAdESSignatureParameters.setCertificateChain(new CertificateToken(certChain.get("ecEstado")));
        //pAdESSignatureParameters.setCertificateChain(new CertificateToken(certChain.get("ecRaizEstado")));

        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);

        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(ocspDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);


        File certfile6 = new File(filesPADES + "/certs/timestamp/cc0018.pem");

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        FileInputStream is6 = new FileInputStream (certfile6);
        X509Certificate cer6 = (X509Certificate) fact.generateCertificate(is6);

        CertificateToken certest6 = new CertificateToken(cer6);

        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

        CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource();
        commonTrustedCertificateSource.addCertificate(certest6);

        commonCertificateVerifier.setTrustedCertSource(commonTrustedCertificateSource);

        PAdESService pAdESService = new PAdESService(commonCertificateVerifier);
        pAdESService.setPdfObjFactory(new ITextDefaultPdfObjFactory());

        OnlineTSPSource tspSource =  new OnlineTSPSource("http://ts.cartaodecidadao.pt/tsa/server");
        tspSource.setDataLoader(new TimestampDataLoader());

        pAdESService.setTspSource(tspSource);

        //Hash do documento para assinar
        DSSDocument fileDocument = new InMemoryDocument(fileToSign);

        if(image!=null && !imageText.isEmpty()){
            pAdESSignatureParameters.setImageParameters(createSignatureImageParameters(image,xAxis,yAxis,width,height,imageText));
        } else if(!imageText.isEmpty()){
            pAdESSignatureParameters.setImageParameters(createSignatureTextImageParameters(imageText));
        }

        InputStream inputStream = fileDocument.openStream();

        byte[] hash = pAdESService.getDataToSign(fileDocument, pAdESSignatureParameters).getBytes();

        //insere prefixo na assinatura
        String base64ToSignString = Base64.encodeBase64String(hash);


        byte[] toDatabase = inputStream.readAllBytes();
        inputStream.close();
        String stream = Base64.encodeBase64String(toDatabase);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode valuesToSave = mapper.createObjectNode();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        ((ObjectNode) valuesToSave).put("date",dateFormat.format(pAdESSignatureParameters.getSigningDate()));

        UUID uuid = UUID.randomUUID();

        String sad = uuid.toString();

        ((ObjectNode) valuesToSave).put("document",stream);
        ((ObjectNode) valuesToSave).put("sad",sad);
        ((ObjectNode) valuesToSave).put("hash",base64ToSignString);

        return valuesToSave;
    }

    public static String padesLTAsendOTPCC(String signatureBase64, List<X509Certificate> certificates, String dateString, String phoneNumber, String processId, byte[] document, String otp,String signatureLevel, byte[] image,int xAxis, int yAxis, int width,int height,String imageText,String reason, String location,String contactInfo) throws Exception {
        PAdESSignatureParameters pAdESSignatureParameters1 = createPAdESSignatureParametersBlevel("PSSSignatureField",signatureLevel,reason,location,contactInfo);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        Date date = dateFormat.parse(dateString);
        pAdESSignatureParameters1.bLevel().setSigningDate(date);


        HashMap<String,X509Certificate> certChain = new HashMap<String,X509Certificate>();

        certChain.put("signingCert",certificates.get(0));
        certChain.put("subEcEstado",certificates.get(1));
        certChain.put("ecEstado",certificates.get(2));
        certChain.put("ecRaizEstado",certificates.get(3));

        CertificateToken certificateSignerToken = new CertificateToken(certChain.get("signingCert"));
        pAdESSignatureParameters1.setSigningCertificate(certificateSignerToken);

        pAdESSignatureParameters1.setCertificateChain(certificateSignerToken);
        pAdESSignatureParameters1.setCertificateChain(new CertificateToken(certChain.get("subEcEstado")));
        pAdESSignatureParameters1.setCertificateChain(new CertificateToken(certChain.get("ecEstado")));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNodeSendOtp = mapper.createObjectNode();


        byte[] signature = Base64.decodeBase64(signatureBase64.getBytes("UTF-8"));

        //cria um signatureValue
        SignatureValue signatureValue = new SignatureValue(SignatureAlgorithm.RSA_SHA256,signature);

        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);

        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(ocspDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);


        File certfile6 = new File(filesPADES + "certs/timestamp/cc0018.pem");

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        FileInputStream is6 = new FileInputStream (certfile6);
        X509Certificate cer6 = (X509Certificate) fact.generateCertificate(is6);

        CertificateToken certest6 = new CertificateToken(cer6);

        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

        CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource();
        commonTrustedCertificateSource.addCertificate(certest6);

        commonCertificateVerifier.setTrustedCertSource(commonTrustedCertificateSource);

        PAdESService pAdESService = new PAdESService(commonCertificateVerifier);

        OnlineTSPSource tspSource =  new OnlineTSPSource("http://ts.cartaodecidadao.pt/tsa/server");
        tspSource.setDataLoader(new TimestampDataLoader());

        pAdESService.setTspSource(tspSource);


        if(image!=null && !imageText.isEmpty()){
            pAdESSignatureParameters1.setImageParameters(createSignatureImageParameters(image,xAxis,yAxis,width,height,imageText));
        } else if(!imageText.isEmpty()){
            pAdESSignatureParameters1.setImageParameters(createSignatureTextImageParameters(imageText));
        }

        DSSDocument dssDocument1 = new InMemoryDocument(document);

        //assina e grava no pdf sign_result
        dssDocument1 = pAdESService.signDocument(dssDocument1, pAdESSignatureParameters1, signatureValue);

        System.out.println(dssDocument1.getName());
        try {
            dssDocument1.save(filesPADES + "/" + dssDocument1.getName());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return "success";
    }

    public static void testerPSS() throws Exception {
        PAdESSignatureParameters pAdESSignatureParameters = createPAdESSignatureParametersBlevel("treta","pades_lta","asd","asd","asd");

        HashMap<String,X509Certificate> certChain = getCert("+351 912123123");

        CertificateToken certificateSignerToken = new CertificateToken(certChain.get("signingCert"));
        pAdESSignatureParameters.setSigningCertificate(certificateSignerToken);

        pAdESSignatureParameters.setCertificateChain(certificateSignerToken);
        pAdESSignatureParameters.setCertificateChain(new CertificateToken(certChain.get("subEcEstado")));
        pAdESSignatureParameters.setCertificateChain(new CertificateToken(certChain.get("ecEstado")));

        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);

        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(ocspDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);


        File certfile6 = new File(filesPADES + "/certs/timestamp/mycert.pem");

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        FileInputStream is6 = new FileInputStream (certfile6);
        X509Certificate cer6 = (X509Certificate) fact.generateCertificate(is6);

        CertificateToken certest6 = new CertificateToken(cer6);

        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

        CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource();
        commonTrustedCertificateSource.addCertificate(certest6);

        commonCertificateVerifier.setTrustedCertSource(commonTrustedCertificateSource);

        PAdESService pAdESService = new PAdESService(commonCertificateVerifier);
        pAdESService.setPdfObjFactory(new ITextDefaultPdfObjFactory());

        OnlineTSPSource tspSource =  new OnlineTSPSource("http://ts.cartaodecidadao.pt/tsa/server");
        tspSource.setDataLoader(new TimestampDataLoader());

        pAdESService.setTspSource(tspSource);

        //Hash do documento para assinar
        DSSDocument fileDocument = new FileDocument(filesPADES + "/pss_tester.pdf");

        InputStream inputStream = fileDocument.openStream();

        byte[] hash = pAdESService.getDataToSign(fileDocument, pAdESSignatureParameters).getBytes();

        //insere prefixo na assinatura
        String base64ToSignString = Base64.encodeBase64String(hash);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNodeAuthorize = mapper.createObjectNode();

        ((ObjectNode) rootNodeAuthorize).put("phoneNumber","+351 912123123");
        ((ObjectNode) rootNodeAuthorize).put("pin","1111");
        ((ObjectNode) rootNodeAuthorize).put("docName","padestester");
        ((ObjectNode) rootNodeAuthorize).put("hash",base64ToSignString);

        String processId = SoapClient.signRequest(rootNodeAuthorize);

        byte[] toDatabase = inputStream.readAllBytes();
        inputStream.close();
        String stream = Base64.encodeBase64String(toDatabase);

        JsonNode valuesToSave = mapper.createObjectNode();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        ((ObjectNode) valuesToSave).put("date",dateFormat.format(pAdESSignatureParameters.getSigningDate()));

        ((ObjectNode) valuesToSave).put("document",stream);
        ((ObjectNode) valuesToSave).put("processId",processId);


        Scanner sc= new Scanner(System.in); //System.in is a standard input stream
        System.out.print("Enter a otp: ");
        String otp = sc.nextLine();              //reads string

        //ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNodeSendOtp = mapper.createObjectNode();

        ((ObjectNode) rootNodeSendOtp).put("SAD",processId);
        ((ObjectNode) rootNodeSendOtp).put("applicationId",applicationId);
        ((ObjectNode) rootNodeSendOtp).put("code",otp);

        String signaturebase64 = SoapClient.validateOtp(rootNodeSendOtp);
        byte[] signature = Base64.decodeBase64(signaturebase64.getBytes("UTF-8"));

        //cria um signatureValue
        SignatureValue signatureValue = new SignatureValue(SignatureAlgorithm.RSA_SHA256,signature);


        System.out.println("EStou dentro do docuemnto");
        //assina e grava no pdf sign_result
        DSSDocument dssDocument1 = pAdESService.signDocument(fileDocument, pAdESSignatureParameters, signatureValue);

        System.out.println(dssDocument1.getName());
        try {
            dssDocument1.save(filesPADES + "/" + dssDocument1.getName());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }



    public static void main(String[] args) throws Exception {
        //File file = new File(filesPADES + "/pss_tester.pdf");
        /*
        FileInputStream fis = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();

        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }


        String encoded = Base64.encodeBase64String(bArray);
        System.out.println(encoded);

         */

        //padesTester("+351 912123123","",applicationId, "PADESDSSTESTER",file);


        //JsonNode toDatabase = crpbs("+351 912123123",pin,applicationId, "PADESDSSTESTER",file);

        //byte[] document = Base64.decodeBase64(toDatabase.get("document").asText());

        //String ola = DSS.sendOTP(toDatabase.get("date").asText(),"+351 912123123",toDatabase.get("processId").asText(),document);
        //testerPSS();

    }
    
}