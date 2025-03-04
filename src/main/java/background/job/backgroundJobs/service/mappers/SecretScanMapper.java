package background.job.backgroundJobs.service.mappers;

import background.job.backgroundJobs.model.Severity;
import background.job.backgroundJobs.model.Status;

public class SecretScanMapper {

    public Status mapStatus(String rawState,String dismissedReason) {
        if (rawState == null) {
            return Status.OPEN;
        }
        String lowerState = rawState.toLowerCase();
        String lowerReason = (dismissedReason == null) ? "" : dismissedReason.toLowerCase();
        switch (lowerState) {
            case "open":
                return Status.OPEN;
            case "resolved":
                switch (lowerReason) {
                    case "false_positive":
                        return Status.FALSE_POSITIVE;
                    case "used_in_tests":
                    case "revoked":
                    case "wont_fix":
                        return Status.SUPPRESSED;
                    default:
                        return Status.FALSE_POSITIVE;
                }
            default:
                return Status.OPEN;
        }
    }

    public Severity mapSeverity(String rawSeverity) {
        return Severity.CRITICAL;

    }
    public String dismissedReason(String state) {
        switch (state) {
            case "SUPPRESSED":
                return "revoked";
            case "FALSE_POSITIVE":
                return "false_positive";
            default:
                return "false_positive";
        }
    }

    public String dismissedState(String state) {
        switch (state) {
            case "SUPPRESSED":
            case "FALSE_POSITIVE":
            case "FIXED":
                return "resolved";
            case "OPEN":
                return "open";
            default:
                return "open";
        }
    }

}