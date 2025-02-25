package background.job.backgroundJobs.repository;

import background.job.backgroundJobs.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {
}