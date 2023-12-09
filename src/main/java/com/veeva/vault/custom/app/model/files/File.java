package com.veeva.vault.custom.app.model.files;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class File {
    private java.io.File file;
    private boolean isDeleteAsync = false;

    /**
     * @hidden
     * @param file
     */
    public File(@NotNull java.io.File file) {
        this.file=file;
    }

    /**
     * @hidden
     * @param file
     * @param isDeleteAsync
     */
    public File(@NotNull java.io.File file, boolean isDeleteAsync) {
        this.isDeleteAsync = isDeleteAsync;
        this.file = file;
    }

    public OutputStream getOutputStream() throws Exception{
        return new FileOutputStream(this.file);
    }

    public InputStream getInputStream() throws Exception{
        return new FileInputStream(this.file);
    }

    public String getAbsolutePath(){
        return this.file!=null? this.file.getAbsolutePath() : null;
    }

    public boolean delete(){
        return this.file.delete();
    }
}
