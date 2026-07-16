import aburiSalmonImage from "@/assets/images/aburi-salmon-sushi.jpg";
import caramelPuddingImage from "@/assets/images/caramel-pudding.jpg";
import homeImage from "@/assets/images/Home.jpg";
import japaneseSakeImage from "@/assets/images/japanese-sake.jpg";
import matchaDessertImage from "@/assets/images/matcha-dessert.jpg";
import reservationImage from "@/assets/images/reservation.jpg";
import salmonSashimiImage from "@/assets/images/salmon-sashimi.jpg";
import sashimiImage from "@/assets/images/sashimi.jpg";
import seafoodSaladImage from "@/assets/images/seafood-salad.jpg";
import sushiImage from "@/assets/images/sushi.jpg";
import sukiyakiImage from "@/assets/images/sukiyaki.jpg";
import tempuraImage from "@/assets/images/tempura.jpg";
import tofuImage from "@/assets/images/tofu.jpg";

const formatDate = (offsetDays) => {
  const date = new Date();
  date.setDate(date.getDate() + offsetDays);
  return date.toISOString().slice(0, 10);
};

export const demoHomepage = {
  heroScenes: [
    {
      label: "夜席",
      imageUrl: homeImage,
      eyebrow: "Xuri Washoku",
      title: "敘日和食",
      lines: ["把訂位、點餐與付款整理成一條清楚流程", "讓餐廳營運從前台到後台都能被追蹤"],
    },
    {
      label: "旬味",
      imageUrl: sashimiImage,
      eyebrow: "Seasonal Table",
      title: "今日旬味",
      lines: ["菜單、價格與門市狀態由後台控管", "前台只呈現顧客當下需要的資訊"],
    },
    {
      label: "營運",
      imageUrl: reservationImage,
      eyebrow: "Operations",
      title: "一張桌背後的系統",
      lines: ["訂位容量、營業時段與消息發布都會連動", "讓 demo 不是切版，而是可營運的流程"],
    },
  ],
  featuredStoreIds: [1, 2, 3],
  featuredNewsIds: [101, 102, 103],
  storyKicker: "Demo Mode",
  storyTitle: "線上展示版保留完整品牌與營運情境。",
  storyDescription:
    "目前 Vercel 版不連接付費雲端資料庫，因此以前端展示資料呈現首頁、門市、消息、FAQ 與菜單。完整登入、後台與付款流程可在本機環境展示。",
};

