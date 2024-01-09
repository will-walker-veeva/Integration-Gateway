package com.veeva.vault.custom.app;

import com.veeva.vault.custom.app.client.FilesClient;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.files.File;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.FileNotFoundException;

@Controller
@RequestMapping(path = "/resources/docs/")
public class JavadocController {
    @Autowired
    ResourceLoader resourceLoader;

    @GetMapping("/**")
    public ResponseEntity<byte[]> handleGet(HttpServletRequest request) throws FileNotFoundException, ProcessException {
        String requestURL = request.getRequestURL().toString();
        String path = requestURL.split("/resources/docs/")[1];
        return ResponseEntity.ok().body(new FilesClient().readFileToByteArray(new File(ResourceUtils.getFile("classpath:public/resources/docs/"+path))));
    }
}
