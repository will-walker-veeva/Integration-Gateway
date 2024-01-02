package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.query.QueryModel;
import com.veeva.vault.custom.app.model.query.QueryResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;


public class QueryableRepositoryImpl implements QueryableRepository{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public QueryResult<JsonObject> query(String sqlString) throws ProcessException {
        try {
            Query query = entityManager.createNativeQuery(sqlString, JsonObject.class);
            return new QueryResult<JsonObject>(query.getResultList(), query.getMaxResults());
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    @Override
    public <T extends QueryModel>  QueryResult<T> query(String sql, Class<T> className) throws ProcessException {
        try {
            TypedQuery<T> queryResult = entityManager.createQuery(sql, className);
            return new QueryResult<T>(queryResult.getResultList(), queryResult.getMaxResults());
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }
}
