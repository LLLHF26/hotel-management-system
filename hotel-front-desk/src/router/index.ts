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
          path: 'orders',
          name: 'Orders',
          component: () => import('@/views/order/OrderListView.vue'),
          meta: { title: '订单管理' },
        },
        {
          path: 'orders/:id',
          name: 'OrderDetail',
          component: () => import('@/views/order/OrderDetailView.vue'),
          meta: { title: '订单详情' },
        },
        {
          path: 'customers',
          name: 'Customers',
          component: () => import('@/views/customer/CustomerListView.vue'),
          meta: { title: '客户管理' },
        },
        {
          path: 'customers/:id',
          name: 'CustomerDetail',
          component: () => import('@/views/customer/CustomerDetailView.vue'),
          meta: { title: '客户详情' },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/ProfileView.vue'),
          meta: { title: '个人信息' },
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
