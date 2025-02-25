package background.job.backgroundJobs.model;


public class Ticket {
    private String ticketId;           // e.g. "CAP-123"
    private String issueTypeName;      // e.g. "Task"
    private String issueTypeDescription;
    private String issueSummary;
    private String statusName;

    public Ticket() {
    }

    public Ticket(String ticketId, String issueTypeName, String issueTypeDescription, String issueSummary, String statusName) {
        this.ticketId = ticketId;
        this.issueTypeName = issueTypeName;
        this.issueTypeDescription = issueTypeDescription;
        this.issueSummary = issueSummary;
        this.statusName = statusName;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getIssueTypeName() {
        return issueTypeName;
    }

    public void setIssueTypeName(String issueTypeName) {
        this.issueTypeName = issueTypeName;
    }

    public String getIssueTypeDescription() {
        return issueTypeDescription;
    }

    public void setIssueTypeDescription(String issueTypeDescription) {
        this.issueTypeDescription = issueTypeDescription;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(String issueSummary) {
        this.issueSummary = issueSummary;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

}
