package com.ffssabcloud.file_system.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ffssabcloud.file_system.service.StorageService;

@Controller
public class FileController extends BaseController{
    
    private final StorageService storageService;
    
    @Autowired
    public FileController(StorageService service) {
        storageService = service;
    }
    
    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        return showFiles(model, request);
    }
    
    @GetMapping("/dirs/**")
    public String showFiles(Model model, HttpServletRequest request) {
        String fileUrl = extractPathFromPattern(request);
        fileUrl = StringUtils.cleanPath(fileUrl);
        Path parentPath = Paths.get(fileUrl).getParent();
        List<Entity> entitys = storageService.loadAll(fileUrl).map(
                path -> {
                    String prefix, uri, filename, 
                        suffix = "";
                    if(path.toFile().isDirectory()) {
                        prefix = "/dirs/";
                        suffix = "/";
                    } else prefix = "/files/";
                    uri = String.format("%s%s", prefix, path.toString());
                    filename = String.format("%s%s", path.getFileName().toString(), suffix);
                    return new Entity<String, String>(
                            path.equals(parentPath) ? ".." : filename, uri);
                }).collect(Collectors.toList());
        model.addAttribute("files", entitys);
        return "fileList";
    }
    
    @GetMapping("/files/**")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        String path = extractPathFromPattern(request);
        Resource file = storageService.loadAsResource(path);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
