package com.veeva.vault.custom.app.model.query;

import java.util.List;

public class QueryResult <T>{
    private List<T> results;
    private Integer maxResults;

    public QueryResult(List<T> results, Integer maxResults) {
        this.results = results;
        this.maxResults = maxResults;
    }

    public List<T> getResults() {
        return results;
    }

    public Integer getMaxResults() {
        return maxResults;
    }
}