export const demoStores = [
  {
    storeId: 1,
    storeName: "敘日信義 A11 店",
    city: "台北市",
    district: "信義區",
    address: "台北市信義區松壽路 11 號 6 樓",
    phone: "02-2722-1688",
    mrtInfo: "捷運台北 101/世貿站步行 6 分鐘",
    latitude: 25.036,
    longitude: 121.567,
    openNow: true,
    reservationOpenDays: 30,
    description: "商圈聚餐與晚間約會主力門市，支援線上訂位與桌型容量控管。",
    mainImageUrl: homeImage,
    imageUrls: [homeImage, sashimiImage, japaneseSakeImage],
    featureTags: [
      { featureKey: "counter", featureLabel: "吧台座位", sortOrder: 1 },
      { featureKey: "reservation", featureLabel: "可線上訂位", sortOrder: 2 },
      { featureKey: "late", featureLabel: "晚餐熱門", sortOrder: 3 },
    ],
    storeHours: [
      { dayOfWeek: 1, mealPeriod: "LUNCH", openTime: "11:30", closeTime: "14:30" },
      { dayOfWeek: 1, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 2, mealPeriod: "LUNCH", openTime: "11:30", closeTime: "14:30" },
      { dayOfWeek: 2, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 3, mealPeriod: "LUNCH", openTime: "11:30", closeTime: "14:30" },
      { dayOfWeek: 3, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 4, mealPeriod: "LUNCH", openTime: "11:30", closeTime: "14:30" },
      { dayOfWeek: 4, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 5, mealPeriod: "LUNCH", openTime: "11:30", closeTime: "14:30" },
      { dayOfWeek: 5, mealPeriod: "DINNER", openTime: "17:30", closeTime: "22:00" },
      { dayOfWeek: 6, mealPeriod: "ALL_DAY", openTime: "11:30", closeTime: "22:00" },
      { dayOfWeek: 7, mealPeriod: "ALL_DAY", openTime: "11:30", closeTime: "21:30" },
    ],
    upcomingHolidays: [{ holidayDate: formatDate(10), reason: "設備保養" }],
    tables: [
      { tableId: 1, tableSize: 2, zone: "A 區", status: "AVAILABLE" },
      { tableId: 2, tableSize: 4, zone: "A 區", status: "AVAILABLE" },
      { tableId: 3, tableSize: 6, zone: "包廂", status: "AVAILABLE" },
    ],
  },
  {
    storeId: 2,
    storeName: "敘日台中勤美店",
    city: "台中市",
    district: "西區",
    address: "台中市西區公益路 68 號",
    phone: "04-2328-1688",
    mrtInfo: "勤美綠園道步行 3 分鐘",
    latitude: 24.151,
    longitude: 120.663,
    openNow: true,
    reservationOpenDays: 21,
    description: "家庭聚餐與週末訂位較多，後台可調整門市營業時段與可訂日期。",
    mainImageUrl: seafoodSaladImage,
    imageUrls: [seafoodSaladImage, tempuraImage, matchaDessertImage],
    featureTags: [
      { featureKey: "family", featureLabel: "家庭聚餐", sortOrder: 1 },
      { featureKey: "parking", featureLabel: "鄰近停車場", sortOrder: 2 },
      { featureKey: "dessert", featureLabel: "甜點推薦", sortOrder: 3 },
    ],
    storeHours: [
      { dayOfWeek: 1, mealPeriod: "LUNCH", openTime: "11:30", closeTime: "14:30" },
      { dayOfWeek: 1, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 3, mealPeriod: "LUNCH", openTime: "11:30", closeTime: "14:30" },
      { dayOfWeek: 3, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 5, mealPeriod: "DINNER", openTime: "17:30", closeTime: "22:00" },
      { dayOfWeek: 6, mealPeriod: "ALL_DAY", openTime: "11:30", closeTime: "22:00" },
      { dayOfWeek: 7, mealPeriod: "ALL_DAY", openTime: "11:30", closeTime: "21:30" },
    ],
    upcomingHolidays: [],
    tables: [
      { tableId: 4, tableSize: 2, zone: "窗邊", status: "AVAILABLE" },
      { tableId: 5, tableSize: 4, zone: "主廳", status: "AVAILABLE" },
      { tableId: 6, tableSize: 8, zone: "包廂", status: "AVAILABLE" },
    ],
  },
  {
    storeId: 3,
    storeName: "敘日高雄駁二店",
    city: "高雄市",
    district: "鹽埕區",
    address: "高雄市鹽埕區大勇路 1 號",
    phone: "07-521-1688",
    mrtInfo: "捷運鹽埕埔站步行 8 分鐘",
    latitude: 22.62,
    longitude: 120.282,
    openNow: false,
    reservationOpenDays: 30,
    description: "港邊觀光與晚餐餐期為主，適合展示門市狀態與營業時段連動。",
    mainImageUrl: japaneseSakeImage,
    imageUrls: [japaneseSakeImage, sukiyakiImage, caramelPuddingImage],
    featureTags: [
      { featureKey: "view", featureLabel: "港邊景觀", sortOrder: 1 },
      { featureKey: "sake", featureLabel: "清酒搭餐", sortOrder: 2 },
      { featureKey: "group", featureLabel: "團體聚餐", sortOrder: 3 },
    ],
    storeHours: [
      { dayOfWeek: 2, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 3, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 4, mealPeriod: "DINNER", openTime: "17:30", closeTime: "21:30" },
      { dayOfWeek: 5, mealPeriod: "DINNER", openTime: "17:30", closeTime: "22:00" },
      { dayOfWeek: 6, mealPeriod: "ALL_DAY", openTime: "11:30", closeTime: "22:00" },
      { dayOfWeek: 7, mealPeriod: "ALL_DAY", openTime: "11:30", closeTime: "21:30" },
    ],
    upcomingHolidays: [{ holidayDate: formatDate(17), reason: "店休" }],
    tables: [
      { tableId: 7, tableSize: 2, zone: "吧台", status: "AVAILABLE" },
      { tableId: 8, tableSize: 4, zone: "主廳", status: "AVAILABLE" },
      { tableId: 9, tableSize: 6, zone: "景觀區", status: "AVAILABLE" },
    ],
  },
];

