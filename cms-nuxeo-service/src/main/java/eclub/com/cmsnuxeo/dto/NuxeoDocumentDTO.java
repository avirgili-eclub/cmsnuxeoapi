package eclub.com.cmsnuxeo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
public class NuxeoDocumentDTO {
    @JsonProperty("entity-type")
    public String entityType;
    public String repository;
    public String uid;
    public String path;
    public String type;
    public String state;
    public String parentRef;
    public boolean isCheckedOut;
    public boolean isVersion;
    public String changeToken;
    public boolean isTrashed;
    public String title;
    public Date lastModified;
    public Properties properties;
    public ArrayList<String> facets;
    public ArrayList<Schema> schemas;
}
