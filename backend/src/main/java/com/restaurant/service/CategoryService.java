//Service 層的任務就是：「把零散的數據（參數），處理成有意義的狀態（物件），最後交給持久層（Repository）
package com.restaurant.service; // 👈 補上這行，告訴電腦妳在二樓辦公室！

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.restaurant.entity.CategoryMenu;
import com.restaurant.repository.CategoryMenuRepository; // 2. 補上搬運工的聯絡電話！

@Service // 💡 貼上這張貼紙，Spring Boot 就會知道：「喔！這是一位專門動腦的業務員！」
public class CategoryService {

    @Autowired // 💡 貼上這張貼紙，電腦會自動把「倉庫搬運工」派給這個業務員當助手；如果沒有這張便利貼，妳的 private CategoryMenuRepository repository; 就只是一個空殼子，它不具備任何連線能力。一旦貼上 @Autowired，Spring Boot 就會像拉電話線一樣，把一個已經連好資料庫的實體接到這個變數上。
    private CategoryMenuRepository repository; //寫這行的目的:在辦公室裡「留一個位置」給搬運工（Repository內存佔位），並規定這條線路「只能傳輸什麼樣的指令」，再給這個組件取一個名字（引用名稱），在下面的 createCategory 動作裡就能寫 repository.save(...)
    // 搬運工的名字叫 repository；private是告訴電腦：「這個搬運工是 CategoryService 專用的，外面的類別（像是別的 Service）不能直接過來指派他做事
    //類別類型:CategoryMenuRepository告訴電腦：「我需要的是一個懂 CategoryMenu 搬運規則的專業人員。
    // 變數名稱:repository，給這個幫手取的「綽號」。在後面的程式碼裡，妳只要打 repository.save()，電腦就知道妳在叫這位幫手。
    
    // 接下來，我們要教業務員怎麼做事...
    //動作：員工新增菜單分類

    public void createCategory(String name, Integer order){ 
        //這是一個 public（公開的：代表這個方法可以被一樓 Controller 或是其他地方呼叫） void（不回傳的：代表這個動作只需要完成存入資料庫的任務，不需要吐回任何計算結果） 的 createCategory 方法
        //這個方法的小括號裡，我們定義了兩個 「臨時外號（參數）」：一個是 String name（用來接收傳進來的文字名稱），另一個是 Integer order（用來接收傳進來的排序數字） 
        //Controller呼叫一個createCategory 動作！server叫他裡面要新增名稱+順序給我，不然無法開工；櫃台給資料後，業務員就開示執行{}裡的東西了。
        // 1. 【誕生儀式】：呼叫建構子，生出一個空白分類夾

        //空白的簡易文件要自己用new出來，就像是給他一張空白的文件，讓他寫上名字；
        //印好了「名稱欄」、「順序欄」的空白格式；再從抽屜拿出一張新的空白紙。如果沒有這張紙，妳手上的「名字」跟「順序」就沒地方寫。
        CategoryMenu newCategory = new CategoryMenu();
        //實例化：在記憶體堆區開闢一個空間，把 Entity 的藍圖變成一個真正活著、可以裝資料的物件）。我們給這個新物件取個代碼叫 newCategory
        
        // 2. 【設定資料】：填入名字與順序
        // 💡 提醒：這裡的 setCategoryName 必須跟妳 Entity 裡的變數名對齊喔！
        //這是呼叫物件內部的 set 方法（設定器：把剛才那個「臨時外號 name」裡的文字，塞進這個物件專屬的 name 格子裡）。
        newCategory.setName(name);
        newCategory.setSortOrder(order);

        // 3. 【下達指令】：叫搬運工存進去
        repository.save(newCategory);
    }
    
}


