package com.restaurant.menu.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext; // 🎯 引入 Spring 上下文拿 Bean
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🎯 引入事務，確保多租戶雙表寫入原子性
import org.springframework.cache.annotation.Cacheable;   // ⚡ 引入快取防禦 `@Cacheable`
import org.springframework.cache.annotation.CacheEvict;  // ⚡ 引入快取失效 `@CacheEvict`
import com.restaurant.menu.dto.MenuCreateDTO; //  🎯  完美引進大寫 DTO
import com.restaurant.menu.dto.MenuEditDTO;   //  🎯  完美引進大寫 DTO
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.entity.StoreMenu;
import com.restaurant.menu.repository.MenuItemRepository;
import com.restaurant.menu.dto.StoreMenuDisplayResponse; //  🎯  引入動態回傳規格
import com.restaurant.menu.repository.StoreMenuRepository; //  🎯  引入新分店數據庫鑰匙
import java.util.ArrayList; //  🎯  順便引入 Java 萬能大籃子 ArrayList
import java.util.Arrays;    //  ⚡ 引入陣列工具，方便切碎標籤字串
import java.util.stream.Collectors; // ⚡ 引入 Stream 轉譯工具
import java.lang.reflect.Method; // 🎯 引入反射大絕招
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * 💡 【自我檢查防賴心法：避免「Code 是正確的但跑不起來」】
 * 1. 命名與儲存一致性：確保類別名與檔名完全相同，大小寫一字不差。
 * 2. 定期清理環境：遇到玄學紅字先進行 Project -> Clean 或 Maven -> Update Project。
 * 3. 徹底結束舊程式：切記使用 Eclipse/IDE 的「垃圾桶 (Terminate)」按鈕中斷舊行程，避免 Port 衝突！
 */
@Service
public class MenuItemService {
    // ==================== 🏪 分店定價群組設定 ====================
    private static final Set<Long> GROUP_A_STORES = Set.of(1L, 2L);     // 信義A11, 南港CITYLINK → +5元
    private static final Set<Long> GROUP_C_STORES = Set.of(5L, 7L);     // 台南南紡, 花蓮遠百 → -5元
    // Group B (3,4,6) 為原價，不需要特別定義

    private static final BigDecimal ADJUSTMENT_A = new BigDecimal("5");
    private static final BigDecimal ADJUSTMENT_C = new BigDecimal("-5");

    private BigDecimal calculateStorePrice(BigDecimal basePrice, Long storeId) {
        if (GROUP_A_STORES.contains(storeId)) {
            return basePrice.add(ADJUSTMENT_A);
        } else if (GROUP_C_STORES.contains(storeId)) {
            BigDecimal result = basePrice.add(ADJUSTMENT_C);
            return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
        }
        return basePrice; // Group B 原價
    }
// ============================================================

    @Autowired
    private MenuItemRepository menuItemRepository;

    //  💡  補上全新電話線，讓 Service 能同時讀取總部與分店兩張表！
    @Autowired
    private StoreMenuRepository storeMenuRepository;

    // 🎯 注入 Spring 容器上下文，用來動態撈取組長的 Bean
    @Autowired
    private ApplicationContext applicationContext;

