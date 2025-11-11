package com.padesserversigner.PadesServerSigner.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.padesserversigner.PadesServerSigner.Security.JwtUtil;
import com.padesserversigner.PadesServerSigner.Service.CmdService;
import com.padesserversigner.PadesServerSigner.Util.CCReader.CCReader;
import com.padesserversigner.PadesServerSigner.Util.JsonSchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/cc")
public class CCController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CmdService cmdService;

    @Autowired
    private JwtUtil jwtUtil;

    @RequestMapping(value = "/sendCertificateValues",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSignature(@RequestBody JsonNode payload, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        String certificates = payload.get("certificates").asText();
        String credentialID = payload.get("credentialID").asText();

        return cmdService.storeCertificates(certificates,credentialID);
    }

    @RequestMapping(value = "/sendSignatureValues",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity sendSignatureValues(@RequestBody JsonNode payload, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        
        return cmdService.storeCCSignatureValues(payload.get("signature").asText(),payload.get("certificates").asText(),payload.get("credentialID").asText(),payload.get("SAD").asText());
    }

    /**
     * Gera um token e envia para o utilizador.
     * @param payload Contem campo phoneNumber e pin.
     * @return ResponseEntity<JsonNode> Retorna o token.
     */
    @RequestMapping(value = "/auth/login",method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> register(@RequestBody JsonNode payload) {
        JsonSchemaValidator.jsonValidator(payload,"cmdAuthLogin.json");
        return cmdService.authLogin(payload.get("phoneNumber").asText(),payload.get("pin").asText());
    }

    /**
     * Recebe Token e envia o credentialID.
     * @return ResponseEntity<JsonNode> Retorna o credetialId relativamente ao Token recebido.
     */
    @RequestMapping(value = "/credentials/list",method = RequestMethod.GET)
    public ResponseEntity credentialsList(HttpServletRequest httpServletRequest,  HttpServletResponse httpServletResponse){
        String authorizationHeader = httpServletRequest.getHeader("Authorization");

        String jwt = null,credentialID = null;
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                jwt = authorizationHeader.substring(7);
                credentialID = jwtUtil.extractName(jwt);
            }else{
                ResponseEntity.status(HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e){
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return cmdService.credentialsList(credentialID);
    }


    /**
     * Envia o certificado de assinautra ou cadeia de certificação de acordo com a especificação.
     * @param credentialID Contem o credentiaID.
     * @param certificates (none,single,chain).
     * @return ResponseEntity<JsonNode> Retorna o SAD e tempo para realizar a assiantura.
     */
    @RequestMapping(value = "/credentials/info",method = RequestMethod.GET)
    public ResponseEntity credentialsInfo(@RequestBody JsonNode payload) throws Exception {
        JsonSchemaValidator.jsonValidator(payload,"cmdCredentialsInfo.json");

        return cmdService.getCertificatesCC(payload.get("certificates").asText(),payload.get("credentialID").asText());
    }


    /**
     * Envia as hash's e dados para realizar a assinatura.
     * @param credentialID Contem o credentiaID.
     * @param clientData nome dos ficheiro.
     * @param hash hash para assinar.
     * @param numSignatures numero de assinaturas = 1.
     * @return ResponseEntity<JsonNode> Retorna SAD e OTP para o cliente.
     */
    @RequestMapping(value = "/credentials/authorize",method = RequestMethod.GET)
    public ResponseEntity credentialsAuthorize(@RequestBody JsonNode payload) throws Exception {
        JsonSchemaValidator.jsonValidator(payload,"ccCredentialsAuthorize.json");

        return cmdService.credentialsAuthorizeCC(payload);
    }


    /**
     * Envia o OTP para receber a assinatura.
     * @param credentialID Contem o credentiaID.
     * @param SAD sad retornado no método anterior.
     * @param hash hash para assinar.
     * @return ResponseEntity<JsonNode> Retorna a assinatura.
     */
    @RequestMapping(value = "/signatures/signHash",method = RequestMethod.GET)
    public ResponseEntity signaturesSignHash(@RequestBody JsonNode payload) throws Exception {
        JsonSchemaValidator.jsonValidator(payload,"ccSignaturesSignHash.json");

        return cmdService.getCCSignatureValues(payload);
    }

    /**
     * Envia o ficheiro e dados(imagem,etc) para realizar a assinatura do tipo PADES.
     * @param phoneNumber Numero de telefone.
     * @param pin Pin da Chave Movel Digital.
     * @param signatureLevel Nivel da assinatura PADES(pades_b,pades_t,pades_lt,pades_lta).
     * @param docName Nome do documento.
     * @param reason Razao para a assinatura.
     * @param location Local da assinatura.
     * @param contactInfo Informacao para contacto.
     * @param imageText Texto para inserir na assinatura visivel.
     * @param imageParameters Parameteros da imagem para assinatura visivel.
     * @param file Documento para assinar.
     * @return ResponseEntity<JsonNode> Retorna SAD, credentialID e token.
     */
    @RequestMapping(value = "/dssFiler2Sign",method = RequestMethod.POST)
    public ResponseEntity dssFiler2Sign(@RequestBody JsonNode payload) throws Exception {

        return cmdService.dssFiler2SignCC(payload);
    }


    /**
     * Envia OTP para receber ficheiro assinado.
     * @param credentialID Contem o credentiaID.
     * @param SAD sad retornado no método anterior.
     * @return ResponseEntity<JsonNode> Retorna PDF assinado.
     */
    @RequestMapping(value = "/dssFiler2SendOTP",method = RequestMethod.POST)
    public ResponseEntity dssFiler2SendOTP(@RequestBody JsonNode payload,HttpServletRequest httpServletRequest,  HttpServletResponse httpServletResponse) throws Exception {
        String authorizationHeader = httpServletRequest.getHeader("Authorization");

        String jwt = null,credentialID = null;
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                jwt = authorizationHeader.substring(7);
                credentialID = jwtUtil.extractName(jwt);
            }else{
                ResponseEntity.status(HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e){
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }

        return cmdService.dssFiler2SendOTPCC(payload,credentialID);
    }
}