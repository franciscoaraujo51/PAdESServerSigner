package com.plugin.Plugin.Service;


import org.poreid.POReIDException;
import org.poreid.PkAlias;
import org.poreid.RSAPaddingSchemes;
import org.poreid.cc.gemsafe.GemsafeCard;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import org.poreid.cc.CitizenCard;
import org.poreid.CardFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class PluginService{

    public static ResponseEntity sendCertificates(String credentialID,String access_token) throws POReIDException {
        Locale locale = new Locale("pt","PT");
        String allCerts = "";
        CitizenCard cc = null;
        try{
            cc = CardFactory.getCard();

            List<X509Certificate> certificates = cc.getAuthenticationCertificateChain();
            

            Base64 encoder = new Base64(64);
            String cert_begin = "-----BEGIN CERTIFICATE-----\n";
            String end_cert = "-----END CERTIFICATE-----\n";
            for (X509Certificate certificate : certificates) {
                byte[] derCert = certificate.getEncoded();
                String pemCertPre = new String(encoder.encode(derCert));
                String pemCert = cert_begin + pemCertPre + end_cert;
                allCerts += pemCert;
            }


            System.out.println(allCerts);

        }catch (Exception e){
            return new ResponseEntity<>("Erro ao ler o cartao. Tentar novamente", HttpStatus.BAD_REQUEST);
        } finally {
            cc.close();
        }   

        try {
            URL url = new URL("http://localhost:8080/cc/sendCertificateValues");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            // create `ObjectMapper` instance
            ObjectMapper mapper = new ObjectMapper();

            // create a JSON object
            ObjectNode user = mapper.createObjectNode();
            user.put("certificates", allCerts);
            user.put("access_token",access_token);
            user.put("credentialID",credentialID);

            // convert `ObjectNode` to pretty-print JSON
            // without pretty-print, use `user.toString()` method
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(json);
            wr.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
            } else {
                System.out.println(con.getResponseMessage());
            }

        }catch (Exception e){
            return new ResponseEntity<>("Erro ao ler o cartao. Tentar novamente", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("success");

    }

    public static ResponseEntity sendHashSigned(String sad, String pinCode, String credentialID, String access_token, String hash) throws POReIDException {
        Locale locale = new Locale("pt","PT");
        String allCerts = "";
        CitizenCard cc = null;
        String signature ="";
        try{
            cc = CardFactory.getCard();

            List<X509Certificate> certificates = cc.getAuthenticationCertificateChain();

            Base64 encoder = new Base64(64);
            String cert_begin = "-----BEGIN CERTIFICATE-----\n";
            String end_cert = "-----END CERTIFICATE-----\n";
            for (X509Certificate certificate : certificates) {
                byte[] derCert = certificate.getEncoded();
                String pemCertPre = new String(encoder.encode(derCert));
                String pemCert = cert_begin + pemCertPre + end_cert;
                allCerts += pemCert;
            }

            MessageDigest d = MessageDigest.getInstance("SHA-1");

            d.update(hash.getBytes());

            byte[] arrayToSign = d.digest();

            byte[] signatureByte = cc.sign(arrayToSign, pinCode.getBytes(), "SHA-1", PkAlias.ASSINATURA, RSAPaddingSchemes.PKCS1);

            signature=Base64.encodeBase64String(signatureByte);

        }catch (Exception e){
            return new ResponseEntity<>("Erro ao ler o cartao. Tentar novamente", HttpStatus.BAD_REQUEST);
        } finally {
            cc.close();
        }

        try {
            URL url = new URL("http://localhost:8080/cc/sendSignatureValues");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            // create `ObjectMapper` instance
            ObjectMapper mapper = new ObjectMapper();

            // create a JSON object
            ObjectNode user = mapper.createObjectNode();
            user.put("certificates", allCerts);
            user.put("access_token",access_token);
            user.put("credentialID",credentialID);
            user.put("signature", signature);
            user.put("SAD", sad);

            // convert `ObjectNode` to pretty-print JSON
            // without pretty-print, use `user.toString()` method
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(json);
            wr.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
            } else {
                System.out.println(con.getResponseMessage());
            }

        }catch (Exception e){
            return new ResponseEntity<>("Erro ao tentar enviar os dados", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("success");
    }

    public static void main(String[] args) {
    }


}
