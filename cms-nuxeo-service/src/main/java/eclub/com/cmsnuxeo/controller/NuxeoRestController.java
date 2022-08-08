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
    /**
     * La función toma una cadena JSON y una lista de archivos, convierte la cadena JSON en un objeto DocumentDTO y luego
     * llama a la función newApplication en la capa de servicio para crear y/o aprobar un documento.
     *
     * @param document Esta es la cadena JSON que contiene los metadatos del documento.
     * @param files Esta es una lista de los archivos que se cargan.
     * @return Entidad de respuesta<ResponseNuxeo>
     */
    @RequestMapping(value={"/onboarding/new", "/onboarding/approve"}, method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
    MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody ResponseEntity<?> newApplication(@RequestPart("document") String document,
                                                          @RequestPart("files") List<MultipartFile> files) {
        try {
            DocumentDTO documentDTO = service.convertDocumentJsonToDTO(document, files);
            ResponseNuxeo response = service.newApplication(documentDTO);
            logger.debug("Result response {} ", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error Occurred: ", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.I_AM_A_TEAPOT);
        }
    }

    /**
     * Actualiza un documento en Nuxeo.
     *
     * @param name El nombre del documento.
     * @param description La descripción del documento.
     * @param uid El identificador único del documento.
     * @param file El archivo que se va a cargar.
     * @return Entidad de respuesta<ResponseNuxeo>
     */
    @RequestMapping(value = "/update/document", method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody ResponseEntity<ResponseNuxeo> updateDocument(@RequestParam(required = false) String name,
                                                                      @RequestParam(required = false) String description,
                                                                      @RequestParam String uid,
                                                                      @RequestPart(required = false) MultipartFile file) throws IOException {
        try {
            //TODO: quitar logica del controller y pasarla en el serivio.
            NuxeoDocument nuxeoDocument = service.getDocumentById(uid);

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

    /**
     * Devuelve una lista de documentos que tienen las etiquetas especificadas en la solicitud.
     *
     * @param tags Una lista de etiquetas para buscar.
     * @return Una lista de documentos que coinciden con las etiquetas.
     */
    @RequestMapping(value={"/search/documentsByTags"}, method = RequestMethod.GET, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody ResponseEntity<?> getDocumentByTag(@RequestParam List<String> tags) {
        try {
            if (tags.size() >= 1){
                //TODO: catch error inside service.
                ResponseNuxeo response = service.getDocumentsByTag(tags);
                logger.debug("Result response {} ", response);
                return ResponseEntity.ok(response.searchNuxeoDocument);
            }else{
                return  ResponseEntity.ok(new SearchNuxeoDocument());
            }
        } catch (Exception e) {
            logger.error("Error Occurred: ", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.I_AM_A_TEAPOT);
        }
    }
    public static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + fileName);
        multipart.transferTo(convFile);
        return convFile;
    }
}
