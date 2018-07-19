package com.github.huksley.app.system;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

/**
 * Fixing date deserialization for Jackson JSON
 */
public class DateDeserializer extends com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer {
    private static Logger log = LoggerFactory.getLogger(DateDeserializer.class);
    private static final long serialVersionUID = 1L;
    private TimeZone tz = null;

    public DateDeserializer() {
    }

    public DateDeserializer(DateDeserializer orig, DateFormat df, String fmt) {
        super(orig, df, fmt);
    }

    @Override
    protected DateDeserializer withDateFormat(DateFormat df, String formatString) {
        tz = df.getTimeZone();
        if (tz == null) {
            tz = TimeZone.getDefault();
        }
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateDeserializer inst = new DateDeserializer(this, df, formatString);
        inst.tz = tz;
        return inst;
    }

    @Override
    protected Date _parseDate(JsonParser p, DeserializationContext ctxt) throws IOException {
        Date val = super._parseDate(p, ctxt);
        if (val != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(tz);
            cal.setTime(val);
            Calendar d = Calendar.getInstance();
            d.set(Calendar.HOUR_OF_DAY, 0);
            d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0);
            d.set(Calendar.MILLISECOND, 0);
            int year = cal.get(Calendar.YEAR);
            int mon = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            d.set(Calendar.YEAR, year);
            d.set(Calendar.MONTH, mon);
            d.set(Calendar.DAY_OF_MONTH, day);
            d.getTime(); // FIXME: modifies inner structure, REQUIRED!
            //log.info("Intermediary {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ").format(d.getTime()));
            d.setTimeZone(tz);
            d.getTime(); // FIXME: modifies inner structure, REQUIRED!
            //log.info("Intermediary {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ").format(d.getTime()));
            // Adjust for DST
            while (d.get(Calendar.DAY_OF_MONTH) < day) {
                d.add(Calendar.HOUR, 1);
            }
            //log.info("Intermediary {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ").format(d.getTime()));
            val = d.getTime();
        }
        return val;
    }
}