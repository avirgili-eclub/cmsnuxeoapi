package eclub.com.cmsnuxeo.service;

import eclub.com.cmsnuxeo.exception.NuxeoManagerException;


import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import eclub.com.cmsnuxeo.dto.*;

import java.util.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;



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
    public ResponseNuxeo createDocument(List<File> fileList, String path) throws NuxeoManagerException, ParseException {
        List<BatchDTO> batchDocuments = new ArrayList<>();
        ResponseNuxeo responseNuxeo = new ResponseNuxeo();

        try {
            String batchId = createBatchId();

            if(batchId == null || batchId.isEmpty()){
                responseNuxeo.success = false;
                responseNuxeo.friendlyErrorMessage = "Error al crear el batch.";
                return responseNuxeo;
            }

            //region NuxeoClient SDK

//            NuxeoClient nuxeoClient = null;
//            try {
//                nuxeoClient = new NuxeoClient.Builder()
//            .url("http://10.30.0.51:8080/nuxeo")
//            .authentication(user, password)
//            .schemas("*") // fetch all document schemas
//            .build();
//            } catch (Exception e) {
//                logger.info(e.getMessage());
//                logger.info(e.getStackTrace().toString());
//                throw new RuntimeException(e);
//            }
//
//            BatchUploadManager batchUploadManager = nuxeoClient.batchUploadManager();
//
//            // create a new batch
//            BatchUpload batchUpload = batchUploadManager.createBatch();
//
//            Blob fileBlob = new FileBlob(fileList.get(0));
//
//            batchUpload = batchUpload.upload("0", fileBlob);
//
//            // create document with the blob
//            Document doc = Document.createWithName("file002", "File");
//            doc.setPropertyValue("file:content", batchUpload.getBatchBlob());
//            doc = nuxeoClient.repository().createDocumentByPath("/", doc);
           // endregion

            fileList.forEach(file -> {

                BatchDTO batchDocument = null;
                try {
                    batchDocument = uploadDocument(file, batchId, fileList.indexOf(file));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (!batchDocument.uploaded)
                    logger.info("Upload failed for file: " + batchDocument.name);

                batchDocument.name = file.getName();
                batchDocuments.add(batchDocument);

                logger.info(batchDocument.batchId);
            });

            boolean batchList = verifiedBatch(batchId);

            if (!batchList) {
                responseNuxeo.success = false;
                responseNuxeo.friendlyErrorMessage = "Fallo la subida de archivo.";
                return responseNuxeo;
            }

            responseNuxeo.nuxeoDocuments = new ArrayList<>();
            batchDocuments.forEach(batchDTO -> responseNuxeo.nuxeoDocuments.add(createDocumentWithBatchAndPath(batchDTO, path)));

            if (responseNuxeo.nuxeoDocuments.size() < 1){
                responseNuxeo.success = false;
                responseNuxeo.friendlyErrorMessage = "No se pudo crear el documento.";
                return responseNuxeo;
            }

            return responseNuxeo;

        }catch (Exception e) {
               logger.info(e.getStackTrace().toString());
               return null;
        }
    }

    @Override
    public ResponseNuxeo newOnboarding(DocumentDTO document) throws NuxeoManagerException, Exception {

         //TODO: check if directory exists first before create it to avoid duplicate.
        ResponseNuxeo parentFolder = createFolderWithParentId("14814521-2052-4c3b-824f-fc09b9331a4d", document.name);
        if (!parentFolder.success)
             return parentFolder;

        ResponseNuxeo solicitudFolder =  createFolderWithParentId(parentFolder.nuxeoDocument.uid, UUID.randomUUID().toString());
        if (!solicitudFolder.success)
            return solicitudFolder;

        ResponseNuxeo result = createDocument(document.fileList, solicitudFolder.nuxeoDocument.path);

        if (!result.success) {
            deleteDocumentById(solicitudFolder.nuxeoDocument.uid);
        }

        return solicitudFolder;
    }

    //metodos de alcantarilla
    private ResponseNuxeo createFolderWithParentId(String id, String name){
        ResponseNuxeo response = new ResponseNuxeo();

        ResponseEntity<NuxeoDocumentDTO> responseEntity;

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("entity-type", "document");
        jsonBody.put("type", "Folder");
        jsonBody.put("name", name);

        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM,
                MediaType.MULTIPART_FORM_DATA, MediaType.IMAGE_JPEG));
        headers.set("Nuxeo-Transaction-Timeout","3");
        headers.set("X-NXproperties","*");
        headers.set("X-NXRepository","default");
        
        HttpEntity<?> entity = new HttpEntity<>(jsonBody, headers);

        responseEntity = restTemplate.exchange(url+"/id/"+id, HttpMethod.POST, entity, NuxeoDocumentDTO.class);

        response.nuxeoDocument = responseEntity.getBody();
        response.success = true;

        logger.info("Headers params {} ",headers);
        logger.info("Body params {} ",jsonBody);

        return response;
    }
    private String createBatchId() throws ParseException {

        ResponseEntity<BatchDTO> responseEntity;

        HttpHeaders headers = createHttpHeaders(user, password);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        //String result = restTemplate.exchange(url+"/upload/", HttpMethod.POST, entity, String.class).getBody();
        responseEntity = restTemplate.exchange(url+"/upload/", HttpMethod.POST, entity, BatchDTO.class);

        return responseEntity.getBody().batchId;
}

    private BatchDTO uploadDocument(File file, String batchId, int batchIdx) throws IOException {

        ResponseEntity<BatchDTO> responseEntity;

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        FileSystemResource fileToSend = new FileSystemResource(file);
        byte[] data = IOUtils.toByteArray(fileToSend.getInputStream());
        map.add("file", data);

        //region ayudamemoria

        //Path path = Paths.get(file.getAbsolutePath());

        //FileBlob fileBlob = new FileBlob(file);

        //InputStream inputStream = new BufferedInputStream(new FileInputStream(
        //        file));

        //byte[] binaryData = IOUtils.toByteArray(inputStream);

        //Base64.Encoder encoder = Base64.getMimeEncoder();

        //byte[] eStr = encoder.encode(binaryData);

        //logger.info("Encoded MIME message: " + eStr);

        //MultiValueMap<String, Object> map = new LinkedMultiValueMap();
        //MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        //LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        //FileSystemResource fileToSend = new FileSystemResource(file);


        //FileBody fileToSend = new FileBody(file, ContentType.DEFAULT_BINARY, file.getName());
        //body.put("file", fileToSend);

        //params.put("file", fileToSend);
        //map.add("file", fileToSend);

        //try {
            //FileInputStream fileToSend = new FileInputStream(file);

            //var fileInputStreamByte = fileToSend.readAllBytes();
            //var fileBlobByte = fileBlob.getStream().readAllBytes();
            //var multipartFileByte = multipartFile.getBytes();

            //body.put("file", fileToSend);
            //body.put("data", file);
            //body.put("file", file);

        //} catch (Exception e){
        //    logger.info(e.getMessage());
        //}
        //endregion

        //region header creation
        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

        headers.set("X-File-Name", file.getName());
        headers.setContentLength(file.length());
        //headers.set("X-File-Type", fileBlob.getMimeType());
        headers.set("X-File-Type", "image/jpeg");
        //endregion

        //region ayudamemoria
        //MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        //builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        //builder.addPart("file", fileToSend);
        //org.apache.http.HttpEntity requestEntity = builder.build();

        //HttpEntity<MultiValueMap<String, Object>> requestEntity =
        //        new HttpEntity<>(map, headers);

        //MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM,
        //        MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA));

        //restTemplate.setMessageConverters(Arrays.asList(converter, new FormHttpMessageConverter()));

        //restTemplate.getMessageConverters().add(converter);

        //responseEntity = restTemplate.exchange(url+"/upload/" + batchId + "/" + batchIdx, HttpMethod.POST, requestEntity,
        //        BatchDTO.class);

        //endregion

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(data, headers);
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

        try {

            responseEntity = restTemplate.postForEntity(url+"/upload/" + batchId + "/" + batchIdx, requestEntity,
                    BatchDTO.class);
        } catch (RestClientException ex) {

            logger.info("Error occured duting posting the file to api gateway:"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
        logger.info("Headers params {} ",headers);
        logger.info("Body params {} ",responseEntity.getBody());
        return responseEntity.getBody();
    }

    private boolean verifiedBatch(String batchId) {
        boolean success = false;
        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url+"/upload/" + batchId, HttpMethod.GET, entity,
                String.class);

        logger.info("Headers params {} ",headers);

        if(response.getStatusCodeValue() == 200)
            success = true;

        return success;

    }

    private NuxeoDocumentDTO createDocumentWithBatchAndPath(BatchDTO batch, String path) {

        JSONObject content = new JSONObject();
        content.put("upload-batch", batch.batchId);
        content.put("upload-fileId", batch.fileIdx);

//        Map<String, String> content = new HashMap<>();
//        content.put("upload-batch", batch.batchId);
//        content.put("upload-fileId", batch.fileIdx);

//        JSONObject file = new JSONObject();
//        file.put("upload-batch",batch.batchId);
//        file.put("upload-fileId",batch.fileIdx);

//        JSONArray files = new JSONArray();
//        files.add(file);

        JSONObject properties = new JSONObject();
        properties.put("dc:title",batch.name);
        properties.put("file:content",content);
//        properties.put("files:files",files);

        JSONObject body = new JSONObject();
        body.put("entity-type", "document");
        body.put("type", "File");
        body.put("name", batch.name);
        body.put("repository","default");
        body.put("properties", properties);

        //TODO: improve method of "createHttpHeaders"
        HttpHeaders headers = createHttpHeaders(user, password);

        headers.setAccept(Arrays.asList(MediaType.ALL));
        headers.setContentLength(Long.parseLong(MAX_TRANSFER_SIZE));
        headers.set("Nuxeo-Transaction-Timeout","3");
        headers.set("X-NXproperties","*");
        headers.set("X-NXRepository","default");

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        ResponseEntity<NuxeoDocumentDTO> response = restTemplate.exchange(url+"/path"+path, HttpMethod.POST, entity, NuxeoDocumentDTO.class);

        logger.info("Headers params {} ",headers);
        logger.info("Body params {} ",body);

        return response.getBody();
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
//    public String ImageToBase64Convert(String filePath) throws Exception {
//
//        File f = new File(filePath); // change path of image according to you
//        FileInputStream fis = new FileInputStream(f);
//        byte byteArray[] = new byte[(int) f.length()];
//        fis.read(byteArray);
//        String imageString = Base64.encodeBase64String(byteArray);
//        fis.close();
//        return imageString;
//    }
}
