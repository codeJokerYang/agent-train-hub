import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'datasets',
        name: 'datasets',
        component: () => import('@/views/DatasetList.vue'),
        meta: { title: '数据集' }
      },
      {
        path: 'jobs',
        name: 'jobs',
        component: () => import('@/views/JobList.vue'),
        meta: { title: '训练任务' }
      },
      {
        path: 'jobs/create',
        name: 'job-create',
        component: () => import('@/views/JobCreate.vue'),
        meta: { title: '创建任务' }
      },
      {
        path: 'jobs/:id',
        name: 'job-detail',
        component: () => import('@/views/JobDetail.vue'),
        meta: { title: '任务详情' }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '用户管理' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫（第一阶段占位）：仅根据本地 token 是否存在决定是否跳转登录页。
// 第二阶段接入真实 JWT 登录后，这里改为校验 token 有效性并拉取用户信息。
router.beforeEach((to) => {
  const token = localStorage.getItem('ath_token')
  if (!to.meta.public && !token) {
    return { name: 'login' }
  }
  if (to.name === 'login' && token) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
