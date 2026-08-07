package models;

public class AuditEvent {
    private final int eventId;
    private final String description;
    private final String timestamp;

    public AuditEvent(int eventId, String description, String timestamp) {
        this.eventId = eventId;
        this.description = description;
        this.timestamp = timestamp;
    }

    public int getEventId() { return eventId; }
    public String getDescription() { return description; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Audit{id=%d, desc='%s', time='%s'}", eventId, description, timestamp);
    }
}
