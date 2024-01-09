package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.admin.VaultConfigurationRecordCache;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VaultConfigurationCacheRepository extends CrudRepository<VaultConfigurationRecordCache, String> {

    @Query("SELECT e FROM VaultConfigurationRecordCache e WHERE e.objectName = ?1 AND e.field = ?2 AND e.globalId = ?3 AND DATEDIFF(MINUTE, e.cachedTime, CURRENT_TIMESTAMP)<5")
    Optional<VaultConfigurationRecordCache> findByObjectNameAndFieldAndGlobalId(String objectName, String field, String globalId);

    @Query("SELECT e FROM VaultConfigurationRecordCache e WHERE e.objectName = ?1 AND e.field = ?2 AND e.id = ?3 AND DATEDIFF(MINUTE, e.cachedTime, CURRENT_TIMESTAMP)<5")
    Optional<VaultConfigurationRecordCache> findByObjectNameAndFieldAndId(String objectName, String field, String id);
}
