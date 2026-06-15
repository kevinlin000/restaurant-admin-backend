-- ============================================================
-- 找門市 Demo 內容精修
-- 用途：在既有測試資料上補強門市文案、交通、停車與展示語氣
-- 執行時機：已跑過 01_create_tables.sql + 02_insert_test_data.sql 之後
-- ============================================================

USE restaurant_db;

UPDATE store
SET
    mrt_info = '捷運市政府站 3 號出口步行 5 分鐘，連通百貨空橋動線',
    parking_info = '統一時代百貨地下停車場，消費可依商場規則折抵；尖峰建議提早 15 分鐘抵達',
    description = '信義 A11 店是敘日的城市旗艦店，靠窗區保留台北夜景視野，板前座位可以看見出餐節奏，包廂則適合生日、紀念日與商務款待。晚餐時段建議提前訂位，若有蛋糕、花束或長輩同行，可於訂位備註先告知門市。'
WHERE store_code = 'TPE001';

UPDATE store
SET
    mrt_info = '捷運南港站、台鐵、高鐵共構，CITYLINK 連通道可直達',
    parking_info = 'CITYLINK 地下停車場，展覽檔期車流較高；用餐可折抵 2 小時',
    description = '南港 CITYLINK 店主打車站直達與展後聚餐，動線俐落、入席速度快，適合商務午餐、展覽結束後的團體聚會與家庭用餐。店內保留開放式廚房視線，尖峰時段仍能維持安靜舒適的用餐感。'
WHERE store_code = 'TPE002';

UPDATE store
SET
    mrt_info = '新竹火車站轉乘巨城接駁車約 8 分鐘，公車民族路口站步行 3 分鐘',
    parking_info = '巨城購物中心地下停車場，親子車位與無障礙車位較集中於 B2',
    description = '新竹巨城店服務科學園區客群與家庭聚餐，空間採明亮木質、寬桌距與親子友善座席。午餐適合快速完整的商務聚會，晚餐則保留彈性併桌與包廂，方便家庭慶生或小型團體。'
WHERE store_code = 'HSC001';

UPDATE store
SET
    mrt_info = '公車科博館站步行 4 分鐘，勤美綠園道沿線步行可達',
    parking_info = '勤美誠品綠園道停車場及周邊路外停車場；假日建議停公益路周邊停車場',
    description = '台中勤美店以綠園道景觀、沉穩包廂與晚餐酒水搭配為亮點。窗景區適合朋友聚會，包廂區適合慶祝與接待，吧台座位則提供更接近料理現場的用餐節奏。'
WHERE store_code = 'TXG001';

UPDATE store
SET
    mrt_info = '台南火車站轉乘公車約 12 分鐘，南紡購物中心站下車即達',
    parking_info = '南紡購物中心地下停車場，消費可折抵；連假午後車位較滿',
    description = '台南南紡店延續府城溫潤色調，以半開放包廂和寬敞主用餐區為主。午餐時段節奏輕快，適合購物中途用餐；晚餐則推薦給重視氛圍、想慢慢用餐的情侶與家庭。'
WHERE store_code = 'TNN001';

UPDATE store
SET
    mrt_info = '輕軌夢時代站步行 3 分鐘，捷運凱旋站可轉乘接駁車',
    parking_info = '夢時代購物中心停車場，平日停車充足；假日建議由成功二路入口進場',
    description = '高雄夢時代店是南台灣大型聚餐據點，港景區、團體桌與包廂配置完整，適合家庭聚會、公司餐敘與節慶慶祝。週一固定公休，連假營業狀態請以門市公告為準。'
WHERE store_code = 'KHH001';

UPDATE store
SET
    mrt_info = '花蓮轉運站步行約 10 分鐘，花蓮火車站搭車約 8 分鐘',
    parking_info = '遠東百貨停車場，周邊和平路亦有路外停車場；旅遊旺季建議預留停車時間',
    description = '花蓮遠百店以石材、木紋與留白空間呈現旅途中的慢食氛圍，座位數較精簡但桌距舒適。適合旅行途中安排一段安靜完整的用餐時間，也適合小家庭與雙人約會。'
WHERE store_code = 'HUA001';

UPDATE store_holiday
SET reason = '內部教育訓練，午晚餐皆暫停訂位'
WHERE store_id = 1 AND holiday_date = '2026-06-24';

UPDATE store_holiday
SET reason = '館內設備保養，門市暫停對外營業'
WHERE store_id = 2 AND holiday_date = '2026-07-02';

UPDATE store_holiday
SET reason = '商場年度保養，暫停午晚餐服務'
WHERE store_id = 3 AND holiday_date = '2026-07-08';

UPDATE store_holiday
SET reason = '包場活動，當日不開放一般訂位'
WHERE store_id = 4 AND holiday_date = '2026-06-30';

UPDATE store_holiday
SET reason = '內場設備保養，晚餐時段暫停'
WHERE store_id = 5 AND holiday_date = '2026-07-01';

UPDATE store_holiday
SET reason = '夢時代館內年度檢修，門市公休'
WHERE store_id = 6 AND holiday_date = '2026-07-06';

UPDATE store_holiday
SET reason = '地方活動交通管制，暫停開放訂位'
WHERE store_id = 7 AND holiday_date = '2026-06-29';
