package com.veeva.vault.custom.app.model.query;

/**
 * Abstract class representing a Queryable model, should have {@link QueryModelInfo} annotation
 */
public abstract class QueryModel {
    @QueryProperty(key = "id", type = QueryPropertyType.BIG_INTEGER)
    private Long id;
    public QueryModel(){

    }

    public QueryModel(Long id) {
        this.id = id;
    }

    public final Long getId() {
        return id;
    }
}
