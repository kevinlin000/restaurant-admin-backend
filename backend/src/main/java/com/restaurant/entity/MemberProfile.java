package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "member_profile")
@Data
public class MemberProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    // 設定一對一關聯，共用User的主鍵
    @OneToOne(fetch = FetchType.LAZY) // 懶加載，需要時才從資料庫撈出來
    @MapsId // 此表的主鍵來自於User實體
    @JoinColumn(name = "user_id")
    @ToString.Exclude // @Data 生成 toString 時跳過這個欄位
    @EqualsAndHashCode.Exclude // 避免物件比較時觸發多餘的加載
    private User user;// 會員資料與User資料為一對一關聯

    @Column(name = "birthday")
    private LocalDate birthday;// 會員生日

    @Column(name = "point_balance", nullable = false)
    private BigDecimal pointBalance = BigDecimal.ZERO; // 會員點數預設為0

    @Column(name = "member_level", nullable = false)
    private String memberLevel = "REGULAR"; // 會員等級預設為REGULAR
}