package com.ffssabcloud.file_system.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ffssabcloud.file_system.exception.StorageException;
import com.ffssabcloud.file_system.exception.StorageFileNotFoundException;
import com.ffssabcloud.file_system.service.StorageService;

@Service
public class FileSystemStorageServiceImpl implements StorageService{
    
    private final Path rootLocation;
    private static String location = "root";
    
    public FileSystemStorageServiceImpl() {
        rootLocation = Paths.get(location);
    }
    
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch(IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public Path store(MultipartFile file, String storeUri) {
        Path path = getPath(storeUri);
        if(!path.toFile().isDirectory()) {
            throw new StorageException("path is not directory");
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if(file.isEmpty()) {
            throw new StorageException("file is empty: " + filename);
        }
        if(filename.contains("..")) {
            throw new StorageException("Cannot store file with relative path: " + filename);
        }
        try(InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path.resolve(filename), 
                StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename);
        }
    }
    
    @Override
    public Stream<Path> loadAll(Path p) {
        try {
            Stream<Path> stream = Files.walk(p, 1)
                        .filter(path -> !p.equals(path));
            if(!rootLocation.equals(p)) {
                List<Path> temp = stream.collect(Collectors.toList());
                temp.add(0, p.getParent());
                stream = temp.stream();
            }
            return stream;
            
        } catch (IOException e) {
            throw new StorageException("Faild to load stored files");
        }
    }
    
    @Override
    public Stream<Path> loadAll(String uri) {
        Path path = getPath(uri);
        return loadAll(path);
    }
    
    
    @Override
    public Path load(String uri) {
        return getPath(uri);
    }

    @Override
    public Resource loadAsResource(String filename) {
        Path file = load(filename);
        try {
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable())
                return resource;
            else
                throw new StorageException("Could not read file: " + filename);
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
    
    @Override
    public Path getPath(String uri) {
        if(StringUtils.isEmpty(uri)) return rootLocation;
        return Paths.get(StringUtils.cleanPath(uri));
    }
    
}
