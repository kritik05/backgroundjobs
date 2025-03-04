package background.job.backgroundJobs.repository;

import background.job.backgroundJobs.model.Runbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunbookRepository extends JpaRepository<Runbook, Integer> {

    // e.g. load all runbooks for a tenant that are enabled
    List<Runbook> findByTenantIdAndIsEnabledTrue(Long tenantId);


}