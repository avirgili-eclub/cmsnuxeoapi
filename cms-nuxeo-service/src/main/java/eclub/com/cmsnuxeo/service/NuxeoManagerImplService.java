package eclub.com.cmsnuxeo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eclub.com.cmsnuxeo.exception.NuxeoManagerException;


import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import eclub.com.cmsnuxeo.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.*;

import java.io.*;
import java.util.stream.Collectors;


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

    public static final String MAX_TRANSFER_SIZE = "4294967295";


    @Override
    public ResponseNuxeo createDocument(DocumentDTO document, String path) throws NuxeoManagerException, ParseException {
        ResponseNuxeo responseNuxeo = new ResponseNuxeo();
        responseNuxeo.nuxeoDocuments = new ArrayList<>();
        for (File file : document.fileList) {
            try {
                String batchId = createBatchId();

                if (batchId == null || batchId.isEmpty()) {
                    responseNuxeo.success = false;
                    responseNuxeo.friendlyErrorMessage = "Error al crear el batch.";
                    return responseNuxeo;
                }

                BatchDTO batchDocument = uploadDocument(file, batchId, 0/*fileList.indexOf(file)*/);
                batchDocument.name = file.getName();

                boolean batchUploaded = verifiedBatch(batchDocument.batchId);
                if (!batchUploaded) {
                    logger.info("Upload failed for file: " + batchDocument.name);
                    responseNuxeo.success = false;
                    responseNuxeo.friendlyErrorMessage = "Fallo la subida de archivo.";
                    break;
                }
                logger.info(batchDocument.batchId);

                NuxeoDocument nuxeoDocument = createNuxeoDocumentWithPath(batchDocument, path, document.getTags());
                //TODO: Optimizar la creacion de versionado del documento.
                createVersioningDocument(nuxeoDocument.uid, "");
                responseNuxeo.nuxeoDocuments.add(nuxeoDocument);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (responseNuxeo.nuxeoDocuments.size() < 1) {
            responseNuxeo.success = false;
            responseNuxeo.friendlyErrorMessage = "No se pudo crear el documento.";
        }

        return responseNuxeo;

    }

    @Override
    public ResponseNuxeo newApplication(DocumentDTO document) throws NuxeoManagerException, Exception {
        String applicationType = document.getApplicationEclub().getApplicationType().name();
        //get the application folder (onboarding, expedient, etc).
        //This folder must be created inside nuxeo by an admin
        NuxeoDocument applicationFolder = getDocumentByPath(applicationType);
        if (applicationFolder == null) {
            //TODO: return a response nuxeo with an error number.
            return null;
        }
        //first check if directory exists before create it to avoid duplicate.
        NuxeoDocument clientFolder = getDocumentByPath(applicationFolder.title + "/" + document.getCostumer());

        if (clientFolder == null) {
            ResponseNuxeo result = createFolderWithParentId(applicationFolder.uid, document.getCostumer());
            clientFolder = result.nuxeoDocument;
            if (!result.success)
                return result;
        }
        //create child folder inside parent (numberForm folder)
        ResponseNuxeo formFolder = createFolderWithParentId(clientFolder.uid, document.getApplicationEclub().getApplicationNumber());
        if (!formFolder.success)
            return formFolder;

        ResponseNuxeo result = createDocument(document, formFolder.nuxeoDocument.path);

        //TODO: test this scenario
        if (!result.success) {
            deleteDocumentByUid(formFolder.nuxeoDocument.uid);
        }

        return formFolder;
    }

    @Override
    public ResponseNuxeo updateDocument(DocumentDTO document) {

        ResponseNuxeo responseNuxeo = new ResponseNuxeo();
        ResponseEntity<NuxeoDocument> responseEntity;
        JSONObject content = new JSONObject();
        try {
            if (document.file != null) {
                String batchId = createBatchId();
                if (batchId == null || batchId.isEmpty()) {
                    responseNuxeo.success = false;
                    responseNuxeo.friendlyErrorMessage = "Error al crear el batch.";
                    logger.info(responseNuxeo.friendlyErrorMessage);
                    return responseNuxeo;
                }

                BatchDTO batchDocument = uploadDocument(document.file, batchId, 0/*fileList.indexOf(file)*/);
                batchDocument.name = document.file.getName();

                boolean batchUploaded = verifiedBatch(batchDocument.batchId);

                if (!batchUploaded) {
                    logger.info("Upload failed for file: " + batchDocument.name);
                    responseNuxeo.success = false;
                    responseNuxeo.friendlyErrorMessage = "Fallo la subida de archivo.";
                    return responseNuxeo;
                }

                logger.info(batchDocument.batchId);

                content.put("upload-batch", batchDocument.batchId);
                content.put("upload-fileId", batchDocument.fileIdx);
            }

            JSONObject properties = new JSONObject();
            properties.put("dc:title", document.getCostumer());
            if (document.file != null)
                properties.put("file:content", content);
            properties.put("dc:description", document.getDescription());
            //TODO: Agregar actualizacion de attachments al documento.
            //properties.put("files:files",files);

            JSONObject body = new JSONObject();
            body.put("entity-type", "document");
            body.put("uid", document.getUid());
            body.put("repository", "default");
            body.put("properties", properties);

            //TODO: improve method of "createHttpHeaders"
            HttpHeaders headers = createHttpHeaders(user, password);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("Nuxeo-Transaction-Timeout", "3");
            headers.set("X-NXproperties", "*");
            headers.set("X-NXRepository", "default");
            headers.set("X-Versioning-Option", "minor");

            HttpEntity<?> entity = new HttpEntity<>(body, headers);

            responseEntity = restTemplate.exchange(url + "/id/" + document.getUid(), HttpMethod.PUT, entity, NuxeoDocument.class);

            responseNuxeo.nuxeoDocument = responseEntity.getBody();
            responseNuxeo.success = true;

            logger.info("Headers params {} ", headers);
            logger.info("Body params {} ", body);

            return responseNuxeo;

        } catch (ParseException | IOException e) {
            logger.error("Message: ", e.getMessage());
            logger.error("Cause: ", e.getCause());
            responseNuxeo.friendlyErrorMessage = "Ocurrio un error al intentar actualizar el documento.";
            responseNuxeo.success = false;
            return responseNuxeo;
        }
    }

    //region metodos de alcantarilla
    private ResponseNuxeo createFolderWithParentId(String id, String name) {
        ResponseNuxeo response = new ResponseNuxeo();

        ResponseEntity<NuxeoDocument> responseEntity;

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("entity-type", "document");
        jsonBody.put("type", "Folder");
        jsonBody.put("name", name);

        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM,
                MediaType.MULTIPART_FORM_DATA, MediaType.IMAGE_JPEG));
        headers.set("Nuxeo-Transaction-Timeout", "3");
        headers.set("X-NXproperties", "*");
        headers.set("X-NXRepository", "default");

        HttpEntity<?> entity = new HttpEntity<>(jsonBody, headers);

        responseEntity = restTemplate.exchange(url + "/id/" + id, HttpMethod.POST, entity, NuxeoDocument.class);

        response.nuxeoDocument = responseEntity.getBody();
        response.success = true;

        logger.info("Headers params {} ", headers);
        logger.info("Body params {} ", jsonBody);

        return response;
    }

    private String createBatchId() throws ParseException {

        ResponseEntity<BatchDTO> responseEntity;

        HttpHeaders headers = createHttpHeaders(user, password);
        //Para agregar mas de un mediaType usar AArrays.asList(MediaType.APPLICATION_JSON)
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        responseEntity = restTemplate.exchange(url + "/upload/", HttpMethod.POST, entity, BatchDTO.class);

        //TODO: controlar nulo.
        return responseEntity.getBody().batchId;
    }

    private BatchDTO uploadDocument(File file, String batchId, int batchIdx) throws IOException {

        ResponseEntity<BatchDTO> responseEntity;

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        FileSystemResource fileToSend = new FileSystemResource(file);
        String mimeType = Files.probeContentType(fileToSend.getFile().toPath());
        byte[] data = IOUtils.toByteArray(fileToSend.getInputStream());
        map.add("file", data);

        //region header creation
        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));

        headers.set("X-File-Name", file.getName());
        headers.setContentLength(file.length());
        //headers.set("X-File-Type", fileBlob.getMimeType());
        headers.set("X-File-Type", mimeType);
        //endregion


        HttpEntity<byte[]> requestEntity = new HttpEntity<>(data, headers);
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

        try {

            responseEntity = restTemplate.postForEntity(url + "/upload/" + batchId + "/" + batchIdx, requestEntity,
                    BatchDTO.class);
        } catch (RestClientException ex) {
            logger.error("Message: ", ex.getMessage());
            logger.error("Cause: ", ex.getCause());
            logger.error("StackTrace: ", Arrays.asList(ex.getStackTrace())
                    .stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining("\n")));
            //TODO: remove throw an return a response nuxeo instead of batchDto.
            throw new RuntimeException(ex);
        }
        logger.info("Headers params {} ", headers);
        logger.info("Body params {} ", responseEntity.getBody());
        return responseEntity.getBody();
    }

    private boolean verifiedBatch(String batchId) {
        boolean success = false;
        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url + "/upload/" + batchId, HttpMethod.GET, entity,
                String.class);

        logger.info("Headers params {} ", headers);

        if (response.getStatusCodeValue() == 200)
            success = true;

        return success;

    }

    private NuxeoDocument createNuxeoDocumentWithPath(BatchDTO batch, String path, List<String> tags) {

        JSONObject content = new JSONObject();
        content.put("upload-batch", batch.batchId);
        content.put("upload-fileId", batch.fileIdx);

        JSONArray jsonArrayTags = new JSONArray();
        tags.forEach(value -> {
            JSONObject tag = new JSONObject();
            tag.put("label", value.toLowerCase());
            //TODO: valor traer de archivo .env
            tag.put("username", "devnull@nuxeo.com");

            jsonArrayTags.add(tag);
        });
//        JSONObject file = new JSONObject();
//        file.put("upload-batch",batch.batchId);
//        file.put("upload-fileId",batch.fileIdx);

//        JSONArray files = new JSONArray();
//        files.add(file);

        JSONObject properties = new JSONObject();
        properties.put("dc:title", batch.name);
        properties.put("file:content", content);
        properties.put("nxtag:tags", jsonArrayTags);

        //TODO: agregar attachments.
//        properties.put("files:files",files);

        JSONObject body = new JSONObject();
        body.put("entity-type", "document");
        body.put("type", "File");
        body.put("name", batch.name);
        body.put("repository", "default");
//        body.put("isCheckedOut", "false");
        body.put("properties", properties);

        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);

        headers.setAccept(Arrays.asList(MediaType.ALL));
        headers.setContentLength(Long.parseLong(MAX_TRANSFER_SIZE));
        headers.set("Nuxeo-Transaction-Timeout", "10");
        headers.set("X-NXproperties", "*");
        headers.set("X-NXRepository", "default");

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        ResponseEntity<NuxeoDocument> response = restTemplate.exchange(url + "/path" + path, HttpMethod.POST, entity, NuxeoDocument.class);

        logger.info("Headers params {} ", headers);
        logger.info("Body params {} ", body);

        return response.getBody();
    }

    //TODO: Implement deleteDocumentByUid
    public ResponseNuxeo deleteDocumentByUid(String uid) {
        return null;
    }

    @Override
    public NuxeoDocument getDocumentById(String id) throws NuxeoManagerException {
        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.set("X-NXproperties", "*");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<NuxeoDocument> response = null;
        try {
            response = restTemplate.exchange(url + "/id/" + id, HttpMethod.GET, entity,
                    NuxeoDocument.class);

        } catch (RestClientException e) {
            //Si el codigo devuelto es not_found se retorna null para crear el elemento, no se considera error.
            if (((HttpClientErrorException.NotFound) e).getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            logger.error("Message: ", e.getMessage());
            logger.error("Cause: ", e.getCause());
            logger.error("StackTrace: ", Arrays.asList(e.getStackTrace())
                    .stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining("\n")));
            throw new NuxeoManagerException(e.getMessage(), e.getCause());
        }
        logger.info("Headers params {} ", headers);
        logger.info("Response: {}", response);

        if (response.getStatusCodeValue() == 200)
            return response.getBody();
        //TODO: check this.
        return null;
    }

    @Override
    public ResponseNuxeo getDocumentsByTag(List<String> tags) {
        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.set("X-NXproperties", "*");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<SearchNuxeoDocument> response;
        //Se elimina todos los espacios en blanco.
        tags.replaceAll(tag -> tag.replaceAll("\\s",""));

        if (tags.size() > 1){
            final String[] query = {url};
            query[0] += "/search/lang/NXQL/execute?query=SELECT * FROM Document WHERE ecm:tag IN ( ";
            tags.forEach(x -> query[0] += "'" + x + "'" + ", ");
            query[0] = query[0].substring(0, query[0].length() -2);
            query[0] += " )" + " and ecm:isLatestVersion = 1";
            response = restTemplate.exchange(query[0], HttpMethod.GET, entity,
                    SearchNuxeoDocument.class);
        }else {
            response = restTemplate.exchange(url + "/search/lang/NXQL/execute?query=SELECT * FROM Document WHERE ecm:tag = "+ tags.get(0) + " and ecm:isLatestVersion = 1", HttpMethod.GET, entity,
                    SearchNuxeoDocument.class);
        }
        logger.info("Headers params {} ", headers);

        ResponseNuxeo result = new ResponseNuxeo();
        result.success = true;
        result.searchNuxeoDocument = response.getBody();

        return result;
    }

    @Override
    public DocumentDTO convertDocumentJsonToDTO(String document, List<MultipartFile> files) {
        try {
            DocumentDTO documentObjet;
            ObjectMapper mapper = new ObjectMapper();
            documentObjet = mapper.readValue(document, DocumentDTO.class);
            documentObjet.fileList = new ArrayList<>();
            files.forEach(multipartFile -> {
                try {
                    documentObjet.fileList.add(multipartToFile(multipartFile, multipartFile.getOriginalFilename()));
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            });
            return documentObjet;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders createHttpHeaders(String user, String password) {
        String notEncoded = user + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(notEncoded.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    private NuxeoDocument getDocumentByPath(String path) throws NuxeoManagerException {

        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.set("X-NXproperties", "*");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<NuxeoDocument> response = null;
        try {
            response = restTemplate.exchange(url + "/path/default-domain/workspaces/EClub/" + path, HttpMethod.GET, entity,
                    NuxeoDocument.class);

        } catch (RestClientException e) {
            if (((HttpClientErrorException.NotFound) e).getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new NuxeoManagerException(e.getMessage(), e.getCause());
        }
        logger.info("Headers params {} ", headers);

        if (response.getStatusCodeValue() == 200)
            return response.getBody();

        return null;
    }

    //TODO: recrear metodo para pasarle body, reutilizar en updateDocument y createDocument.
    private void createVersioningDocument(String id, String description) {

        JSONObject properties = new JSONObject();
        properties.put("dc:description", description);

        JSONObject body = new JSONObject();
        body.put("entity-type", "document");
        body.put("uid", id);
        body.put("repository", "default");
        body.put("properties", properties);

        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Nuxeo-Transaction-Timeout", "3");
        headers.set("X-NXproperties", "*");
        headers.set("X-NXRepository", "default");
        headers.set("X-Versioning-Option", "minor");

        HttpEntity<?> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url + "/id/" + id, HttpMethod.PUT, entity, String.class);
        logger.info("Headers params {} ", headers);
        logger.info("Body params {} ", body);
        logger.info("Status code {} ", responseEntity.getStatusCode());

    }

    private static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + fileName);
        multipart.transferTo(convFile);
        return convFile;
    }
    //endregion

}
