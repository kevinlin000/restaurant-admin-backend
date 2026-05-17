package com.restaurant.repository;

import com.restaurant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 登入用：尋找沒被刪除的 email
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    // 註冊檢查用：看看這個 email 是否已經存在
    boolean existsByEmailAndIsDeletedFalse(String email);
}