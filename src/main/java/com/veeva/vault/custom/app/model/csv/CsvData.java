package com.veeva.vault.custom.app.model.csv;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsvData {
    private List<CsvRecord> records;
    private CsvFormat format;
    private List<String> headers;

    public CsvData(CsvFormat csvFormat, List<CsvRecord> records){
        this.format = csvFormat;
        this.records = records;
        this.headers = Arrays.asList(csvFormat.getCsvFormat().getHeader());
    }

    public CsvData(CsvFormat csvFormat, String data) throws Exception{
        Reader in = new StringReader(data);
        CSVParser parser = csvFormat.getCsvFormat().parse(in);
        this.format = csvFormat;
        this.records = parser.stream().map(each -> new CsvRecord(each)).collect(Collectors.toList());
        this.headers = Arrays.asList(csvFormat.getCsvFormat().getHeader());
    }

    public List<CsvRecord> getRecords() {
        return records;
    }

    public CsvFormat getFormat() {
        return format;
    }

    public List<String> getHeaders(){
        return this.headers;
    }

    @Override
    public String toString(){
        StringWriter writer = new StringWriter();
        try {
            CSVPrinter printer = new CSVPrinter(writer, this.format.getCsvFormat());
            printer.printRecords(this.records);
            printer.close();
            return writer.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
