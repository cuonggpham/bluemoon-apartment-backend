package com.dev.tagashira.repository;

import com.dev.tagashira.entity.Fund;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public interface FundRepository extends JpaRepository<Fund, Long>, JpaSpecificationExecutor<Fund> {
     Fund findByFundCode(String fundCode);
 }
