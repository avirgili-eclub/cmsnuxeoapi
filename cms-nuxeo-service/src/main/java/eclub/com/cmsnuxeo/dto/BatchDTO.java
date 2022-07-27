package eclub.com.cmsnuxeo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BatchDTO {
    public boolean uploaded;
    public String fileIdx;
    @JsonProperty("uploadedSize")
    private String size;
    public String uploadedSize;
    public String name;
    private String uploadType;
    public String batchId;
}
