package org.example.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadJSON {

//    @Getter
//    @Setter
//    public static class Mapping {
//        private String mongoKey;
//        private String psqlCol;
//        private String type;
//    }

//  using record : because this class only carry vars....
    record Mapping(String mongoKey, String psqlCol ,String type) { };

    private String source;
    private String destination;
    private String urlDestination;
    private String destinationPassWord;
    private String collectionName;
    private String tableName;
    private String cronSchedule;
    private Mapping[] mappings;
}
