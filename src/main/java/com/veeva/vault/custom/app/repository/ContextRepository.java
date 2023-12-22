package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.admin.CacheContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContextRepository extends CrudRepository<CacheContext, String> {
}
