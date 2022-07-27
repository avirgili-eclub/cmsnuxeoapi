package eclub.com.cmsnuxeo.service;

import eclub.com.cmsnuxeo.dto.DocumentDTO;
import eclub.com.cmsnuxeo.dto.ResponseNuxeo;
import eclub.com.cmsnuxeo.exception.NuxeoManagerException;

import java.io.File;
import java.util.List;

public interface NuxeoManagerService {

    ResponseNuxeo createDocument(List<File> fileList, String path) throws NuxeoManagerException, Exception;
    ResponseNuxeo newOnboarding(DocumentDTO document) throws NuxeoManagerException, Exception;
}
