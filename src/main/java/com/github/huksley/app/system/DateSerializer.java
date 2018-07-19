package com.github.huksley.app.system;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Fixing date serialization for Jackson JSON
 */
public class DateSerializer extends com.fasterxml.jackson.databind.ser.std.DateSerializer {
    private static Logger log = LoggerFactory.getLogger(DateSerializer.class);
    private static final long serialVersionUID = 1L;

    public DateSerializer() {
    }

    public DateSerializer(boolean timestamp, DateFormat fmt) {
        super(timestamp, fmt);
    }
    
    @Override
    public DateSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
        return new DateSerializer(timestamp, customFormat);
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
