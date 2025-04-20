package com.dev.tagashira.repository;

import com.dev.tagashira.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

}
