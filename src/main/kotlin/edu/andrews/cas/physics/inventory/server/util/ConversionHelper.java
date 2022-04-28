package edu.andrews.cas.physics.inventory.server.util;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class ConversionHelper {
    private static final Logger logger = LogManager.getLogger();

    public static URL parseURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static LocalDate parseDate(String date) {
        if (date == null) return null;
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            logger.error(e);
            return null;
        }
    }

    public static LocalDate parseDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.of("UTC-5")).toLocalDate();
    }
}
