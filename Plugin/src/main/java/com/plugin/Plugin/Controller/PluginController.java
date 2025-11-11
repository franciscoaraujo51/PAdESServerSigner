package com.plugin.Plugin.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.plugin.Plugin.Service.PluginService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/plugin")
public class PluginController {

    /**
     * Recebe os dados do utilizador e envia os certificados para o PAdES Server Signer.
     * @param credentialID Contem o credentiaID.
     * @param acess_token Contem o Access Token para aceder á conta do utilizado no PAdES Server Signer.
     * @return Mensagem de sucesso se tudo correr bem.
     */
    @RequestMapping(value = "/getCertificates",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity sendCertificateValues(@RequestBody JsonNode payload, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        var responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);

        try {
            String credentialID = payload.get("credentialID").asText();
            String access_token = payload.get("access_token").asText();

            responseEntity = PluginService.sendCertificates(credentialID,access_token);
        }catch (Exception e){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }

    /**
     * Recebe os dados do utilizador e envia os certificados e assinatura para o PAdES Server Signer.
     * @param credentialID Contem o credentiaID.
     * @param acess_token Contem o Access Token para aceder á conta do utilizado no PAdES Server Signer.
     * @param sad Sad realativo à assinatura.
     * @param pinCode Pin de assintura do cartão de cidadão.
     * @param hash Hash para realizar a assinatura.
     * @return Mensagem de sucesso se tudo correr bem.
     */
    @RequestMapping(value = "/sign",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity sign(@RequestBody JsonNode payload, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        var responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);

        try {
            String credentialID = payload.get("credentialID").asText();
            String access_token = payload.get("access_token").asText();
            String pinCode = payload.get("pinCode").asText();
            String hash = payload.get("hash").asText();
            String sad = payload.get("sad").asText();

            responseEntity = PluginService.sendHashSigned(sad,pinCode,credentialID,access_token,hash);
        }catch (Exception e){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }
}
