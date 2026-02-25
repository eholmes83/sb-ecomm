package com.echapps.ecom.project.product.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    String uploadImage(String path, MultipartFile file) throws IOException;
}
