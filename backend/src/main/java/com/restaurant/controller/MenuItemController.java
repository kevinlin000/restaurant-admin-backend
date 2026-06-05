package com.restaurant.controller; // 宣告檔案位置。這份檔案住在「控制中心 (controller)」資料夾。

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.restaurant.entity.MenuItem; // 把「餐點身分證 (Entity)」拿過來，讓接待員知道新貨物長什麼樣。
import com.restaurant.service.MenuItemService; // 👈 升級：把二樓的「餐點業務員」請過來！
import java.util.List;

@RestController // 告訴 Spring 這是櫃檯接待員，負責接收前端網頁的請求，並吐回 JSON 格式。
@RequestMapping("/api/menu-items") // 👈 餐廳的餐點分機！當客人在網址輸入 /api/menu-items，就由這位接待員負責。
public class MenuItemController {

    @Autowired // 「自動媒合」。Spring 會自動把我們剛剛寫好的 MenuItemService 拉一條電話線接進來。
    private MenuItemService menuItemService; // 在接待員旁邊安排一個專屬的二樓業務員分機，方便隨時轉接業務。

    /**
     * 動作1：員工敲鍵盤新增餐點
     * 網址：POST http://localhost:8080/api/menu-items
     */
    @PostMapping // 💡 貼上這張貼紙，代表這個方法是專門處理「POST 請求」（也就是新增資料、送信進來的動作）
    public String createMenuItem(@RequestBody MenuItem menuItemData) {
        // 💡 @RequestBody 的魔術：
        // 前端網頁送過來的 JSON 資料（例如：{"itemName": "鮭魚握壽司", "price": 120...}），
        // Spring Boot 會自動像拆包裹一樣，把裡面的數值通通塞進 menuItemData 這個物件裡！

        // 接待員自己不動腦，立刻打電話給二樓專門動腦的業務員，把包裹交給他：
        menuItemService.createFood(menuItemData);

        // 任務完成後，跟前台回報一聲口頭禪，代表大功告成！
        return "恭喜！新餐點「" + menuItemData.getItemName() + "」已成功上架！";
    }

    //我們在類別最上方貼了 @RequestMapping("/api/menu-items")（主線路），再加上這裡的 /active（子分機），合起來的完整網址就是：GET http://localhost:8080/api/menu-items/active。
    // 當客人在瀏覽器輸入這個網址時，訊號就會一秒傳送到下面這個動作。
    /**
     * 動作2：前台網頁打來要「上架中的餐點菜單」
     * 網址動作：GET http://localhost:8080/api/menu-items/active
     */
    @GetMapping("/active") // 💡 貼上 Get 貼紙，櫃檯接待員（Controller）在門口貼上一張告示：「只要有人用 GET（查閱）的方式撥打 /api/menu-items/active 這支分機，一律由我這條線路來接聽電話！」
    public List<MenuItem> getActiveMenuItems() {
        //List<MenuItem>:「回傳型別」。代表這個動作執行完畢後，櫃檯接待員承諾會端出「一整箱裝滿餐點（MenuItem）的清單」給客人。
        //getActiveMenuItems():這個動作的 Java 名字（方法名稱），我們取一個好懂的名字叫「獲取啟用中的餐點」
    
        return menuItemService.getActiveMenuItems();
        //他立刻轉身，撥打對內分機給二樓專門動腦的業務員（Service），喊一聲：「喂！二樓的，前台客人要看現在有上架的菜單，快把那箱資料給我！
        //return:把那箱資料交給櫃檯後，櫃檯接待員立刻把這箱資料（List）端給網頁前端。
    }

    /**
    * 動作三：前台主管修改餐點
    * 網址動作：PUT http://localhost:8080/api/menu-items/1
    */
   @PutMapping("/{id}") // 💡 修改「現有對象」的標準貼紙
    public String updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItemData) {
    // 1. 【接聽】：@PathVariable 抓號碼牌 (id)
    // 2. 【拆包】：@RequestBody 拿新包裹 (menuItemData)

    // 3. 【轉交】：打給二樓業務員，請他執行「找舊、塗改、存回」的動作
        menuItemService.updateFood(id, menuItemData);

        return "餐點編號 " + id + " 已修改完成，資料庫已更新！";
    }

    /**
    * 動作四：前台主管點擊「下架餐點」按鈕
    * 網址動作：DELETE http://localhost:8080/api/menu-items/1  (代表下架 id=1 的餐點)
    */
   @DeleteMapping("/{id}") // 💡 貼上刪除貼紙，{id} 代表網址最後面的數字會變成號碼牌變數
    public String deleteMenuItem(@PathVariable Long id) {
        // @PathVariable 的魔術：會自動把網址最後面的數字抓出來，塞進這個 id 變數裡！
    
        // 櫃檯接待員自己不動腦，立刻打電話轉接二樓，把號碼牌交給業務員處置：
        menuItemService.deleteFood(id);
    
        // 業務員辦完事後，櫃檯大聲回報成果！
        return "餐點編號 " + id + " 已成功下一鍵架，前台將不會再顯示這道美味餐點！";
}
}