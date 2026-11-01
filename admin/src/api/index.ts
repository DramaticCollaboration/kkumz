import axios from 'axios'

// 'defHttp' common axios wrapper with interceptors
const defHttp = axios.create({
  baseURL: '/api/v1/kkumz',
  timeout: 5000,
  headers: { 'Content-Type': 'application/json' }
})

defHttp.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const getDashboardSummary = () => {
  return defHttp.get('/api/v1/kkumz/dashboard/kpi')
}
