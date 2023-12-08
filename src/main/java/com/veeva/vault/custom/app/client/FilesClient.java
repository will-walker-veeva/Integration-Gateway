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

    public FilesClient(Client client){
        this.client = client;
    }

    public File createTemporaryFile(String extension) throws Exception{
        File file = new File(java.io.File.createTempFile("tmp", "."+extension));
        this.client.registerFile(file);
        return file;
    }

    public String readFileToString(File file, Charset charset) throws Exception{
        return FileUtils.readFileToString(new java.io.File(file.getAbsolutePath()), charset);
    }

    public List<String> readLines(File file, Charset charset) throws Exception{
        return FileUtils.readLines(new java.io.File(file.getAbsolutePath()), charset);
    }

    public CsvData readCSV(File file, CsvFormat csvFormat) throws Exception{
        Reader in = new InputStreamReader(file.getInputStream());
        CSVParser parser = csvFormat.getCsvFormat().parse(in);
        return new CsvData(csvFormat, parser.stream().map(each -> new CsvRecord(each)).collect(Collectors.toList()));
    }

    public JsonObject readJson(File file) throws Exception{
        return new JsonObject(readFileToString(file, StandardCharsets.UTF_8));
    }

    public byte[] readFileToByteArray(File file) throws Exception{
        return FileUtils.readFileToByteArray(new java.io.File(file.getAbsolutePath()));
    }

    public void writeStringToFile(File file, String data, Charset charset) throws Exception{
        FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), data, charset);
    }

    public void writeStringToFile(File file, String data, Charset charset, boolean append) throws Exception{
        FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), data, charset, append);
    }

    public void writeLines(File file, Collection<?> lines) throws Exception{
        FileUtils.writeLines(new java.io.File(file.getAbsolutePath()), lines);
    }

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

    public void writeLines(File file, Collection<?> lines, boolean append) throws Exception{
        FileUtils.writeLines(new java.io.File(file.getAbsolutePath()), lines, append);
    }

    public void writeByteArrayToFile(File file, byte[] data)throws Exception{
        FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data);
    }

    public void writeByteArrayToFile(File file, byte[] data, boolean append)throws Exception{
        FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, append);
    }

    public void writeByteArrayToFile(File file, byte[] data, int off, int len)throws Exception{
        FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, off, len);
    }

    public void writeByteArrayToFile(File file, byte[] data, int off, int len, boolean append)throws Exception{
        FileUtils.writeByteArrayToFile(new java.io.File(file.getAbsolutePath()), data, off, len, append);
    }

    public void writeJson(File file, JsonObject jsonObject) throws Exception{
        FileUtils.writeStringToFile(new java.io.File(file.getAbsolutePath()), jsonObject.toString(), StandardCharsets.UTF_8);
    }

}
