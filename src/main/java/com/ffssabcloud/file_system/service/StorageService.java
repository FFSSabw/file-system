package com.ffssabcloud.file_system.service;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    
    void init();
    
    Path store(MultipartFile file, String storeUrl);
    
    Stream<Path> loadAll(Path path);
    
    Stream<Path> loadAll(String uri);
    
    Path load(String uri);
    
    Resource loadAsResource(String filename);
    
    void deleteAll();
    
    Path getPath(String uri);
}
