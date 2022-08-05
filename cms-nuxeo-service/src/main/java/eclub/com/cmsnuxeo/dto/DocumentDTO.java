package eclub.com.cmsnuxeo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentDTO {
    private String uid;
    private String costumer;
    private String description;
    private List<String> tags;
    public File file;
    public List<File> fileList;

    public String path;

    @JsonAlias("application_eclub")
    private ApplicationEclub applicationEclub;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCostumer() {
        return costumer;
    }

    public void setCostumer(String costumer) {
        this.costumer = costumer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public ApplicationEclub getApplicationEclub() {
        return applicationEclub;
    }

    public void setApplicationEclub(ApplicationEclub applicationEclub) {
        this.applicationEclub = applicationEclub;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
