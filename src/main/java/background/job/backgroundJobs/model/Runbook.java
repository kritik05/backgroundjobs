package background.job.backgroundJobs.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name="runbooks")
public class Runbook {

    @Id
    @Column(name = "runbook_id")
    private Long runbookId;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "runbook_name")
    private String runbookName;

    @Column(name = "runbook_description")
    private String runbookDescription;

    @Column(name = "is_enabled")
    private boolean isEnabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Runbook() {}

    public Runbook(Long runbookId, Long tenantId, String runbookName, String runbookDescription, boolean isEnabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.runbookId = runbookId;
        this.tenantId = tenantId;
        this.runbookName = runbookName;
        this.runbookDescription = runbookDescription;
        this.isEnabled = isEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public Long getRunbookId() {
        return runbookId;
    }

    public void setRunbookId(Long runbookId) {
        this.runbookId = runbookId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getRunbookName() {
        return runbookName;
    }

    public void setRunbookName(String runbookName) {
        this.runbookName = runbookName;
    }

    public String getRunbookDescription() {
        return runbookDescription;
    }

    public void setRunbookDescription(String runbookDescription) {
        this.runbookDescription = runbookDescription;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Runbook{" +
                ", runbookId=" + runbookId +
                ", tenantId=" + tenantId +
                ", runbookName='" + runbookName + '\'' +
                ", runbookDescription='" + runbookDescription + '\'' +
                ", isEnabled=" + isEnabled +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}
