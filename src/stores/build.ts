import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Build, BuildQueryParams } from '@/types'
import { buildApi } from '@/api/build'
import axios from 'axios'

function getErrorMessage(e: unknown, fallback: string): string {
  if (axios.isAxiosError(e)) {
    if (e.response?.status === 404) return 'Build not found'
    if (e.response?.status === 403) return 'Access denied'
    if (e.response?.status === 500) return 'Server error. Please try again later.'
    if (e.code === 'ECONNREFUSED') return 'Unable to connect to server'
    return e.response?.data?.message || fallback
  }
  if (e instanceof Error) return e.message
  return fallback
}

export const useBuildStore = defineStore('build', () => {
  const builds = ref<Build[]>([])
  const currentBuild = ref<Build | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  function clearError() {
    error.value = null
  }

  async function fetchBuilds(params?: BuildQueryParams) {
    loading.value = true
    error.value = null
    try {
      const response = await buildApi.getAll(params)
      builds.value = response.data
    } catch (e) {
      console.error('Failed to fetch builds:', e)
      error.value = getErrorMessage(e, 'Failed to fetch builds')
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
      console.error(`Failed to fetch build ${id}:`, e)
      error.value = getErrorMessage(e, 'Failed to fetch build')
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
      console.error(`Failed to trigger release for build ${buildId}:`, e)
      error.value = getErrorMessage(e, 'Failed to trigger release')
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

  function $reset() {
    builds.value = []
    currentBuild.value = null
    loading.value = false
    error.value = null
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
    clearError,
    $reset,
  }
})
