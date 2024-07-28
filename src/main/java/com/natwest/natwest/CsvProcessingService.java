package com.natwest.natwest;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(CsvProcessingService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Value("${scholarship.criteria.science}")
    private int scienceCriteria;
    @Value("${scholarship.criteria.maths}")
    private int mathsCriteria;
    @Value("${scholarship.criteria.english}")
    private int englishCriteria;
    @Value("${scholarship.criteria.computer}")
    private int computerCriteria;

    public ByteArrayInputStream processCsv(MultipartFile file) {
        List<StudentModel> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream())); ByteArrayOutputStream out = new ByteArrayOutputStream(); CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withHeader("Roll Number", "Student Name", "Science", "Maths", "English", "Computer", "Eligible"))) {

            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            for (CSVRecord record : csvParser) {
                StudentModel student = new StudentModel();
                student.setRollNumber(record.get("Roll Number"));
                student.setStudentName(record.get("Student Name"));
                student.setScience(Integer.parseInt(record.get("Science")));
                student.setMaths(Integer.parseInt(record.get("Maths")));
                student.setEnglish(Integer.parseInt(record.get("English")));
                student.setComputer(Integer.parseInt(record.get("Computer")));

                boolean eligible = student.getScience() > scienceCriteria && student.getMaths() > mathsCriteria && student.getEnglish() > englishCriteria && student.getComputer() > computerCriteria;
                student.setEligibility(eligible ? "YES" : "NO");
                students.add(student);

                // Save to repository
                studentRepository.save(student);

                // Write to output CSV
                csvPrinter.printRecord(student.getRollNumber(), student.getStudentName(), student.getScience(), student.getMaths(), student.getEnglish(), student.getComputer(), student.getEligibility());
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            logger.error("Error processing CSV file", e);
            return null;
        }
    }
}


