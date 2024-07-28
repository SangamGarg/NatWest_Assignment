package com.natwest.natwest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "NatWest Assignment", description = "API Documentation")
public class StudentController {

    private static final Logger logger = Logger.getLogger(StudentController.class.getName());

    @Autowired
    private CsvProcessingService csvProcessingService;

    @Autowired
    private StudentRepository studentRepository;

    @Operation(summary = "Upload CSV Using Postman", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary"))))
    @ApiResponse(responseCode = "200", description = "Uploaded Csv Successfully")

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InputStreamResource> uploadCsv(@RequestPart MultipartFile file) {
        try {
            ByteArrayInputStream in = csvProcessingService.processCsv(file);
            if (in == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            InputStreamResource resource = new InputStreamResource(in);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed_students.csv").contentType(MediaType.parseMediaType("application/csv")).body(resource);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing CSV file: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Get Eligibility Form From Roll Number")
    @ApiResponse(responseCode = "200", description = "Eligibility fetched successfully")
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
