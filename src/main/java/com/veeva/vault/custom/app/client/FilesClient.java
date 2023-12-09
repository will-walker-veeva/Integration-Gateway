package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.csv.CsvData;
import com.veeva.vault.custom.app.model.csv.CsvFormat;
import com.veeva.vault.custom.app.model.csv.CsvRecord;
import com.veeva.vault.custom.app.model.files.File;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FilesClient {
    private Client client;

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
     * @throws Exception
     */
    public File createTemporaryFile(String extension) throws Exception{
        File file = new File(java.io.File.createTempFile("tmp", "."+extension));
        this.client.registerFile(file);
        return file;
    }

    /**
     *
     * @param file
     * @param charset
     * @return
     * @throws Exception
     */
    public String readFileToString(File file, Charset charset) throws Exception{
        return FileUtils.readFileToString(new java.io.File(file.getAbsolutePath()), charset);
    }

    /**
     *
     * @param file
     * @param charset
     * @return
     * @throws Exception
     */
    public List<String> readLines(File file, Charset charset) throws Exception{
        return FileUtils.readLines(new java.io.File(file.getAbsolutePath()), charset);
    }

    /**
     *
     * @param file
     * @param csvFormat
     * @return
     * @throws Exception
     */
    public CsvData readCSV(File file, CsvFormat csvFormat) throws Exception{
        Reader in = new InputStreamReader(file.getInputStream());
        CSVParser parser = csvFormat.getCsvFormat().parse(in);
        return new CsvData(csvFormat, parser.stream().map(each -> new CsvRecord(each)).collect(Collectors.toList()));
    }

    /**
     *
     * @param file
     * @return
     * @throws Exception
     */
    public JsonObject readJson(File file) throws Exception{
        return new JsonObject(readFileToString(file, StandardCharsets.UTF_8));
    }

    /**
     *
     * @param file
     * @return
     * @throws Exception
     */
    public byte[] readFileToByteArray(File file) throws Exception{
        return FileUtils.readFileToByteArray(new java.io.File(file.getAbsolutePath()));
    }

    /**
     *
     * @param file
     * @param data
     * @param charset
     * @throws Exception
     */
    public void writeStringToFile(File file, String data, Charset charset) throws Exception{
        FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), data, charset);
    }

    /**
     *
     * @param file
     * @param data
     * @param charset
     * @param append
     * @throws Exception
     */
    public void writeStringToFile(File file, String data, Charset charset, boolean append) throws Exception{
        FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), data, charset, append);
    }

    /**
     *
     * @param file
     * @param lines
     * @throws Exception
     */
    public void writeLines(File file, Collection<?> lines) throws Exception{
        FileUtils.writeLines(new java.io.File(file.getAbsolutePath()), lines);
    }

    /**
     *
     * @param file
     * @param data
     * @throws Exception
     */
    public void writeStringToPdf(File file, String data) throws Exception{
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
            throw e;
        }
    }

    /**
     *
     * @param file
     * @param lines
     * @param append
     * @throws Exception
     */
    public void writeLines(File file, Collection<?> lines, boolean append) throws Exception{
        FileUtils.writeLines(new java.io.File(file.getAbsolutePath()), lines, append);
    }

    /**
     *
     * @param file
     * @param data
     * @throws Exception
     */
    public void writeByteArrayToFile(File file, byte[] data)throws Exception{
        FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data);
    }

    /**
     *
     * @param file
     * @param data
     * @param append
     * @throws Exception
     */

    public void writeByteArrayToFile(File file, byte[] data, boolean append)throws Exception{
        FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, append);
    }

    /**
     *
     * @param file
     * @param data
     * @param off
     * @param len
     * @throws Exception
     */
    public void writeByteArrayToFile(File file, byte[] data, int off, int len)throws Exception{
        FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, off, len);
    }

    /**
     *
     * @param file
     * @param data
     * @param off
     * @param len
     * @param append
     * @throws Exception
     */
    public void writeByteArrayToFile(File file, byte[] data, int off, int len, boolean append)throws Exception{
        FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, off, len, append);
    }

    /**
     *
     * @param file
     * @param jsonObject
     * @throws Exception
     */
    public void writeJson(File file, JsonObject jsonObject) throws Exception{
        FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), jsonObject.toString(), StandardCharsets.UTF_8);
    }

}
