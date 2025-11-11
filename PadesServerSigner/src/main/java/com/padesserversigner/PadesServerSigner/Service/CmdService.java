package com.padesserversigner.PadesServerSigner.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.padesserversigner.PadesServerSigner.Exception.ApiRequestException;
import com.padesserversigner.PadesServerSigner.Model.User;
import com.padesserversigner.PadesServerSigner.Repository.UserDao;
import com.padesserversigner.PadesServerSigner.Security.JwtUtil;
import com.padesserversigner.PadesServerSigner.Util.CmdSoap.SoapClient;
import com.padesserversigner.PadesServerSigner.Util.DSS;
import org.apache.commons.codec.binary.Base64;
import org.aspectj.weaver.bcel.BcelRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import javax.sql.rowset.serial.*;

import javax.xml.soap.SOAPException;
import java.io.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.padesserversigner.PadesServerSigner.Util.CmdSoap.SoapClient.*;
import com.padesserversigner.PadesServerSigner.Util.DSS.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class CmdService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ResourceLoader resourceLoader;

    public ResponseEntity requirePDF() {
        Resource resource = resourceLoader.getResource("classpath:/pdf1234.pdf");

        try {
            File file = resource.getFile();

            eu.europa.esig.dss.model.FileDocument fileDocument = new eu.europa.esig.dss.model.FileDocument(file);
            System.out.println(fileDocument.getName());

            return ResponseEntity.ok("Success");
        }catch (Exception e){
            throw new ApiRequestException("Resource not found!","An error occurred during the read of the resource",HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //verificar se o resultado do id e null
        User u = userDao.getByPCredentialID(username);
           return new org.springframework.security.core.userdetails.User(u.getCredentialID(),u.getPin(), new ArrayList<>());
    }

    public ResponseEntity authLogin(String phoneNumber, String pin){
        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.createObjectNode();

        System.out.println("here1");

        UUID uuid = UUID.randomUUID();

        String credentialId = uuid.toString();
        System.out.println("here2");

        UserDetails us = new org.springframework.security.core.userdetails.User(credentialId,pin, new ArrayList<>());

        String token = JwtUtil.generateToken(us);

        System.out.println("here3");

        ((ObjectNode) rootNode).put("access_token", token);

        try {
            User user = new User();

            user.setPhoneNumber(phoneNumber);
            user.setPin(pin);
            user.setToken(token);
            user.setCredentialID(credentialId);

            userDao.save(user);
        }catch (Exception e){
            throw new ApiRequestException("authentication_error","An error occurred during authentication process",HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(rootNode);
    }

    public ResponseEntity<JsonNode> credentialsList(String credentialID) {
        try {
            User user = userDao.getByPCredentialID(credentialID);

            ObjectMapper mapper = new ObjectMapper();
            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.add(user.getCredentialID());

            JsonNode rootNode = mapper.createObjectNode();
            ((ObjectNode) rootNode).put("credentialID", arrayNode);

            return ResponseEntity.status(HttpStatus.OK).body(rootNode);
        }catch (Exception e){
            throw new ApiRequestException("invalid_request","Invalid parameter userID",HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<JsonNode> credentialsInfo(JsonNode parameters) {
        try {
            String credentialId = parameters.get("credentialID").asText();
            String certificates = parameters.get("certificates").asText();

            User user = userDao.getByPCredentialID(credentialId);
            String certs = null;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode parametersToCMD = mapper.createObjectNode();
            ((ObjectNode) parametersToCMD).put("phoneNumber", user.getPhoneNumber());

            System.out.println(parametersToCMD.get(""));

            JsonNode rootNode = mapper.createObjectNode();

            switch (certificates) {
                case "none":
                    break;
                case "single":
                    String certChain = SoapClient.getCertificate(parametersToCMD);

                    certs=certChain.split("\\n\\n")[0];
                    break;
                case "chain":
                    certs = SoapClient.getCertificate(parametersToCMD);
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rootNode);
            }

            JsonNode keyNode = mapper.createObjectNode();

            ((ObjectNode) keyNode).put("status","enable");
            ((ObjectNode) keyNode).put("algo","1.2.840.113549.1.1.11");
            ((ObjectNode) keyNode).put("len","3072");//deve-se ir buscar ao certificado

            JsonNode certNode = mapper.createObjectNode();
            ((ObjectNode) certNode).put("status","valid");
            ((ObjectNode) certNode).put("certificates",certs);//array em vez de string


            ((ObjectNode) rootNode).put("key",keyNode);
            ((ObjectNode) rootNode).put("cert",certNode);

            ((ObjectNode) rootNode).put("authMode","implicit");
            ((ObjectNode) rootNode).put("SCAL","2");
            ((ObjectNode) rootNode).put("multisign","10");

            return ResponseEntity.status(HttpStatus.OK).body(rootNode);
        }catch (Exception e){
            throw new ApiRequestException("invalid_request","Invalid parameter credentialID",HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<JsonNode> credentialsAuthorize(JsonNode parameters)  {
        int numSignatures = parameters.get("numSignatures").asInt();
        String sad;

        String credentialId = parameters.get("credentialID").asText();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.createObjectNode();

        if (numSignatures==1) {
            String uniqueHash;
            String docName;

            if(parameters.get("hash").isArray()){
                ArrayNode arrayNode = (ArrayNode) parameters.get("hash");
                uniqueHash = arrayNode.get(0).asText();
            }else{
                throw new ApiRequestException("invalid_request","Missing (or invalid type) array parameter hash",HttpStatus.BAD_REQUEST);
            }

            if(parameters.get("clientData").isArray()){
                ArrayNode arrayNode = (ArrayNode) parameters.get("clientData");
                docName = arrayNode.get(0).asText();
            }else{
                throw new ApiRequestException("invalid_request","The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed.",HttpStatus.BAD_REQUEST);
            }

            try {
                User user = userDao.getByPCredentialID(credentialId);

                JsonNode parametersToCMD = mapper.createObjectNode();
                ((ObjectNode) parametersToCMD).put("phoneNumber", user.getPhoneNumber());
                ((ObjectNode) parametersToCMD).put("pin", user.getPin());
                ((ObjectNode) parametersToCMD).put("docName", docName);
                ((ObjectNode) parametersToCMD).put("hash", uniqueHash);

                sad = SoapClient.signRequest(parametersToCMD);
            } catch (Exception e) {
                throw new ApiRequestException("invalid_request", "invalid_request", HttpStatus.BAD_REQUEST);
            }
            ((ObjectNode) jsonNode).put("SAD", sad);
            ((ObjectNode) jsonNode).put("expiresIn", "300");
        }else if (numSignatures>1 && numSignatures < 10){
            ArrayNode hash = (ArrayNode) parameters.get("hash");
            ArrayNode clientData = (ArrayNode) parameters.get("clientData");
            if(hash.size() != numSignatures && clientData.size() != numSignatures){
                throw new ApiRequestException("invalid_request","The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed.",HttpStatus.BAD_REQUEST);
            }
            else {
                try {
                    User user = userDao.getByPCredentialID(credentialId);

                    JsonNode parametersToCMD = mapper.createObjectNode();
                    ((ObjectNode) parametersToCMD).put("phoneNumber", user.getPhoneNumber());
                    ((ObjectNode) parametersToCMD).put("pin", user.getPin());
                    ((ObjectNode) parametersToCMD).put("docName", parameters.get("clientData"));
                    ((ObjectNode) parametersToCMD).put("hash", parameters.get("hash"));
                    ((ObjectNode) parametersToCMD).put("numSignatures", numSignatures);

                    sad = SoapClient.multipleSignRequest(parametersToCMD);
                } catch (Exception e) {
                    throw new ApiRequestException("invalid_request", "invalid_request", HttpStatus.BAD_REQUEST);
                }
            }
            ((ObjectNode) jsonNode).put("SAD", sad);
            ((ObjectNode) jsonNode).put("expiresIn", "300");
        }else {
            System.out.println("less than 1");
            throw new ApiRequestException("invalid_request","Invalid value for parameter numSignatures",HttpStatus.BAD_REQUEST);
        }
        try {
            User user = userDao.getByPCredentialID(credentialId);
            user.setNumSignatures(numSignatures);
            userDao.save(user);
        }catch (Exception e){
            throw new ApiRequestException("invalid_request","Invalid parameter credentialID",HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.status(HttpStatus.OK).body(jsonNode);
    }

    public ResponseEntity<JsonNode> signaturesSignHash(JsonNode parameters)  {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.createObjectNode();

        String credentialID = parameters.get("credentialID").asText();
        int numSignatures;
        try {
            numSignatures=userDao.getByPCredentialID(credentialID).getNumSignatures();
        }catch (Exception e){
            throw new ApiRequestException("invalid_request","Invalid parameter userID",HttpStatus.BAD_REQUEST);
        }

        try {
            if(numSignatures == 1){
                String certChain = SoapClient.validateOtp(parameters);
                ((ObjectNode) jsonNode).put("signatures", certChain);
            }else if(numSignatures > 1 && numSignatures < 10){
                jsonNode =  SoapClient.validateOtpMultipleRequest(parameters);
            }else {
               throw new ApiRequestException("invalid_request","Invalid digest value length",HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e){
            throw new ApiRequestException("database error","database error",HttpStatus.BAD_REQUEST);
        }
        return  ResponseEntity.status(HttpStatus.OK).body(jsonNode);
    }

    public ResponseEntity dssFiler2Sign(JsonNode parameters) throws Exception {
        JsonNode imageParameters = parameters.get("imageParameters");
        byte[] image = null;
        int xAxis = 0;
        int yAxis = 0;
        int width = 0;
        int height = 0;
        String imageText = "";
        if (imageParameters != null){
            xAxis=imageParameters.get("xAxis").asInt();
            yAxis=imageParameters.get("yAxis").asInt();
            width=imageParameters.get("width").asInt();
            height=imageParameters.get("height").asInt();
            image = Base64.decodeBase64(imageParameters.get("image").asText());
        }

        if(parameters.get("imageText")!=null){
            imageText=parameters.get("imageText").asText();
        }

        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.createObjectNode();

        UUID uuid = UUID.randomUUID();

        String credentialId = uuid.toString();

        UserDetails us = new org.springframework.security.core.userdetails.User(credentialId,parameters.get("pin").asText(), new ArrayList<>());

        String token = JwtUtil.generateToken(us);

        JsonNode toDatabase;
        try {
            byte[] document = Base64.decodeBase64(parameters.get("file").asText());

            toDatabase = DSS.padesLTASignCMD(parameters.get("phoneNumber").asText(),parameters.get("pin").asText(),parameters.get("docName").asText() ,document,parameters.get("signatureLevel").asText(),image,xAxis,yAxis,width,height,imageText,parameters.get("reason").asText(),parameters.get("location").asText(),parameters.get("contactInfo").asText());


        }catch (Exception e){
            throw e;
            //throw new ApiRequestException("cannot sign","database error",HttpStatus.BAD_REQUEST);

        }

        try {

            byte[] document = Base64.decodeBase64(toDatabase.get("document").asText());

            User user = new User();

            user.setPhoneNumber(parameters.get("phoneNumber").asText());
            user.setPin(parameters.get("pin").asText());
            user.setToken(token);
            user.setCredentialID(credentialId);
            user.setDate(toDatabase.get("date").asText());
            user.setDocument(document);
            user.setSignatureLevel(parameters.get("signatureLevel").asText());
            user.setxAxis(xAxis);
            user.setyAxis(yAxis);
            user.setWidth(width);
            user.setHeight(height);
            user.setImage(image);
            user.setImageText(imageText);
            user.setReason(parameters.get("reason").asText());
            user.setLocation(parameters.get("location").asText());
            user.setContactInfo(parameters.get("contactInfo").asText());

            userDao.save(user);

        }catch (Exception e){
            throw new ApiRequestException("Database error","Already exists an equal phone number",HttpStatus.BAD_REQUEST);
        }

        ((ObjectNode) rootNode).put("token", token);
        ((ObjectNode) rootNode).put("sad", toDatabase.get("processId").asText());
        ((ObjectNode) rootNode).put("credentialId", credentialId);

        return ResponseEntity.ok(rootNode);
    }

    public ResponseEntity dssFiler2SendOTP(JsonNode parameters, String credentialID) throws Exception {

        User user = userDao.getByPCredentialID(credentialID);

        String phoneNumber = user.getPhoneNumber();

        byte[] document = user.getDocument();

        try {
           DSS.padesLTAsendOTP(user.getDate(),phoneNumber,parameters.get("sad").asText(),document,parameters.get("otp").asText(),user.getSignatureLevel(),user.getImage(),user.getxAxis(),user.getyAxis(),user.getWidth(),user.getHeight(),user.getImageText(),user.getReason(),user.getLocation(),user.getContactInfo());
        }catch (Exception e){
            ResponseEntity.badRequest();
        }

        try {
            userDao.delete(user);

        }catch (Exception e){
            throw new ApiRequestException("Database error","Already exists an equal phone number",HttpStatus.BAD_REQUEST);
        }

        Resource fileLoad = resourceLoader.getResource("classpath:/pades_signed.pdf");

        try {
            InputStreamResource resource = new InputStreamResource(fileLoad.getInputStream());


            HttpHeaders headers = new HttpHeaders();

            headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", "pades_signed.pdf"));
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            ResponseEntity<Object>
                    responseEntity = ResponseEntity.ok().headers(headers).contentLength(
                    fileLoad.contentLength()).contentType(MediaType.parseMediaType("application/txt")).body(resource);

            return responseEntity;
        }catch (Exception e){
            throw new ApiRequestException("File not received","The File was not received with the signature",HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity storeCertificates(String certificates, String credentialID) throws Exception {

        try {

            System.out.println(certificates);

            User user = userDao.getByPCredentialID(credentialID);

            byte[] certificates_bytes = Base64.encodeBase64(certificates.getBytes());

            user.setCertificates(certificates_bytes);

            userDao.save(user);

        }catch(Exception e){
            new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("Success");
    }

    public ResponseEntity getCertificatesCC(String certificates,String credentialID) throws Exception {
        try {
            User user = userDao.getByPCredentialID(credentialID);

            String certificatesDatabase = new String(Base64.decodeBase64(user.getCertificates()));

            String certificatesToSend = "none";

            CertificateFactory fact = CertificateFactory.getInstance("X.509");

            InputStream certsChain = new ByteArrayInputStream(certificatesDatabase.getBytes());
            List<X509Certificate> certsChainCollection = (List<X509Certificate>) fact.generateCertificates(certsChain);

            System.out.println(certsChainCollection.get(1).toString());

            switch (certificates) {
                case "none":
                    break;
                case "single":
                    Base64 encoder = new Base64(64);
                    String cert_begin = "-----BEGIN CERTIFICATE-----\n";
                    String end_cert = "-----END CERTIFICATE-----\n";

                    byte[] derCert = certsChainCollection.get(0).getEncoded();
                    String pemCertPre = new String(encoder.encode(derCert));
                    certificatesToSend = cert_begin + pemCertPre + end_cert;

                    break;
                case "chain":
                    certificatesToSend = certificatesDatabase;
                    break;
                default:
                    throw new ApiRequestException("Database error","There is no data in the database, try to connect to plugin first",HttpStatus.BAD_REQUEST);
            }



            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.createObjectNode();

            // create a JSON object
            JsonNode keyNode = mapper.createObjectNode();

            ((ObjectNode) keyNode).put("status","enable");
            ((ObjectNode) keyNode).put("algo","1.2.840.113549.1.1.11");
            ((ObjectNode) keyNode).put("len","3072");//deve-se ir buscar ao certificado

            JsonNode certNode = mapper.createObjectNode();
            ((ObjectNode) certNode).put("status","valid");
            ((ObjectNode) certNode).put("certificates",certificatesToSend);//array em vez de string


            ((ObjectNode) rootNode).put("key",keyNode);
            ((ObjectNode) rootNode).put("cert",certNode);

            ((ObjectNode) rootNode).put("authMode","implicit");
            ((ObjectNode) rootNode).put("SCAL","2");
            ((ObjectNode) rootNode).put("multisign","10");

            return ResponseEntity.ok(rootNode);
        }catch (Exception e){

            throw new ApiRequestException("Database error","There is no data in the database, try to connect to plugin first",HttpStatus.BAD_REQUEST);

        }
    }
    public ResponseEntity storeCCSignatureValues(String hash, String certificates, String credentialID, String sad) throws Exception {

        try {
            System.out.println(certificates);

            User user = userDao.getByPCredentialID(credentialID);

            byte[] certificates_bytes = Base64.encodeBase64(certificates.getBytes());

            user.setCertificates(certificates_bytes);
            user.setSignatureCC(hash);

            userDao.save(user);

        }catch(Exception e){
            new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("Success");
    }

    public ResponseEntity credentialsAuthorizeCC(JsonNode parameters) throws Exception {
        try {
            User user = userDao.getByPCredentialID(parameters.get("credentialID").asText());

            UUID uuid = UUID.randomUUID();

            String sad = uuid.toString();

            user.setSad(sad);

            userDao.save(user);

            String uniqueHash;
            String docName;

            if(parameters.get("hash").isArray()){
                ArrayNode arrayNode = (ArrayNode) parameters.get("hash");
                uniqueHash = arrayNode.get(0).asText();
            }else{
                throw new ApiRequestException("invalid_request","Missing (or invalid type) array parameter hash",HttpStatus.BAD_REQUEST);
            }

            if(parameters.get("clientData").isArray()){
                ArrayNode arrayNode = (ArrayNode) parameters.get("clientData");
                docName = arrayNode.get(0).asText();
            }else{
                throw new ApiRequestException("invalid_request","The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed.",HttpStatus.BAD_REQUEST);
            }

            ObjectMapper mapper = new ObjectMapper();

            JsonNode jsonNode = mapper.createObjectNode();

            ((ObjectNode) jsonNode).put("SAD", sad);
            ((ObjectNode) jsonNode).put("expiresIn", "300");

            return ResponseEntity.ok(jsonNode);
        }catch (Exception e){

            throw new ApiRequestException("Database error","There is no data in the database, try to connect to plugin first",HttpStatus.BAD_REQUEST);

        }
    }

    public ResponseEntity getCCSignatureValues(JsonNode parameters) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.createObjectNode();

        String credentialID = parameters.get("credentialID").asText();
        String signature;
        try {
            signature=userDao.getByPCredentialID(credentialID).getSignatureCC();

            if(signature == null){
                throw new ApiRequestException("invalid_request","No CC signature stored",HttpStatus.BAD_REQUEST);
            }

            ((ObjectNode) jsonNode).put("signatures", signature);
        }catch (Exception e){
            throw new ApiRequestException("invalid_request","No CC signature stored",HttpStatus.BAD_REQUEST);
        }

        return  ResponseEntity.status(HttpStatus.OK).body(jsonNode);
    }

    public ResponseEntity dssFiler2SignCC(JsonNode parameters) throws Exception {

        String credentialID = parameters.get("credentialID").asText();
        User user = userDao.getByPCredentialID(credentialID);

        JsonNode imageParameters = parameters.get("imageParameters");
        byte[] image = null;
        int xAxis = 0;
        int yAxis = 0;
        int width = 0;
        int height = 0;
        String imageText = "";
        if (imageParameters != null){
            xAxis=imageParameters.get("xAxis").asInt();
            yAxis=imageParameters.get("yAxis").asInt();
            width=imageParameters.get("width").asInt();
            height=imageParameters.get("height").asInt();
            image = Base64.decodeBase64(imageParameters.get("image").asText());
        }

        if(parameters.get("imageText")!=null){
            imageText=parameters.get("imageText").asText();
        }

        ObjectMapper mapper = new ObjectMapper();

        JsonNode toDatabase;
        try {

            String certificates = new String(Base64.decodeBase64(user.getCertificates()));

            System.out.println(certificates);

            CertificateFactory fact = CertificateFactory.getInstance("X.509");

            InputStream certsChain = new ByteArrayInputStream(certificates.getBytes());
            List<X509Certificate> certsChainCollection = (List<X509Certificate>) fact.generateCertificates(certsChain);

            System.out.println(certsChainCollection.get(1).toString());
            byte[] document = Base64.decodeBase64(parameters.get("file").asText());

            toDatabase = DSS.padesLTASignCMDCC(certsChainCollection,parameters.get("docName").asText() ,document,parameters.get("signatureLevel").asText(),image,xAxis,yAxis,width,height,imageText,parameters.get("reason").asText(),parameters.get("location").asText(),parameters.get("contactInfo").asText());

        }catch (Exception e){
            throw new ApiRequestException("cannot sign","database error",HttpStatus.BAD_REQUEST);

        }

        try {

            byte[] document = Base64.decodeBase64(toDatabase.get("document").asText());

            user.setDate(toDatabase.get("date").asText());
            user.setDocument(document);
            user.setSignatureLevel(parameters.get("signatureLevel").asText());
            user.setxAxis(xAxis);
            user.setyAxis(yAxis);
            user.setWidth(width);
            user.setHeight(height);
            user.setImage(image);
            user.setImageText(imageText);
            user.setReason(parameters.get("reason").asText());
            user.setLocation(parameters.get("location").asText());
            user.setContactInfo(parameters.get("contactInfo").asText());

            userDao.save(user);

        }catch (Exception e){
            throw new ApiRequestException("Database error","Already exists an equal phone number",HttpStatus.BAD_REQUEST);
        }

        JsonNode rootNode = mapper.createObjectNode();

        ((ObjectNode) rootNode).put("sad", toDatabase.get("sad").asText());
        ((ObjectNode) rootNode).put("hash", toDatabase.get("hash").asText());

        return ResponseEntity.ok(rootNode);
    }


    public ResponseEntity dssFiler2SendOTPCC(JsonNode parameters, String credentialID) throws Exception {

        User user = userDao.getByPCredentialID(credentialID);

        String phoneNumber = user.getPhoneNumber();

        byte[] document = user.getDocument();

        String certificates = new String(Base64.decodeBase64(user.getCertificates()));

        System.out.println(certificates);

        CertificateFactory fact = CertificateFactory.getInstance("X.509");

        InputStream certsChain = new ByteArrayInputStream(certificates.getBytes());
        List<X509Certificate> certsChainCollection = (List<X509Certificate>) fact.generateCertificates(certsChain);

        try {
            DSS.padesLTAsendOTPCC(user.getSignatureCC(),certsChainCollection,user.getDate(),phoneNumber,parameters.get("sad").asText(),document,parameters.get("otp").asText(),user.getSignatureLevel(),user.getImage(),user.getxAxis(),user.getyAxis(),user.getWidth(),user.getHeight(),user.getImageText(),user.getReason(),user.getLocation(),user.getContactInfo());
        }catch (Exception e){
            ResponseEntity.badRequest();
        }

        try {
            userDao.delete(user);

        }catch (Exception e){
            throw new ApiRequestException("Database error","Already exists an equal phone number",HttpStatus.BAD_REQUEST);
        }


        Resource fileLoad = resourceLoader.getResource("classpath:/pades_signed.pdf");

        try {
            InputStreamResource resource = new InputStreamResource(fileLoad.getInputStream());


            HttpHeaders headers = new HttpHeaders();

            headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", "pades_signed.pdf"));
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            ResponseEntity<Object>
                    responseEntity = ResponseEntity.ok().headers(headers).contentLength(
                    fileLoad.contentLength()).contentType(MediaType.parseMediaType("application/txt")).body(resource);

            return responseEntity;
        }catch (Exception e){
            throw new ApiRequestException("File not received","The File was not received with the signature",HttpStatus.BAD_REQUEST);
        }
    }

}