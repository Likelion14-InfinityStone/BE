package com.medipass.server.domain.medication.repository;

import com.medipass.server.domain.medication.entity.MfdsProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfdsProductRepository extends JpaRepository<MfdsProduct, String> {
}
