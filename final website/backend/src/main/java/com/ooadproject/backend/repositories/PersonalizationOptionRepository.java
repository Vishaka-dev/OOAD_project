package com.ooadproject.backend.repositories;

import com.ooadproject.backend.entities.PersonalizationOption;
import com.ooadproject.backend.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalizationOptionRepository extends JpaRepository<PersonalizationOption, Integer> {
    List<PersonalizationOption> findByProduct(Product product);
}


