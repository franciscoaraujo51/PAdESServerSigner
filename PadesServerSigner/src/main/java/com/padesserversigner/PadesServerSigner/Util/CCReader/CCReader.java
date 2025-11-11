package com.padesserversigner.PadesServerSigner.Util.CCReader;

import com.padesserversigner.PadesServerSigner.Service.CmdService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import org.apache.commons.codec.binary.Base64;

import org.poreid.CardFactory;
import org.poreid.PkAlias;
import org.poreid.RSAPaddingSchemes;
import org.poreid.cc.CitizenCard;
import org.poreid.cc.gemsafe.GemsafeCard;
import org.poreid.cc.ias.IASCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/*
* APENAS utilizado para testes com CARTAO DE CIDADAO
* */
public class CCReader {


    private static CmdService cmdService;

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

    public static SignatureImageParameters createSignatureImageParameters(){
        SignatureImageParameters signatureImageParameters = new SignatureImageParameters();
        File file = new File("/Users/franciscoaraujo/Desktop/tese_codigo/pades_serversigner/PadesServerSigner/src/main/resources/padesimagem.png");

        signatureImageParameters.setPage(1);

        signatureImageParameters.setBackgroundColor(Color.BLACK);
        signatureImageParameters.setxAxis(200);
        signatureImageParameters.setyAxis(600);
        signatureImageParameters.setWidth(400);
        signatureImageParameters.setHeight(80);

        signatureImageParameters.setAlignmentHorizontal(SignatureImageParameters.VisualSignatureAlignmentHorizontal.CENTER);
/*
        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
        // Defines the text content
        textParameters.setText("John \n +351 911111111");
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
    */
        signatureImageParameters.setImage(new FileDocument(file));

        return signatureImageParameters;
    }

    public static PAdESSignatureParameters createPAdESSignatureParametersBlevel(String signatureField,String signatureLevel){
        PAdESSignatureParameters pAdESSignatureParameters = new PAdESSignatureParameters();

        pAdESSignatureParameters.setReason("testepadesserversigner");
        pAdESSignatureParameters.setLocation("testepadesserversignerLocation");
        pAdESSignatureParameters.setReason("padesdsstest");
        pAdESSignatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA1);
        pAdESSignatureParameters.setContentSize(25000);
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

        return pAdESSignatureParameters;
    }

    public static HashMap<String,X509Certificate> getCertificate() {
        HashMap<String,X509Certificate> certChain = new HashMap<String,X509Certificate>();
        CitizenCard cc;

        Locale locale = new Locale("pt","PT");


        try{
            cc = CardFactory.getCard(locale);

            List<X509Certificate> certificates = cc.getAuthenticationCertificateChain();

            certChain.put("signingCert",certificates.get(0));
            certChain.put("subEcEstado",certificates.get(1));
            certChain.put("ecEstado",certificates.get(2));
            certChain.put("ecRaizEstado",certificates.get(3));

            cc.close();


        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return certChain;
    }

    public static String sign() {
        PAdESSignatureParameters pAdESSignatureParameters = createPAdESSignatureParametersBlevel("treta","pades_b");

        HashMap<String,X509Certificate> certChain = getCertificate();
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

        DSSDocument fileDocument = new FileDocument("/Users/franciscoaraujo/Desktop/tese_codigo/pades_serversigner/PadesServerSigner/src/main/resources/pss_tester.pdf");

        pAdESSignatureParameters.setImageParameters(createSignatureImageParameters());

        InputStream inputStream = fileDocument.openStream();

        PAdESService pAdESService = new PAdESService(commonCertificateVerifier);
        byte[] hash = pAdESService.getDataToSign(fileDocument, pAdESSignatureParameters).getBytes();

        //insere prefixo na assinatura
        String base64ToSignString = Base64.encodeBase64String(hash);

        System.out.println(base64ToSignString);

         GemsafeCard instance;

        Locale locale = new Locale("pt","PT");


        try{
            instance = CardFactory.getCard();

            byte[] sha256Prefix =  new byte[]{0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14};

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write( sha256Prefix );

            MessageDigest d = MessageDigest.getInstance("SHA-1");
            d.update(hash);
            byte[] arrayToSign = d.digest();

            outputStream.write( arrayToSign );

            byte[] newmessage = outputStream.toByteArray();

            byte[] signature = instance.sign(arrayToSign, "123".getBytes(),"SHA-1", PkAlias.ASSINATURA, RSAPaddingSchemes.PKCS1);

            System.out.println(Base64.encodeBase64String(signature));

            SignatureValue signatureValue = new SignatureValue(SignatureAlgorithm.RSA_SHA1,signature);

            DSSDocument dssDocument1 = pAdESService.signDocument(fileDocument, pAdESSignatureParameters, signatureValue);

            System.out.println(dssDocument1.getName());
            try {
                dssDocument1.save("/Users/franciscoaraujo/Desktop/tese_codigo/pades_serversigner/PadesServerSigner/src/main/resources/pades_signed.pdf");
            }catch (Exception e){
                System.out.println(e.getMessage());
            }


            return new String(signature);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return "error";

    }
}