export const demoCities = [...new Set(demoStores.map((store) => store.city))];

export const getDemoDistricts = (city) => [
  ...new Set(demoStores.filter((store) => store.city === city).map((store) => store.district)),
];

export const getDemoStoreDetail = (storeId) =>
  demoStores.find((store) => String(store.storeId) === String(storeId)) || demoStores[0];

export const demoNews = [
  {
    newsId: 101,
    category: "event",
    categoryLabel: "活動",
    title: "夏季旬味菜單上線",
    summary: "刺身、冷物與炙燒握壽司同步更新，適合聚餐與晚間小酌。",
    storeScope: "全門市",
    publishedAt: "2026-07-01",
    periodLabel: "2026.07.01 - 2026.08.31",
    coverImageUrl: seafoodSaladImage,
    isFeatured: true,
  },
  {
    newsId: 102,
    category: "notice",
    categoryLabel: "公告",
    title: "假日熱門時段建議提前訂位",
    summary: "系統會依門市桌型與時段容量回覆可訂選項，完成後可查看訂位狀態。",
    storeScope: "全門市",
    publishedAt: "2026-07-03",
    periodLabel: "長期公告",
    coverImageUrl: reservationImage,
    isFeatured: false,
  },
  {
    newsId: 103,
    category: "member",
    categoryLabel: "會員",
    title: "會員點數折抵功能開放展示",
    summary: "會員登入後可累積點數，結帳流程可依規則折抵消費金額。",
    storeScope: "全門市",
    publishedAt: "2026-07-08",
    periodLabel: "長期公告",
    coverImageUrl: matchaDessertImage,
    isFeatured: false,
  },
  {
    newsId: 104,
    category: "opening",
    categoryLabel: "展店",
    title: "高雄駁二店試營運",
    summary: "港邊景觀門市加入營運後台，可獨立維護營業時間與公告。",
    storeScope: "高雄駁二店",
    publishedAt: "2026-07-12",
    periodLabel: "2026.07",
    coverImageUrl: japaneseSakeImage,
    isFeatured: false,
  },
];

