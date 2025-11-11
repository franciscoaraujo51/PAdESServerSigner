package com.padesserversigner.PadesServerSigner.Model;

public class ResponseTranfer {
    private String text;

    public ResponseTranfer(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
