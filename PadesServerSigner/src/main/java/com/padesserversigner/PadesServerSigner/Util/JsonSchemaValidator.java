package com.padesserversigner.PadesServerSigner.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.padesserversigner.PadesServerSigner.Exception.ApiException;
import com.padesserversigner.PadesServerSigner.Exception.ApiRequestException;
import org.springframework.http.HttpStatus;
import java.io.InputStream; 

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

public class JsonSchemaValidator {

    public static void jsonValidator(JsonNode payload,String fileName){
        ObjectMapper mapper= new ObjectMapper();
        
        try {
            InputStream input = new ClassPathResource("jsonValidator/" + fileName).getInputStream();

            JsonNode fstabSchema = mapper.readTree(input);

            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

            JsonSchema schema = factory.getJsonSchema(fstabSchema);

            ProcessingReport report;

            boolean valid = schema.validInstance(payload);

            if(!valid) throw new ApiRequestException("Schema Error","Schema Error", HttpStatus.BAD_REQUEST);

        }catch (Exception e) {
            throw new ApiRequestException("Schema Error","Schema Error", HttpStatus.BAD_REQUEST);
        }
    }

}
