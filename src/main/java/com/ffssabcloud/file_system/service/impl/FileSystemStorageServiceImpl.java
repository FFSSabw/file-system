package com.ffssabcloud.file_system.service.impl;

import java.io.File;
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

import com.ffssabcloud.file_system.exception.MkdirException;
import com.ffssabcloud.file_system.exception.MkdirExistedException;
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
    public Path store(MultipartFile file, String storeUrl) {
        Path path = getPath(StringUtils.cleanPath(storeUrl));
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
            Stream<Path> dirStream = Files.walk(p, 1)
                        .filter(path -> !p.equals(path))
                        .filter(path -> path.toFile().isDirectory())
                        .sorted((x, y) -> PathComparator.compareFileName(x, y));
            Stream<Path> fileStream = Files.walk(p, 1)
                        .filter(path -> path.toFile().isFile())
                        .sorted((x, y) -> PathComparator.compareFileName(x, y));
            List<Path> temp = dirStream.collect(Collectors.toList());
            temp.addAll(fileStream.collect(Collectors.toList()));
            if(!rootLocation.equals(p))
                temp.add(0, p.getParent());
            return temp.stream();
        } catch (IOException e) {
            throw new StorageException("Faild to load stored files");
        }
    }
    
    @Override
    public Stream<Path> loadAll(String url) {
        Path path = getPath(url);
        return loadAll(path);
    }
    
    
    @Override
    public Path load(String url) {
        return getPath(url);
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
    public void delete(String url) throws Exception {
        Path path = getPath(url);
        if(rootLocation.equals(path)) 
            throw new Exception("do not permission to delete root");
        if(path.toFile().isDirectory())
            deleteAll(path); 
        else 
            path.toFile().delete();
    }
    
    @Override
    public void deleteAll(Path path) {
        FileSystemUtils.deleteRecursively(path.toFile());
    }
    
    @Override
    public Path getPath(String url) {
        if(StringUtils.isEmpty(url)) return rootLocation;
        return Paths.get(StringUtils.cleanPath(url));
    }

    @Override
    public Path newDir(String dirName, String url) throws MkdirException{
        if(StringUtils.isEmpty(dirName)) throw new MkdirException("dirName is empty");
        Path path = getPath(url);
        File dirFile = path.resolve(dirName).toFile();
        if(dirFile.exists()) throw new MkdirExistedException("dir is existed");
        dirFile.mkdir();
        return path;
    }
    
    private static class PathComparator {

        public static int compareDirAndFile(Path x, Path y) {
            if(allDir(x, y) || allFile(x, y))
                return 0;
            else if(x.toFile().isDirectory())
                return -1;
            else
                return 1;
        }
        
        public static int compareFileName(Path x, Path y) {
            return x.getFileName().compareTo(y.getFileName());
        }
        
        public static boolean allDir(Path x, Path y) {
            if(x.toFile().isDirectory() && y.toFile().isDirectory())
                return true;
            return false;
        }
        
        public static boolean allFile(Path x, Path y) {
            if(x.toFile().isFile() && y.toFile().isFile())
                return true;
            return false;
        }
        
    }
    
}
