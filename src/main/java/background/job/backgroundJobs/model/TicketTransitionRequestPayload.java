package background.job.backgroundJobs.model;


public class TicketTransitionRequestPayload {
    Integer tenantId;
    String ticketId;

    public TicketTransitionRequestPayload(){};

    public TicketTransitionRequestPayload(Integer tenantId, String ticketId) {
        this.tenantId = tenantId;
        this.ticketId = ticketId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

}
