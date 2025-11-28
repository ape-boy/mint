import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import LoginView from '../views/LoginView.vue'

// Simple auth check - in production, replace with actual auth service
function isAuthenticated(): boolean {
  return localStorage.getItem('mintportal_auth') === 'true'
}

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
    ],
  },
  // 404 Catch-all route
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue'),
    meta: {
      title: '404 Not Found',
      requiresAuth: false,
    },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// Navigation guard for authentication and title
router.beforeEach((to, _from, next) => {
  // Set page title
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} - MintPortal` : 'MintPortal'

  // Check authentication for protected routes
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !isAuthenticated()) {
    // Redirect to login with return URL
    next({
      name: 'login',
      query: { redirect: to.fullPath },
    })
  } else if (to.name === 'login' && isAuthenticated()) {
    // Already logged in, redirect to home
    next({ name: 'projects' })
  } else {
    next()
  }
})

export default router
