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
          title: 'Projects',
          breadcrumb: 'Projects',
        },
      },
      {
        path: 'projects/:projectId',
        name: 'project-detail',
        component: () => import('../views/ProjectDetailView.vue'),
        props: true,
        meta: {
          title: 'Project Detail',
          breadcrumb: 'Project Detail',
        },
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/HomeView.vue'),
        meta: {
          title: 'Dashboard',
          breadcrumb: 'Dashboard',
        },
      },
      {
        path: 'build',
        name: 'build-list',
        component: () => import('../views/BuildListView.vue'),
        meta: {
          title: 'Builds',
          breadcrumb: 'Builds',
        },
      },
      {
        path: 'build/new',
        name: 'build-new',
        component: () => import('../views/BuildNewView.vue'),
        meta: {
          title: 'New Build',
          breadcrumb: 'New Build',
        },
      },
      {
        path: 'build/:id',
        name: 'build-detail',
        component: () => import('../views/BuildDetailView.vue'),
        meta: {
          title: 'Build Detail',
          breadcrumb: 'Build Detail',
        },
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('../views/SettingsView.vue'),
        meta: {
          title: 'Settings',
          breadcrumb: 'Settings',
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
