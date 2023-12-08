package com.veeva.vault.custom.app.admin.templateprocessor;

import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

public class TemplateProcessorDialect implements IExpressionObjectDialect {

    public TemplateProcessorDialect(){

    }

    public String getName(){
        return "E2B Thymeleaf Dialect";
    }

    public IExpressionObjectFactory getExpressionObjectFactory(){
        return new TemplateProcessorFactory();
    }

}
