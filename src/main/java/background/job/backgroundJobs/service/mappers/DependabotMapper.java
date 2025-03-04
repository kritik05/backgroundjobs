package background.job.backgroundJobs.service.mappers;

import background.job.backgroundJobs.model.Severity;
import background.job.backgroundJobs.model.Status;

public class DependabotMapper {

    public Status mapStatus(String rawState, String dismissedReason) {
        if (rawState == null) {
            return Status.OPEN;
        }
        String lowerState = rawState.toLowerCase();
        String lowerReason = (dismissedReason == null) ? "" : dismissedReason.toLowerCase();

        switch (lowerState) {
            case "open":
                return Status.OPEN;
            case "auto_dismissed":
                return Status.SUPPRESSED;
            case "dismissed":
                switch (lowerReason) {
                    case "fix_started":
                        return Status.SUPPRESSED;
                    case "inaccurate":
                        return Status.FALSE_POSITIVE;
                    case "no_bandwidth":
                    case "not_used":
                    case "tolerable_risk":
                        return Status.SUPPRESSED;
                    default:
                        return Status.FALSE_POSITIVE;
                }
            case "fixed":
                return Status.FIXED;
            default:
                return Status.OPEN;
        }
    }

    public Severity mapSeverity(String rawSeverity) {
        if (rawSeverity == null) {
            return Severity.INFO;
        }
        switch (rawSeverity.toLowerCase()) {
            case "critical":
                return Severity.CRITICAL;
            case "high":
                return Severity.HIGH;
            case "medium":
                return Severity.MEDIUM;
            case "low":
                return Severity.LOW;
            default:
                return Severity.INFO;
        }
    }
    public String dismissedReason(String state) {
        switch (state) {
            case "SUPPRESSED":
                return "fix_started";
            case "FALSE_POSITIVE":
                return "inaccurate";
            case "FIXED":
                return "fixed";
            default:
                return "false positive";
        }
    }

    public String dismissedState(String state) {
        switch (state) {
            case "SUPPRESSED":
            case "FALSE_POSITIVE":
            case "FIXED":
                return "dismissed";
            case "OPEN":
                return "open";
            default:
                return "false positive";
        }
    }
}