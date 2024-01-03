package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.admin.Processor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VaultProcessorRepository extends CrudRepository<Processor, String>, QueryByExampleExecutor<Processor> {
}
