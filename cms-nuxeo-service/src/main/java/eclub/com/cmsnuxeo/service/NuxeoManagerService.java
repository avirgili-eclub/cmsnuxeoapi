package eclub.com.cmsnuxeo.service;

import eclub.com.cmsnuxeo.dto.ApplicationType;
import eclub.com.cmsnuxeo.dto.DocumentDTO;
import eclub.com.cmsnuxeo.dto.NuxeoDocumentDTO;
import eclub.com.cmsnuxeo.dto.ResponseNuxeo;
import eclub.com.cmsnuxeo.exception.NuxeoManagerException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NuxeoManagerService {

    //TODO: sacar este enpoint y poner privado en la implementacion.
    ResponseNuxeo createDocument(DocumentDTO document, String path) throws Exception;
    ResponseNuxeo newApplication(DocumentDTO document) throws Exception;
    ResponseNuxeo updateDocument(DocumentDTO document);
    ResponseNuxeo deleteDocumentById(String id);
    NuxeoDocumentDTO getDocumentById(String id) throws NuxeoManagerException;
    ResponseNuxeo getDocumentsByTag(List<String> tags);

    DocumentDTO convertDocumentJsonToDTO(String document, List<MultipartFile> files);
}
