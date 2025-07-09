package com.mx.mitienda.repository;

import com.mx.mitienda.model.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    // Si quieres: métodos custom, por ejemplo buscar por número de parte
    boolean existsByPartNumber(String partNumber);
}
