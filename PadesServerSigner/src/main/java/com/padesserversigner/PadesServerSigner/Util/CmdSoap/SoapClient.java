package com.padesserversigner.PadesServerSigner.Util.CmdSoap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.xml.soap.SAAJMetaFactory;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.function.Function;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.soap.SoapMessage;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;


@Component
public class SoapClient {

    @Value("${application.id}")
    private String propertiesApplicationId;

    // Static field that can be accessed by static methods
    private static String applicationId;


    // PostConstruct method, called after the bean is initialized
    @PostConstruct
    public void init() {
        applicationId = propertiesApplicationId;
    }

    private static String prepareDataToBeSigned(byte[] message) throws IOException, GeneralSecurityException {
        byte[] sha256Prefix =  {(byte) 0x30, (byte) 0x31,(byte)  0x30, (byte) 0x0d,(byte)0x06, (byte)0x09, (byte)0x60, (byte)0x86, (byte)0x48, (byte)0x01, (byte)0x65, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01, (byte)0x05, (byte)0x00, (byte)0x04, (byte)0x20 };

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write( sha256Prefix );

        MessageDigest d = MessageDigest.getInstance("SHA-256");
        d.update(message);
        byte[] arrayToSign = d.digest();

        outputStream.write( arrayToSign );

        byte[] newmessage = outputStream.toByteArray();

        return Base64.encodeBase64String(newmessage);
    }

    private static void createSoapEnvelopeGetCertificate(SOAPMessage soapMessage, JsonNode parameters) throws SOAPException, NoSuchAlgorithmException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespace = "ns1";
        String myNamespaceURI = "http://Ama.Authentication.Service/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

        String phoneNumber = parameters.get("phoneNumber").asText();

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("GetCertificate", myNamespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("applicationId", myNamespace);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("userId", myNamespace);

        soapBodyElem1.addTextNode(applicationId);
        soapBodyElem2.addTextNode(phoneNumber);
    }

