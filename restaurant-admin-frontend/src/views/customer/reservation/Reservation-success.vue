<script setup>

</script>

<template>
    <div class="page-container">
        <h2 class="mb-4">訂位<span class="text-muted fw-light"> / Reservation</span></h2>
              <div class="card mb-4 py-4 align-items-center" style="max-width: 600px; margin: 0 auto;">
                <div class="avatar me-2">
                    <span class="avatar-initial rounded-circle bg-label-success">
                        <i class="bx bx-check-double bx-sm" style="color: green;"></i>
                    </span>
                </div>
                <h3 class="card-header">訂位成功</h3>
                <form class="card-body text-center">
                  <hr class="my-3" />
                  <div class="row g-3">
                    <div class="col-md-12">
                        <p style="font-size: 25px">XXX 小姐</p>
                    </div>
                    <div class="col-md-12">
                        <p style="font-size: 25px">3 位</p>
                    </div>
                    <div class="col-md-12">
                        <p style="font-size: 25px"> 6 月 15 日</p>
                    </div>
                    <div class="col-md-12">
                        <p style="font-size: 25px">14:00-16:00</p>
                    </div>
                    <div class="col-md-12">
                        <p style="font-size: 15px">09XX-XXX-XXX</p>
                    </div>
                    <div class="col-md-12">
                        <p style="font-size: 15px">aaa@gmail.com</p>
                    </div>
                  </div>
                  <div class="pt-4">
                    <button type="submit" class="btn btn-primary me-sm-3 me-1">保留訂位</button>
                    <button type="submit" class="btn btn-primary me-sm-3 me-1">編輯訂位</button>
                    <button type="submit" class="btn btn-primary me-sm-3 me-1">取消訂位</button>
                    <button type="submit" class="btn btn-primary">
                        <RouterLink class="nav-link" to="/menu">瀏覽菜單</RouterLink>
                        <i class="bx bx-chevron-right me-sm-n2"></i>
                    </button>
                  </div>
                </form>
              </div>
            </div>
</template>

<style scoped>

</style>