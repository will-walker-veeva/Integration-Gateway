package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.admin.WhitelistedElement;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IPWhitelistRepository extends CrudRepository<WhitelistedElement, String> {

    @Query("SELECT e FROM WhitelistedElement e")
    Collection<WhitelistedElement> findAll();

    @Query("SELECT e FROM WhitelistedElement e WHERE e.processorId = ?1")
    Collection<WhitelistedElement> findAllByProcessor(String processorId);

    @Transactional
    List<WhitelistedElement> deleteByProcessorId(String processorId);
}
