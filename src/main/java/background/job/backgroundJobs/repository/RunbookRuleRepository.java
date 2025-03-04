package background.job.backgroundJobs.repository;

import background.job.backgroundJobs.model.RunbookRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunbookRuleRepository extends JpaRepository<RunbookRule, Long> {

    List<RunbookRule> findByRunbookInAndIsEnabledTrue(List<background.job.backgroundJobs.model.Runbook> runbooks);


}