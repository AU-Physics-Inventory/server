package edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import lombok.NonNull;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceRecord implements IDocumentConversion {
    private static final MaintenanceEvent UNKNOWN_EVENT = new MaintenanceEvent(Status.UNKNOWN, LocalDate.EPOCH);

    private MaintenanceEvent currentStatus;
    private List<MaintenanceEvent> history;
    private CalibrationDetails calibrationDetails;
    private String notes;

    public MaintenanceRecord() {
        this.history = new ArrayList<>();
        this.calibrationDetails = new CalibrationDetails(null, null, null, null);
        this.currentStatus = UNKNOWN_EVENT;
    }

    public MaintenanceRecord(@NonNull MaintenanceEvent currentStatus, List<MaintenanceEvent> history, CalibrationDetails calibrationDetails, String notes) {
        this.currentStatus = currentStatus;
        this.history = history;
        this.calibrationDetails = calibrationDetails;
        this.notes = notes;
    }

    public MaintenanceRecord(List<MaintenanceEvent> history, CalibrationDetails calibrationDetails) {
        this.history = history;
        this.calibrationDetails = calibrationDetails;
        this.currentStatus = UNKNOWN_EVENT;
    }

    public static MaintenanceRecord fromDocument(Document d) {
        return new MaintenanceRecord()
                .currentStatus(MaintenanceEvent.fromDocument(d.get("currentStatus", Document.class)))
                .history(d.getList("history", Document.class).parallelStream().map(MaintenanceEvent::fromDocument).toList())
                .calibrationDetails(CalibrationDetails.fromDocument(d.get("calibration", Document.class)))
                .notes(d.getString("notes"));
    }

    private MaintenanceRecord currentStatus(MaintenanceEvent event) {
        this.currentStatus = event;
        return this;
    }

    private MaintenanceRecord history(List<MaintenanceEvent> history) {
        this.history = history;
        return this;
    }

    private MaintenanceRecord calibrationDetails(CalibrationDetails details) {
        this.calibrationDetails = details;
        return this;
    }

    private MaintenanceRecord notes(String notes) {
        this.notes = notes;
        return this;
    }

    public MaintenanceEvent getCurrentStatus() {
        return currentStatus;
    }

    public List<MaintenanceEvent> getHistory() {
        return history;
    }

    public CalibrationDetails getCalibrationDetails() {
        return calibrationDetails;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void changeStatus(MaintenanceEvent newStatus) {
        if (!this.currentStatus.equals(UNKNOWN_EVENT)) history.add(this.currentStatus);
        this.currentStatus = newStatus;
    }

    public void addCalibrationEvent(LocalDate eventDate) {
        this.calibrationDetails.addEvent(eventDate);
    }

    public void addCalibrationEvent(LocalDate eventDate, LocalDate nextDate) {
        this.calibrationDetails.addEvent(eventDate, nextDate);
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("currentStatus", getCurrentStatus().toDocument())
                .append("history", getHistory().parallelStream().map(MaintenanceEvent::toDocument).toList())
                .append("calibration", getCalibrationDetails().toDocument())
                .append("notes", getNotes());
    }
}
