package com.trybe.trybe.dto;

import com.trybe.trybe.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gopi on 17-Apr-16.
 */
public class ChatMessageDTO {

    public List<Message> DATA;

    public ChatMessageDTO() {
        DATA = new ArrayList<Message>();
    }
}
