package eclub.com.cmsnuxeo.dto;

import java.io.File;
import java.util.List;

public class DocumentDTO {
    private String uid;
    private String costumer;
    private String description;
    public File file;
    public List<File> fileList;

    public String path;
//    private String applicationNumber;
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

//    public String getApplicationNumber() {
//        return applicationNumber;
//    }
//
//    public void setApplicationNumber(String applicationNumber) {
//        this.applicationNumber = applicationNumber;
//    }

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
