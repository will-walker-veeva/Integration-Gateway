package com.veeva.vault.custom.app.model.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate a field (such as a Map or JsonObject) can be used to deserialize the additional properties of JSON/XML/CSV in the similar fashion as other properties.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnySetter {
}
