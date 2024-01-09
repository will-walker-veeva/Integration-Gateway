package com.veeva.vault.custom.app.admin;

import com.veeva.vault.custom.app.client.Client;
import com.veeva.vault.custom.app.client.Logger;
import com.veeva.vault.custom.app.model.http.HttpRequest;
import com.veeva.vault.custom.app.model.http.HttpResponse;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;

public class SecureTypeCheckedCompilerExtension extends AbstractTypeCheckingExtension {
    public SecureTypeCheckedCompilerExtension(final StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor);
    }

    @Override
    public boolean handleUnresolvedVariableExpression(final VariableExpression vexp) {
        if ("client".equals(vexp.getName())) {
            storeType(vexp, ClassHelper.make(Client.class));
            setHandled(true);
            return true;
        }else if ("logger".equals(vexp.getName())){
            storeType(vexp, ClassHelper.make(Logger.class));
            setHandled(true);
            return true;
        }else if ("request".equals(vexp.getName())){
            storeType(vexp, ClassHelper.make(HttpRequest.class));
            setHandled(true);
            return true;
        }else if ("response".equals(vexp.getName())){
            storeType(vexp, ClassHelper.make(HttpResponse.class));
            setHandled(true);
            return true;
        }
        return false;
    }

}
