import aburiSalmonImage from "@/assets/images/aburi-salmon-sushi.jpg";
import caramelPuddingImage from "@/assets/images/caramel-pudding.jpg";
import homeImage from "@/assets/images/Home.jpg";
import japaneseSakeImage from "@/assets/images/japanese-sake.jpg";
import matchaDessertImage from "@/assets/images/matcha-dessert.jpg";
import reservationImage from "@/assets/images/reservation.jpg";
import salmonSashimiImage from "@/assets/images/salmon-sashimi.jpg";
import sashimiImage from "@/assets/images/sashimi.jpg";
import seafoodSaladImage from "@/assets/images/seafood-salad.jpg";
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
    question: "線上訂位可以預約多久以後的日期？",
    answer: "展示資料預設開放未來 30 天。正式環境會依各門市後台設定與桌型容量動態產生可訂時段。",
    keywords: "訂位 日期 時段 容量",
  },
  {
    faqId: 2,
    category: "DEPOSIT",
    question: "哪些訂位需要付訂金？",
    answer: "特殊餐期、包廂或大人數訂位可能需要訂金。是否收取與金額由後台時段設定控制。",
    keywords: "訂金 包廂 大人數 付款",
  },
  {
    faqId: 3,
    category: "ORDER",
    question: "外帶點餐可以選不同門市嗎？",
    answer: "可以。菜單會依選擇門市載入，分店價格也能各自維護。",
    keywords: "外帶 點餐 菜單 分店價格",
  },
  {
    faqId: 4,
    category: "PAYMENT",
    question: "系統支援哪些付款流程？",
    answer: "專題整合綠界與 LINE Pay 流程，並保留付款狀態回寫與後台訂單查詢。",
    keywords: "綠界 LINE Pay 付款 狀態",
  },
  {
    faqId: 5,
    category: "STORE",
    question: "前台營業中狀態怎麼判斷？",
    answer: "正式後端會比對門市營業時段、公休日與現在時間，不只看單一狀態欄位。",
    keywords: "營業中 公休日 門市 狀態",
  },
  {
    faqId: 6,
    category: "MEMBER",
    question: "會員登入後能看到哪些資料？",
    answer: "會員可查看個人資料、訂位紀錄、訂單紀錄與點數等級。展示版不連接真實會員資料。",
    keywords: "會員 JWT 點數 訂單",
  },
  {
    faqId: 7,
    category: "SERVICE",
    question: "過敏或特殊需求要怎麼告知？",
    answer: "訂位備註可先填寫需求，正式環境會讓店長在後台名單中查看。",
    keywords: "過敏 素食 兒童椅 特殊需求",
  },
];

export const demoMenuStores = demoStores.map((store) => ({
  id: store.storeId,
  name: store.storeName,
}));

export const demoMenuCategories = [
  { id: 1, name: "前菜" },
  { id: 2, name: "生魚片" },
  { id: 3, name: "壽司" },
  { id: 4, name: "熟食定食" },
  { id: 5, name: "甜點飲品" },
];

export const demoMenuItems = [
  {
    itemId: 1,
    categoryId: 1,
    itemName: "胡麻豆腐",
    description: "白芝麻慢磨，佐柴魚高湯與山葵。",
    finalPrice: 120,
    featureTags: ["手作工法", "經典必點"],
    imageUrl: tofuImage,
  },
  {
    itemId: 2,
    categoryId: 2,
    itemName: "極上生魚片盛合",
    description: "鮭魚、鮪魚與當日白身魚，依產地狀態調整。",
    finalPrice: 480,
    featureTags: ["主廚推薦", "鮮味極致", "限量供應"],
    imageUrl: sashimiImage,
  },
  {
    itemId: 3,
    categoryId: 3,
    itemName: "炙燒鮭魚握壽司",
    description: "昆布熟成鮭魚、赤醋飯，現炙帶出油脂香。",
    finalPrice: 220,
    featureTags: ["人氣爆棚", "職人精神"],
    imageUrl: aburiSalmonImage,
  },
  {
    itemId: 4,
    categoryId: 4,
    itemName: "壽喜燒牛肉定食",
    description: "關西風割下醬汁，搭配溫泉蛋與季節蔬菜。",
    finalPrice: 360,
    featureTags: ["經典必點", "嚴選食材"],
    imageUrl: sukiyakiImage,
  },
  {
    itemId: 5,
    categoryId: 4,
    itemName: "海老天婦羅",
    description: "薄衣高溫快炸，保留海味與蔬菜水分。",
    finalPrice: 280,
    featureTags: ["職人精神", "主廚推薦"],
    imageUrl: tempuraImage,
  },
  {
    itemId: 6,
    categoryId: 5,
    itemName: "宇治抹茶甘味",
    description: "抹茶冰淇淋、黑糖蜜與手作布丁收尾。",
    finalPrice: 180,
    featureTags: ["季節限定", "手作工法"],
    imageUrl: matchaDessertImage,
  },
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
