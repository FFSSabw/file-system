package com.ffssabcloud.file_system.service;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.ffssabcloud.file_system.exception.MkdirException;

public interface StorageService {
    
    void init();
    
    Path store(MultipartFile file, String storeUrl);
    
    Stream<Path> loadAll(Path path);
    
    Stream<Path> loadAll(String url);
    
    Path load(String url);
    
    Resource loadAsResource(String filename);
    
    void delete(String url) throws Exception;
    
    void deleteAll(Path path);
    
    Path getPath(String url);
    
    Path newDir(String dirName, String url) throws MkdirException;
    
}
