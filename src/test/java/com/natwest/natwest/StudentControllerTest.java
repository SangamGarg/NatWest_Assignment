package com.natwest.natwest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CsvProcessingService csvProcessingService;

    @MockBean
    private StudentRepository studentRepository;

    @Test
    public void testUploadCsv() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "students.csv", MediaType.TEXT_PLAIN_VALUE, "sample data".getBytes());

        Mockito.when(csvProcessingService.processCsv(any(MultipartFile.class))).thenReturn(new ByteArrayInputStream("processed data".getBytes()));

        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload").file(file)).andExpect(status().isOk()).andExpect(header().string("Content-Disposition", "attachment; filename=processed_students.csv")).andExpect(content().contentType("application/csv"));
    }

    @Test
    public void testGetEligibility() throws Exception {
        StudentModel student = new StudentModel();
        student.setRollNumber("123");
        student.setEligibility("YES Eligible");

        Mockito.when(studentRepository.findByRollNumber(anyString())).thenReturn(Optional.of(student));

        mockMvc.perform(MockMvcRequestBuilders.get("/123")).andExpect(status().isOk()).andExpect(content().string("YES Eligible"));
    }
}
