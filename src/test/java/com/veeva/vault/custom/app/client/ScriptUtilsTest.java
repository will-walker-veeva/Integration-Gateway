package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.ScriptUtilities;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ScriptUtilsTest {
    public static void main(String[] args) throws Exception {
        ScriptUtilities scriptExecutionUtils = new ScriptUtilities();
        Map<String, String> scripts = new HashMap<String, String>();
        scripts.put("com.veeva.vault.custom.app.client.MyModel", IOUtils.toString(new FileInputStream("src/test/java/com/veeva/vault/custom/app/client/MyModel.java"), StandardCharsets.UTF_8));
        //GroovyShell shell = scriptExecutionUtils.loadScriptContext(scripts);
        //shell.evaluate("import com.veeva.vault.custom.app.client.MyModel; \n MyModel model = new MyModel()");
    }
}
