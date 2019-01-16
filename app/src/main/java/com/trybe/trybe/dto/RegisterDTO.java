package com.trybe.trybe.dto;

/**
 * Created by gopi.ra on 29-Feb-16.
 */
public class RegisterDTO {

    public RegisterUser USER;

    public static class RegisterUser {
        public String UD_NAME;
        public String UD_EMAIL;
        public String UD_PASSWORD;
        public String UD_PHONE;
    }

    public RegisterDTO() {
        USER = new RegisterUser();
    }
}