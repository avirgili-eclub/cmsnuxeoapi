package eclub.com.cmsnuxeo.dto;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public class DocumentDTO {
    public String name;
    public MultipartFile file;
    public List<File> fileList;
    public List<MultipartFile> multipartFiles;
    public String path;
    public String id;

}
