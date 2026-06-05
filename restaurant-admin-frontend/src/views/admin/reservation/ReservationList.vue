<script setup>

</script>
<template>
    <!-- Content -->
    <div class="container-xxl flex-grow-1 container-p-y">
        <h2 class="py-3 mb-4">訂位名單<span class="text-muted fw-light"> / Reservation List</span></h2>
            <!-- Content wrapper -->
          <div class="content-wrapper">
              <!-- Advanced Search -->
              <div class="card">
                <h5 class="card-header">Advanced Search</h5>
                <!--Search Form -->
                <div class="card-body">
                  <form class="dt_adv_search" method="GET">
                    <div class="row">
                      <div class="col-12">
                        <div class="row g-3">
                          <div class="col-12 col-sm-6 col-lg-4">
                            <label class="form-label">姓名 Name</label>
                            <input
                              type="text"
                              class="form-control dt-input dt-full-name"
                              data-column="1"
                              data-column-index="0" />
                          </div>
                          <div class="col-12 col-sm-6 col-lg-4">
                            <label class="form-label">日期 Date</label>
                            <div class="mb-0">
                              <input
                                type="text"
                                class="form-control dt-date flatpickr-range dt-input"
                                data-column="5"
                                data-column-index="4"
                                placeholder="起～迄日"
                                name="dt_date" />
                              <input
                                type="hidden"
                                class="form-control dt-date start_date dt-input"
                                data-column="5"
                                data-column-index="4"
                                name="value_from_start_date" />
                              <input
                                type="hidden"
                                class="form-control dt-date end_date dt-input"
                                name="value_from_end_date"
                                data-column="5"
                                data-column-index="4" />
                            </div>
                          </div>
                          <div class="col-12 col-sm-6 col-lg-4">
                            <label class="form-label">時段 Time</label>
                            <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                                <option value="">請選擇</option>
                                <option value="1">12:00-14:00</option>
                                <option value="2">14:00-16:00</option>
                                <option value="3">16:00-18:00</option>
                                <option value="4">18:00-20:00</option>
                                <option value="5">20:00-22:00</option>
                            </select>
                          </div>
                          <div class="col-12 col-sm-6 col-lg-4">
                            <label class="form-label">手機 Phone</label>
                            <input
                                type="text"
                                id="multicol-phone"
                                class="form-control phone-mask"
                                aria-label="09xx xxx xxx" />
                          </div>
                          <div class="col-12 col-sm-6 col-lg-4">
                            <label class="form-label">訂位狀態 Status</label>
                            <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                                <option value="">請選擇</option>
                                <option value="confirm">已保留</option>
                                <option value="notable">未配桌</option>
                                <option value="arrived">已入座</option>
                            </select>
                          </div>
                          <div class="col-12 col-sm-6 col-lg-4">
                            <label class="form-label">分店 Store</label>
                            <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                                <option value="">請選擇</option>
                                <option value="store1">分店 1</option>
                                <option value="store2">分店 2</option>
                                <option value="store3">分店 3</option>
                            </select>
                          </div>
                          <div class="align-content text-center">
                            <button type="submit" class="btn btn-primary">查詢 Search</button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </form>
                </div>
                <hr class="mt-0" />
                <div class="card-datatable table-responsive">
                  <table class="dt-advanced-search table border-top">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Date</th>
                        <th>Time</th>
                        <th>Phone</th>
                        <th>Email</th>
                        <th>Persons</th>
                        <th>Store</th>
                        <th>Table</th>
                        <th>Status</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><span class="fw-medium">姓名</span></td>
                            <td>2026-06-05</td>
                            <td>12:00~14:00</td>
                            <td>09xx-xxx-xxx</td>
                            <td>abc@gmail.com</td>
                            <td>2位</td>
                            <td>分店 1</td>
                            <td>桌號</td>
                            <td><span class="badge bg-label-warning me-1">已保留</span></td>
                            <td>
                                <div class="dropdown">
                                    <button type="button" class="btn p-0 dropdown-toggle hide-arrow" data-bs-toggle="dropdown">
                                    <i class="bx bx-dots-vertical-rounded"></i>
                                    </button>
                                    <div class="dropdown-menu">
                                    <a class="dropdown-item" href="javascript:void(0);">
                                        <i class="bx bx-edit-alt me-1"></i> Edit
                                    </a>
                                    <a class="dropdown-item" href="javascript:void(0);">
                                        <i class="bx bx-trash me-1"></i> Delete
                                    </a>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                    <!-- <tfoot>
                      <tr>
                        <th></th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Post</th>
                        <th>City</th>
                        <th>Date</th>
                        <th>Salary</th>
                      </tr>
                    </tfoot> -->
                  </table>
                </div>
              </div>
              <!--/ Advanced Search -->
            <!-- / Content -->
            </div>
        </div>
</template>

<style scoped>

</style>