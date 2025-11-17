package org.example;

//if JSON has somthing not found in java file: ignore it and dont throww error
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)

public class ReadJSON {
    static class Mapping {
        String mongoDoc;
        String psqlCol;
        String type;
        Object sourceValue;

        public String getMongoDoc(){return mongoDoc;}
        public String getPsqlCol(){return psqlCol;}
        public String getType(){return type;}
        public Object getSourceValue(){return sourceValue;}

        public void setMongoDoc(String mongoDoc){this.mongoDoc = mongoDoc;}
        public void setPsqlCol(String psqlCol){this.psqlCol = psqlCol;}
        public void setType(String type){this.type = type;}
        public void setSourceValue(Object sourceValue){this.sourceValue=sourceValue;}
    };
    //        JSON Configuration
    String source;
    String destination;
    String collectionName;
    String tableName;
    String cronSchedule;
//array of objects
    Mapping[] mappings ;



    public String  getSource(){
        return source;
    }
    public void setSource(String source){
        this.source = source;
    }
    public String getDestination(){
        return destination;
    }
    public void setDestination(String destination){
        this.destination = destination;
    }

    public String  getCollectionName(){
        return collectionName;
    }
    public void setCollectionName(String collectionName){
        this.collectionName = collectionName;
    }

    public String getTableName(){
        return tableName;
    }
    public void setTableName(String tableName){
        this.tableName = tableName;
    }

    public String getCronSchedule(){
        return cronSchedule;
    }
    public void setCronSchedule(String cronSchedule){
        this.cronSchedule = cronSchedule;
    }

    public Mapping[] getMappings() {
        return mappings;
    }
    public void setMappings(Mapping[] mappings) {
        this.mappings = mappings;
    }
}
