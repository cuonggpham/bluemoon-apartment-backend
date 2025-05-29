package com.dev.tagashira.util;

import com.dev.tagashira.constant.PaymentEnum;
import com.dev.tagashira.entity.UtilityBill;
import com.dev.tagashira.exception.FileProcessingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileParsingUtil {

    public static List<UtilityBill> parseCSV(InputStream inputStream, String name) {
        try {
            byte[] allBytes = inputStream.readAllBytes();
            InputStream newInputStream = new ByteArrayInputStream(allBytes);
            
            Reader reader = new InputStreamReader(newInputStream, StandardCharsets.UTF_8);
            CSVParser parser = new CSVParser(reader,
                    CSVFormat.DEFAULT
                            .withFirstRecordAsHeader()
                            .withTrim()
                            .withIgnoreEmptyLines()
                            .withQuote('"'));

            List<UtilityBill> bills = new ArrayList<>();
            
            for (CSVRecord rec : parser) {
                try {
                    Long aptId = Long.valueOf(rec.get("apartmentId"));
                    double electricity = Double.parseDouble(rec.get("electricity"));
                    double water = Double.parseDouble(rec.get("water"));
                    double internet = Double.parseDouble(rec.get("internet"));
                    
                    bills.add(UtilityBill.builder()
                            .apartmentId(aptId)
                            .electricity(electricity)
                            .water(water)
                            .internet(internet)
                            .name(name)
                            .paymentStatus(PaymentEnum.Unpaid)
                            .build());
                } catch (NumberFormatException ex) {
                    throw new FileProcessingException(
                            "Invalid number at line " + rec.getRecordNumber() + ": " + ex.getMessage(), ex);
                }
            }
            
            parser.close();
            reader.close();
            return bills;
            
        } catch (IOException e) {
            throw new FileProcessingException("Error parsing CSV: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FileProcessingException("Unexpected error parsing CSV: " + e.getMessage(), e);
        }
    }

    public static List<UtilityBill> parseExcel(MultipartFile file, String name) {
        List<UtilityBill> utilityBills = new ArrayList<>();
        
        try {
            byte[] allBytes = file.getBytes();
            InputStream inputStream = new ByteArrayInputStream(allBytes);
            
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // Skip the header row
                if (row.getRowNum() == 0) continue;

                // Check if the row is empty
                if (row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    break;
                }

                try {
                    Long apartmentId = (long) row.getCell(0).getNumericCellValue();
                    double electricity = row.getCell(1).getNumericCellValue();
                    double water = row.getCell(2).getNumericCellValue();
                    double internet = row.getCell(3).getNumericCellValue();

                    utilityBills.add(UtilityBill.builder()
                            .apartmentId(apartmentId)
                            .electricity(electricity)
                            .water(water)
                            .internet(internet)
                            .name(name)
                            .paymentStatus(PaymentEnum.Unpaid)
                            .build());
                } catch (Exception e) {
                    throw new FileProcessingException("Error processing row " + row.getRowNum() + ": " + e.getMessage(), e);
                }
            }
            
            workbook.close();
            inputStream.close();
            return utilityBills;
            
        } catch (IOException e) {
            throw new FileProcessingException("Error parsing Excel: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FileProcessingException("Unexpected error parsing Excel: " + e.getMessage(), e);
        }
    }
}
