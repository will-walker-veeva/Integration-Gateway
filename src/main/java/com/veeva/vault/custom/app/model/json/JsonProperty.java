package com.veeva.vault.custom.app.model.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation representing a JSON serializable property
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonProperty {
    public String key();
    public String[] aliases() default {""};
    public JsonPropertyOption[] options() default {};
}
