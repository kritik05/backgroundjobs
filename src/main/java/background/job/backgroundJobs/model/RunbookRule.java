package background.job.backgroundJobs.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="runbook_rules")
public class RunbookRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @ManyToOne
    @JoinColumn(name="runbook_id",referencedColumnName="runbook_id")
    private Runbook runbook;

    @Column(name = "trigger_type")
    private String triggerType;  // e.g. "SCAN_EVENT"

    // JSON array of strings: ["status","severity"] or ["status"] etc.
    @Column(name = "filter_type", columnDefinition = "JSON")
    private String filterType;

    // JSON object for the actual filter values (e.g. {"status":"OPEN","severity":"CRITICAL"})
    @Column(name = "filter_params", columnDefinition = "JSON")
    private String filterParams;

    // JSON array of strings: e.g. ["create_ticket","update_status"]
    @Column(name = "action_type", columnDefinition = "JSON")
    private String actionType;

    @Column(name = "action_params", columnDefinition = "JSON")
    private String actionParams;

    @Column(name = "is_enabled")
    private boolean isEnabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RunbookRule() {}

    public RunbookRule(Long ruleId, Runbook runbook, String triggerType, String filterType, String filterParams, String actionType, String actionParams, boolean isEnabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.ruleId = ruleId;
        this.runbook = runbook;
        this.triggerType = triggerType;
        this.filterType = filterType;
        this.filterParams = filterParams;
        this.actionType = actionType;
        this.actionParams = actionParams;
        this.isEnabled = isEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
// constructor, getters, setters, etc.

    public Long getRuleId() {
        return ruleId;
    }
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Runbook getRunbook() {
        return runbook;
    }
    public void setRunbook(Runbook runbook) {
        this.runbook = runbook;
    }

    public String getTriggerType() {
        return triggerType;
    }
    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getFilterType() {
        return filterType;
    }
    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterParams() {
        return filterParams;
    }
    public void setFilterParams(String filterParams) {
        this.filterParams = filterParams;
    }

    public String getActionType() {
        return actionType;
    }
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionParams() {
        return actionParams;
    }
    public void setActionParams(String actionParams) {
        this.actionParams = actionParams;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
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

    // Optional helper
    public boolean matchesTrigger(String trigger) {
        return (triggerType != null) && triggerType.equalsIgnoreCase(trigger);
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
        return "RunbookRule{" +
                "ruleId=" + ruleId +
                ", runbook=" + (runbook != null ? runbook.getRunbookName() : "null") +
                ", triggerType='" + triggerType + '\'' +
                ", filterType='" + filterType + '\'' +
                ", filterParams='" + filterParams + '\'' +
                ", actionType='" + actionType + '\'' +
                ", actionParams='" + actionParams + '\'' +
                '}';
    }
}