export const demoFaqs = [
  {
    faqId: 1,
    category: "RESERVATION",
    question: "訂位最早可以預約多久之後的日期？",
    answer: "線上訂位開放未來 30 天內的餐期。熱門餐期建議提前安排，若指定時段額滿，可改選相近門市或其他時段。",
    keywords: "訂位 預約 30天 一個月 熱門餐期 時段",
  },
  {
    faqId: 2,
    category: "RESERVATION",
    question: "訂位需要在幾點前抵達？",
    answer: "請依訂位時間準時抵達。若會晚到，建議先聯繫門市；座位通常會保留 10 分鐘，實際安排仍依當日候位狀況與門市說明為準。",
    keywords: "遲到 保留 抵達 候位 10分鐘 門市",
  },
  {
    faqId: 3,
    category: "RESERVATION",
    question: "可以修改訂位人數或時間嗎？",
    answer: "可於訂位紀錄中查看是否仍可調整。若餐期已額滿、接近用餐時間或涉及訂金，建議直接聯繫門市協助確認。",
    keywords: "修改訂位 改時間 改人數 訂位紀錄 額滿",
  },
  {
    faqId: 4,
    category: "RESERVATION",
    question: "大人數或包廂訂位要怎麼預約？",
    answer: "8 人以上、包廂或特殊餐期建議提前洽詢門市。門市會依座位配置、餐期人流與是否需要訂金，回覆可預約時段。",
    keywords: "大人數 包廂 多人訂位 特殊餐期 門市洽詢",
  },
  {
    faqId: 5,
    category: "DEPOSIT",
    question: "哪些訂位需要支付訂金？",
    answer: "特殊餐期、包廂、大人數或高需求時段可能需要支付訂金。系統會在訂位流程中顯示是否需付訂金與付款期限。",
    keywords: "訂金 付款期限 包廂 大人數 特殊餐期",
  },
  {
    faqId: 6,
    category: "DEPOSIT",
    question: "訂金付款期限是多久？",
    answer: "若訂位需要訂金，請依頁面顯示的期限完成付款。逾期未付款時，系統可能釋出座位供其他顧客預約。",
    keywords: "訂金期限 付款期限 逾期 釋出座位 付款",
  },
  {
    faqId: 7,
    category: "DEPOSIT",
    question: "取消訂位後訂金可以退嗎？",
    answer: "符合取消期限的訂位可依原付款方式辦理退款；若已超過取消期限、未到或臨時取消，訂金規則會依該餐期公告處理。",
    keywords: "取消 退款 訂金退還 未到 no show",
  },
  {
    faqId: 8,
    category: "DEPOSIT",
    question: "沒有準時支付訂金會怎麼樣？",
    answer: "未於期限內付款的訂位不會完成保留。若仍想用餐，可重新選擇目前可預約的門市、日期與時段。",
    keywords: "未付款 訂金 保留 重新訂位 逾期",
  },
  {
    faqId: 9,
    category: "ORDER",
    question: "可以先線上點餐再到店用餐嗎？",
    answer: "目前點餐功能支援依門市菜單建立訂單。內用訂單需選擇門市與桌位；外帶訂單可不指定桌位，實際供應品項依各門市設定為準。",
    keywords: "點餐 內用 外帶 桌位 門市菜單",
  },
  {
    faqId: 10,
    category: "ORDER",
    question: "可以外帶點餐嗎？",
    answer: "可以。外帶訂單可選擇支援外帶的門市與供應品項，完成付款後依頁面或門市通知的時間取餐。",
    keywords: "外帶 點餐 取餐 付款 門市",
  },
  {
    faqId: 11,
    category: "ORDER",
    question: "為什麼不同門市看到的菜單不一樣？",
    answer: "菜單會依門市供應、時段、庫存與營業狀態顯示。若某品項暫停供應，頁面會以當下可販售內容為準。",
    keywords: "菜單 門市菜單 庫存 暫停供應 品項",
  },
  {
    faqId: 12,
    category: "PAYMENT",
    question: "付款方式有哪些？",
    answer: "系統支援信用卡與 Line Pay 等付款流程。部分門市現場付款、發票與退款處理方式，請依結帳頁面與門市說明為準。",
    keywords: "付款 信用卡 Line Pay 發票 退款 綠界 現金",
  },
  {
    faqId: 13,
    category: "PAYMENT",
    question: "發票如何開立或查詢？",
    answer: "線上付款完成後，發票資訊會依結帳頁面填寫內容處理。若需統編或載具，請在付款前確認資料正確。",
    keywords: "發票 統編 載具 付款 電子發票",
  },
  {
    faqId: 14,
    category: "PAYMENT",
    question: "退款通常多久會入帳？",
    answer: "退款送出後會依原付款方式與銀行作業時間入帳。若超過常見處理時間仍未收到，可提供訂單資訊請客服協助查詢。",
    keywords: "退款 入帳 信用卡 Line Pay 訂單資訊",
  },
  {
    faqId: 15,
    category: "STORE",
    question: "如何查詢各門市營業狀態？",
    answer: "可到分店資訊頁查看門市位置、交通方式、營業狀態與特色標籤。若門市暫停營業，系統會避免建立不適用的訂位或點餐流程。",
    keywords: "門市 營業狀態 交通 分店資訊 暫停營業 營業中 公休日",
  },
  {
    faqId: 16,
    category: "STORE",
    question: "門市附近可以停車嗎？",
    answer: "各門市交通與停車條件不同，建議先查看分店資訊頁的交通說明。商場門市請以商場停車與折抵規則為準。",
    keywords: "停車 交通 分店資訊 商場 折抵",
  },
  {
    faqId: 17,
    category: "STORE",
    question: "臨時公休或營業異動會怎麼通知？",
    answer: "若遇設備維護、天候或特殊營運調整，門市狀態會同步更新；已成立的訂位或訂單，會依留存聯絡資料通知。",
    keywords: "公休 營業異動 維護 通知 門市狀態",
  },
  {
    faqId: 18,
    category: "MEMBER",
    question: "一定要登入會員才能訂位或點餐嗎？",
    answer: "一般訂位與 demo 點餐流程可未登入使用；登入會員後可查看個人資料、訂位紀錄與消費紀錄，後續也能累積會員服務。",
    keywords: "會員 登入 訂位紀錄 消費紀錄 個人資料",
  },
  {
    faqId: 19,
    category: "MEMBER",
    question: "登入後可以查看哪些紀錄？",
    answer: "會員可查看個人資料、訂位紀錄、訂單與付款狀態。若使用不同電話或信箱建立紀錄，請以當次留存資料查詢。",
    keywords: "會員 訂位紀錄 訂單紀錄 付款狀態 個人資料 點數",
  },
  {
    faqId: 20,
    category: "SERVICE",
    question: "有特殊需求或過敏資訊要怎麼告知？",
    answer: "建議在訂位備註中先寫明需求，例如兒童椅、包廂、慶生或過敏資訊。門市會依現場條件協助安排，實際結果以門市回覆為準。",
    keywords: "過敏 兒童椅 包廂 慶生 備註 特殊需求 素食",
  },
  {
    faqId: 21,
    category: "SERVICE",
    question: "可以攜帶寵物或外食嗎？",
    answer: "餐廳以現場用餐品質與食品安全為優先，寵物、外食與蛋糕等規則依各門市條件辦理。建議訂位前先與門市確認。",
    keywords: "寵物 外食 蛋糕 食品安全 門市確認",
  },
  {
    faqId: 22,
    category: "SERVICE",
    question: "兒童椅或慶生服務可以先備註嗎？",
    answer: "可以在訂位備註先提出需求。兒童椅、慶生安排與座位配置數量有限，門市會依現場狀況協助保留。",
    keywords: "兒童椅 慶生 備註 座位配置 特殊需求",
  },
];

