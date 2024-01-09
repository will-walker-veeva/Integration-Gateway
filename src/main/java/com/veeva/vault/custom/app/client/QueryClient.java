package com.veeva.vault.custom.app.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veeva.vault.custom.app.admin.ThreadItem;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.json.JsonModel;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.json.JsonProperty;
import com.veeva.vault.custom.app.model.query.QueryModel;
import com.veeva.vault.custom.app.model.query.QueryModelInfo;
import com.veeva.vault.custom.app.model.query.QueryProperty;
import com.veeva.vault.custom.app.model.query.QueryResult;
import com.veeva.vault.custom.app.repository.ThreadRegistry;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.ValidationException;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.validator.SelectValidator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Client for Query-based operations, including registering temporary tables, saving elements to and querying these
 */
@Service
public class QueryClient {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThreadRegistry threadRegistry;
    private JsonClient jsonClient = new JsonClient();
    private ObjectMapper objectMapper;

    protected QueryClient(){
        objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setAnnotationIntrospector(new QueryAnnotationIntrospector());
        objectMapper.registerModule(new JavaTimeModule());
    }
    public <T extends QueryModel> void newTable(Class<T> newClass) throws ProcessException{
        ThreadItem threadItem = threadRegistry.findById(Thread.currentThread().getName()).orElse(null);
        Logger logger = Logger.getLogger(this.getClass());
        QueryModelInfo modelInfo = newClass.getAnnotation(QueryModelInfo.class);
        if(modelInfo==null || modelInfo!=null && modelInfo.name().isBlank()){
            throw new ProcessException("Annotation 'QueryModelInfo' missing or invalid");
        }else if(!modelInfo.name().matches("^[A-Za-z-_0-9]*$")){
            throw new ProcessException("Annotation 'QueryModelInfo' missing or invalid");
        }
        String tableName = modelInfo.name()+"_"+Thread.currentThread().getId();
        List<QueryProperty> queryPropertyList = Arrays.stream(newClass.getDeclaredFields()).map(field -> {
            field.setAccessible(true);
            return field.getAnnotation(QueryProperty.class);
        }).filter(el -> el!=null).sorted(Comparator.comparing(el -> el.key())).collect(Collectors.toList());
        if(queryPropertyList.isEmpty() || queryPropertyList.stream().anyMatch(property -> !property.key().matches("^[A-Za-z-_0-9]*$"))){
            throw new ProcessException("Annotation 'QueryProperty' missing or invalid");
        }
        String sql = "CREATE TEMPORARY TABLE "+tableName+" (id bigint auto_increment, "+ queryPropertyList.stream().map(el -> toCreateTableSqlPropertyType(el)).collect(Collectors.joining(", ")) +");";
        try{
            if(threadItem!=null) {
                logger.info("Executing SQL Statement: {}", sql);
                this.jdbcTemplate.execute(sql);
                threadItem.addTemporaryTable(tableName);
                threadRegistry.save(threadItem);
            }
        }catch (Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    private String toCreateTableSqlPropertyType(QueryProperty property){
        String dataType = "";
        switch(property.type()){
            case TEXT:
                dataType = "VARCHAR";
                break;
            case BOOLEAN:
                dataType = "BOOLEAN";
                break;
            case INTEGER:
                dataType = "INTEGER";
                break;
            case JSON:
                dataType = "JSON";
                break;
            case DOUBLE:
                dataType = "DOUBLE";
                break;
            case LONG_TEXT:
                dataType = "CLOB";
                break;
            case BIG_INTEGER:
                dataType = "BIGINT";
                break;
        }
        return property.key()+" "+dataType;
    }

    public <T extends QueryModel> T save(T entity) throws ProcessException {
        try{
            String statement = entity.getId()!=null? toUpdateSqlStatement(entity) : toCreateSqlStatement(entity);
            Logger.getLogger(this.getClass()).info("Executing SQL statement: {}", statement);
            this.jdbcTemplate.batchUpdate(statement);
        }catch (Exception e){
            throw new ProcessException(e.getMessage());
        }
        return entity;
    }

    public <T extends QueryModel> Collection<T> saveAll(Collection<T> entities) throws ProcessException {
        String[] statements = entities.stream().map(el -> el.getId()!=null? toUpdateSqlStatement(el) : toCreateSqlStatement(el)).collect(Collectors.toList()).toArray(new String[entities.size()]);
        try{
            this.jdbcTemplate.batchUpdate(statements);
        }catch (Exception e){
            throw new ProcessException(e.getMessage());
        }
        return entities;
    }

    public <T extends QueryModel> void delete(T entity) throws ProcessException {
        QueryModelInfo modelInfo = entity.getClass().getAnnotation(QueryModelInfo.class);
        try{
            String tableName = modelInfo.name()+"_"+Thread.currentThread().getId();
            this.jdbcTemplate.execute("DELETE FROM "+tableName+" WHERE id="+entity.getId());
        }catch (Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    private <T extends QueryModel> String toCreateSqlStatement(T entity){
        QueryModelInfo modelInfo = entity.getClass().getAnnotation(QueryModelInfo.class);
        List<String> fields = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        toKeyValuePairs(entity, fields, values);
        String tableName = modelInfo.name()+"_"+Thread.currentThread().getId();
        return "INSERT INTO "+tableName+" ("+String.join(", ", fields)+") VALUES ("+String.join(", ", values)+")";
    }

    private <T extends QueryModel> void toKeyValuePairs(T entity, List<String> fields, List<String> values) {
        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(QueryProperty.class)!=null)
                .sorted(Comparator.comparing(field -> field.getAnnotation(QueryProperty.class).key())).forEach(field -> {
                    QueryProperty propertyInfo = field.getAnnotation(QueryProperty.class);
                    try{
                        field.setAccessible(true);
                        Object obj = field.get(entity);
                        String name = propertyInfo.key();
                        String value = "";
                        if(obj!=null) {
                            switch (propertyInfo.type()) {
                                case TEXT:
                                case LONG_TEXT:
                                    value = "'" + escapeSQL( obj.toString()) + "'";
                                    break;
                                case BOOLEAN:
                                    value = "" + (Boolean) obj;
                                    break;
                                case INTEGER:
                                    value = "" + (Integer) obj;
                                    break;
                                case JSON:
                                    if(obj instanceof JsonModel){
                                        value = "'" + escapeSQL( jsonClient.serializeObject((JsonModel) obj)) + "'";
                                    }else if (obj instanceof Collection){
                                        value = "'" + escapeSQL( jsonClient.serializeObjects((Collection<JsonModel>)obj)) + "'";
                                    }else{
                                        value = "'" + escapeSQL( new ObjectMapper(new JsonFactory()).writeValueAsString(obj)) + "'";
                                    }
                                    break;
                                case DOUBLE:
                                    value = "" + (Double) obj;
                                    break;
                                case BIG_INTEGER:
                                    value = "" + (Long) obj;
                                    break;
                            }
                        }else{
                            value = "NULL";
                        }
                        fields.add(name);
                        values.add(value);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
    }

    private <T extends QueryModel> String toUpdateSqlStatement(T entity) {
        QueryModelInfo modelInfo = entity.getClass().getAnnotation(QueryModelInfo.class);
        List<String> fields = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        toKeyValuePairs(entity, fields, values);
        String tableName = modelInfo.name()+"_"+Thread.currentThread().getId();
        return "UPDATE "+tableName+" SET ("+String.join(", ", fields)+") = ("+String.join(", ", values)+") WHERE id = "+entity.getId()+";";
    }

    public QueryResult<JsonObject> query(String sqlString) throws ProcessException {
        try{
            Logger logger = Logger.getLogger(this.getClass());
            String transformedStatement = validateStatement(sqlString);
            logger.info("Executing SQL statement: {}", transformedStatement);
            List<Map<String,Object>> result = this.jdbcTemplate.queryForList(transformedStatement);
            return new QueryResult<JsonObject>(result.stream().map(each -> new JsonObject(each)).collect(Collectors.toList()));
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    public <T extends QueryModel> QueryResult<T> query(String sqlString, Class<T> className) throws ProcessException {
        try{
            Logger logger = Logger.getLogger(this.getClass());
            String transformedStatement = validateStatement(sqlString);
            logger.info("Executing SQL statement: {}", transformedStatement);
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, className);
            List<Map<String,Object>> result = this.jdbcTemplate.queryForList(transformedStatement);
            logger.debug("SQL Results: {}", result);
            String jsonString = new ObjectMapper().writeValueAsString(result);
            return new QueryResult<T>(objectMapper.readValue(jsonString, type));
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    private String validateStatement(String sqlString) throws ProcessException {
        try{
            Statement statement = CCJSqlParserUtil.parse(sqlString);
            if(statement instanceof Select){
                PlainSelect plainSelect = ((Select) statement).getPlainSelect();
                ValidationContext context = Validation.createValidationContext(new FeatureConfiguration(), Arrays.asList(DatabaseType.H2));
                CustomSelectValidator selectValidator = new CustomSelectValidator();
                selectValidator.setContext(context);
                plainSelect.getSelectItems().stream().forEach(selectItem -> selectValidator.validate(selectItem));
                if(selectValidator.isValid()){
                    StringBuilder buffer = new StringBuilder();
                    ExpressionDeParser expressionDeParser = new ExpressionDeParser();
                    SelectDeParser deparser = new CustomSelectVisitor(expressionDeParser,buffer);
                    expressionDeParser.setSelectVisitor(deparser);
                    expressionDeParser.setBuffer(buffer);
                    plainSelect.accept(deparser);
                    return buffer.toString();
                }else{
                    ValidationException t = selectValidator.getValidationErrors().values().stream().flatMap(each -> each.stream()).findFirst().orElse(null);
                    if(t!=null){
                        throw t;
                    }else{
                        throw new Exception("Unknown during sql validation");
                    }
                }
            }else{
                throw new Exception("Only select statements may be used");
            }
        }catch(Exception e){
            e.printStackTrace();
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     * @hidden
     */
    public void dropTables(){
        ThreadItem threadItem = threadRegistry.findById(Thread.currentThread().getName()).orElse(null);
        if(threadItem!=null){
            List<String> tables = threadItem.getTemporaryTables();
            tables.forEach(table -> {
                jdbcTemplate.execute("DROP TABLE  IF EXISTS "+table);
            });
            threadItem.clearTemporaryTables();
            threadRegistry.save(threadItem);
        }
    }

    private static class CustomSelectValidator extends SelectValidator {
        private static final List<String> FORBIDDEN_TABLES = Arrays.asList("CACHE_CONTEXT", "PROCESSOR", "SCRIPT_LIBRARY", "SESSION", "THREAD_ITEM", "THREAD_ITEM_TEMPORARY_TABLES", "VaultConfigurationRecordCache", "WhitelistedElement");
        public CustomSelectValidator(){
            super();
        }
        @Override
        public void visit(Table table){
            String matchedTableName = FORBIDDEN_TABLES.stream().filter(el -> el.toLowerCase().toString().equals(table.getName().toLowerCase())).findFirst().orElse(null);
            if(matchedTableName!=null) {
                putError(FeaturesAllowed.SELECT, new ValidationException("Select against "+matchedTableName+" is forbidden"));
            }
        }
    }

    private static class CustomSelectVisitor extends SelectDeParser {
        public CustomSelectVisitor(ExpressionDeParser expressionDeParser, StringBuilder buffer) {
            super(expressionDeParser, buffer);
        }

        @Override
        public void visit(Table tableName) {
            getBuffer().append(tableName.getName()+"_"+Thread.currentThread().getId());
            if(tableName.getAlias()!=null) getBuffer().append(' ').append(tableName.getAlias().getName());
        }
    }

    public static String escapeSQL(String s){
        int length = s.length();
        int newLength = length;
        // first check for characters that might
        // be dangerous and calculate a length
        // of the string that has escapes.
        for (int i=0; i<length; i++){
            char c = s.charAt(i);
            switch(c){
                case '\\':
                case '\"':
                case '\'':
                case '\0':{
                    newLength += 1;
                } break;
            }
        }
        if (length == newLength){
            // nothing to escape in the string
            return s;
        }
        StringBuffer sb = new StringBuffer(newLength);
        for (int i=0; i<length; i++){
            char c = s.charAt(i);
            switch(c){
                case '\\':{
                    sb.append("\\\\");
                } break;
                case '\"':{
                    sb.append("\\\"");
                } break;
                case '\'':{
                    sb.append("\\\'");
                } break;
                case '\0':{
                    sb.append("\\0");
                } break;
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }



    private static class QueryAnnotationIntrospector extends JacksonAnnotationIntrospector {

        @Override
        public PropertyName findNameForDeserialization(Annotated a) {
            QueryProperty property = a.getAnnotation(QueryProperty.class);
            JsonProperty jsonProperty = a.getAnnotation(JsonProperty.class);
            if (property!=null){
                return PropertyName.construct(property.key().toUpperCase());
            } else if (jsonProperty!=null){
                return PropertyName.construct(jsonProperty.key().toUpperCase());
            } else {
                return PropertyName.USE_DEFAULT;
            }
        }

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            if(m instanceof AnnotatedField && m.getAnnotation(QueryProperty.class) == null && m.getAnnotation(JsonProperty.class) == null){
                return true;
            }
            return false;
        }
    }

}
