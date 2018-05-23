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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        return "redirect:/dirs";
    }
    
    @GetMapping("/dirs/**")
    public String showFiles(Model model, HttpServletRequest request) {
        String fileUri = extractPathFromPattern(request);
        Path parentPath = storageService.getPath(fileUri).getParent();
        List<Entity> entitys = storageService.loadAll(fileUri).map(
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
        String uri = extractPathFromPattern(request);
        Resource file = storageService.loadAsResource(uri);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
    
    @PostMapping("/dirs/**")
    public String storeFile(MultipartFile file, Model model, HttpServletRequest request, 
            RedirectAttributes redirect) {
        String storedUri = extractPathFromPattern(request);
        storedUri = StringUtils.cleanPath(storedUri);
        Path storedPath = storageService.store(file, storedUri);
        return "redirect:/dirs/" + storedPath;
    }
}
