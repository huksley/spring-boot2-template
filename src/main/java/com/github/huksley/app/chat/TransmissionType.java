package com.github.huksley.app.chat;

import com.github.huksley.app.chat.msg.ChatGetUsers;
import com.github.huksley.app.chat.msg.ChatMessage;
import com.github.huksley.app.chat.msg.ChatNewUser;
import com.github.huksley.app.chat.msg.ChatUsers;

public enum TransmissionType {
    message(ChatMessage.class),
    newUser(ChatNewUser.class),
    users(ChatUsers.class),
    getUsers(ChatGetUsers.class);

    public final Class<? extends ChatTransmission> type;

    TransmissionType(Class<? extends ChatTransmission> type) {
        this.type = type;
    }
}
