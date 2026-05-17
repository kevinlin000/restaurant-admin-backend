package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "member_profile")
@Data
public class MemberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 由資料庫自動生成主鍵
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "point_balance", nullable = false)
    private Integer pointBalance;

    @Column(name = "member_level", nullable = false)
    private String memberLevel;
}