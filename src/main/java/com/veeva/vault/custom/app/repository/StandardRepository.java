package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.model.query.QueryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandardRepository extends JpaRepository<QueryModel, Integer>, QueryableRepository {
}
