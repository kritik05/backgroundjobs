package background.job.backgroundJobs.event;

import background.job.backgroundJobs.model.TicketTransitionRequestPayload;

import java.util.UUID;

public final class TicketTransitionRequestEvent implements Event<TicketTransitionRequestPayload> {
    private  TicketTransitionRequestPayload payload;
    private String eventId;

    public TicketTransitionRequestEvent(TicketTransitionRequestPayload payload) {
        this.payload = payload;
        this.eventId= UUID.randomUUID().toString();
    }
    public TicketTransitionRequestEvent(){
        this.eventId = UUID.randomUUID().toString();
    }
    @Override
    public String getType() {
        return "ticketTransition";
    }

    @Override
    public TicketTransitionRequestPayload getPayload() {
        return payload;
    }

    public void setPayload(TicketTransitionRequestPayload payload) {
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