package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.admin.ScriptLibrary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VaultScriptLibraryRepository extends CrudRepository<ScriptLibrary, String>, QueryByExampleExecutor<ScriptLibrary> {
}
