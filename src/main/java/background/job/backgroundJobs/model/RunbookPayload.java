package background.job.backgroundJobs.model;


import java.util.List;

public class RunbookPayload {

    private Long runbookId;
    private Integer tenantId;
    private List<String> findingIds;
    String triggerType;
    // Constructors
    public RunbookPayload() {}

    public RunbookPayload(Long runbookId, Integer tenantId, List<String> findingIds,String triggerType) {
        this.runbookId = runbookId;
        this.tenantId = tenantId;
        this.findingIds = findingIds;
        this.triggerType=triggerType;
    }

    // getters/setters
    public Long getRunbookId() { return runbookId; }
    public void setRunbookId(Long runbookId) { this.runbookId = runbookId; }

    public Integer getTenantId() { return tenantId; }
    public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }

    public List<String> getFindingIds() { return findingIds; }
    public void setFindingIds(List<String> findingIds) { this.findingIds = findingIds; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }


}