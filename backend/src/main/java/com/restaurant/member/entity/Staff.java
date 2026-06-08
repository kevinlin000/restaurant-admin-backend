package com.restaurant.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.restaurant.store.entity.Store;
import java.time.LocalDate;

/**
 * 員工資訊實體，對應 staff 資料表。
 * 掛載於 User 主表之下，用以記錄員工特有的工號、入職日與在職狀態。
 */
@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {
    public static final Integer STATUS_ACTIVE = 1; // 在職
    public static final Integer STATUS_RESIGNED = 0; // 離職

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "staff_no", nullable = false, unique = true, length = 20)
    private String staffNo;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "status")
    @Builder.Default
    private Integer status = 1; // 1 = 在職 (ACTIVE), 0 = 離職 (RESIGNED)

    public class StaffStatus {
    }
}
