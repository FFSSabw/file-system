package com.ffssabcloud.file_system.controller.store;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ffssabcloud.file_system.controller.BaseController;
import com.ffssabcloud.file_system.exception.MkdirException;
import com.ffssabcloud.file_system.exception.MkdirExistedException;
import com.ffssabcloud.file_system.exception.StorageException;
import com.ffssabcloud.file_system.exception.StorageFileNotFoundException;
import com.ffssabcloud.file_system.model.RestResponseBo;
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
        return "redirect:/dirs/root";
    }
    
    @GetMapping("/dirs/**")
    public String showFiles(Model model, HttpServletRequest request, HttpSession session) {
        String fileUrl = extractPathFromPattern(request);
        Path parentPath = storageService.getPath(fileUrl).getParent();
        List<Entity<String, String>> entitys = storageService.loadAll(fileUrl).map(
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
        model.addAttribute("authenticated", session.getAttribute("authenticated"));
        return "fileList";
    }
    
    @SuppressWarnings("rawtypes")
    @PostMapping("/dirs/**")
    @ResponseBody
    public RestResponseBo newDir(@RequestParam String name, HttpServletRequest request) {
        String url = extractPathFromPattern(request);
        try {
            storageService.newDir(name, url);
        } catch (Exception e) {
            String msg;
            if(e instanceof MkdirExistedException) msg = "文件夹名已存在!";
            else if(e instanceof MkdirException) msg = "文件名不能为空!";
            else msg = "未知错误";
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }
    
    @GetMapping("/files/**")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        String url = extractPathFromPattern(request);
        Resource file = storageService.loadAsResource(url);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
    
    @PostMapping("/files/**")
    public String storeFile(MultipartFile file, Model model, HttpServletRequest request, 
            RedirectAttributes redirect) throws UnsupportedEncodingException {
        String storedUrl = extractPathFromPattern(request);
        Path storedPath = storageService.store(file, storedUrl);
        return "redirect:/dirs/" + encodeURL(storedPath.toString(), "UTF-8");
    }
    
    @SuppressWarnings("rawtypes")
    @DeleteMapping("/{prefix:files|dirs}/**")
    @ResponseBody
    public RestResponseBo deleteFile(@PathVariable String prefix, HttpServletRequest request) {
        String url = extractPathFromPattern(request);
        try {
            storageService.delete(url);
        } catch (Exception e) {
            return RestResponseBo.fail(e.getMessage());
        }
        return RestResponseBo.ok();
    }
    
    @ExceptionHandler(StorageException.class)
    public String handlerStorageException(Model model, StorageException e) {
        String msg;
        if(e instanceof StorageFileNotFoundException) msg = "未找到相应文件";
        else if(e instanceof StorageException) msg = e.getMessage();
        else msg = "未知错误";
        model.addAttribute("message", msg);
        return "400";
    }
}
