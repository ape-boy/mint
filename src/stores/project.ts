import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Project, ProjectQueryParams } from '@/types'
import { projectApi } from '@/api/project'
import axios from 'axios'

function getErrorMessage(e: unknown, fallback: string): string {
  if (axios.isAxiosError(e)) {
    if (e.response?.status === 404) return 'Resource not found'
    if (e.response?.status === 403) return 'Access denied'
    if (e.response?.status === 500) return 'Server error. Please try again later.'
    if (e.code === 'ECONNREFUSED') return 'Unable to connect to server'
    return e.response?.data?.message || fallback
  }
  if (e instanceof Error) return e.message
  return fallback
}

export const useProjectStore = defineStore('project', () => {
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  function clearError() {
    error.value = null
  }

  async function fetchProjects(params?: ProjectQueryParams) {
    loading.value = true
    error.value = null
    try {
      const response = await projectApi.getAll(params)
      projects.value = response.data
    } catch (e) {
      console.error('Failed to fetch projects:', e)
      error.value = getErrorMessage(e, 'Failed to fetch projects')
    } finally {
      loading.value = false
    }
  }

  async function fetchProjectById(id: string) {
    loading.value = true
    error.value = null
    try {
      const response = await projectApi.getById(id)
      currentProject.value = response.data
    } catch (e) {
      console.error(`Failed to fetch project ${id}:`, e)
      error.value = getErrorMessage(e, 'Failed to fetch project')
    } finally {
      loading.value = false
    }
  }

  async function updateProject(id: string, data: Partial<Project>) {
    loading.value = true
    error.value = null
    try {
      const response = await projectApi.update(id, data)
      currentProject.value = response.data
      // Update in list as well
      const index = projects.value.findIndex(p => p.id === id)
      if (index !== -1) {
        projects.value[index] = response.data
      }
    } catch (e) {
      console.error(`Failed to update project ${id}:`, e)
      error.value = getErrorMessage(e, 'Failed to update project')
    } finally {
      loading.value = false
    }
  }

  function setCurrentProject(project: Project | null) {
    currentProject.value = project
  }

  function $reset() {
    projects.value = []
    currentProject.value = null
    loading.value = false
    error.value = null
  }

  return {
    projects,
    currentProject,
    loading,
    error,
    fetchProjects,
    fetchProjectById,
    updateProject,
    setCurrentProject,
    clearError,
    $reset,
  }
})
