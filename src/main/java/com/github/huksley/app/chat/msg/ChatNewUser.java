package com.github.huksley.app.chat.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.huksley.app.chat.ChatTransmission;
import com.github.huksley.app.chat.TransmissionType;
import lombok.*;

import javax.persistence.Entity;


/**
 * Announcement from user about his username
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(value = { "version", "created", "updated" }, ignoreUnknown = true)
public class ChatNewUser extends ChatTransmission {
    {
        type = TransmissionType.newUser;
    }

    String userName;
}
