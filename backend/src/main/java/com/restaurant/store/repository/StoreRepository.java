package com.restaurant.store.repository;

import com.restaurant.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByIsDeletedFalseAndStatus(String status);

    List<Store> findByIsDeletedFalseAndStatusAndCity(String status, String city);

    List<Store> findByIsDeletedFalseAndStatusAndCityAndDistrict(String status, String city, String district);

    Optional<Store> findByStoreIdAndIsDeletedFalse(Long storeId);

    List<Store> findByIsDeletedFalse();

    @Query("SELECT s FROM Store s WHERE s.isDeleted = false AND s.status = 'OPEN' AND " +
           "(s.storeName LIKE %:keyword% OR s.city LIKE %:keyword% OR " +
           "s.district LIKE %:keyword% OR s.address LIKE %:keyword%)")
    List<Store> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT s.city FROM Store s WHERE s.isDeleted = false AND s.status = 'OPEN' ORDER BY s.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT s.district FROM Store s WHERE s.city = :city AND s.isDeleted = false AND s.status = 'OPEN' ORDER BY s.district")
    List<String> findDistinctDistrictsByCity(@Param("city") String city);
}
