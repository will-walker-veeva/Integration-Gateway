package com.veeva.vault.custom.app.model.xml;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation representing an XML serializable field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XmlProperty {
    public String key() default "";
    public int order() default -1;
    public XmlPropertyOption[] options() default {};
}
