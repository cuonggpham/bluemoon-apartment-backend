package com.dev.tagashira.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.constant.PaymentEnum;
import com.dev.tagashira.service.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice {
    @Id
    String id;
    String name;
    @Column(columnDefinition = "MEDIUMTEXT")
    String description;

    @JsonIgnore  //hide this field
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)  //cascade: used for auto updating at fees and invoices table
    List<FeeInvoice> feeInvoices;

    @JsonIgnore  //hide this field
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)  //cascade: used for auto updating at fees and invoices table
    List<InvoiceApartment> invoiceApartments;

    int isActive;
    Instant updatedAt;
    LocalDate createdAt;    @PrePersist
    public void beforeCreate() {
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = generateInvoiceId();
        }
        this.isActive = 1;
        this.updatedAt = Instant.now();
        this.createdAt = LocalDate.now();
    }
    
    private String generateInvoiceId() {
        // Tạo mã theo format: INV-YYYY-MM-DDDD
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        // Tạo số sequence đơn giản dựa trên timestamp
        long sequence = System.currentTimeMillis() % 10000; // Lấy 4 chữ số cuối của timestamp
        return "INV-" + datePrefix + "-" + String.format("%04d", sequence);
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
    }

}
