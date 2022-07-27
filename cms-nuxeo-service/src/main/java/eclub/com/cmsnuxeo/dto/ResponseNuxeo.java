package eclub.com.cmsnuxeo.dto;

import java.util.List;

public class ResponseNuxeo {

    public ResponseNuxeo(){
        super();
        this.setSuccess(true);
    }
    public boolean success;
    public NuxeoDocumentDTO nuxeoDocument;
    public List<NuxeoDocumentDTO> nuxeoDocuments;
    public String friendlyErrorMessage;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
