package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.exception.AuthenticationException;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.query.QueryModel;
import com.veeva.vault.custom.app.model.query.QueryResult;
import com.veeva.vault.custom.app.repository.StandardRepository;
import com.veeva.vault.vapil.api.client.VaultClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.veeva.vault.custom.app.client.Client.VAULT_CLIENT_ID;

/**
 * @hidden
 */
@Service
public class QueryClient {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private StandardRepository repository;


    public <T extends QueryModel> T save(QueryModel entity) {
        return (T) repository.save(entity);
    }

    public <T extends QueryModel> T saveAll(Iterable<QueryModel> entities) {
        return (T) repository.saveAll(entities);
    }

    public QueryResult<JsonObject> query(String sqlString) throws ProcessException {
        try{
            return repository.query(sqlString);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    public <T extends QueryModel> QueryResult<T> query(String sql, Class<T> className) throws ProcessException {
        return repository.query(sql, className);
    }

}
