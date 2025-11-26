import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const user = ref(null)
    const isAuthenticated = ref(false)

    function login(userData: any) {
        user.value = userData
        isAuthenticated.value = true
    }

    function logout() {
        user.value = null
        isAuthenticated.value = false
    }

    return { user, isAuthenticated, login, logout }
})
