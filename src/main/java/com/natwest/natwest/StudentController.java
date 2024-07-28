package com.natwest.natwest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class StudentController {

    private static final Logger logger = Logger.getLogger(StudentController.class.getName());

    @Autowired
    private CsvProcessingService csvProcessingService;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping("/upload")
    public ResponseEntity<InputStreamResource> uploadCsv(@RequestPart("file") MultipartFile file) {
        ByteArrayInputStream in = csvProcessingService.processCsv(file);
        if (in == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        InputStreamResource resource = new InputStreamResource(in);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed_students.csv")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(resource);
    }

    @GetMapping("/{rollNumber}")
    public ResponseEntity<String> getEligibility(@PathVariable String rollNumber) {
        try {
            logger.log(Level.INFO, "Fetching eligibility for roll number: {0}", rollNumber);
            Optional<StudentModel> student = studentRepository.findByRollNumber(rollNumber);
            return student.map(value -> {
                logger.log(Level.INFO, "Eligibility found: {0}", value.getEligibility());
                return ResponseEntity.ok(value.getEligibility());
            }).orElseGet(() -> {
                logger.log(Level.INFO, "No eligibility found for roll number: {0}", rollNumber);
                return ResponseEntity.ok("NA");
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching eligibility for roll number: " + rollNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
}
