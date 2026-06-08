<script setup>

</script>

<template>
    <!-- Content wrapper -->
          <div class="content-wrapper">
            <!-- Content -->

            <div class="container-xxl flex-grow-1 container-p-y">
              <h2 class="py-3 mb-4">訂位管理總覽<span class="text-muted fw-light"> / Reservation</span></h2>

            <!-- ＊改：加寬總寬分三等份的寬度* -->
            <!-- 總覽 card -->
              <div class="row">
                <div class="col-sm-6 col-lg-3 mb-4">
                  <div class="card card-border-shadow-primary h-100">
                    <div class="card-body">
                      <div class="d-flex align-items-center mb-2 pb-1">
                        <div class="avatar me-2">
                          <span class="avatar-initial rounded bg-label-primary">
                            <i class="bx bx-calendar bx-sm"></i>
                        </span>
                        </div>
                        <h3 class="ms-1 mb-0">60</h3>
                      </div>
                      <p class="mb-0">訂位數量</p>
                    </div>
                  </div>
                </div>
                <div class="col-sm-6 col-lg-3 mb-4">
                  <div class="card card-border-shadow-warning h-100">
                    <div class="card-body">
                      <div class="d-flex align-items-center mb-2 pb-1">
                        <div class="avatar me-2">
                          <span class="avatar-initial rounded bg-label-warning">
                            <i class="bx bx-check-double"></i>
                        </span>
                        </div>
                        <h3 class="ms-1 mb-0">15</h3>
                      </div>
                      <p class="mb-0">已確認保留訂位</p>
                    </div>
                  </div>
                </div>
                <div class="col-sm-6 col-lg-3 mb-4">
                  <div class="card card-border-shadow-danger h-100">
                    <div class="card-body">
                      <div class="d-flex align-items-center mb-2 pb-1">
                        <div class="avatar me-2">
                          <span class="avatar-initial rounded bg-label-danger">
                            <i class="bx bx-error"></i>
                        </span>
                        </div>
                        <h4 class="ms-1 mb-0">50</h4>
                      </div>
                      <p class="mb-0">尚未配桌</p>
                    </div>
                  </div>
                </div>
              </div>

            <!-- 今日 card -->
              <div class="card mb-4">
                <div class="card-widget-separator-wrapper">
                  <div class="card-body card-widget-separator">
                    <div class="row gy-4 gy-sm-1">
                      <div class="col-sm-6 col-lg-3">
                        <div
                          class="d-flex justify-content-between align-items-start card-widget-1 border-end pb-3 pb-sm-0">
                          <div>
                            <h3 class="mb-2">6</h3>
                            <p class="mb-0">今日訂位</p>
                          </div>
                          <div class="avatar me-sm-4">
                            <span class="avatar-initial rounded bg-label-primary">
                              <i class="bx bx-calendar bx-sm"></i>
                            </span>
                          </div>
                        </div>
                        <hr class="d-none d-sm-block d-lg-none me-4" />
                      </div>
                      <div class="col-sm-6 col-lg-3">
                        <div
                          class="d-flex justify-content-between align-items-start card-widget-2 border-end pb-3 pb-sm-0">
                          <div>
                            <h3 class="mb-2">2</h3>
                            <p class="mb-0">今日已保留訂位</p>
                          </div>
                          <div class="avatar me-lg-4">
                            <span class="avatar-initial rounded bg-label-warning">
                              <i class="bx bx-check-double bx-sm"></i>
                            </span>
                          </div>
                        </div>
                        <hr class="d-none d-sm-block d-lg-none" />
                      </div>
                      <div class="col-sm-6 col-lg-3">
                        <div
                          class="d-flex justify-content-between align-items-start border-end pb-3 pb-sm-0 card-widget-3">
                          <div>
                            <h3 class="mb-2">5</h3>
                            <p class="mb-0">今日尚未配桌</p>
                          </div>
                          <div class="avatar me-sm-4">
                            <span class="avatar-initial rounded bg-label-danger">
                              <i class="bx bx-error"></i>
                            </span>
                          </div>
                        </div>
                      </div>
                      <div class="col-sm-6 col-lg-3">
                        <div class="d-flex justify-content-between align-items-start">
                          <div>
                            <h3 class="mb-2">5</h3>
                            <p class="mb-0">今日實際入座</p>
                          </div>
                          <div class="avatar">
                            <span class="avatar-initial rounded bg-label-success">
                              <i class="bx bx-check-double bx-sm"></i>
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

            <!-- *加篩選名單、搜尋器* -->
            <!-- 今日訂位名單 -->
              <div class="card">
                <h5 class="card-header">今日訂位名單 / Today </h5>
                <div class="table-responsive text-nowrap">
                  <table class="table">
                    <thead class="table-dark">
                      <tr>
                        <th>12:00~14:00</th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody class="table-border-bottom-0">
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>
                            <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                                <option value="">請選擇</option>
                                <option value="table1">桌號 1</option>
                                <option value="table2">桌號 2</option>
                                <option value="table3">桌號 3</option>
                            </select>
                        </td>
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
                      <tr>
                        <!-- 打勾已入座 桌號不能更改 -->
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>1 桌</td>
                        <td><span class="badge bg-label-success me-1">已入座</span></td>
                        <td>
                          <!-- <div class="dropdown">
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
                          </div> -->
                        </td>
                      </tr>
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>
                            <select id="multicol-country" class="select2 form-select" data-allow-clear="true">
                                <option value="">請選擇</option>
                                <option value="table1">桌號 1</option>
                                <option value="table2">桌號 2</option>
                                <option value="table3">桌號 3</option>
                            </select>
                        </td>
                        <td><span class="badge bg-label-danger me-1">未配桌</span></td>
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
                    <thead class="table-dark">
                      <tr>
                        <th>14:00~16:00</th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody class="table-border-bottom-0">
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-primary me-1">Active</span></td>
                        <td>
                          <div class="dropdown">
                            <button type="button" class="btn p-0 dropdown-toggle hide-arrow" data-bs-toggle="dropdown">
                              <i class="bx bx-dots-vertical-rounded"></i>
                            </button>
                            <div class="dropdown-menu">
                              <a class="dropdown-item" href="javascript:void(0);"
                                ><i class="bx bx-edit-alt me-1"></i> Edit</a
                              >
                              <a class="dropdown-item" href="javascript:void(0);"
                                ><i class="bx bx-trash me-1"></i> Delete</a
                              >
                            </div>
                          </div>
                        </td>
                      </tr>
                      <tr>
                       <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-success me-1">Completed</span></td>
                        <td>
                          <div class="dropdown">
                            <button type="button" class="btn p-0 dropdown-toggle hide-arrow" data-bs-toggle="dropdown">
                              <i class="bx bx-dots-vertical-rounded"></i>
                            </button>
                            <div class="dropdown-menu">
                              <a class="dropdown-item" href="javascript:void(0);"
                                ><i class="bx bx-edit-alt me-1"></i> Edit</a
                              >
                              <a class="dropdown-item" href="javascript:void(0);"
                                ><i class="bx bx-trash me-1"></i> Delete</a
                              >
                            </div>
                          </div>
                        </td>
                      </tr>
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-info me-1">Scheduled</span></td>
                        <td>
                          <div class="dropdown">
                            <button type="button" class="btn p-0 dropdown-toggle hide-arrow" data-bs-toggle="dropdown">
                              <i class="bx bx-dots-vertical-rounded"></i>
                            </button>
                            <div class="dropdown-menu">
                              <a class="dropdown-item" href="javascript:void(0);"
                                ><i class="bx bx-edit-alt me-1"></i> Edit</a
                              >
                              <a class="dropdown-item" href="javascript:void(0);"
                                ><i class="bx bx-trash me-1"></i> Delete</a
                              >
                            </div>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                    <thead class="table-dark">
                      <tr>
                        <th>16:00~18:00</th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody class="table-border-bottom-0">
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-primary me-1">Active</span></td>
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
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-success me-1">Completed</span></td>
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
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-info me-1">Scheduled</span></td>
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
                    <thead class="table-dark">
                      <tr>
                        <th>18:00~20:00</th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody class="table-border-bottom-0">
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-primary me-1">Active</span></td>
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
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-success me-1">Completed</span></td>
                        <td>
                          <div class="dropdown">
                            <button type="button" class="btn p-0 dropdown-toggle hide-arrow" data-bs-toggle="dropdown">
                              <i class="bx bx-dots-vertical-rounded"></i>
                            </button>
                            <div class="dropdown-menu">
                              <a class="dropdown-item" href="javascript:void(0);"
                                ><i class="bx bx-edit-alt me-1"></i> Edit</a
                              >
                              <a class="dropdown-item" href="javascript:void(0);"
                                ><i class="bx bx-trash me-1"></i> Delete</a
                              >
                            </div>
                          </div>
                        </td>
                      </tr>
                      <tr>
                        <td>
                            <input type="checkbox" class="form-check-input" id="basic-default-checkbox" required="">
                        </td>
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-info me-1">Scheduled</span></td>
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
                  </table>
                </div>
              </div>
            </div>
          </div>
</template>

<style scoped>

</style>