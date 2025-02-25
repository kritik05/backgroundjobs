package background.job.backgroundJobs.model;


import jakarta.persistence.*;

@Entity
@Table(name = "tenant_tickets")
public class TenantTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Column(name = "finding_id", nullable = false)
    private String findingId;

    public TenantTicket() {
    }

    public TenantTicket(Integer tenantId, String ticketId, String findingId) {
        this.tenantId = tenantId;
        this.ticketId = ticketId;
        this.findingId = findingId;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
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

    public String getFindingId() {
        return findingId;
    }

    public void setFindingId(String findingId) {
        this.findingId = findingId;
    }

    @Override
    public String toString() {
        return "TenantTicket{" +
                "id=" + id +
                ", tenantId='" + tenantId + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", findingId='" + findingId + '\'' +
                '}';
    }
}
