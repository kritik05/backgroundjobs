package background.job.backgroundJobs.event;

import background.job.backgroundJobs.model.UpdateRequestPayload;

import java.util.UUID;

public class UpdateRequestEvent implements Event<UpdateRequestPayload> {
    private UpdateRequestPayload payload;
    private String eventId;

    public UpdateRequestEvent(UpdateRequestPayload payload) {
        this.payload = payload;
        this.eventId= UUID.randomUUID().toString();
    }
    public UpdateRequestEvent(){
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public String getType() {
        return "update";
    }

    @Override
    public UpdateRequestPayload getPayload() {
        return payload;
    }

    public void setPayload(UpdateRequestPayload payload) {
        this.payload = payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
