<script setup>

</script>
<template>
    <!-- Content -->
    <div class="container-xxl flex-grow-1 container-p-y">
        <h2 class="py-3 mb-4">桌位分配<span class="text-muted fw-light"> / Reservation Table</span></h2>
    <!-- 未配桌名單 -->
     <div class="content-wrapper">
            <!-- Content -->
              <div class="card">
                <h5 class="card-header">今日未配桌名單 / Today </h5>
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
                      </tr>
                    </thead>
                    <tbody class="table-border-bottom-0">
                      <tr>
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
                      </tr>
                      <tr>
                        <!-- 打勾已入座 桌號不能更改 -->
                        
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
                      </tr>
                      <tr>
                        
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
                      </tr>
                    </thead>
                    <tbody class="table-border-bottom-0">
                      <tr>
                        
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-primary me-1">Active</span></td>
                    
                      </tr>
                      <tr>
                       
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-success me-1">Completed</span></td>
                        
                      </tr>
                      <tr>
                        
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-info me-1">Scheduled</span></td>
                        
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
                      </tr>
                    </thead>
                    <tbody class="table-border-bottom-0">
                      <tr>
                        
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-primary me-1">Active</span></td>
                        
                      </tr>
                      <tr>
                        
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-success me-1">Completed</span></td>
                        
                      </tr>
                      <tr>
                        
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-info me-1">Scheduled</span></td>
                        
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
                      </tr>
                    </thead>
                    <tbody class="table-border-bottom-0">
                      <tr>
                        
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-primary me-1">Active</span></td>
                        
                      </tr>
                      <tr>
                        
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-success me-1">Completed</span></td>
                       
                      </tr>
                      <tr>
                        
                        <td>
                          <span class="fw-medium">姓名</span>
                        </td>
                        <td>09xx-xxx-xxx</td>
                        <td>abc@gmail.com</td>
                        <td>2位</td>
                        <td>桌號</td>
                        <td><span class="badge bg-label-info me-1">Scheduled</span></td>
                        
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