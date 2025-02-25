package background.job.backgroundJobs.model;


public class UpdateRequestPayload {
    String uuid;
    String tooltype;
    StateRequest request;
    String alertNumber;
    int tenantId;

    public UpdateRequestPayload(){};

    public UpdateRequestPayload(String uuid, String tooltype, StateRequest request, String alertNumber, int tenantId) {
        this.uuid = uuid;
        this.tooltype = tooltype;
        this.request = request;
        this.alertNumber = alertNumber;
        this.tenantId = tenantId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTooltype() {
        return tooltype;
    }

    public void setTooltype(String tooltype) {
        this.tooltype = tooltype;
    }

    public StateRequest getRequest() {
        return request;
    }

    public void setRequest(StateRequest request) {
        this.request = request;
    }

    public String getAlertNumber() {
        return alertNumber;
    }

    public void setAlertNumber(String alertNumber) {
        this.alertNumber = alertNumber;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