    private static void createSoapEnvelopeSignRequest(SOAPMessage soapMessage, JsonNode parameters) throws SOAPException, GeneralSecurityException, IOException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespace = "s11";
        String myNamespaceURI = "http://Ama.Authentication.Service/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ns1","http://Ama.Authentication.Service/");

        String docName = parameters.get("docName").asText();
        String pin = parameters.get("pin").asText();
        String hash = prepareDataToBeSigned(Base64.decodeBase64(parameters.get("hash").asText()));

        String phoneNumber = parameters.get("phoneNumber").asText();

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyCCMovelSign = soapBody.addChildElement("CCMovelSign","ns1");

        SOAPElement soapBodyElem = soapBodyCCMovelSign.addChildElement("request", "ns1");
        soapBodyElem.addNamespaceDeclaration("ns2", "http://schemas.datacontract.org/2004/07/Ama.Structures.CCMovelSignature");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("ApplicationId", "ns2");

        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("DocName", "ns2");

        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("Hash", "ns2");

        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("Pin", "ns2");

        SOAPElement soapBodyElem5 = soapBodyElem.addChildElement("UserId", "ns2");

        soapBodyElem1.addTextNode(applicationId);
        soapBodyElem2.addTextNode(docName);

        soapBodyElem3.addTextNode(hash);

        soapBodyElem4.addTextNode(pin);

        soapBodyElem5.addTextNode(phoneNumber);
    }


    private static void createSoapEnvelopeMultipleSignRequest(SOAPMessage soapMessage, JsonNode parameters) throws SOAPException, GeneralSecurityException, IOException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespace = "s11";
        String myNamespaceURI = "http://Ama.Authentication.Service/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ns1","http://Ama.Authentication.Service/");

        System.out.println("erro");

        String pin = parameters.get("pin").asText();

        int numSignatures = parameters.get("numSignatures").asInt();
        ArrayNode hashs = (ArrayNode) parameters.get("hash");
        ArrayNode docNames = (ArrayNode) parameters.get("docName");
        String phoneNumber = parameters.get("phoneNumber").asText();

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyCCMovelSign = soapBody.addChildElement("CCMovelMultipleSign","ns1");

        SOAPElement soapBodyElemRequest = soapBodyCCMovelSign.addChildElement("request", "ns1");
        soapBodyElemRequest.addNamespaceDeclaration("ns2", "http://schemas.datacontract.org/2004/07/Ama.Structures.CCMovelSignature");
        SOAPElement soapBodyElem1 = soapBodyElemRequest.addChildElement("ApplicationId", "ns2");

        SOAPElement soapBodyElem2 = soapBodyElemRequest.addChildElement("Pin", "ns2");

        SOAPElement soapBodyElem3 = soapBodyElemRequest.addChildElement("UserId", "ns2");

        soapBodyElem1.addTextNode(applicationId);

        soapBodyElem2.addTextNode(pin);

        soapBodyElem3.addTextNode(phoneNumber);

        SOAPElement soapBodyElemDocuments = soapBodyCCMovelSign.addChildElement("documents", "ns1");
        soapBodyElemDocuments.addNamespaceDeclaration("ns2", "http://schemas.datacontract.org/2004/07/Ama.Structures.CCMovelSignature");

        for (int i = 0; i<numSignatures;i++){


            SOAPElement soapBodyElemDocumentsHashStructure = soapBodyElemDocuments.addChildElement("HashStructure", "ns2");

            SOAPElement soapBodyElemDocumentsHashStructure1 = soapBodyElemDocumentsHashStructure.addChildElement("Hash", "ns2");

            SOAPElement soapBodyElemDocumentsHashStructure2 = soapBodyElemDocumentsHashStructure.addChildElement("Name", "ns2");

            SOAPElement soapBodyElemDocumentsHashStructure3 = soapBodyElemDocumentsHashStructure.addChildElement("id", "ns2");

            soapBodyElemDocumentsHashStructure1.addTextNode(prepareDataToBeSigned(Base64.decodeBase64(hashs.get(i).asText())));

            soapBodyElemDocumentsHashStructure2.addTextNode(docNames.get(i).asText());

            soapBodyElemDocumentsHashStructure3.addTextNode(docNames.get(i).asText());

        }
    }

    private static void createSoapEnvelopeValidateOtp(SOAPMessage soapMessage, JsonNode parameters) throws SOAPException, NoSuchAlgorithmException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespace = "ns1";
        String myNamespaceURI = "http://Ama.Authentication.Service/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

        String code = parameters.get("code").asText();
        String processId = parameters.get("SAD").asText();

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("ValidateOtp", myNamespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("code", myNamespace);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("processId", myNamespace);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("applicationId", myNamespace);

        soapBodyElem1.addTextNode(code);
        soapBodyElem2.addTextNode(processId);
        soapBodyElem3.addTextNode(applicationId);
    }



    private static SOAPMessage createSOAPRequest(String soapAction, String createEnvelopeType, JsonNode parameters) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage soapMessage = messageFactory.createMessage();

        switch (createEnvelopeType) {
            case "getCertificate":
                createSoapEnvelopeGetCertificate(soapMessage,parameters);
                break;
            case "signRequest":
                createSoapEnvelopeSignRequest(soapMessage,parameters);
                break;
            case "validateOtp":
                createSoapEnvelopeValidateOtp(soapMessage,parameters);
                break;
            case "multipleSignRequest":
                createSoapEnvelopeMultipleSignRequest(soapMessage,parameters);
                break;
            default:
                return null;
        }


        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);

        soapMessage.saveChanges();

        // Print the request message
        System.out.println("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println("\n");

        return soapMessage;
    }

    public static SOAPMessage callSoapWebService(String soapAction, String createEnvelopeType, JsonNode parameters) {

        String wsdlURL = "https://cmd.autenticacao.gov.pt/Ama.Authentication.Frontend/CCMovelDigitalSignature.svc";
        String certs = null;
        try {
            System.setProperty("javax.xml.soap.SAAJMetaFactory", "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(soapAction,createEnvelopeType, parameters), wsdlURL);


            // Print the SOAP Response
            System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);
            System.out.println();

            soapConnection.close();

            return soapResponse;

        } catch (Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
        return null;
    }



    public static String getCertificate(JsonNode parameters) throws SOAPException {
        String soapAction = "http://Ama.Authentication.Service/CCMovelSignature/GetCertificate";
        String certs = null;
        SOAPMessage soapResponse = callSoapWebService(soapAction, "getCertificate",parameters);

        SOAPPart sp = soapResponse.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        Iterator it = sb.getChildElements();
        while (it.hasNext()) {
            SOAPBodyElement bodyElement = (SOAPBodyElement) it.next();
            Iterator it2 = bodyElement.getChildElements();
            while (it2.hasNext()) {
                SOAPElement element2 = (SOAPElement) it2.next();
                if (element2.getElementQName().getLocalPart().equals("GetCertificateResult")) certs = element2.getValue(); ;

            }
        }
        return certs;
    }

    public static String signRequest(JsonNode parameters) throws SOAPException, IOException {
        String soapAction = "http://Ama.Authentication.Service/CCMovelSignature/CCMovelSign";
        String processId = null;
        SOAPMessage soapResponse = callSoapWebService(soapAction,"signRequest", parameters);


        SOAPPart sp = soapResponse.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        Iterator it = sb.getChildElements();
        while (it.hasNext()) {
            SOAPBodyElement bodyElement = (SOAPBodyElement) it.next();
            Iterator it2 = bodyElement.getChildElements();
            while (it2.hasNext()) {
                SOAPElement element2 = (SOAPElement) it2.next();
                if (element2.getElementQName().getLocalPart().equals("CCMovelSignResult")) {
                    Iterator it3 = element2.getChildElements();
                    while (it3.hasNext()) {
                        SOAPElement element3 = (SOAPElement) it3.next();
                        if (element3.getElementQName().getLocalPart().equals("ProcessId"))  processId = element3.getValue();
                    }

                }
            }
        }

        return processId;
    }


    public static String multipleSignRequest(JsonNode parameters) throws SOAPException, IOException {
        String soapAction = "http://Ama.Authentication.Service/CCMovelSignature/CCMovelMultipleSign";
        String processId = null;
        SOAPMessage soapResponse = callSoapWebService(soapAction,"multipleSignRequest", parameters);


        SOAPPart sp = soapResponse.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        Iterator it = sb.getChildElements();
        while (it.hasNext()) {
            SOAPBodyElement bodyElement = (SOAPBodyElement) it.next();
            Iterator it2 = bodyElement.getChildElements();
            while (it2.hasNext()) {
                SOAPElement element2 = (SOAPElement) it2.next();
                if (element2.getElementQName().getLocalPart().equals("CCMovelMultipleSignResult")) {
                    Iterator it3 = element2.getChildElements();
                    while (it3.hasNext()) {
                        SOAPElement element3 = (SOAPElement) it3.next();
                        if (element3.getElementQName().getLocalPart().equals("ProcessId"))  processId = element3.getValue();
                    }

                }
            }
        }

        return processId;
    }

    public static String validateOtp(JsonNode parameters) throws SOAPException, IOException {
        String soapAction = "http://Ama.Authentication.Service/CCMovelSignature/ValidateOtp";
        String signature = null;
        SOAPMessage soapResponse = callSoapWebService(soapAction,"validateOtp", parameters);

        SOAPPart sp = soapResponse.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        Iterator it = sb.getChildElements();
        while (it.hasNext()) {
            SOAPBodyElement bodyElement = (SOAPBodyElement) it.next();
            Iterator it2 = bodyElement.getChildElements();
            while (it2.hasNext()) {
                SOAPElement element2 = (SOAPElement) it2.next();
                if (element2.getElementQName().getLocalPart().equals("ValidateOtpResult")) {
                    Iterator it3 = element2.getChildElements();
                    while (it3.hasNext()) {
                        SOAPElement element3 = (SOAPElement) it3.next();
                        if (element3.getElementQName().getLocalPart().equals("Signature"))  signature = element3.getValue();
                    }

                }
            }
        }

        return signature;
    }


    public static JsonNode validateOtpMultipleRequest(JsonNode parameters) throws SOAPException, IOException {
        String soapAction = "http://Ama.Authentication.Service/CCMovelSignature/ValidateOtp";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.createObjectNode();
        SOAPMessage soapResponse = callSoapWebService(soapAction,"validateOtp", parameters);

        SOAPPart sp = soapResponse.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        Iterator it = sb.getChildElements();
        while (it.hasNext()) {
            SOAPBodyElement bodyElement = (SOAPBodyElement) it.next();
            Iterator it2 = bodyElement.getChildElements();
            while (it2.hasNext()) {
                SOAPElement element2 = (SOAPElement) it2.next();
                if (element2.getElementQName().getLocalPart().equals("ValidateOtpResult")) {
                    Iterator it3 = element2.getChildElements();
                    while (it3.hasNext()) {
                        SOAPElement element3 = (SOAPElement) it3.next();
                        if (element3.getElementQName().getLocalPart().equals("ArrayOfHashStructure")) {
                            Iterator it4 = element3.getChildElements();
                            ArrayNode arrayNode = mapper.createArrayNode();
                            ((ObjectNode) jsonNode).put("signatures", arrayNode);
                            while (it4.hasNext()) {
                                SOAPElement element4 = (SOAPElement) it4.next();
                                Iterator it5 = element4.getChildElements();

                                JsonNode responseValues = mapper.createObjectNode();
                                String hash=null,name = null,id = null;
                                while (it5.hasNext()) {
                                    SOAPElement element5 = (SOAPElement) it5.next();
                                    switch (element5.getElementQName().getLocalPart()){
                                        case "Hash":
                                            hash = element5.getValue();
                                            ((ObjectNode) responseValues).put("Hash", hash);
                                            break;
                                        case "id":
                                            id = element5.getValue();
                                            ((ObjectNode) responseValues).put("id", id);
                                            break;
                                        case "Name":
                                            name = element5.getValue();
                                            ((ObjectNode) responseValues).put("name", name);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                arrayNode.add(responseValues);
                            }
                        }

                    }
                }
            }
        }

        return jsonNode;
    }
}
