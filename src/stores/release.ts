import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useReleaseStore = defineStore('release', () => {
    const releases = ref<any[]>([])
    const currentRelease = ref(null)

    function setReleases(data: any) {
        releases.value = data
    }

    function setCurrentRelease(release: any) {
        currentRelease.value = release
    }

    return { releases, currentRelease, setReleases, setCurrentRelease }
})
