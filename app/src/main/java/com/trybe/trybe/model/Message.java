package com.trybe.trybe.model;

import java.io.Serializable;

/**
 * Created by Gopi on 14-Apr-16.
 */
public class Message implements Serializable {
    public String ID;
    public String MESSAGE;
    public String CREATE_DT;
    public String UD_ID;

    public Message() {
    }

    public Message(String userId, String message, String createdDt) {
        this.UD_ID = userId;
        this.MESSAGE = message;
        this.CREATE_DT = createdDt;
    }

}
