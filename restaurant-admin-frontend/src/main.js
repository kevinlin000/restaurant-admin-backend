import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 樣式
import '../src/assets/css/main.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Core CSS
import '../src/assets/vendor/css/rtl/core.css'
import '../src/assets/vendor/css/rtl/theme-default.css'
import '../src/assets/css/demo.css'

// Icons
import '../src/assets/vendor/fonts/boxicons.css'
import '../src/assets/vendor/fonts/fontawesome.css'
import '../src/assets/vendor/fonts/flag-icons.css'

// Vendors CSS
import '../src/assets/vendor/libs/perfect-scrollbar/perfect-scrollbar.css'
import '../src/assets/vendor/libs/typeahead-js/typeahead.css'
import '../src/assets/vendor/libs/fullcalendar/fullcalendar.css'
import '../src/assets/vendor/libs/flatpickr/flatpickr.css'
import '../src/assets/vendor/libs/select2/select2.css'
import '../src/assets/vendor/libs/quill/editor.css'
import '../src/assets/vendor/libs/@form-validation/form-validation.css'
import '../src/assets/vendor/libs/bootstrap-select/bootstrap-select.css'

// Page CSS
import '../src/assets/vendor/css/pages/app-calendar.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
