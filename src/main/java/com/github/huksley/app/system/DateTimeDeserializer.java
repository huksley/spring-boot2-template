package com.github.huksley.app.system;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;

/**
 * Fixing datetime deserialization for Jackson JSON
 */
public class DateTimeDeserializer extends DateDeserializer {
    private static Logger log = LoggerFactory.getLogger(DateTimeDeserializer.class);
    private static final long serialVersionUID = 1L;
    private TimeZone tz = null;

    public DateTimeDeserializer() {
    }

    public DateTimeDeserializer(DateTimeDeserializer orig, DateFormat df, String fmt) {
        super(orig, df, fmt);
    }

    @Override
    protected DateDeserializer withDateFormat(DateFormat df, String formatString) {
        tz = df.getTimeZone();
        if (tz == null) {
            tz = TimeZone.getDefault();
        }
        //df.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateTimeDeserializer inst = new DateTimeDeserializer(this, df, formatString);
        inst.tz = tz;
        return inst;
    }

    @Override
    protected Date _parseDate(JsonParser p, DeserializationContext ctxt) throws IOException {
        Date val = super._parseDate(p, ctxt);
        if (val != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(val);
            cal.setTimeZone(tz);
            //log.info("Intermediary {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ").format(cal.getTime()));
            Calendar d = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int mon = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int sec = cal.get(Calendar.SECOND);
            int msec = cal.get(Calendar.MILLISECOND);
            //log.info("Got date {}-{}-{} {}:{}:{}.{}", year, mon + 1, day, hour, min, sec, msec);
            d.setTimeZone(tz);
            d.clear();
            d.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            d.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            d.set(Calendar.SECOND, cal.get(Calendar.SECOND));
            d.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));
            d.set(Calendar.YEAR, cal.get(Calendar.YEAR));
            d.set(Calendar.MONTH, cal.get(Calendar.MONTH));
            d.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
            d.getTime();
            SimpleDateFormat df = new SimpleDateFormat("HH");
            //log.info("Intermediary {} - {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ").format(d.getTime()), d.get(Calendar.HOUR_OF_DAY));
            while (Integer.parseInt(df.format(d.getTime())) != hour) {
                d.add(Calendar.HOUR, 1);
                d.getTime();
            }
            //log.info("Intermediary {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ").format(d.getTime()));
            val = d.getTime();
        }
        return val;
    }
}