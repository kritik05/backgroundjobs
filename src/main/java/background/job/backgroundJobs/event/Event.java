package background.job.backgroundJobs.event;

public interface Event <T>{
    String getType();
    T getPayload();
    String getEventId();
}
