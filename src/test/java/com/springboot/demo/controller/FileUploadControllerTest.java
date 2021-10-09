package com.springboot.demo.controller;

import com.springboot.demo.service.FileUploadService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.internal.invocation.MatchersBinder;
import org.mockito.internal.matchers.text.MatchersPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@AutoConfigureMockMvc
@SpringBootTest
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService fileUploadService;

    @Test
    public void shouldListAllFiles() throws Exception{
        given(this.fileUploadService.loadAll())
                .willReturn(Stream.of(Paths.get("first.txt"),Paths.get("second.txt")));
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("files", Matchers.contains("http://localhost/files/first.txt ","http://localhost/files/second.txt")));

    }

    @Test
    public void shouldSaveUploadFile() throws Exception{
        MockMultipartFile mockMultipartFile = new MockMultipartFile
                ("file","text.txt","text/plain","Spring Framework".getBytes());
        this.mockMvc.perform(multipart("/").file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(header().string("Location","/"));
        then(this.fileUploadService).should().store(mockMultipartFile);
    }

    @Test
    public void should404WhenMissingFile() throws Exception{
        given(this.fileUploadService.loadAsResource("text.txt")).willThrow(FileNotFoundException.class);
        this.mockMvc.perform(get("/files/text.txt"))
                .andExpect(status().isNotFound());


    }

}
