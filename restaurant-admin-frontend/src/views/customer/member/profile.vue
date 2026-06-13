<template>
  <div class="profile-container">
    <div class="profile-box">
      <h2 class="title">會員資料</h2>

      <div class="profile-card">
        <p><strong>姓名：</strong> {{ userInfo.name }}</p>
        <p><strong>Email：</strong> {{ userInfo.email }}</p>
        <p><strong>電話：</strong> {{ userInfo.phone }}</p>
        <p><strong>生日：</strong> {{ userInfo.birthday }}</p>

        <div class="birthday-gift-hint" v-if="birthdayCountdown !== null">
          <p v-if="birthdayCountdown === 0" class="gift-active">
            🎉 生日快樂！快來店領取您的生日專屬禮！
          </p>
          <p v-else class="gift-pending">
            距離您的生日還有 {{ birthdayCountdown }} 天，期待與您一同慶祝！
          </p>
        </div>
      </div>

      <div class="benefits-card">
        <div class="level-info">
          <p><strong>目前等級：</strong> {{ memberLevelText }}</p>
          <p class="upgrade-hint">{{ upgradeHint }}</p>
        </div>
        <hr />
        <div class="points-info">
          <p>
            <strong>目前點數：</strong>
            <span class="points-val">{{ userInfo.points }} 點</span>
          </p>
        </div>
      </div>

      <div class="action-buttons">
        <button class="pwd-btn" @click="handleEditPassword">修改密碼</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

const userInfo = ref({
  name: "黃小裕",
  email: "xeriof0000@example.com",
  phone: "0912-345-678",
  birthday: "20020101",
  level: "BRONZE",
  points: 500,
});

// 會員等級轉換
const memberLevelText = computed(() => {
  const levels = {
    BRONZE: "銅卡會員",
    SILVER: "銀卡會員",
    GOLD: "金卡會員",
    DIAMOND: "鑽石卡會員",
  };
  return levels[userInfo.value.level] || "一般會員";
});

// 升級提示
const upgradeHint = computed(() => {
  if (userInfo.value.level === "BRONZE")
    return "再消費 $1,500 即可升級為銀卡會員！";
  if (userInfo.value.level === "SILVER")
    return "再消費 $5,000 即可升級為金卡會員！";
  return "您已達最高等級，繼續保持！";
});

// 生日倒數計算
const birthdayCountdown = computed(() => {
  const today = new Date();
  const birthStr = userInfo.value.birthday;
  const month = parseInt(birthStr.substring(4, 6)) - 1;
  const day = parseInt(birthStr.substring(6, 8));

  const birthdayThisYear = new Date(today.getFullYear(), month, day);
  let nextBirthday = birthdayThisYear;
  if (today > birthdayThisYear) {
    nextBirthday = new Date(today.getFullYear() + 1, month, day);
  }

  return Math.ceil((nextBirthday - today) / (1000 * 60 * 60 * 24));
});
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 60px 20px;
  background-color: #f8f9fa;
}

.profile-box {
  width: 90%;
  max-width: 500px;
  padding: 50px;
  border-radius: 16px;
  background: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  margin-top: 20px;
  margin-bottom: 20px;
}

.title {
  text-align: center;
  color: #55606e;
  margin-bottom: 25px;
}

.profile-card,
.benefits-card {
  border: 1px solid #eee;
  padding: 20px;
  margin-bottom: 15px;
  border-radius: 10px;
  background-color: #ffffff;
}

.benefits-card {
  background-color: #fffaf7;
  border-color: #f7e0ce;
}
.upgrade-hint {
  font-size: 0.85em;
  color: #d97706;
  margin-top: 5px;
}
.points-val {
  font-size: 1.2em;
  color: #e3ac7f;
  font-weight: bold;
}

hr {
  border: 0;
  border-top: 1px solid #eee;
  margin: 15px 0;
}

.birthday-gift-hint {
  margin-top: 15px;
  padding: 10px;
  background-color: #fff1f2;
  border-left: 4px solid #e11d48;
  border-radius: 4px;
  font-size: 0.9em;
  color: #9f1239;
}
.gift-active {
  font-weight: bold;
  color: #e11d48;
}

.action-buttons {
  margin-top: 20px;
}
.pwd-btn {
  width: 100%;
  padding: 12px;
  background: #55606e;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}
.pwd-btn:hover {
  background: #3d4651;
}
</style>
