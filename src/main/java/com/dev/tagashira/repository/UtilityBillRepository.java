package com.dev.tagashira.repository;

import com.dev.tagashira.entity.Invoice;
import com.dev.tagashira.entity.UtilityBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long>, JpaSpecificationExecutor<UtilityBill> {
    Optional<UtilityBill> findById(Long id);

    @Query("SELECT u FROM UtilityBill u WHERE u.apartment.addressNumber = :apartmentId")
    List<UtilityBill> findByApartmentId(@Param("apartmentId") Long apartmentId);

}
