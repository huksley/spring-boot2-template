package com.github.huksley.app.chat;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.huksley.app.chat.msg.ChatMessage;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;

/**
 * Base wire message type.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(value = { "version", "created", "updated" }, ignoreUnknown = true)
public class ChatTransmission {
    @JsonIgnore
    private static Logger log = LoggerFactory.getLogger(ChatTransmission.class);

    @JsonInclude
    @JsonProperty
    protected TransmissionType type;

    public static String serialize(ChatTransmission t) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        return om.writeValueAsString(t);
    }

    public static <T extends ChatTransmission> T parse(Class<T> type, String json) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        log.info(om.writeValueAsString(new ChatMessage()));
        ChatTransmission tt =  om.readValue(json, ChatTransmission.class);
        if (tt == null) {
            throw new IllegalArgumentException("Can`t parse JSON: " + json);
        }
        if (tt.type == null) {
            throw new IllegalArgumentException("Invalid type in JSON: " + json);
        }
        if (type != null && !type.isAssignableFrom(tt.type.type)) {
            throw new IllegalStateException("Unexpected type in JSON, expected " + type + ": " + json);
        }
        return (T) om.readValue(json, tt.type.type);
    }
}
