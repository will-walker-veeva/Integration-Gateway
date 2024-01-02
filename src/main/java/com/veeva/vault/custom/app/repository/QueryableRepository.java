package com.veeva.vault.custom.app.repository;

import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.query.QueryModel;
import com.veeva.vault.custom.app.model.query.QueryResult;

public interface QueryableRepository {
    QueryResult<JsonObject> query(String sql) throws ProcessException;
    <T extends QueryModel>  QueryResult<T> query(String sql, Class<T> className) throws ProcessException;
}
