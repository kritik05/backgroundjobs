package background.job.backgroundJobs.model;

public class AcknowledgementPayload {
    private String originalEventId; // The eventId from the event being acknowledged
    private String status;          // e.g. "SUCCESS", "FAILURE"

    public AcknowledgementPayload() {}

    public AcknowledgementPayload(String originalEventId, String status) {
        this.originalEventId = originalEventId;
        this.status = status;
    }

    public String getOriginalEventId() {
        return originalEventId;
    }

    public void setOriginalEventId(String originalEventId) {
        this.originalEventId = originalEventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}