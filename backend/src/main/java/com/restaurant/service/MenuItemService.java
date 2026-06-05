package com.restaurant.service; // 👈 同樣在二樓業務辦公室！

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.restaurant.entity.MenuItem; // 1. 換成餐點的設計圖！
import com.restaurant.repository.MenuItemRepository; // 2. 換成餐點搬運工的電話！

import java.util.List; // 👈 告訴電腦：我要使用 Java 內建的清單工具！

@Service // 💡 貼上貼紙，讓 Spring Boot 知道這是一位專門處理「餐點業務」的動腦業務員！
public class MenuItemService {

    @Autowired // 💡 自動拉好電話線，把「餐點倉庫搬運工」指派進來當助手
    private MenuItemRepository menuItemRepository; // 在辦公室留個位置給餐點搬運工

    /**
     * 動作：員工新增新餐點功能
     * 我們這裡直接接收一個裝好資料的 MenuItem 物件（或是你未來會學到的 DTO）
     */
    public void createFood(MenuItem menuItemData) {
        
        // 1. 檢查是不是根本沒傳名字進來（Null 檢查）或只打了「空字串」（像是 "" 這樣裡面沒字)
        if (menuItemData.getItemName() == null || menuItemData.getItemName().equals("")){
            throw new IllegalArgumentException("開工失敗:餐點名稱不能是空的！");
        }


        // 1. 【誕生儀式】：從抽屜拿出一張全新、空白的餐點夾
        // 雖然傳進來的 menuItemData 已經有資料，但為了安全起見，
        // 我們在 Service 層自己 new 一個乾淨的實體，或者直接對傳進來的資料做處理。
        MenuItem newFood = new MenuItem();
        
        // 2. 【設定資料】：運用三段式思維，把資料一個個精準填入格式中
        // 💡 核心提醒：千萬不要 setId() 喔！讓資料庫 AUTO_INCREMENT 自己發號碼牌。
        newFood.setCategoryId(menuItemData.getCategoryId());   // 填入所屬分類 ID (Long)
        newFood.setItemName(menuItemData.getItemName());       // 填入餐點名稱
        newFood.setDescription(menuItemData.getDescription()); // 填入美味描述
        newFood.setPrice(menuItemData.getPrice());             // 填入價格 (base_price)
        newFood.setImageUrl(menuItemData.getImageUrl());       // 填入圖片網址
        newFood.setAllergenInfo(menuItemData.getAllergenInfo()); // 填入過敏原資訊
        newFood.setSortOrder(menuItemData.getSortOrder()); // 👈 把前台傳來的排序也填進空白夾子！
        
        // 業務邏輯貼心設定：新上架的餐點，預設直接幫他開起「啟用中」狀態！
        newFood.setIsActive(true); 

        // 3. 【下達指令】：叫餐點搬運工把這盤香噴噴的新餐點存進資料庫！
        menuItemRepository.save(newFood);
    }

    /**
    * 動作1：獲取前台要展示的餐點列表（只拿啟用中、且排好序的）
    */
   public List<MenuItem> getActiveMenuItems(){
        // 業務員不囉唆，直接指派懂餐點的搬運工去使出大招，並把整箱餐點清單（List）搬回來
        return menuItemRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
    * 動作2：修改現有的餐點資料
    * 參數：id（我們要改哪一盤菜）、menuItemData（前台送過來的新資料包裹）
    */
    public void updateFood(Long id, MenuItem menuItemData){

        // 1. 【找出來】：請搬運工去資料庫把「原本舊的餐點夾子」撈出來
        // .findById(id).orElse(null) 的意思是：幫我在menuItemRepository找這個 ID，如果找不到，就吐回 null 給我
        MenuItem oldFood = menuItemRepository.findById(id).orElse(null);

        // 💡 觸發「大腦核心提醒」的 if 攔截網！
    // 思考：「如果現在突然發現資料庫裡根本沒有這筆資料（舊夾子是空的），我要怎麼反應？」
        if(oldFood == null){
            throw new IllegalArgumentException("修改失敗：資料庫沒有這筆餐點！");
        }
        // 💡 順手幫新資料做初學者防呆：如果新名字是空的，拒絕修改！
        if(menuItemData.getItemName() == null || menuItemData.getItemName().equals("")) {
             throw new IllegalArgumentException("修改失敗：餐點名稱不能改成空值！");
        }
       
        // 2. 【擦掉改寫】：如果都沒有上述的問題，業務員打開舊夾子，拿著新包裹（menuItemData）裡的 150 元，硬生生【擦掉並寫入】（oldFood.setPrice(...)）舊夾子原本的價格格子。現在，舊夾子裡面的價格被【改寫】成 150 元了！
        // 用前台送來的新資料，覆蓋掉舊夾子裡的內容
        //舉例:業務員右手伸進**「新包裹（menuItemData）」裡，大喊一聲 getPrice()，把裡面寫著 150 元 的小紙條【拿了出來】**；
        // 緊接著，業務員轉過身，用左手**【打開舊夾子（oldFood）】，找到裡面原本寫著舊價格的格子 setPrice(...)，【把剛剛右手的 150 元硬生生塞進去，擦掉並覆蓋掉舊價格】
        oldFood.setCategoryId(menuItemData.getCategoryId());
        oldFood.setItemName(menuItemData.getItemName());
        oldFood.setDescription(menuItemData.getDescription());
        oldFood.setPrice(menuItemData.getPrice());
        oldFood.setImageUrl(menuItemData.getImageUrl());
        oldFood.setAllergenInfo(menuItemData.getAllergenInfo());
        oldFood.setSortOrder(menuItemData.getSortOrder());
        // 備註：修改時我們不主動去動它的 isActive 狀態，前台傳來是什麼就是什麼

        // 3. 【存回去】：因為 oldFood 身上本來就有 id，搬運工 save 時會自動去修改那筆舊資料，不會建立新的！
        menuItemRepository.save(oldFood);
    }

    /**
    * 動作3：軟刪除（一鍵下架餐點）
    * 參數：id（經理給的號碼牌）
    */
    public void deleteFood(Long id) {
    
        // 1. 【指派與搬運】：叫搬運工去地下室把舊夾子搬上來
        MenuItem food = menuItemRepository.findById(id).orElse(null);
    
        // 💡 觸發「大腦核心提醒」的攔截網：如果突然發現是空的...
        if (food == null) {
            throw new IllegalArgumentException("下架失敗：找不到該餐點！");
        }
    
        // 2. 【左手塗改】：不需要看新包裹，直接左手一筆劃掉，改成 false！
        food.setIsActive(false);
    
        // 3. 【下令存檔】：叫搬運工送回地下室覆蓋存檔
        menuItemRepository.save(food);
    }
}