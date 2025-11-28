import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Build, BuildQueryParams } from '@/types'
import { buildApi } from '@/api/build'

export const useBuildStore = defineStore('build', () => {
  const builds = ref<Build[]>([])
  const currentBuild = ref<Build | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchBuilds(params?: BuildQueryParams) {
    loading.value = true
    error.value = null
    try {
      const response = await buildApi.getAll(params)
      builds.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch builds'
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
    } finally {
      loading.value = false
    }
  }

  async function triggerRelease(buildId: string) {
    loading.value = true
    error.value = null
    try {
      const response = await buildApi.triggerRelease(buildId)
      currentBuild.value = response.data
      // Update in list
      const index = builds.value.findIndex(b => b.id === buildId)
      if (index !== -1) {
        builds.value[index] = response.data
      }
    } catch (e) {
      error.value = 'Failed to trigger release'
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

  function getBuildsByProjectId(projectId: string) {
    return builds.value.filter((b) => b.projectId === projectId)
  }

  return {
    builds,
    currentBuild,
    loading,
    error,
    fetchBuilds,
    fetchBuildById,
    triggerRelease,
    setCurrentBuild,
    getBuildsByLayerId,
    getBuildsByProjectId,
  }
})
