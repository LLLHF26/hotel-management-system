import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from './layout/AppLayout.vue'
import Dashboard from './pages/Dashboard.vue'
import RoomStatus from './pages/RoomStatus.vue'
import OrderList from './pages/OrderList.vue'
import OrderDetail from './pages/OrderDetail.vue'
import RoomTypes from './pages/RoomTypes.vue'
import Rooms from './pages/Rooms.vue'
import Customers from './pages/Customers.vue'
import Employees from './pages/Employees.vue'
import FinancePage from './pages/FinancePage.vue'
import Alerts from './pages/Alerts.vue'
import Knowledge from './pages/Knowledge.vue'
import Settings from './pages/Settings.vue'
import Profile from './pages/Profile.vue'

const routes = [
  { path: '/', redirect: '/admin/dashboard' },
  {
    path: '/admin',
    component: AppLayout,
    children: [
      { path: '', redirect: 'dashboard' },
      { path: 'dashboard', component: Dashboard },
      { path: 'room-status', component: RoomStatus },
      { path: 'orders', component: OrderList },
      { path: 'orders/:id', component: OrderDetail },
      { path: 'room-types', component: RoomTypes },
      { path: 'rooms', component: Rooms },
      { path: 'customers', component: Customers },
      { path: 'employees', component: Employees },
      { path: 'finance', component: FinancePage },
      { path: 'alerts', component: Alerts },
      { path: 'knowledge', component: Knowledge },
      { path: 'settings', component: Settings },
      { path: 'profile', component: Profile }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

createApp(App).use(router).mount('#app')
