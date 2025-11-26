import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '',
          name: 'projects',
          component: () => import('../views/ProjectListView.vue'),
        },
        {
          path: 'projects/:projectId',
          name: 'project-detail',
          component: () => import('../views/ProjectDetailView.vue'),
          props: true,
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/HomeView.vue'),
        },
        {
          path: 'build',
          name: 'build-list',
          component: () => import('../views/BuildListView.vue'),
        },
        {
          path: 'build/:id',
          name: 'build-detail',
          component: () => import('../views/BuildDetailView.vue'),
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('../views/SettingsView.vue'),
        },
      ],
    },
  ],
})

export default router
