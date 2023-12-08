package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.admin.ThreadItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreadRegistry extends CrudRepository<ThreadItem, String> {
}
