import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Build } from '@/types'
import { buildApi } from '@/api/build'

export const useBuildStore = defineStore('build', () => {
  const builds = ref<Build[]>([])
  const currentBuild = ref<Build | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchBuilds(projectId?: string, layerId?: string) {
    loading.value = true
    error.value = null
    try {
      const response = await buildApi.getAll(projectId, layerId)
      builds.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch builds'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  async function fetchBuildById(id: string) {
    loading.value = true
    error.value = null
    try {
      const response = await buildApi.getById(id)
      currentBuild.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch build'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  function setCurrentBuild(build: Build | null) {
    currentBuild.value = build
  }

  function getBuildsByLayerId(layerId: string) {
    return builds.value.filter((b) => b.layerId === layerId)
  }

  return {
    builds,
    currentBuild,
    loading,
    error,
    fetchBuilds,
    fetchBuildById,
    setCurrentBuild,
    getBuildsByLayerId,
  }
})
