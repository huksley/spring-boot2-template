package com.github.huksley.app.system;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Fixing datetime serialization for Jackson JSON
 */
public class DateTimeSerializer extends com.fasterxml.jackson.databind.ser.std.DateSerializer {
    private static Logger log = LoggerFactory.getLogger(DateTimeSerializer.class);
    private static final long serialVersionUID = 1L;

    public DateTimeSerializer() {
    }
    
    public DateTimeSerializer(boolean timestamp, DateFormat fmt) {
        super(timestamp, fmt);
    }
    
    @Override
    public DateTimeSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
        return new DateTimeSerializer(timestamp, customFormat);
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (_customFormat != null) {
            _customFormat.setTimeZone(TimeZone.getDefault());
        }
        //log.info("Intermediary2 {} - {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ").format(value));
        //log.info("Intermediary3 {} - {}", _customFormat.format(value));
        super.serialize(value, gen, provider);
    }
}
