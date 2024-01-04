package com.veeva.vault.custom.app.model.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation representing a Queryable model
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryModelInfo {
    /**
     * Temporary Table Name for the Query Model
     * @return
     */
    public String name();
}
