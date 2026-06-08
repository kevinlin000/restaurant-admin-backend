<script setup>
</script>

<template>

  <!-- Introduction 簡介 -->
  <section class="introduction-section">
    <div class="introduction-overlay">
      <div class="introduction-content">
        <h1 class="introduction-title">標題</h1>
        <p class="introduction-desc">介紹</p>

        <div class="introduction-buttons">
        <RouterLink class="primary-btn" to="/reservation">立即訂位</RouterLink>
        <RouterLink class="secondary-btn" to="/menu">瀏覽菜單</RouterLink>
        </div>
      </div>
    </div>
  </section>

  <!-- About 關於品牌 -->
  <section id="about" class="about-section">
    <div class="section-container">
      <div class="section-title">品牌介紹</div>
      <div class="about-grid">
        <div class="about-text">
          <h2>品牌介紹標題</h2>
          <p>品牌介紹內容</p>
        </div>
        <div class="about-image"></div>
      </div>
    </div>
  </section>

  <!-- Menu 菜單 -->
  <section id="menu" class="menu-section">
    <div class="section-container">
      <div class="section-title">精選菜單</div>

        <div class="menu-grid">
            <div class="menu-card">
                <div class="menu-image example1"></div>
                <h3>菜單種類１</h3>
            </div>
            <div class="menu-card">
                <div class="menu-image example2"></div>
                <h3>菜單種類２</h3>
            </div>
            <div class="menu-card">
                <div class="menu-image example3"></div>
                <h3>菜單種類３</h3>
            </div>
        </div>
    </div>
    <div class="text-center mt-5">
        <RouterLink class="primary-btn" to="/order">點餐</RouterLink>
    </div>
  </section>

  <!-- Reservation 訂位-->
  <section id="reservation" class="reservation-section">
    <div class="section-container text-center">
        <div class="section-title">線上訂位</div>
        <p class="reservation-text">訂位說明</p>
        <br>
        <RouterLink class="primary-btn" to="/reservation">立即訂位</RouterLink>
    </div>
  </section>

  <!-- Store 分店資訊 -->
  <section id="store" class="store-section">
    <div class="section-container">
      <div class="section-title">分店資訊</div>

      <div class="store-grid">
        <div class="store-card">
          <h3>分店 1</h3>
          <p>分店地址 1</p>
        </div>

        <div class="store-card">
          <h3>分店 2</h3>
          <p>分店地址 2</p>
        </div>

        <div class="store-card">
          <h3>分店 3</h3>
          <p>分店地址 3</p>
        </div>

      </div>
    </div>
  </section>
</template>

<style scoped>

/* sections */

.section-container {
  width: 80%;
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-size: 42px;
  text-align: center;
  margin-bottom: 80px;
}

/* buttons */

.primary-btn {
  background: #d6a679;
  color: white;
  padding: 14px 30px;
  border-radius: 12px;
  text-decoration: none;
}

.secondary-btn {
  border: 1px solid white;
  color: white;
  padding: 14px 30px;
  border-radius: 12px;
  text-decoration: none;
}

/* Introduction 簡介 */

.introduction-section {
  height: 100vh;
  background:
    linear-gradient(rgba(0,0,0,0.35), rgba(0,0,0,0.35)),
    url('../assets/images/bg-introduction.jpg');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
}

.introduction-overlay {
  width: 100%;
}

.introduction-content {
  width: 80%;
  max-width: 1200px;
  margin: 0 auto;
  color: white;
}

.introduction-title {
  font-size: 72px;
  line-height: 1.3;
  margin-bottom: 30px;
}

.introduction-desc {
  font-size: 22px;
  margin-bottom: 40px;
}

.introduction-buttons {
  display: flex;
  gap: 20px;
}

/* about 關於品牌 */

.about-section {
  padding: 140px 0;
  background: url('../assets/images/bg-about.jpg');
  background-size: cover;
  background-color: #ffffff;
}

.about-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
}

.about-image {
  height: 500px;
  border-radius: 20px;
  background:url('../assets/images/about-food.jpg');
  background-size: cover;
}

/* reservation 訂位 */

.reservation-section {
  padding: 140px 0;
  background:
    linear-gradient(rgba(0,0,0,0.45), rgba(0,0,0,0.45)),
    url('../assets/images/bg-reservation.jpg');
  background-size: cover;
  color: white;
}

/* menu 菜單 */

.menu-section {
  padding: 140px 0;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(3,1fr);
  gap: 30px;
}

.menu-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  padding: 20px;
  text-align: center;
}

.menu-image {
  height: 240px;
  background-size: cover;
  background-position: center;
}

.example1 {
  background-image: url('../assets/images/menu-example1.jpg');
}

.example2 {
  background-image: url('../assets/images/menu-example2.jpg');
}

.example3 {
  background-image: url('../assets/images/menu-example3.jpg');
}

/* store 分店資訊 */

.store-section {
  padding: 140px 0;
  background:url('../assets/images/bg-store.jpg');
  background-size: cover;

}

.store-grid {
  display: grid;
  grid-template-columns: repeat(3,1fr);
  gap: 30px;
}

.store-card {
  background: rgba(255,255,255,0.88);
  padding: 40px;
  border-radius: 20px;
}

/* mobile */

@media (max-width: 991px) {

  .introduction-title {
    font-size: 42px;
  }

  .about-grid,
  .menu-grid,
  .store-grid {
    grid-template-columns: 1fr;
  }
}
</style>