package eclub.com.cmsnuxeo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileContent {
    public String name;
    @JsonProperty("mime-type")
    public String mimeType;
    public Object encoding;
    public String digestAlgorithm;
    public String digest;
    public String length;
    public String data;

}
