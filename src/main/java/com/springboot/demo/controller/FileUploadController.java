package com.springboot.demo.controller;

import com.springboot.demo.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final FileUploadService fileUploadService;

    private final static Logger  log = LoggerFactory.getLogger(FileUploadController.class);



    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/")
    public String listUploadFile(Model model) throws IOException {
        model.addAttribute("files",fileUploadService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,"serveFile",path.getFileName().toString())
                        .build().toUri().toString()
        ).collect(Collectors.toList()));
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename){
        Resource file = fileUploadService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=\""+file.getFilename()+"\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file")MultipartFile file, RedirectAttributes redirectAttributes){
        fileUploadService.store(file);
        log.info("file is upload the name is {}",file.getName());
        return "redirect:/";
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?>handleStorageFileNotFound(FileNotFoundException exc){
        return ResponseEntity.notFound().build();
    }





}
