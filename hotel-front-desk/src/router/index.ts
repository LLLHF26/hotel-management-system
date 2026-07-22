import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '@/utils/constants'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/front-desk',
      component: () => import('@/layouts/FrontDeskLayout.vue'),
      redirect: '/front-desk/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '工作台' },
        },
        {
          path: 'room-status',
          name: 'RoomStatus',
          component: () => import('@/views/room/RoomStatusView.vue'),
          meta: { title: '房态看板' },
        },
        {
          path: 'checkout',
          name: 'CheckOut',
          component: () => import('@/views/checkout/CheckOutView.vue'),
          meta: { title: '退房办理' },
        },
        {
          path: 'orders',
          name: 'Orders',
          component: () => import('@/views/order/OrderListView.vue'),
          meta: { title: '订单中心' },
        },
        {
          path: 'billing',
          name: 'Billing',
          component: () => import('@/views/billing/BillingListView.vue'),
          meta: { title: '账单管理' },
        },
        {
          path: 'orders/:id',
          name: 'OrderDetail',
          component: () => import('@/views/order/OrderDetailView.vue'),
          meta: { title: '订单详情' },
        },
        {
          path: 'notifications',
          name: 'Notifications',
          component: () => import('@/views/notice/NoticeListView.vue'),
          meta: { title: '消息通知' },
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/settings/SettingsView.vue'),
          meta: { title: '系统设置' },
        },
      ],
    },
    { path: '/', redirect: '/front-desk/dashboard' },
    { path: '/:pathMatch(.*)*', redirect: '/front-desk/dashboard' },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && token) {
    return '/front-desk/dashboard'
  }
  return true
})

export default router
