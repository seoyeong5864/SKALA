package com.sk.skala.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sk.skala.myapp.domain.Product;
import com.sk.skala.myapp.domain.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long>{
    List<Product> findByStatus(ProductStatus status);

    // user_id 값으로 상품 목록을 조회
    List<Product> findByUserId(Long userId);

    // user_name 값으로 상품 목록 조회
    List<Product> findByUserName(String userName);
}
