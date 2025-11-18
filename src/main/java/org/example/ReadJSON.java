package org.example;

//if JSON has somthing not found in java file: ignore it and dont throww error
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)

public class ReadJSON {
    static class Mapping {
        String mongoKey;
        String psqlCol;
        String type;

        public String getMongoKey(){return mongoKey;}
        public String getPsqlCol(){return psqlCol;}
        public String getType(){return type;}

        public void setMongoKey(String mongoKey){this.mongoKey = mongoKey;}
        public void setPsqlCol(String psqlCol){this.psqlCol = psqlCol;}
        public void setType(String type){this.type = type;}
    };
    //        JSON Configuration
    String source;
    String destination;
    String urlDestination;
    String destinationPassWord;
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
    public String getUrlDestination(){
        return urlDestination;
    }
    public void setUrlDestination(String urlDestination){
        this.urlDestination = urlDestination;
    }
    public String getDestinationPassWord(){
        return destinationPassWord;
    }
    public void setDestinationPassWord(String destinationPassWord){
        this.destinationPassWord = destinationPassWord;
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
