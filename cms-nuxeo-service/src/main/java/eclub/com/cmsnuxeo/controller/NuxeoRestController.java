package eclub.com.cmsnuxeo.controller;

import eclub.com.cmsnuxeo.dto.*;
import eclub.com.cmsnuxeo.exception.NuxeoManagerException;
import eclub.com.cmsnuxeo.service.NuxeoManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/nuxeo")
public class NuxeoRestController {

    private final Environment env;
    private final NuxeoManagerService service;

    final static Logger logger = LoggerFactory.getLogger(NuxeoRestController.class);

    public NuxeoRestController(Environment env, NuxeoManagerService service) {
        this.env = env;
        this.service = service;
    }

    @Value("${configuracion.ambiente}")
    private String config;


    @GetMapping("/get-config")
    public ResponseEntity getConfig() {
        System.out.println("get-config invocado");
        Map<String, String> map = new HashMap<>();
        map.put("config", config);
        return ResponseEntity.ok(map);
    }

    @RequestMapping(value={"/onboarding/new", "/onboarding/approve"}, method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody ResponseEntity<?> newApplication(@RequestParam String costumer,
                                                          @RequestParam String applicationNumber,
                                                          @RequestParam int applicationType,
                                                          @RequestPart List<MultipartFile> files) {
        try {

            ApplicationEclub applicationEclub = new ApplicationEclub();
            applicationEclub.setApplicationNumber(applicationNumber);
            applicationEclub.setApplicationType(ApplicationType.getApplicationType(applicationType));

            DocumentDTO docu = new DocumentDTO();
            docu.setCostumer(costumer);
            docu.setApplicationEclub(applicationEclub);

            docu.fileList = new ArrayList<>();

            files.forEach(multipartFile -> {
                try {
                    docu.fileList.add(multipartToFile(multipartFile, multipartFile.getOriginalFilename()));
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            });

            ResponseNuxeo response = service.newApplication(docu, ApplicationType.getApplicationType(applicationType));
            logger.debug("Result response {} ", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error Occurred: ", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.I_AM_A_TEAPOT);
        }
    }

    @RequestMapping(value = "/update/document", method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody ResponseEntity<ResponseNuxeo> updateDocument(@RequestParam(required = false) String name,
                                                                      @RequestParam(required = false) String description,
                                                                      @RequestParam String uid,
                                                                      @RequestPart(required = false) MultipartFile file) throws IOException {
        try {
            NuxeoDocumentDTO nuxeoDocument = service.getDocumentById(uid);

            DocumentDTO document = new DocumentDTO();
            document.setUid(uid);

            if(name != null){
                document.setCostumer(name);
            }else{
                document.setCostumer(nuxeoDocument.title);
            }
            if(description != null){
                document.setDescription(description);
            }else{
                document.setDescription(nuxeoDocument.properties.dcDescription.toString());
            }
            if (file != null){
                if(name != null){
                    document.setFile(multipartToFile(file, name));
                }else{
                    document.setFile(multipartToFile(file, nuxeoDocument.title));
                }
            }
            ResponseNuxeo result = service.updateDocument(document);

            return ResponseEntity.ok(result);

        } catch (NuxeoManagerException e) {
            logger.error(e.getMessage());
            logger.error(e.getStackTrace().toString());
            ResponseNuxeo result = new ResponseNuxeo();
            result.success = false;
            result.friendlyErrorMessage = "Docummento no encontrado";
            return ResponseEntity.ok(result);
        }
    }


    public static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + fileName);
        multipart.transferTo(convFile);
        return convFile;
    }
}
