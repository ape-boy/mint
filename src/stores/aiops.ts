import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAIOpsStore = defineStore('aiops', () => {
    const insights = ref<any[]>([])
    const chatHistory = ref<any[]>([])

    function addMessage(message: any) {
        chatHistory.value.push(message)
    }

    function setInsights(data: any) {
        insights.value = data
    }

    return { insights, chatHistory, addMessage, setInsights }
})
