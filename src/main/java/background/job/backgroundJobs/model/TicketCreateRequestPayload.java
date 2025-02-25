package background.job.backgroundJobs.model;


import java.util.Objects;


public class TicketCreateRequestPayload {
    String uuid;
    Integer tenantId;
    String summary;
    String description;

    public TicketCreateRequestPayload(){};

    public TicketCreateRequestPayload(String uuid, Integer tenantId, String summary, String description) {
        this.uuid = uuid;
        this.tenantId = tenantId;
        this.summary = summary;
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TicketCreateRequestPayload that = (TicketCreateRequestPayload) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(tenantId, that.tenantId) && Objects.equals(summary, that.summary) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, tenantId, summary, description);
    }

    @Override
    public String toString() {
        return "TicketCreateRequestPayload{" +
                "uuid='" + uuid + '\'' +
                ", tenantId=" + tenantId +
                ", summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

