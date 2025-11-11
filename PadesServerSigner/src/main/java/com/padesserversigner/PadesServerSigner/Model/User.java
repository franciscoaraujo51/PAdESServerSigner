package com.padesserversigner.PadesServerSigner.Model;

import javax.persistence.*;
import java.sql.Blob;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "user",schema = "user")
public class User {

    @Id
    @Column(name = "credential_id")
    private String credentialID;

    private String phoneNumber;

    private String pin;

    private String token;

    @Column(name = "signing_date")
    private String date;

    @Column(name = "signing_level")
    private String signatureLevel;

    @Column(name = "num_signatures")
    private int numSignatures;

    @Lob
    private byte[] document;

    @Lob
    private byte[] certificates;

    @Column(name = "signature_cc")
    private String signatureCC;

    private String reason;

    private String location;

    @Column(name = "contact_info")
    private String contactInfo;

    @Lob
    private byte[] image;

    @Column(name = "xaxis")
    private int xAxis;

    @Column(name = "yaxis")
    private int yAxis;

    @Column(name = "width")
    private int width;

    @Column(name = "height")
    private int height;

    @Column(name = "image_text")
    private String imageText;

    @Column(name = "sad")
    private String sad;


    public User(String phoneNumber, String pin, String token) {
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.token = token;
    }

    public User() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }

    public String getCredentialID() {
        return credentialID;
    }

    public void setCredentialID(String credentialID) {
        this.credentialID = credentialID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSignatureLevel() { return signatureLevel;}

    public void setSignatureLevel(String signatureLevel) {this.signatureLevel = signatureLevel;}

    public int getNumSignatures() { return numSignatures; }

    public void setNumSignatures(int numSignatures) { this.numSignatures = numSignatures; }

    public byte[] getCertificates() {
        return certificates;
    }

    public void setCertificates(byte[] certificates) {
        this.certificates = certificates;
    }

    public String getSignatureCC() {
        return signatureCC;
    }

    public void setSignatureCC(String signatureCC) {
        this.signatureCC = signatureCC;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getxAxis() {
        return xAxis;
    }

    public void setxAxis(int xAxis) {
        this.xAxis = xAxis;
    }

    public int getyAxis() {
        return yAxis;
    }

    public void setyAxis(int yAxis) {
        this.yAxis = yAxis;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getImageText() {
        return imageText;
    }

    public void setImageText(String imageText) {
        this.imageText = imageText;
    }

    public String getSad() {
        return sad;
    }

    public void setSad(String sad) {
        this.sad = sad;
    }

    @Override
    public String toString() {
        return "User{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", pin='" + pin + '\'' +
                ", token='" + token + '\'' +
                ", credentialID='" + credentialID + '\'' +
                '}';
    }

}
