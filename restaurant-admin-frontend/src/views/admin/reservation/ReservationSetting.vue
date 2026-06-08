<script setup>

</script>
<template>
    <!-- Content -->
    <div class="container-xxl flex-grow-1 container-p-y">
        <h2 class="py-3 mb-4">訂位設定<span class="text-muted fw-light"> / Reservation Settings</span></h2>
        <!-- Content wrapper -->
          <div class="content-wrapper">
            <!-- Content -->
              <div class="row">
                <div class="col-xl-8 col-lg-7 col-md-7 order-0 order-md-1">
                  <ul class="nav nav-pills flex-column flex-md-row mb-3 gap-3">
                    <li class="nav-item">
                      <a class="nav-link active" href="javascript:void(0);"><i class="bx bx-calendar me-1"></i>開放訂位日期＆時段</a>
                    </li>
                    <li class="nav-item">
                      <a class="nav-link" href="../reservation/ReservationStoreTable.vue">
                        <i class="bx bx-store me-1"></i>
                        各分店桌位數
                      </a>
                    </li>
                  </ul>
                </div>
            </div>
        </div>
    <div class="layout-wrapper layout-content-navbar">
      <!-- <div class="layout-container"> -->
        <div class="layout-page">
          <div class="content-wrapper">
            <!-- 日曆 -->
            <div class="container-l flex-grow-1 container-p-y">
              <div class="card app-calendar-wrapper">
                <div class="row g-0">
                  <!-- Calendar Sidebar -->
                  <div class="col app-calendar-sidebar" id="app-calendar-sidebar">
                    <!-- 分店篩選 Store Filter -->
                    <div class="border-bottom p-4 my-sm-0 mb-3 gap-2 d-flex align-items-center">
                        <span class="align-middle">分店</span>
                      <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                        <option value="">請選擇</option>
                        <option value="1">分店1</option>
                        <option value="2">分店2</option>
                        <option value="3">分店3</option>
                      </select>
                    </div>
                    <div class="border-bottom p-4 my-sm-0 mb-3">
                      <div class="d-grid">
                        <button
                          class="btn btn-primary btn-toggle-sidebar"
                          data-bs-toggle="offcanvas"
                          data-bs-target="#addEventSidebar"
                          aria-controls="addEventSidebar">
                          <i class="bx bx-plus me-1"></i>
                          <span class="align-middle">新增訂位日期＆時段</span>
                        </button>
                      </div>
                    </div>
                    <div class="p-4">
                      <!-- inline calendar (flatpicker) -->
                      <div class="ms-n2">
                        <div class="inline-calendar"></div>
                      </div>

                      <hr class="container-m-nx my-4" />

                      <!-- Filter -->
                      <div class="mb-4">
                        <small class="text-small text-muted text-uppercase align-middle">Filter</small>
                      </div>

                      <div class="form-check mb-2">
                        <input
                          class="form-check-input select-all"
                          type="checkbox"
                          id="selectAll"
                          data-value="all"
                          checked />
                        <label class="form-check-label" for="selectAll">全部</label>
                      </div>

                      <div class="app-calendar-events-filter">
                        <div class="form-check form-check-danger mb-2">
                          <input
                            class="form-check-input input-filter"
                            type="checkbox"
                            id="select-personal"
                            data-value="personal"
                            checked />
                          <label class="form-check-label" for="select-personal">平日(一～四)</label>
                        </div>
                        <div class="form-check mb-2">
                          <input
                            class="form-check-input input-filter"
                            type="checkbox"
                            id="select-business"
                            data-value="business"
                            checked />
                          <label class="form-check-label" for="select-business">週末、國定假日</label>
                        </div>
                        <div class="form-check form-check-warning mb-2">
                          <input
                            class="form-check-input input-filter"
                            type="checkbox"
                            id="select-family"
                            data-value="family"
                            checked />
                          <label class="form-check-label" for="select-family">公休日</label>
                        </div>
                        <div class="form-check form-check-success mb-2">
                          <input
                            class="form-check-input input-filter"
                            type="checkbox"
                            id="select-holiday"
                            data-value="holiday"
                            checked />
                          <label class="form-check-label" for="select-holiday">不開放訂位</label>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- /Calendar Sidebar -->

                  <!-- 新增 Modal -->
                  <div class="col app-calendar-content">
                    <div class="card shadow-none border-0">
                      <div class="card-body pb-0">
                        <!-- FullCalendar -->
                        <div id="calendar"></div>
                      </div>
                    </div>
                    <div class="app-overlay"></div>
                    <!-- FullCalendar Offcanvas -->
                    <div
                      class="offcanvas offcanvas-end event-sidebar"
                      tabindex="-1"
                      id="addEventSidebar"
                      aria-labelledby="addEventSidebarLabel">
                      <div class="offcanvas-header border-bottom">
                        <h5 class="offcanvas-title mb-2" id="addEventSidebarLabel">新增訂位日期＆時段</h5>
                        <button
                          type="button"
                          class="btn-close text-reset"
                          data-bs-dismiss="offcanvas"
                          aria-label="Close"></button>
                      </div>
                      <div class="offcanvas-body">
                        <form class="event-form pt-0" id="eventForm" onsubmit="return false">
                          <div class="mb-3">
                            <label class="form-label" for="eventTitle">日期</label>
                            <!-- 日期-選項 -->
                            <div class="app-calendar-events-filter">
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input select-all"
                                type="checkbox"
                                id="selectAll"
                                data-value="all"
                                checked />
                                <label class="form-check-label" for="selectAll">每天</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">平日(一～五)</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-business"
                                data-value="business"
                                checked />
                            <label class="form-check-label" for="select-business">週末、國定假日</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜一</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜二</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜三</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜四</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜五</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜六</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜日</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-family"
                                data-value="family"
                                checked />
                            <label class="form-check-label" for="select-family">自訂日期</label>
                            <input
                                type="text"
                                id="multicol-birthdate"
                                class="form-control dob-picker"
                                placeholder="YYYY-MM-DD" />
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-family"
                                data-value="family"
                                checked />
                            <label class="form-check-label" for="select-family">自訂期間</label>
                            <input
                                type="text"
                                id="multicol-birthdate"
                                class="form-control dob-picker"
                                placeholder="YYYY-MM-DD" />
                            </div>
                        </div>
                        </div>
                        <!-- 時段 -->
                        <div class="mb-3">
                        <label for="select2Multiple" class="form-label">時段</label>
                          <select id="select2Multiple" class="select2 form-select" multiple>
                              <option value="12">12:00-13:00</option>
                              <option value="13">13:00-14:00</option>
                              <option value="14">14:00-15:00</option>
                              <option value="15">15:00-16:00</option>
                              <option value="16">16:00-17:00</option>
                              <option value="17">17:00-18:00</option>
                              <option value="18">18:00-19:00</option>
                              <option value="19">19:00-20:00</option>
                              <option value="notopen">不開放訂位</option>
                              <option value="close">公休日</option>
                          </select>
                        </div>
                          <div class="mb-3 d-flex justify-content-sm-between justify-content-start my-4">
                            <div>
                              <button type="submit" class="btn btn-primary btn-add-event me-sm-3 me-1">新增
                              </button>
                              <button
                                type="reset"
                                class="btn btn-label-secondary btn-cancel me-sm-0 me-1"
                                data-bs-dismiss="offcanvas">
                                Cancel
                              </button>
                            </div>
                            <div><button class="btn btn-label-danger btn-delete-event d-none">取消</button></div>
                          </div>
                        </form>
                      </div>
                    </div>
                  </div>
                  <!-- /Calendar & Modal -->
                </div>
              </div>
            </div>
            <!-- / Content -->

            <div class="content-backdrop fade"></div>
          </div>

          <div class="card card-action mb-4">
                    <div class="card-header align-items-center flex-wrap gap-3 py-4">
                      <h5 class="card-action-title mb-0">分店名稱(下拉選單)</h5>
                        <!-- 隨下拉選單切換分店變換 -->
                        <span class="mb-0 text-muted">[ 2人桌 : 10 ] [ 4人桌 : 10 ] [ 6人桌 : 5 ]
                        </span>
                      <div class="card-action-element">
                        <button
                          class="btn btn-label-primary"
                          type="button"
                          data-bs-toggle="modal"
                          data-bs-target="#addNewAddress">
                          ＋ 新增訂位日期＆時段
                        </button>
                      </div> 
                        <div class="col-12 col-md-6">
                            <label class="switch">
                            <input type="checkbox" class="switch-input allDay-switch" />
                            <span class="switch-toggle-slider">
                                <span class="switch-on"></span>
                                <span class="switch-off"></span>
                            </span>
                            <span class="switch-label">開放所有日期訂位</span>
                            </label>
                        </div>
                        <div class="col-12 col-md-4">
                            <label class="switch">
                            <input type="checkbox" class="switch-input allDay-switch" />
                            <span class="switch-toggle-slider">
                                <span class="switch-on"></span>
                                <span class="switch-off"></span>
                            </span>
                            <span class="switch-label">開放一個月前訂位</span>
                            </label>
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="switch">
                            <input type="checkbox" class="switch-input allDay-switch" />
                            <span class="switch-toggle-slider">
                                <span class="switch-on"></span>
                                <span class="switch-off"></span>
                            </span>
                            <span class="switch-label">開放兩週前訂位</span>
                            </label>
                        </div>
                        <div class="col-12 col-md-4">
                            <label class="switch">
                            <input type="checkbox" class="switch-input allDay-switch" />
                            <span class="switch-toggle-slider">
                                <span class="switch-on"></span>
                                <span class="switch-off"></span>
                            </span>
                            <span class="switch-label">(自訂多久前可以訂位)</span>
                            </label>
                        </div>
                    </div>

                    <div class="card-body">
                      <div class="accordion accordion-flush accordion-arrow-left" id="ecommerceBillingAccordionAddress">
                        <!-- 時段 -->
                        <div class="accordion-item">
                          <div
                            class="accordion-header d-flex justify-content-between align-items-center flex-wrap flex-sm-nowrap"
                            id="headingHome">
                            <a
                              class="accordion-button collapsed"
                              data-bs-toggle="collapse"
                              data-bs-target="#ecommerceBillingAddressHome"
                              aria-expanded="false"
                              aria-controls="headingHome"
                              role="button">
                              <span>
                                <span class="d-flex gap-2 align-items-baseline">
                                  <span class="h6 mb-0">平日(二～四)</span>
                                  <span class="badge bg-label-success">Default Address</span>
                                </span>
                              </span>
                            </a>
                            <div class="d-flex gap-3 p-4 p-sm-0 pt-0 ms-1 ms-sm-0">
                              <a href="javascript:void(0);"><i class="bx bx-pencil text-secondary fs-4"></i></a>
                              <a href="javascript:void(0);"><i class="bx bx-trash text-secondary fs-4"></i></a>
                              <button class="btn p-0" data-bs-toggle="dropdown" aria-expanded="false" role="button">
                                <i class="bx bx-dots-vertical-rounded text-secondary fs-4"></i>
                              </button>
                              <ul class="dropdown-menu">
                                <li><a class="dropdown-item" href="javascript:void(0);">Set as default address</a></li>
                              </ul>
                            </div>
                          </div>
                          <div
                            id="ecommerceBillingAddressHome"
                            class="accordion-collapse collapse"
                            data-bs-parent="#ecommerceBillingAccordionAddress">
                            <div class="accordion-body ps-4 ms-1">
                              <!-- <h6 class="mb-1">Violet Mendoza</h6> -->
                              <p class="mb-1">12:00 - 14:00</p>
                              <p class="mb-1">14:00 - 16:00</p>
                              <p class="mb-1">18:00 - 20:00</p>
                              <p class="mb-1">20:00 - 22:00</p>
                            </div>
                          </div>
                        </div>

                        <div class="accordion-item">
                          <div
                            class="accordion-header d-flex justify-content-between align-items-center flex-wrap flex-sm-nowrap"
                            id="headingOffice">
                            <a
                              class="accordion-button collapsed"
                              data-bs-toggle="collapse"
                              data-bs-target="#ecommerceBillingAddressOffice"
                              aria-expanded="false"
                              aria-controls="headingOffice"
                              role="button">
                              <span class="d-flex flex-column">
                                <span class="h6 mb-0">週末、假日</span>
                                <!-- <span class="mb-0 text-muted">45 Roker Terrace</span> -->
                              </span>
                            </a>
                            <div class="d-flex gap-3 p-4 p-sm-0 pt-0 ms-1 ms-sm-0">
                              <a href="javascript:void(0);"><i class="bx bx-pencil text-secondary fs-4"></i></a>
                              <a href="javascript:void(0);"><i class="bx bx-trash text-secondary fs-4"></i></a>
                              <button class="btn p-0" data-bs-toggle="dropdown" aria-expanded="false" role="button">
                                <i class="bx bx-dots-vertical-rounded text-secondary fs-4"></i>
                              </button>
                              <ul class="dropdown-menu">
                                <li><a class="dropdown-item" href="javascript:void(0);">Set as default address</a></li>
                              </ul>
                            </div>
                          </div>
                          <div
                            id="ecommerceBillingAddressOffice"
                            class="accordion-collapse collapse"
                            aria-labelledby="headingOffice"
                            data-bs-parent="#ecommerceBillingAccordionAddress">
                            <div class="accordion-body ps-4 ms-1">
                              <!-- <h6 class="mb-1">Violet Mendoza</h6> -->
                              <p class="mb-1">10:00 - 12:00</p>
                              <p class="mb-1">12:00 - 14:00</p>
                              <p class="mb-1">14:00 - 16:00</p>
                              <p class="mb-1">16:00 - 18:00</p>
                              <p class="mb-1">18:00 - 20:00</p>
                              <p class="mb-1">20:00 - 22:00</p>
                            </div>
                          </div>
                        </div>
                        
                        <!-- 日期 -->
                        <hr class="my-4" />
                        <div class="accordion-item">
                          <div
                            class="accordion-header d-flex justify-content-between align-items-center flex-wrap flex-sm-nowrap"
                            id="headingFamily">
                            <a
                              class="accordion-button collapsed"
                              data-bs-toggle="collapse"
                              data-bs-target="#ecommerceBillingAddressFamily"
                              aria-expanded="false"
                              aria-controls="headingFamily"
                              role="button">
                              <span class="d-flex flex-column">
                                <span class="h6 mb-0">公休日</span>
                                <!-- <span class="mb-0 text-muted">512 Water Plant</span> -->
                              </span>
                            </a>
                            <div class="d-flex gap-3 p-4 p-sm-0 pt-0 ms-1 ms-sm-0">
                              <a href="javascript:void(0);"><i class="bx bx-pencil text-secondary fs-4"></i></a>
                              <a href="javascript:void(0);"><i class="bx bx-trash text-secondary fs-4"></i></a>
                              <button class="btn p-0" data-bs-toggle="dropdown" aria-expanded="false" role="button">
                                <i class="bx bx-dots-vertical-rounded text-secondary fs-4"></i>
                              </button>
                              <ul class="dropdown-menu">
                                <li><a class="dropdown-item" href="javascript:void(0);">Set as default address</a></li>
                              </ul>
                            </div>
                          </div>
                          <div
                            id="ecommerceBillingAddressFamily"
                            class="accordion-collapse collapse"
                            aria-labelledby="headingFamily"
                            data-bs-parent="#ecommerceBillingAccordionAddress">
                            <div class="accordion-body ps-4 ms-1">
                              <!-- <h6 class="mb-1">Violet Mendoza</h6> -->
                              <p class="mb-1">每週一</p>
                              <p class="mb-1">禮拜五</p>
                              <p class="mb-1">禮拜日</p>
                            </div>
                          </div>
                        </div>
                        <div class="accordion-item">
                          <div
                            class="accordion-header d-flex justify-content-between align-items-center flex-wrap flex-sm-nowrap"
                            id="headingOffice">
                            <a
                              class="accordion-button collapsed"
                              data-bs-toggle="collapse"
                              data-bs-target="#ecommerceBillingAddressOffice"
                              aria-expanded="false"
                              aria-controls="headingOffice"
                              role="button">
                              <span class="d-flex flex-column">
                                <span class="h6 mb-0">不開放訂位日(特別節日)</span>
                                <!-- <span class="mb-0 text-muted">45 Roker Terrace</span> -->
                              </span>
                            </a>
                            <div class="d-flex gap-3 p-4 p-sm-0 pt-0 ms-1 ms-sm-0">
                              <a href="javascript:void(0);"><i class="bx bx-pencil text-secondary fs-4"></i></a>
                              <a href="javascript:void(0);"><i class="bx bx-trash text-secondary fs-4"></i></a>
                              <button class="btn p-0" data-bs-toggle="dropdown" aria-expanded="false" role="button">
                                <i class="bx bx-dots-vertical-rounded text-secondary fs-4"></i>
                              </button>
                              <ul class="dropdown-menu">
                                <li><a class="dropdown-item" href="javascript:void(0);">Set as default address</a></li>
                              </ul>
                            </div>
                          </div>
                          <div
                            id="ecommerceBillingAddressOffice"
                            class="accordion-collapse collapse"
                            aria-labelledby="headingOffice"
                            data-bs-parent="#ecommerceBillingAccordionAddress">
                            <div class="accordion-body ps-4 ms-1">
                              <!-- <h6 class="mb-1">Violet Mendoza</h6> -->
                              <p class="mb-1">5/10(母親節)</p>
                              <p class="mb-1">8/8(父親節)</p>
                              <p class="mb-1">12/31(跨年)</p>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                <!-- 新增 Modal -->
              <div class="modal fade" id="addNewAddress" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-simple modal-add-new-address">
                  <div class="modal-content p-3 p-md-5">
                    <div class="modal-body">
                      <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                      <div class="text-center mb-4">
                        <h3 class="address-title">新增訂位日期＆時段</h3>
                      </div>
                      <form id="addNewAddressForm" class="row g-3" onsubmit="return false">
                        <div class="col-12">
                          <label class="form-label" for="modalAddressFirstName">日期</label>
                          <!-- 日期-選項 -->
                            <div class="col-12 col-md-6">
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input select-all"
                                type="checkbox"
                                id="selectAll"
                                data-value="all"
                                checked />
                                <label class="form-check-label" for="selectAll">每天</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">平日(一～五)</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-business"
                                data-value="business"
                                checked />
                            <label class="form-check-label" for="select-business">週末、國定假日</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜一</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜二</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜三</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜四</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜五</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜六</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-personal"
                                data-value="personal"
                                checked />
                            <label class="form-check-label" for="select-personal">禮拜日</label>
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-family"
                                data-value="family"
                                checked />
                            <label class="form-check-label" for="select-family">自訂日期</label>
                            <input
                                type="text"
                                id="multicol-birthdate"
                                class="form-control dob-picker"
                                placeholder="YYYY-MM-DD" />
                            </div>
                            <div class="form-check mb-2">
                            <input
                                class="form-check-input input-filter"
                                type="checkbox"
                                id="select-family"
                                data-value="family"
                                checked />
                            <label class="form-check-label" for="select-family">自訂期間</label>
                            <input
                                type="text"
                                id="multicol-birthdate"
                                class="form-control dob-picker"
                                placeholder="YYYY-MM-DD" />
                            </div>
                          </div>
                        </div>
                        <div class="col-12">
                          <label class="form-label" for="modalAddressLastName">時段</label>
                          <select id="select2Multiple" class="select2 form-select" multiple>
                              <option value="12">12:00-13:00</option>
                              <option value="13">13:00-14:00</option>
                              <option value="14">14:00-15:00</option>
                              <option value="15">15:00-16:00</option>
                              <option value="16">16:00-17:00</option>
                              <option value="17">17:00-18:00</option>
                              <option value="18">18:00-19:00</option>
                              <option value="19">19:00-20:00</option>
                              <option value="notopen">不開放訂位</option>
                              <option value="close">公休日</option>
                          </select>
                        </div>
                        <div class="col-12 text-center">
                          <button type="submit" class="btn btn-primary me-sm-3 me-1">新增</button>
                          <button
                            type="reset"
                            class="btn btn-label-secondary"
                            data-bs-dismiss="modal"
                            aria-label="Close">
                            取消
                          </button>
                        </div>
                      </form>
                    </div>
                  </div>
                </div>
              </div>
              <!--/ Add New Address Modal -->
        </div>
        <!-- / Layout page -->
      </div>

      <!-- Overlay -->
      <div class="layout-overlay layout-menu-toggle"></div>

      <!-- Drag Target Area To SlideIn Menu On Small Screens -->
      <div class="drag-target"></div>
    <!-- </div> -->
    <!-- / Layout wrapper -->
    </div>
</template>

<style scoped>

</style>