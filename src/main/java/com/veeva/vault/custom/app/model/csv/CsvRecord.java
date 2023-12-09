package com.veeva.vault.custom.app.model.csv;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CsvRecord {
    private CSVRecord record;

    /**
     * @hidden
     * @param record
     */
    public CsvRecord(CSVRecord record){
        this.record = record;
    }

    public CsvRecord(Map<String, String> data, CsvFormat csvFormat) throws Exception {
        StringBuilder builder = new StringBuilder();
        CSVPrinter csvPrinter = new CSVPrinter(builder, csvFormat.getCsvFormat());
        csvPrinter.printRecord(Arrays.stream(csvFormat.getCsvFormat().getHeader()).map(each -> data.get(each)));
        CSVParser parser = CSVParser.parse(builder.toString(), csvFormat.getCsvFormat());
        this.record = parser.getRecordNumber()>0? parser.getRecords().get(0) : null;
    }

    public String get(int i){
        if(this.record!=null){
            return this.record.get(i);
        }
        return null;
    }

    public Long	getCharacterPosition(){
        if(this.record!=null){
            return this.record.getCharacterPosition();
        }
        return null;
    }

    public Integer size(){
        if(this.record!=null){
            return this.record.size();
        }
        return null;
    }

    public Stream<String> stream(){
        if(this.record!=null){
            return this.record.stream();
        }
        return null;
    }

    public List<String> toList(){
        if(this.record!=null){
            return this.record.stream().toList();
        }
        return null;
    }

    public Map<String,String> toMap(){
        if(this.record!=null){
            return this.record.toMap();
        }
        return null;
    }

    public String toString(){
        if(this.record!=null){
            return this.record.toString();
        }
        return null;
    }

    public String[]	values(){
        if(this.record!=null){
            return this.record.values();
        }
        return null;
    }
}
