package com.github.huksley.app.chat.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.huksley.app.chat.ChatTransmission;
import com.github.huksley.app.chat.TransmissionType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * New message from someone.
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(value = { "version", "created", "updated" }, ignoreUnknown = true)
public class ChatMessage extends ChatTransmission {
    {
        type = TransmissionType.message;
    }

    String userName;
    String id;
    String date; // FIXME: proper type
    String message;
}
