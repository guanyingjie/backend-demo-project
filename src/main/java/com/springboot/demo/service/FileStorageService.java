package com.springboot.demo.service;

import com.springboot.demo.exception.FileNotFoundException;
import com.springboot.demo.exception.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileStorageService implements FileUploadService {

    private final Path rootLocation;


    public FileStorageService(StorageProperties storageProperties) {
        this.rootLocation = Paths.get(storageProperties.getLocation());
    }

    @Override
    public void init() {
        try{
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileUploadException("could init initialize storage");
        }

    }

    @Override
    public void store(MultipartFile file) {
        try{
            if(file.isEmpty()){
                throw new FileNotFoundException("file is empty");
            }
            Path destinationFile = this.rootLocation.resolve(
                    Paths.get(file.getOriginalFilename())
            ).normalize().toAbsolutePath();
            if(destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())){
                //exception
                throw new FileUploadException("cannot store file outsize");
            }
            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream,destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }catch (IOException e){
            //exception
            throw new FileUploadException("failed to store file");

        }


    }

    @Override
    public Stream<Path> loadAll() {
        try{
            return Files.walk(this.rootLocation,1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new FileUploadException("failed to load resouse");
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists()||resource.isReadable()){
                return resource;
            }
            else{
                //exception
                throw new FileUploadException("failed to load loadAsResource");
            }
        } catch (MalformedURLException e) {
            throw new FileUploadException("failed to load MalformedURLException");
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());

    }
}
