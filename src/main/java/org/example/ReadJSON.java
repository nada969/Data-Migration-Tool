package org.example;

public class ReadJSON {
    //        JSON Configuration
    String source;
    String destination;
    String collectionName;
    String tableName;
    String cronSchedule;
    Object mappings;

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

    public Object getMappings() {
        return mappings;
    }
    public void setMappings(Object mappings) {
        this.mappings = mappings;
    }
}
