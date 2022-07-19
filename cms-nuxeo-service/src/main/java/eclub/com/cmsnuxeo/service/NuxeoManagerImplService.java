package eclub.com.cmsnuxeo.service;

import eclub.com.cmsnuxeo.exception.NuxeoManagerException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import eclub.com.cmsnuxeo.dto.*;

import java.io.File;
import java.util.*;

@Service
public class NuxeoManagerImplService implements NuxeoManagerService {

    @Autowired
    private RestTemplate restTemplate;
    final static Logger logger = LoggerFactory.getLogger(NuxeoManagerImplService.class);
    @Value("${nuxeo.api.url}")
    private String url;

    @Value("${nuxeo.api.username}")
    private String user;

    @Value("${nuxeo.api.password}")
    private String password;

    @Override
    public ResponseNuxeo createDocument(List<File> documents, String path) throws NuxeoManagerException, ParseException {

        try {
            String batchId = createBatchId();

            List<BatchDTO> batchDocuments = new ArrayList<>();
            documents.forEach(o -> {
                BatchDTO batchDocument = uploadDocument(batchId);

                logger.info(batchDocument.batchId);
                
                batchDocuments.add(batchDocument);
            });

            List<BatchDTO> batchList = verifiedBatch(batchId);

            if (batchList.size() < 1) {
                ResponseNuxeo responseNuxeo = new ResponseNuxeo();
                responseNuxeo.success = false;
                responseNuxeo.friendlyErrorMessage = "Fallo la subida de archivo.";

                return responseNuxeo;
            }

            batchList.forEach(batchDTO -> createDocumentWithBatchAndPath(batchDTO.batchId, batchDTO.fileIdx, path));

            ResponseNuxeo result = new ResponseNuxeo();
            result.success = true;

            return result;

        }catch (Exception e) {
               logger.info(e.getStackTrace().toString());
               return null;
        }
    }

    @Override
    public ResponseNuxeo newOnboarding(DocumentDTO document) throws NuxeoManagerException, Exception {

         //TODO: check if directory exists first before create it to avoid duplicate.
         NuxeoDocumentDTO parentFolder = createFolderWithParentId("14814521-2052-4c3b-824f-fc09b9331a4d", document.name);

         NuxeoDocumentDTO solicitudFolder =  createFolderWithParentId(parentFolder.uid, UUID.randomUUID().toString());

         ResponseNuxeo result = createDocument(document.fileList, solicitudFolder.path);

        if (!result.success) {
            deleteDocumentById(solicitudFolder.uid);
        }
        return null;
    }

    //metodos de alcantarilla
    private NuxeoDocumentDTO createFolderWithParentId(String id, String name){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("entity-type", "document");
        jsonBody.put("type", "Folder");
        jsonBody.put("name", name);

        //TODO: create private method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Nuxeo-Transaction-Timeout","3");
        headers.set("X-NXproperties","*");
        headers.set("X-NXRepository","default");
        
        HttpEntity<?> entity = new HttpEntity<>(jsonBody, headers);

        String token = restTemplate.exchange(url+"/id/"+id, HttpMethod.POST, entity, String.class).getBody();
        
        logger.info("Headers params {} ",headers);
        logger.info("Body params {} ",jsonBody);

        return null;
    }
    private String createBatchId() throws ParseException {
        //Map<String, String> bodyParams = new HashMap<>();
        //bodyParams.put("idEntidad", idEntidad);
        //bodyParams.put("usuario", usuario);
        //bodyParams.put("clave", clave);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        //headers.set("api-key", apikey);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        //logger.info("Headers params {} ",headers);
        //logger.info("Body params {} ",bodyParams);
        String token = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        //logger.info("Token result {} ", token);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(token);
        String textoToken = (String) jsonObject.get("token");
        //logger.info("TextoToken result {} ", textoToken);
        return textoToken;
}

    private BatchDTO uploadDocument(String batchId) {
        return null;
    }

    private List<BatchDTO> verifiedBatch(String batchId) {
        return null;
    }

    private NuxeoDocumentDTO createDocumentWithBatchAndPath(String batchId, int fileIndex, String path) {
        return null;
    }

    private String deleteDocumentById(String id){
        return null;
    }

    private HttpHeaders createHttpHeaders(String user, String password)
    {
        String notEncoded = user + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(notEncoded.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}
