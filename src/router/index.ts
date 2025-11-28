import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import LoginView from '../views/LoginView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      title: 'Login',
      requiresAuth: false,
    },
  },
  {
    path: '/',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'projects',
        component: () => import('../views/ProjectListView.vue'),
        meta: {
          title: 'Project List',
          breadcrumb: 'Project List',
        },
      },
      {
        path: 'projects/:projectId',
        name: 'project-detail',
        component: () => import('../views/ProjectDetailView.vue'),
        props: true,
        meta: {
          title: 'Project Detail',
          breadcrumb: 'projects',
        },
      },
      {
        path: 'projects/:projectId/builds/:buildId/stages/:stageName',
        name: 'stage-detail',
        component: () => import('../views/StageDetailView.vue'),
        props: true,
        meta: {
          title: 'Stage Detail',
          breadcrumb: 'Stage',
        },
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/HomeView.vue'),
        meta: {
          title: '대시보드',
          breadcrumb: '대시보드',
        },
      },
      {
        path: 'board',
        name: 'board',
        component: () => import('../views/BoardView.vue'),
        meta: {
          title: 'Board',
          breadcrumb: 'Board',
        },
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('../views/SettingsView.vue'),
        meta: {
          title: '설정',
          breadcrumb: '설정',
        },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// Navigation guard for title
router.beforeEach((to, _from, next) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} - MintPortal` : 'MintPortal'
  next()
})

export default router