export const demoMenuStores = demoStores.map((store) => ({
  id: store.storeId,
  name: store.storeName,
}));

export const demoMenuCategories = [
  { id: 1, name: "前菜" },
  { id: 2, name: "刺身" },
  { id: 3, name: "握壽司" },
  { id: 4, name: "主餐" },
  { id: 5, name: "炸物" },
  { id: 6, name: "甜點" },
  { id: 7, name: "飲品" },
  { id: 8, name: "酒類" },
];

export const demoMenuItems = [
  { itemId: 1, categoryId: 1, itemName: "胡麻豆腐", description: "白芝麻慢磨，佐柴魚高湯與山葵。", finalPrice: 120, featureTags: ["手作工法", "經典必點"], imageUrl: tofuImage },
  { itemId: 2, categoryId: 1, itemName: "胡麻菠菜", description: "鮮嫩菠菜搭配濃郁日式焙煎胡麻醬，清爽開胃。", finalPrice: 90, featureTags: ["主廚推薦", "人氣爆棚"], imageUrl: tofuImage },
  { itemId: 3, categoryId: 1, itemName: "和風溫泉蛋", description: "半熟滑嫩溫泉蛋，搭配鰹魚昆布高湯醬汁與蔥花。", finalPrice: 60, featureTags: ["經典必點"], imageUrl: aburiSalmonImage },
  { itemId: 4, categoryId: 1, itemName: "日式香烤茄子", description: "現烤多汁長茄，淋上生薑泥與特製甘純醬油。", finalPrice: 80, featureTags: ["嚴選食材"], imageUrl: seafoodSaladImage },
  { itemId: 5, categoryId: 1, itemName: "和風醃漬小黃瓜", description: "微酸帶甜的清脆小黃瓜，熟客必點的開胃小品。", finalPrice: 50, featureTags: ["季節限定"], imageUrl: seafoodSaladImage },
  { itemId: 6, categoryId: 1, itemName: "築地風厚蛋燒", description: "每日現做高湯玉子燒，口感綿密鬆軟，帶有微甜滋味。", finalPrice: 100, featureTags: ["手作工法", "經典必點"], imageUrl: aburiSalmonImage },
  { itemId: 7, categoryId: 1, itemName: "日式冷拌番茄", description: "熟成番茄切片，淋上特製和風油醋醬與洋蔥碎。", finalPrice: 75, featureTags: ["季節限定", "嚴選食材"], imageUrl: seafoodSaladImage },
  { itemId: 8, categoryId: 1, itemName: "海鮮沙拉", description: "新鮮時蔬搭配鮮蝦、花枝與柚香油醋。", finalPrice: 160, featureTags: ["嚴選食材", "鮮味極致"], imageUrl: seafoodSaladImage },
  { itemId: 9, categoryId: 2, itemName: "綜合生魚片", description: "每日嚴選新鮮漁獲，主廚依當日產地狀態搭配。", finalPrice: 420, featureTags: ["主廚推薦", "鮮味極致"], imageUrl: sashimiImage },
  { itemId: 10, categoryId: 2, itemName: "極上鮭魚肚生魚片", description: "嚴選肥美鮭魚腹肉，油脂豐厚，入口即化。", finalPrice: 380, featureTags: ["主廚推薦", "嚴選食材"], imageUrl: salmonSashimiImage },
  { itemId: 11, categoryId: 2, itemName: "築地風鮪魚生魚片", description: "色澤紅潤的深海鮪魚，肉質紮實且帶有回甘層次。", finalPrice: 360, featureTags: ["經典必點"], imageUrl: sashimiImage },
  { itemId: 12, categoryId: 2, itemName: "鮮美真鯛生魚片", description: "白身魚代表，味道清雅，口感細緻彈牙。", finalPrice: 340, featureTags: ["季節限定"], imageUrl: sashimiImage },
  { itemId: 13, categoryId: 2, itemName: "生食級干貝綜合盛合", description: "北海道干貝搭配甜蝦與鮭魚，呈現海洋甜味。", finalPrice: 480, featureTags: ["極致奢華", "限量供應"], imageUrl: sashimiImage },
  { itemId: 14, categoryId: 3, itemName: "握壽司盛合", description: "主廚推薦 8 貫握壽司，依當日漁獲調整。", finalPrice: 360, featureTags: ["經典必點", "職人精神"], imageUrl: sushiImage },
  { itemId: 15, categoryId: 3, itemName: "炙燒鮭魚壽司", description: "炙燒表面焦香，油脂與醋飯在口中融合。", finalPrice: 180, featureTags: ["人氣爆棚", "鮮味極致"], imageUrl: aburiSalmonImage },
  { itemId: 16, categoryId: 3, itemName: "炙燒焦糖鮭魚握壽司", description: "炙燒鮭魚油脂與香甜焦糖脆層，甜鹹平衡。", finalPrice: 160, featureTags: ["人氣爆棚", "鮮味極致"], imageUrl: aburiSalmonImage },
  { itemId: 17, categoryId: 3, itemName: "盛夏加州卷壽司", description: "酪梨與蟹肉棒入卷，外層飛魚卵帶出脆口層次。", finalPrice: 240, featureTags: ["季節限定", "手作工法"], imageUrl: sushiImage },
  { itemId: 18, categoryId: 3, itemName: "極上星鰻握壽司", description: "整條星鰻刷上香甜特調醬汁，入口鬆軟濃郁。", finalPrice: 220, featureTags: ["職人精神"], imageUrl: sushiImage },
  { itemId: 19, categoryId: 3, itemName: "海老天婦羅花壽司", description: "現炸金黃炸蝦包入壽司，酥脆與醋飯交錯。", finalPrice: 260, featureTags: ["人氣爆棚", "鮮味極致"], imageUrl: tempuraImage },
  { itemId: 20, categoryId: 3, itemName: "辛味噌鮪魚軍艦", description: "細切鮪魚拌入和風辣味噌，微辣開胃。", finalPrice: 150, featureTags: ["主廚推薦", "人氣爆棚"], imageUrl: sushiImage },
  { itemId: 21, categoryId: 4, itemName: "生薑燒肉定食", description: "豬五花與老薑甜醬汁爆炒，經典下飯。", finalPrice: 290, featureTags: ["主廚推薦", "人氣爆棚"], imageUrl: sukiyakiImage },
  { itemId: 22, categoryId: 4, itemName: "鹽烤鯖魚定食", description: "挪威鯖魚鹽烤至外皮金黃酥脆，魚油香氣飽滿。", finalPrice: 280, featureTags: ["嚴選食材", "鮮味極致"], imageUrl: salmonSashimiImage },
  { itemId: 23, categoryId: 4, itemName: "牛肉壽喜燒定食", description: "板腱牛、蒟蒻絲與烤豆腐，以壽喜燒醬汁熬煮。", finalPrice: 360, featureTags: ["職人精神", "嚴選食材"], imageUrl: sukiyakiImage },
  { itemId: 24, categoryId: 4, itemName: "日式碳烤雞肉定食", description: "去骨雞腿肉碳烤至微焦香，搭配特製鹽蔥醬。", finalPrice: 310, featureTags: ["主廚推薦", "手作工法"], imageUrl: sukiyakiImage },
  { itemId: 25, categoryId: 4, itemName: "豪華名古屋鰻魚三吃定食", description: "蒲燒鰻魚可單吃、加配料、注高湯做茶泡飯。", finalPrice: 490, featureTags: ["極致奢華", "經典必點"], imageUrl: sukiyakiImage },
  { itemId: 26, categoryId: 5, itemName: "南蠻炸雞塊", description: "金黃雞塊裹南蠻酸甜醬，再淋手作塔塔醬。", finalPrice: 180, featureTags: ["人氣爆棚", "鮮味極致"], imageUrl: tempuraImage },
  { itemId: 27, categoryId: 5, itemName: "炸蝦天婦羅盛合", description: "大蝦三隻與季節時蔬，外衣輕薄酥脆。", finalPrice: 240, featureTags: ["主廚推薦", "人氣爆棚"], imageUrl: tempuraImage },
  { itemId: 28, categoryId: 5, itemName: "酥炸厚切里肌豬排", description: "國產豬里肌裹粉酥炸，外酥內嫩。", finalPrice: 190, featureTags: ["經典必點", "職人精神"], imageUrl: tempuraImage },
  { itemId: 29, categoryId: 5, itemName: "北海道廣島炸牡蠣", description: "肥美生蠔裹粉酥炸，咬下海味濃郁。", finalPrice: 210, featureTags: ["嚴選食材", "鮮味極致"], imageUrl: tempuraImage },
  { itemId: 30, categoryId: 5, itemName: "炸起司可樂餅", description: "男爵馬鈴薯泥包莫札瑞拉起司，熱食牽絲。", finalPrice: 120, featureTags: ["主廚推薦", "人氣爆棚"], imageUrl: tempuraImage },
  { itemId: 31, categoryId: 6, itemName: "宇治金時黃金蕨餅", description: "日式蕨餅灑上靜岡抹茶粉，搭配香甜紅豆泥。", finalPrice: 110, featureTags: ["季節限定", "手作工法"], imageUrl: matchaDessertImage },
  { itemId: 32, categoryId: 6, itemName: "黑糖香草冰淇淋大福", description: "Q 彈大福包香草冰淇淋，淋上沖繩黑糖蜜。", finalPrice: 90, featureTags: ["季節限定", "手作工法"], imageUrl: caramelPuddingImage },
  { itemId: 33, categoryId: 6, itemName: "十勝紅豆白玉湯", description: "溫熱紅豆湯，搭配現煮手作白玉小湯圓。", finalPrice: 100, featureTags: ["經典必點", "季節限定"], imageUrl: caramelPuddingImage },
  { itemId: 34, categoryId: 6, itemName: "炙燒焦糖布丁", description: "手工布丁表面覆蓋薄脆琥珀色炙燒焦糖。", finalPrice: 120, featureTags: ["主廚推薦", "季節限定"], imageUrl: caramelPuddingImage },
  { itemId: 35, categoryId: 7, itemName: "靜岡御用冰抹茶", description: "無糖高級靜岡抹茶，茶香回甘，解膩首選。", finalPrice: 120, featureTags: ["職人精神", "嚴選食材"], imageUrl: matchaDessertImage },
  { itemId: 36, categoryId: 7, itemName: "巨峰葡萄氣泡飲", description: "巨峰葡萄果汁搭配氣泡水，微甜沁涼。", finalPrice: 90, featureTags: ["季節限定", "手作工法"], imageUrl: seafoodSaladImage },
  { itemId: 37, categoryId: 7, itemName: "紀州梅子可爾必思", description: "酸甜可爾必思融入紀州梅果醬，層次豐富。", finalPrice: 95, featureTags: ["主廚推薦", "人氣爆棚"], imageUrl: seafoodSaladImage },
  { itemId: 38, categoryId: 7, itemName: "日式焙茶鮮奶", description: "慢火焙茶融合鮮乳，口感醇厚。", finalPrice: 110, featureTags: ["職人精神", "嚴選食材"], imageUrl: matchaDessertImage },
  { itemId: 39, categoryId: 8, itemName: "獺祭純米大吟釀 45ml", description: "頂級日本清酒代表，帶有水果香氣與乾淨餘韻。", finalPrice: 280, featureTags: ["極致奢華", "職人精神"], imageUrl: japaneseSakeImage },
  { itemId: 40, categoryId: 8, itemName: "三得利頂級生啤酒", description: "泡沫細緻滑順，麥香濃郁。", finalPrice: 150, featureTags: ["職人精神", "嚴選食材"], imageUrl: japaneseSakeImage },
  { itemId: 41, categoryId: 8, itemName: "盛夏鮮柚沙瓦", description: "現榨柚子果肉、燒酎與氣泡水調和。", finalPrice: 140, featureTags: ["季節限定", "鮮味極致"], imageUrl: japaneseSakeImage },
  { itemId: 42, categoryId: 8, itemName: "角瓶威士忌蘇打", description: "角瓶威士忌與強氣泡水，搭配新鮮檸檬角。", finalPrice: 130, featureTags: ["經典必點", "人氣爆棚"], imageUrl: japaneseSakeImage },
  { itemId: 43, categoryId: 8, itemName: "大關甘口梅酒", description: "日本大關酒造釀製，酸甜濃郁，冰鎮後風味最佳。", finalPrice: 140, featureTags: ["限量供應"], imageUrl: japaneseSakeImage },
];

export const getDemoMenuItems = () => demoMenuItems;

export const demoReservationSlots = [1, 2, 3, 5, 7, 10].flatMap((offset, index) => [
  {
    slotId: 1000 + index * 2,
    storeId: 1,
    reservationDate: formatDate(offset),
    startTime: "11:30:00",
    endTime: "13:00:00",
    mealPeriod: "LUNCH",
    requiresDeposit: false,
    depositAmount: 0,
  },
  {
    slotId: 1001 + index * 2,
    storeId: 1,
    reservationDate: formatDate(offset),
    startTime: "18:00:00",
    endTime: "20:00:00",
    mealPeriod: "DINNER",
    requiresDeposit: index % 3 === 0,
    depositAmount: index % 3 === 0 ? 500 : 0,
  },
]);

export const getDemoReservationSlots = (storeId) =>
  demoReservationSlots.map((slot) => ({ ...slot, storeId: Number(storeId || slot.storeId) }));

export const demoSlotCapacity = [
  { tableSize: 2, totalCount: 5, reservedCount: 1 },
  { tableSize: 4, totalCount: 4, reservedCount: 1 },
  { tableSize: 6, totalCount: 2, reservedCount: 0 },
];
