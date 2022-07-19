package eclub.com.cmsnuxeo.dto;

import java.util.List;

public class ResponseNuxeo {
    public boolean success;
    public NuxeoDocumentDTO nuxeoDocument;
    public List<NuxeoDocumentDTO> nuxeoDocuments;
    public String friendlyErrorMessage;

}
