package background.job.backgroundJobs.event;


import background.job.backgroundJobs.model.RunbookPayload;

import java.util.UUID;

public class RunbookRequestEvent implements Event<RunbookPayload> {

    private RunbookPayload payload;
    private String eventId;

    public RunbookRequestEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

    public RunbookRequestEvent(RunbookPayload payload) {
        this.payload = payload;
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public String getType() {
        return "runbook";
    }

    @Override
    public RunbookPayload getPayload() {
        return this.payload;
    }

    @Override
    public String getEventId() {
        return this.eventId;
    }

    public void setPayload(RunbookPayload payload) {
        this.payload = payload;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
