package eclub.com.cmsnuxeo.dto;

import java.util.List;

public class ResponseNuxeo {

    public ResponseNuxeo(){
        super();
        this.setSuccess(true);
    }
    public boolean success;
    public NuxeoDocument nuxeoDocument;
    public List<NuxeoDocument> nuxeoDocuments;
    public SearchNuxeoDocument searchNuxeoDocument;
    public String friendlyErrorMessage;
    public void setSuccess(boolean success) {
        this.success = success;
    }

}
