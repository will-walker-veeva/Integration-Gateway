onMethodSelection { expr, methodNode ->
    println(methodNode.declaringClass.name)
    if (methodNode.declaringClass.name == 'java.lang.System') {
        addStaticTypeError("Method call is not alllowed!", expr)
    }
}

unresolvedVariable { var ->
    println(var.name);
    if (var.name === 'client') {
        storeType(var, classNodeFor(com.veeva.vault.custom.app.client.Client))
        handled = true
    } else if (var.name === 'logger') {
        storeType(var, classNodeFor(com.veeva.vault.custom.app.client.Logger))
        handled = true
    } else if (var.name === 'request') {
        storeType(var, classNodeFor(com.veeva.vault.custom.app.model.http.HttpRequest))
        handled = true
    } else if (var.name === 'response') {
        storeType(var, classNodeFor(com.veeva.vault.custom.app.model.http.HttpResponse))
        handled = true
    }
}