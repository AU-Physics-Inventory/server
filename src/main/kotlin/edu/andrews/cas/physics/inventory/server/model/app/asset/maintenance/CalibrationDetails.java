package edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalibrationDetails implements IDocumentConversion {
    public static final int DEFAULT_CALIBRATION_INTERVAL = 90;
    private static final Logger logger = LogManager.getLogger();
    private LocalDate nextDate;
    private LocalDate lastDate;
    private Integer interval;
    private List<LocalDate> history;

    public CalibrationDetails(LocalDate nextDate, LocalDate lastDate, Integer interval, List<LocalDate> history) {
        this.nextDate = nextDate;
        this.lastDate = lastDate;
        this.history = history == null ? new ArrayList<>() : history;
        this.setInterval(interval);
    }

    private CalibrationDetails() {}

    public static CalibrationDetails fromDocument(Document d) {
        logger.debug("Calibration details: {}", d);
        return new CalibrationDetails()
                .nextDate(ConversionHelper.parseDate(d.getDate("next")))
                .lastDate(ConversionHelper.parseDate(d.getDate("last")))
                .interval(d.getInteger("interval"))
                .history(d.getList("history", Date.class).parallelStream().map(ConversionHelper::parseDate).toList());
    }

    private CalibrationDetails nextDate(LocalDate nextDate) {
        this.nextDate = nextDate;
        return this;
    }

    private CalibrationDetails lastDate(LocalDate lastDate) {
        this.lastDate = lastDate;
        return this;
    }

    private CalibrationDetails interval(Integer interval) {
        this.interval = interval;
        return this;
    }

    private CalibrationDetails history(List<LocalDate> history) {
        this.history = history;
        return this;
    }

    public void addEvent(LocalDate eventDate) {
        this.history.add(this.lastDate);
        this.lastDate = eventDate;
        this.nextDate = eventDate.plusDays(this.interval == null ? DEFAULT_CALIBRATION_INTERVAL : this.interval);
    }

    public void addEvent(LocalDate eventDate, LocalDate nextDate) {
        this.history.add(this.lastDate);
        this.lastDate = eventDate;
        this.nextDate = nextDate;
    }

    public LocalDate getNextDate() {
        return nextDate;
    }

    public void setNextDate(LocalDate nextDate) {
        this.nextDate = nextDate;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public List<LocalDate> getHistory() {
        return history;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        if (interval != null && interval < 0)
            throw new IllegalArgumentException("Calibration interval may not be a negative value");
        else this.interval = interval;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("next", getNextDate())
                .append("last", getLastDate())
                .append("interval", getInterval())
                .append("history", getHistory());
    }
}