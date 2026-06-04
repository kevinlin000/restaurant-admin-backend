package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "staff")
@Data
public class Staff {

    @Id
    @Column(name = "user_id") // 讓user_id作為此表的主鍵
    private Long userId;

    // 設定一對一關聯，共用User的主鍵
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 此表的主鍵來自於User實體
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 懶加載，需要時才從資料庫撈出來
    @JoinColumn(name = "store_id", nullable = false)
    @ToString.Exclude // @Data 生成 toString 時跳過這個欄位
    @EqualsAndHashCode.Exclude // 避免物件比較時觸發多餘的加載
    private Store store;// 物件導向多對一

    @Column(name = "staff_no", nullable = false, unique = true)
    private String staffNo; // 員工編號

    @Column(name = "hire_date")
    private LocalDate hireDate; // 到職日期

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1-在職, 0-離職
}