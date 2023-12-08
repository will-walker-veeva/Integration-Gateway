package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.admin.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaultSessionRepository extends CrudRepository<Session, Long>{
}
