import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TaskGroup } from '@/types'
import { taskGroupApi } from '@/api/taskGroup'

// Renamed from projectGroup to taskGroup but keeping store name for compatibility
export const useTaskGroupStore = defineStore('taskGroup', () => {
  const taskGroups = ref<TaskGroup[]>([])
  const currentTaskGroup = ref<TaskGroup | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchTaskGroups() {
    loading.value = true
    error.value = null
    try {
      const response = await taskGroupApi.getAll()
      taskGroups.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch task groups'
    } finally {
      loading.value = false
    }
  }

  async function fetchTaskGroupById(id: string) {
    loading.value = true
    error.value = null
    try {
      const response = await taskGroupApi.getById(id)
      currentTaskGroup.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch task group'
    } finally {
      loading.value = false
    }
  }

  function setCurrentTaskGroup(taskGroup: TaskGroup | null) {
    currentTaskGroup.value = taskGroup
  }

  return {
    taskGroups,
    currentTaskGroup,
    loading,
    error,
    fetchTaskGroups,
    fetchTaskGroupById,
    setCurrentTaskGroup,
  }
})

// Alias for backward compatibility
export const useProjectGroupStore = useTaskGroupStore
