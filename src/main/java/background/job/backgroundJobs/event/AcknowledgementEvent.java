package background.job.backgroundJobs.event;


import background.job.backgroundJobs.model.AcknowledgementPayload;

import java.util.UUID;

public class AcknowledgementEvent implements Acknowledgment<AcknowledgementPayload> {

    private String acknowledgementId;
    private AcknowledgementPayload payload;

    public AcknowledgementEvent() {
        // If no ack ID is provided, generate a UUID
        this.acknowledgementId = UUID.randomUUID().toString();
    }

    public AcknowledgementEvent(String acknowledgementId, AcknowledgementPayload payload) {
        // If ack ID is null/empty, generate a fresh one
        this.acknowledgementId = (acknowledgementId == null || acknowledgementId.isEmpty())
                ? UUID.randomUUID().toString()
                : acknowledgementId;

        this.payload = payload;
    }

    @Override
    public String getAcknowledgementId() {
        return acknowledgementId;
    }

    @Override
    public AcknowledgementPayload getPayload() {
        return payload;
    }

    public void setAcknowledgementId(String acknowledgementId) {
        this.acknowledgementId = acknowledgementId;
    }

    public void setPayload(AcknowledgementPayload payload) {
        this.payload = payload;
    }
}