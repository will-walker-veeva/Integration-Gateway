package com.veeva.vault.custom.app.model.csv;

import org.apache.commons.csv.CSVFormat;

public class CsvFormat {
    private CSVFormat csvFormat;

    public CsvFormat(CSVFormat csvFormat){
        this.csvFormat = csvFormat;
    }

    public CsvFormat(String delimiter, Character escape, String nullString, String... headers){
        this.csvFormat = CSVFormat.Builder.create().setDelimiter(delimiter).setEscape(escape).setNullString(nullString).setHeader(headers).build();
    }

    public CSVFormat getCsvFormat(){
        return this.csvFormat;
    }

    public static CsvFormat excel(){
        return new CsvFormat(CSVFormat.EXCEL);
    }

    public static CsvFormat defaultCsv(){
        return new CsvFormat(CSVFormat.DEFAULT);
    }

    public static CsvFormat tdf(){
        return new CsvFormat(CSVFormat.TDF);
    }
}
