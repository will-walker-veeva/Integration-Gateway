package com.veeva.vault.custom.app.model.csv;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvProperty {
    public String key();
    public String[] aliases() default {""};
    public CsvPropertyOption[] options() default {};
}
