package org.example.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadJSON {

//  using record : because this class only carry vars....
    public record Mapping(String mongoKey, String psqlCol ,String type) { };

    private String source;
    private String destination;
    private String urlDestination;
    private String destinationPassWord;
    private String collectionName;
    private String tableName;
    private String cronSchedule;
    private List<Mapping> mappings;


}
