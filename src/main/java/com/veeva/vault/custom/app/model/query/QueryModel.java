package com.veeva.vault.custom.app.model.query;

import jakarta.persistence.*;
import jakarta.persistence.metamodel.EntityType;

import java.util.HashMap;
import java.util.Map;

@Entity
public abstract class QueryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "type")
    private String type = this.getClass().getName();

    //@ManyToMany(cascade = CascadeType.ALL)
    //private Map<String, EntityType> properties = new HashMap<String, EntityType>();

    public final String getType(){
        return this.type;
    }

}