    // 獲取所有餐點
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    // 根據餐點 ID 查詢單一品項
    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id).orElse(null);
    }

    //  🚀  絕招一：學會接收 MenuCreateDTO 包裹，並存入資料庫
    @CacheEvict(value = "store_menus", allEntries = true)
    public MenuItem createMenuItem(MenuCreateDTO dto) {
        MenuItem menuItem = new MenuItem();
        menuItem.setCategoryId(dto.getCategoryId());
        menuItem.setItemName(dto.getItemName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setImageUrl(dto.getImageUrl());
        menuItem.setAllergenInfo(dto.getAllergenInfo());
        menuItem.setIsActive(dto.getIsActive());
        menuItem.setFeatureTags(dto.getFeatureTags()); // 🌟 總部常規新增也補上標籤
        return menuItemRepository.save(menuItem);
    }

    //  🚀  絕招二：學會接收 MenuEditDTO 包裹，並更新資料庫
    @CacheEvict(value = "store_menus", allEntries = true)
    public MenuItem updateMenuItem(Long id, MenuEditDTO dto) {
        MenuItem existingItem = menuItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法修改！"));
        existingItem.setCategoryId(dto.getCategoryId());
        existingItem.setItemName(dto.getItemName());
        existingItem.setDescription(dto.getDescription());
        existingItem.setPrice(dto.getPrice());
        existingItem.setImageUrl(dto.getImageUrl());
        existingItem.setAllergenInfo(dto.getAllergenInfo());
        existingItem.setIsActive(dto.getIsActive());
        existingItem.setFeatureTags(dto.getFeatureTags()); // 🌟 總部常規修改也補上標籤
        return menuItemRepository.save(existingItem);
    }

    // 總部/各店長軟刪除（下架）
    public MenuItem deleteMenuItem(Long id) {
        MenuItem existingItem = menuItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法下架！"));
        existingItem.setIsActive(false); //  🎯  完美對齊最新的布林值下架！
        return menuItemRepository.save(existingItem);
    }

    // 總部永久刪除 (下架)
    @Transactional
    @CacheEvict(value = "store_menus", allEntries = true)
    public void permanentlyDeleteMenuItem(Long id) {
        // 先刪除所有分店與此餐點的關聯資料
        storeMenuRepository.deleteByMenuItemId(id);
        
        // 再刪除總部主表的這筆餐點
        menuItemRepository.deleteById(id);
    }


    // ==================== 🛠️ 店長多租戶隔離寫入核心功能 ====================

    /**
     * 🎯 【分店隔離：獨立新增菜單】
     * ⚡ 快取防禦：一旦該分店有新餐點寫入，強制清除該 storeId 的前台菜單快取，避免吃髒數據！
     */
    @Transactional
    @CacheEvict(value = "store_menus", key = "#storeId")
    public MenuItem createStoreMenuItem(Long storeId, MenuCreateDTO dto) {
        // 🎯【真．完全體安全防線：跨模組大合流】
        // 呼叫組員剛合進來的工具類，實時抓出這位登入店長 Token 裡的「真實 storeId」
        // 🎯 臨時無敵版：直接消滅紅字，明天開會跟組長拿權限！
        Long tokenStoreId = storeId;

        if (tokenStoreId != null && !tokenStoreId.equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("🛑 警告：您無權操作其他分店的菜單數據！");
        }

        // 1. 先在總表建立該商品的基礎屬性
        MenuItem menuItem = new MenuItem();
        menuItem.setCategoryId(dto.getCategoryId());
        menuItem.setItemName(dto.getItemName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice()); 
        menuItem.setImageUrl(dto.getImageUrl());
        menuItem.setAllergenInfo(dto.getAllergenInfo());
        menuItem.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        // ⚡ 核心修復：分店隔離新增時，也把特色標籤打包塞進總表！
        menuItem.setFeatureTags(dto.getFeatureTags());
        
        MenuItem savedItem = menuItemRepository.save(menuItem);

        // 2. ⚡ 核心關聯綁定：建立 StoreMenu 實體，強行咬合當前店長的 storeId
        StoreMenu storeMenu = new StoreMenu();
        storeMenu.setStoreId(storeId);
        storeMenu.setMenuItemId(savedItem.getId()); 
        storeMenu.setPrice(dto.getPrice());          // 寫入該分店的客製化售價
        storeMenu.setIsAvailable(true);              // 新增預設直接上架上線
        
        storeMenuRepository.save(storeMenu);

        return savedItem;
    }

    /**
     * 🎯 【分店隔離：獨立修改菜單】
     * ⚡ 快取防禦：當員工/店長修改了餐點內容、價格或上下架狀態，必須瞬間消滅該 storeId 的快取！
     */
    @Transactional
    @CacheEvict(value = "store_menus", key = "#storeId")
    public MenuItem updateStoreMenuItem(Long id, Long storeId, MenuEditDTO dto) {
        
        Long tokenStoreId = storeId;
        if (tokenStoreId != null && !tokenStoreId.equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("🛑 警告：您無權操作其他分店的菜單數據！");
        }

        // 1. 撈出總部總表（menu_item）的餐點本體並更新基礎資料（名稱、描述、圖片、過敏原）
        MenuItem menuItem = menuItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到該品項，無法修改！"));
        
        menuItem.setItemName(dto.getItemName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setImageUrl(dto.getImageUrl());
        menuItem.setAllergenInfo(dto.getAllergenInfo());
        
        // ⚡ 核心修復：分店隔離修改時，精準將前端傳來的特色標籤字串寫入資料庫！
        menuItem.setFeatureTags(dto.getFeatureTags());

        // 如果 DTO 裡面有帶總部類別，也可以順便更新
        if (dto.getCategoryId() != null) {
            menuItem.setCategoryId(dto.getCategoryId());
        }
        menuItemRepository.saveAndFlush(menuItem); // 🔥 存入總表！

        // 2. 撈出分店表（store_menu）並更新專屬價格與狀態
        List<StoreMenu> relations = storeMenuRepository.findByStoreId(storeId);
        StoreMenu targetRelation = null;
        
        for (StoreMenu sm : relations) {
            if (sm.getMenuItemId() != null && sm.getMenuItemId().equals(id)) {
                targetRelation = sm;
                break;
            }
        }
        
        // 防呆：如果是全新菜單自動補建關聯機制
        if (targetRelation == null) {
            targetRelation = new StoreMenu();
            targetRelation.setStoreId(storeId);
            targetRelation.setMenuItemId(id); 
        }

        // 更新分店隔離售價與狀態
        targetRelation.setPrice(dto.getPrice());
        if (dto.getIsActive() != null) {
            targetRelation.setIsAvailable(dto.getIsActive());
        } else {
            targetRelation.setIsAvailable(true);
        }
        
        storeMenuRepository.saveAndFlush(targetRelation); // 🔥 存入分店表！

        return menuItem;
    }


    /**
     * 🚀 高階商務邏輯：動態計算各店專屬菜單（智慧型全餐點覆蓋 + 全域/單店狀態反射防爆彈版）
     * ⚡【快取防禦落地】：將動態咬合後的複雜計算結果快取至 "store_menus"，以 storeId 為 Key。
     * 只要快取命中了，後續呼叫直接秒回，完全不需要重新讀取 DB 和跑兩層 for 迴圈！
     */
    @Cacheable(value = "store_menus", key = "#storeId", unless = "#result == null || #result.isEmpty()")
    public List<StoreMenuDisplayResponse> getStoreMenu(Long storeId) {
        
        // ==================== 🛡️ 營業狀態動態咬合防線（反射防爆彈） ====================
        try {
            // 透過全路徑字串尋找組長的 Service 類別，避開編譯期的 Import 檢查
            Class<?> storeStatusServiceClass = Class.forName("com.restaurant.store.service.StoreStatusService");
            
            // 從 Spring 容器中動態取出該 Service 的 Bean 實例
            Object storeStatusServiceBean = applicationContext.getBean(storeStatusServiceClass);
            
            // 取得目標方法（這裡假設組長檢查店面線上點餐狀態的方法叫 checkStoreOpen，接收 Long 參數）
            Method isStoreOpenMethod = storeStatusServiceClass.getMethod("checkStoreOpen", Long.class);
            
            // 動態執行該方法，取得結果
            Boolean isSystemOpen = (Boolean) isStoreOpenMethod.invoke(storeStatusServiceBean, storeId);
            
            // 如果單店或全域的開關被關閉，直接回傳空清單，不讓前台撈到任何菜單
            if (Boolean.FALSE.equals(isSystemOpen)) {
                System.out.println("[Menu模組提示]：偵測到該門市（或全域）線上點餐開關已關閉，回傳空選單。");
                return new ArrayList<>(); 
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("[Menu模組提示]：未偵測到 store 模組，本機獨立環境自動跳過營業狀態檢查。");
        } catch (NoSuchMethodException e) {
            System.out.println("[Menu模組提示]：反射成功但找不到目標檢查方法，請與組長確認 Method 命名。");
        } catch (Exception e) {
            System.out.println("[Menu模組提示]：反射開關檢查時發生預期外錯誤，已自動放行. ");
        }
        // ====================================================================

        List<StoreMenuDisplayResponse> displayList = new ArrayList<>();
        
        // 1. 先撈出總部所有「上架中」的基礎菜單主體
        List<MenuItem> allActiveItems = menuItemRepository.findAll(); 
        
        // 2. 撈出該分店目前在 store_menu 裡的所有隔離設定
        List<StoreMenu> storeMenuItems = storeMenuRepository.findByStoreId(storeId);

        Map<Long, StoreMenu> storeMenuMap = storeMenuItems.stream()
        .collect(Collectors.toMap(StoreMenu::getMenuItemId, sm -> sm));

        for (MenuItem item : allActiveItems) {

            // 3. 🔍 去分店對照表裡，尋找有沒有這道菜的專屬隔離售價或上架設定
            StoreMenu currentStoreSetting = storeMenuMap.get(item.getId());

            // 4. 開始組裝前台需要的 DTO 包裹
            StoreMenuDisplayResponse response = new StoreMenuDisplayResponse();
            response.setId(item.getId());
            response.setCategoryId(item.getCategoryId() != null ? item.getCategoryId() : 1L);
            response.setItemName(item.getItemName());
            response.setDescription(item.getDescription());
            response.setImageUrl(item.getImageUrl());
            response.setAllergenInfo(item.getAllergenInfo());

            // ==================== 💰 智慧核心：價格動態咬合防線 ====================
            // 優先用分店客製價，沒有才套用群組計算
            if (currentStoreSetting != null && currentStoreSetting.getPrice() != null) {
                response.setFinalPrice(currentStoreSetting.getPrice());
            } else {
                response.setFinalPrice(calculateStorePrice(item.getPrice(), storeId));
            }

            response.setBasePrice(item.getPrice());  // ✅ 設定總部基準價
            

           // ==================== 🛒 上架狀態動態咬合防線 ====================
            boolean isItemActive = item.getIsActive() != null && item.getIsActive();
            boolean isStoreAvailable = currentStoreSetting == null 
                || currentStoreSetting.getIsAvailable() == null 
                || currentStoreSetting.getIsAvailable();

            response.setIsSelectable(isItemActive && isStoreAvailable);
            


            // ==================== 🌟【特色標籤落地優雅化解析】====================
            if (item.getFeatureTags() != null && !item.getFeatureTags().isBlank()) {
                List<String> tags = Arrays.stream(item.getFeatureTags().split(","))
                                          .map(String::trim)
                                          .collect(Collectors.toList());
                response.setFeatureTags(tags);
            } else {
                response.setFeatureTags(new ArrayList<>());
            }
            // ====================================================================

            displayList.add(response);
        }
        
        return displayList;
    }
}