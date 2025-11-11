package com.padesserversigner.PadesServerSigner.Exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class ApiException {
    private final String error;

    @JsonProperty("error_description")
    private final String errorDescription;

    public ApiException(String error,String errorDescription) {
        this.error= error;
        this.errorDescription = errorDescription;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getError() {
        return error;
    }
}
