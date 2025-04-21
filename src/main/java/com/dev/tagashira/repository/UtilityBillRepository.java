package com.dev.tagashira.repository;
 
 import com.dev.tagashira.entity.Invoice;
 import com.dev.tagashira.entity.UtilityBill;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public interface UtilityBillRepository extends JpaRepository<UtilityBill, String>, JpaSpecificationExecutor<UtilityBill> {
 }