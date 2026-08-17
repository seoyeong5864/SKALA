package com.sk.skala.myapp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_name", nullable=false, length=100)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "stock_quantity", columnDefinition = "INT DEFAULT 0")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)                      // DB에 "ON_SALE" 같은 문자열로 저장
    @Column(nullable = false)
    private ProductStatus status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Transient
    private String displayLabel;

    /**
     * DB에서 조회한 뒤 displayLabel을 조합해 반환하는 편의 메서드
     * @Transient 필드는 이처럼 런타임에 계산되는 값에 활용
     */
    public String getDisplayLabel() {
        return name + " (" + (status != null ? status.name() : "N/A") + ")";
    }


    // 테이블 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
