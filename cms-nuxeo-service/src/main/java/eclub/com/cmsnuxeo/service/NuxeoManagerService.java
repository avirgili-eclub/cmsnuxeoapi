package eclub.com.cmsnuxeo.service;

import eclub.com.cmsnuxeo.dto.DocumentDTO;
import eclub.com.cmsnuxeo.dto.NuxeoDocumentDTO;
import eclub.com.cmsnuxeo.dto.ResponseNuxeo;
import eclub.com.cmsnuxeo.exception.NuxeoManagerException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface NuxeoManagerService {

    //TODO: sacar este enpoint y poner privado en la implementacion.
    ResponseNuxeo createDocument(List<File> fileList, String path) throws NuxeoManagerException, Exception;
    ResponseNuxeo newOnboarding(DocumentDTO document) throws NuxeoManagerException, Exception;
    ResponseNuxeo updateDocument(DocumentDTO document);
    ResponseNuxeo deleteDocumentById(String id);
}
