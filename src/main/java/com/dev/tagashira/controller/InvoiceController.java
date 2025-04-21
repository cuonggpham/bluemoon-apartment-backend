package com.dev.tagashira.controller;
 
 import com.dev.tagashira.dto.request.InvoiceRequest;
 import com.dev.tagashira.dto.response.ApiResponse;
 import com.dev.tagashira.dto.response.InvoiceResponse;
 import com.dev.tagashira.dto.response.PaginatedResponse;
 import com.dev.tagashira.entity.Invoice;
 import com.dev.tagashira.service.InvoiceService;
 import com.turkraft.springfilter.boot.Filter;
 import jakarta.validation.Valid;
 import lombok.AllArgsConstructor;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.*;
 
 @RestController
 @AllArgsConstructor
 @RequestMapping("/api/v1/invoices")
 @CrossOrigin(origins = "http://localhost:5173")
 public class InvoiceController {
     private final InvoiceService invoiceService;
 
     //fetch all invoices
     @GetMapping
     public ResponseEntity<PaginatedResponse<InvoiceResponse>> getAllInvoices(@Filter Specification<Invoice> spec, Pageable pageable){
         PaginatedResponse<InvoiceResponse> invoiceResponses = this.invoiceService.fetchAllInvoices(spec, pageable);
         return ResponseEntity.status(HttpStatus.OK).body(invoiceResponses);
     }
 
     //fetch invoice by id
     @GetMapping("/{id}")
     public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable String id) throws Exception {
         InvoiceResponse invoice = this.invoiceService.fetchInvoiceById(id);
         return ResponseEntity.status(HttpStatus.OK).body(invoice);
     }
 
     @PostMapping
     public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest apiInvoice) throws Exception  {
         InvoiceResponse invoice = this.invoiceService.createInvoice(apiInvoice);
         return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
     }
 
     //update invoice
     @PutMapping()
     public ResponseEntity<InvoiceResponse> updateInvoice(@RequestBody InvoiceRequest apiInvoice) throws Exception {
         InvoiceResponse invoice = this.invoiceService.updateInvoice(apiInvoice);
         return ResponseEntity.status(HttpStatus.OK).body(invoice);
     }
 
     //Delete resident by feeCode
     @DeleteMapping("/{id}")
     public ResponseEntity<ApiResponse<String>> deleteInvoice(@PathVariable("id") String id) throws Exception {
         ApiResponse<String> response = this.invoiceService.deleteInvoice(id);
         return ResponseEntity.ok(response);
     }
 }