package com.padesserversigner.PadesServerSigner.Exception;

import org.springframework.http.HttpStatus;

public class ApiRequestException extends RuntimeException{
    HttpStatus httpStatus;
    String errorDescription;


    public ApiRequestException(String message, String errorDescription,HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorDescription = errorDescription;
    }

    public ApiRequestException(String message, Throwable cause,HttpStatus httpStatus, String errorDescription) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorDescription = errorDescription;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
