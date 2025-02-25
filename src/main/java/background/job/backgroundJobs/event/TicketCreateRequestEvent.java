package background.job.backgroundJobs.event;


import background.job.backgroundJobs.model.TicketCreateRequestPayload;

import java.util.UUID;

public final class TicketCreateRequestEvent implements Event<TicketCreateRequestPayload> {
    private  TicketCreateRequestPayload payload;
    private String eventId;

    public TicketCreateRequestEvent(TicketCreateRequestPayload payload) {
        this.payload = payload;
        this.eventId= UUID.randomUUID().toString();
    }
    public TicketCreateRequestEvent(){
        this.eventId = UUID.randomUUID().toString();
    }
    @Override
    public String getType() {
        return "ticketCreate";
    }

    @Override
    public TicketCreateRequestPayload getPayload() {
        return payload;
    }

    public void setPayload(TicketCreateRequestPayload payload) {
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