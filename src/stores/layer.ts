import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Layer } from '@/types'
import { layerApi } from '@/api/layer'

export const useLayerStore = defineStore('layer', () => {
  const layers = ref<Layer[]>([])
  const currentLayer = ref<Layer | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchLayers(projectId?: string) {
    loading.value = true
    error.value = null
    try {
      const response = await layerApi.getAll(projectId ? { projectId } : undefined)
      layers.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch layers'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  async function fetchLayerById(id: string) {
    loading.value = true
    error.value = null
    try {
      const response = await layerApi.getById(id)
      currentLayer.value = response.data
    } catch (e) {
      error.value = 'Failed to fetch layer'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  function setCurrentLayer(layer: Layer | null) {
    currentLayer.value = layer
  }

  function getLayersByProjectId(projectId: string) {
    return layers.value.filter((l) => l.projectId === projectId)
  }

  return {
    layers,
    currentLayer,
    loading,
    error,
    fetchLayers,
    fetchLayerById,
    setCurrentLayer,
    getLayersByProjectId,
  }
})
