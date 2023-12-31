package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.files.File;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Service
public class FilesClient {
    private Client client;

    /**
     * @hidden
     */
    public FilesClient(){

    }

    /**
     * @hidden
     * @param client
     */
    public FilesClient(Client client){
        this.client = client;
    }

    /**
     *
     * @param extension
     * @return
     * @throws ProcessException
     */
    public File createTemporaryFile(String extension) throws ProcessException {
        if(this.client == null) throw new ProcessException("Unauthorized client");
        File file = null;
        try{
            file = new File(java.io.File.createTempFile("tmp", "."+extension));
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
        this.client.registerFile(file);
        return file;
    }

    /**
     *
     * @param file
     * @param charset
     * @return
     * @throws ProcessException
     */
    public String readFileToString(File file, Charset charset) throws ProcessException {
        try{
            return FileUtils.readFileToString(new java.io.File(file.getAbsolutePath()), charset);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param charset
     * @return
     * @throws ProcessException
     */
    public List<String> readLines(File file, Charset charset) throws ProcessException {
        try{
            return FileUtils.readLines(new java.io.File(file.getAbsolutePath()), charset);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws ProcessException
     */
    public byte[] readFileToByteArray(File file) throws ProcessException {
        try{
            return FileUtils.readFileToByteArray(new java.io.File(file.getAbsolutePath()));
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param data
     * @param charset
     * @throws ProcessException
     */
    public void writeStringToFile(File file, String data, Charset charset) throws ProcessException {
        try{
            FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), data, charset);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param data
     * @param charset
     * @param append
     * @throws ProcessException
     */
    public void writeStringToFile(File file, String data, Charset charset, boolean append) throws ProcessException {
        try{
            FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), data, charset, append);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param lines
     * @throws ProcessException
     */
    public void writeLines(File file, Collection<?> lines) throws ProcessException {
        try{
            FileUtils.writeLines(new java.io.File(file.getAbsolutePath()), lines);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param data
     * @throws ProcessException
     */
    public void writeStringToPdf(File file, String data) throws ProcessException {
        Document document = Jsoup.parse(data);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        try (OutputStream outputStream = new FileOutputStream(file.getAbsolutePath())) {
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sharedContext = renderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);
            renderer.setDocumentFromString(document.html());
            renderer.layout();
            renderer.createPDF(outputStream);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param lines
     * @param append
     * @throws ProcessException
     */
    public void writeLines(File file, Collection<?> lines, boolean append) throws ProcessException {
        try{
            FileUtils.writeLines(new java.io.File(file.getAbsolutePath()), lines, append);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param data
     * @throws ProcessException
     */
    public void writeByteArrayToFile(File file, byte[] data)throws ProcessException {
        try{
            FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param data
     * @param append
     * @throws ProcessException
     */

    public void writeByteArrayToFile(File file, byte[] data, boolean append)throws ProcessException {
        try{
            FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, append);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param data
     * @param off
     * @param len
     * @throws ProcessException
     */
    public void writeByteArrayToFile(File file, byte[] data, int off, int len)throws ProcessException {
        try{
            FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, off, len);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param data
     * @param off
     * @param len
     * @param append
     * @throws ProcessException
     */
    public void writeByteArrayToFile(File file, byte[] data, int off, int len, boolean append) throws ProcessException {
        try{
            FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, off, len, append);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @param jsonObject
     * @throws ProcessException
     */
    public void writeJson(File file, JsonObject jsonObject) throws ProcessException {
        try{
            FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), jsonObject.toString(), StandardCharsets.UTF_8);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

}
