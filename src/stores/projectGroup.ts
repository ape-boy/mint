import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ProjectGroup } from '@/types'
import { projectGroupApi } from '@/api/projectGroup'

export const useProjectGroupStore = defineStore('projectGroup', () => {
  const projectGroups = ref<ProjectGroup[]>([])
  const currentProjectGroup = ref<ProjectGroup | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchProjectGroups() {
    loading.value = true
    error.value = null
    try {
      const response = await projectGroupApi.getAll()
      projectGroups.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch project groups'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  async function fetchProjectGroupById(id: string) {
    loading.value = true
    error.value = null
    try {
      const response = await projectGroupApi.getById(id)
      currentProjectGroup.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch project group'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  function setCurrentProjectGroup(projectGroup: ProjectGroup | null) {
    currentProjectGroup.value = projectGroup
  }

  return {
    projectGroups,
    currentProjectGroup,
    loading,
    error,
    fetchProjectGroups,
    fetchProjectGroupById,
    setCurrentProjectGroup,
  }
})
