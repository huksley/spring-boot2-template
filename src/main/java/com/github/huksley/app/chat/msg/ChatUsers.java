package com.github.huksley.app.chat.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.huksley.app.chat.ChatTransmission;
import com.github.huksley.app.chat.TransmissionType;
import lombok.*;

import javax.persistence.Entity;

/**
 * List of users response
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(value = { "version", "created", "updated" }, ignoreUnknown = true)
public class ChatUsers extends ChatTransmission {
    {
        type = TransmissionType.users;
    }

    String[] users;
}
