package edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper;
import lombok.NonNull;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalibrationDetails implements IDocumentConversion {
    private LocalDate nextDate;
    private LocalDate lastDate;
    private Long interval;
    private List<LocalDate> history;

    public CalibrationDetails(LocalDate nextDate, LocalDate lastDate, Long interval, List<LocalDate> history) {
        this.nextDate = nextDate;
        this.lastDate = lastDate;
        this.history = history == null ? new ArrayList<>() : history;
        this.setInterval(interval);
    }

    private CalibrationDetails() {}

    public static CalibrationDetails fromDocument(Document d) {
        return new CalibrationDetails()
                .nextDate(ConversionHelper.parseDate(d.getString("next")))
                .lastDate(ConversionHelper.parseDate(d.getString("last")))
                .interval(d.getLong("interval"))
                .history(d.getList("history", String.class).parallelStream().map(ConversionHelper::parseDate).toList());
    }

    private CalibrationDetails nextDate(LocalDate nextDate) {
        this.nextDate = nextDate;
        return this;
    }

    private CalibrationDetails lastDate(LocalDate lastDate) {
        this.lastDate = lastDate;
        return this;
    }

    private CalibrationDetails interval(Long interval) {
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
        if (this.interval != null) this.nextDate = this.nextDate.plusDays(interval);
    }

    public void addEvent(LocalDate eventDate, LocalDate nextDate) {
        this.history.add(this.lastDate);
        this.lastDate = eventDate;
        this.nextDate = nextDate;
    }

    public LocalDate getNextDate() {
        return nextDate;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public List<LocalDate> getHistory() {
        return history;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
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