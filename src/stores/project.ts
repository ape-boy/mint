import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Project, ProjectQueryParams } from '@/types'
import { projectApi } from '@/api/project'

export const useProjectStore = defineStore('project', () => {
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchProjects(params?: ProjectQueryParams) {
    loading.value = true
    error.value = null
    try {
      const response = await projectApi.getAll(params)
      projects.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch projects'
      console.error(e)
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
      error.value = 'Failed to fetch project'
      console.error(e)
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
      error.value = 'Failed to update project'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  function setCurrentProject(project: Project | null) {
    currentProject.value = project
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
  }
})
