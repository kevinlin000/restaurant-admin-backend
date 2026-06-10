package com.restaurant.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;// 標記請求是否成功 (true: 成功, false: 失敗)
    private String message;// 給前端的提示訊息 (例如："登入成功" 或 "密碼錯誤")
    private T data;// // 泛型可以是任何物件類別，這裡存放真正的資料 (DTO)

    // 回傳的 ApiResponse 物件能自動對應傳入的資料型別
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "請求成功", data);
    }

    // 參數只需要傳入錯誤訊息字串即可
    public static <T> ApiResponse<T> error(String message) {
        // 回傳一個 success 為 false 的物件，資料欄位 (data) 為 null
        return new ApiResponse<>(false, message, null);
    }
}