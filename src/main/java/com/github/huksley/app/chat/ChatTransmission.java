package com.github.huksley.app.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;

/**
 * Base wire message type.
 */
public class ChatTransmission {
    protected TransmissionType type;

    public static String serialize(ChatTransmission t) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        return om.writeValueAsString(t);
    }

    public static <T extends ChatTransmission> T parse(Class<T> type, String json) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        ChatTransmission tt =  om.readValue(json, ChatTransmission.class);
        assert tt != null;
        if (type != null) {
            assert type.isAssignableFrom(tt.type.type);
        }
        return (T) om.readValue(json, tt.type.type);
    }
}
