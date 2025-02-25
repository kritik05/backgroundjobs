package background.job.backgroundJobs.event;

public interface Acknowledgment<T> {
    String getAcknowledgementId();
    T getPayload();
}