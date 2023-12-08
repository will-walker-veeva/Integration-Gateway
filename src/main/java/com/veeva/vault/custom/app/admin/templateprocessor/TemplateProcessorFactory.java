package com.veeva.vault.custom.app.admin.templateprocessor;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TemplateProcessorFactory implements IExpressionObjectFactory {

    private static final String UTIL_CLASS = "Utils";

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return new HashSet<String>(Arrays.asList(UTIL_CLASS));
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if(expressionObjectName!=null && expressionObjectName.equals(UTIL_CLASS)){
            return new TemplateProcessorUtilities();
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return false;
    }
}