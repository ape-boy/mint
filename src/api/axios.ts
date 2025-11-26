import axios from 'axios'

const api = axios.create({
    baseURL: '/api', // Proxy will handle this in dev, or set absolute URL
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
})

// Request interceptor
api.interceptors.request.use(
    (config) => {
        // TODO: Add auth token if available
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// Response interceptor
api.interceptors.response.use(
    (response) => {
        return response
    },
    (error) => {
        // Handle global errors (e.g., 401 Unauthorized)
        if (error.response && error.response.status === 401) {
            // Redirect to login or clear token
            console.warn('Unauthorized, redirecting to login...')
        }
        return Promise.reject(error)
    }
)

export default api
