<script setup>

</script>

<template>
    <div class="page-container">
        <h2 class="mb-4">訂位<span class="text-muted fw-light"> / Reservation</span></h2>
              <div class="card mb-4">
                <h5 class="card-header text-muted">訂位說明：</h5>
                    <ul class="text-muted">
                        <li>請至少提前一天預約訂位。</li>
                        <li>如有特殊需求或更改訂位資訊，請聯繫我們的客服人員。</li>
                        <li>我們將盡力滿足您的需求，但無法保證所有要求都能被接受。</li>
                    </ul>
                <hr class="my-3" />
                <form class="card-body">
                  <h5>訂位資訊</h5>
                  <div class="row g-3">
                    <div class="col-md-6">
                      <label class="form-label" for="multicol-username">姓名</label>
                      <input type="text" id="multicol-username" class="form-control" />
                    </div>
                    <div class="col-md-6">
                        <label class="d-block form-label">性別</label>
                        <div class="d-flex gap-4">
                          <div class="form-check">
                            <input
                              type="radio"
                              id="bs-validation-radio-male"
                              name="bs-validation-radio"
                              class="form-check-input" required checked />
                            <label class="form-check-label" for="bs-validation-radio-male">男</label>
                          </div>
                          <div class="form-check">
                            <input
                              type="radio"
                              id="bs-validation-radio-female"
                              name="bs-validation-radio"
                              class="form-check-input" required />
                            <label class="form-check-label" for="bs-validation-radio-female">女</label>
                          </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label" for="multicol-phone">手機</label>
                      <input
                        type="text"
                        id="multicol-phone"
                        class="form-control phone-mask"
                        placeholder="09xx xxx xxx"
                        aria-label="09xx xxx xxx" />
                    </div>
                    <div class="col-md-6">
                      <label class="form-label" for="multicol-email">gmail</label>
                      <div class="input-group input-group-merge">
                        <input
                          type="text"
                          id="multicol-email"
                          class="form-control"
                          placeholder="@gmail.com"
                          aria-label="@gmail.com"
                          aria-describedby="multicol-email2" />
                      </div>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label" for="multicol-country">分店</label>
                      <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                        <option value="">請選擇</option>
                        <option value="1">分店1</option>
                        <option value="2">分店2</option>
                        <option value="3">分店3</option>
                      </select>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label" for="multicol-country">人數</label>
                      <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                        <option value="">請選擇</option>
                        <option value="1">1人</option>
                        <option value="2">2人</option>
                        <option value="3">3人</option>
                        <option value="4">4人</option>
                        <option value="5">5人</option>
                        <option value="6">6人</option>
                      </select>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label" for="multicol-birthdate">日期</label>
                      <input
                        type="text"
                        id="multicol-birthdate"
                        class="form-control dob-picker"
                        placeholder="YYYY-MM-DD" />
                    </div>
                    <div class="col-md-6">
                      <label class="form-label" for="multicol-country">時段</label>
                      <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                        <option value="">請選擇</option>
                        <option value="1">12:00-14:00</option>
                        <option value="2">14:00-16:00</option>
                        <option value="3">16:00-18:00</option>
                        <option value="4">18:00-20:00</option>
                        <option value="5">20:00-22:00</option>
                      </select>
                    </div>
                  </div>
                  <div class="pt-4">
                    <button type="submit" class="btn btn-primary me-sm-3 me-1">確認訂位</button>
                    <button type="reset" class="btn btn-label-secondary me-sm-3 me-1">取消</button>
                    <button type="submit" class="btn btn-primary">查詢訂位</button>
                  </div>
                </form>
              </div>
            </div>
</template>

<style scoped>

</style>