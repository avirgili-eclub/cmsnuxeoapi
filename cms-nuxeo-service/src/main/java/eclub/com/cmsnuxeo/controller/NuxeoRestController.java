package eclub.com.cmsnuxeo.controller;

import eclub.com.cmsnuxeo.dto.*;
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


    @RequestMapping(value = "/newonboading", method = RequestMethod.POST, consumes = {MediaType.ALL_VALUE})
    public @ResponseBody ResponseEntity<?> newOnboarding(@RequestParam String costumer,
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
                    throw new RuntimeException(e);
                }
            });

            ResponseNuxeo response = service.newOnboarding(docu);
            //docu.setPath(response.nuxeoDocument.path);
            //docu.setUid(response.nuxeoDocument.uid);
            logger.debug("Result response {} ", response);

            return ResponseEntity.ok(response);
            //return ResponseEntity.ok(docu);

        } catch (Exception e) {
            logger.error("Error Ocurred: ", e.getMessage());
            ResponseEntity<?> responseError = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            return responseError;
        }
    }

    @RequestMapping(value = "/updatedocument", method = RequestMethod.PUT, consumes = {MediaType.ALL_VALUE})
    public @ResponseBody ResponseEntity<ResponseNuxeo> updateDocument(@RequestParam String name,
                                                                      @RequestParam String description,
                                                                      @RequestParam String uid,
                                                                      @RequestPart MultipartFile file) throws IOException {
        DocumentDTO document = new DocumentDTO();
        document.setCostumer(name);
        document.setUid(uid);
        document.setDescription(description);
        document.setFile(multipartToFile(file, name));

        ResponseNuxeo result = service.updateDocument(document);
        return ResponseEntity.ok(result);

    }

    public static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + fileName);
        multipart.transferTo(convFile);
        return convFile;
    }
}
