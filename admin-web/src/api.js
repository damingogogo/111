import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:18763/api',
  timeout: 20000
})

export const apiOrigin = api.defaults.baseURL.replace(/\/api\/?$/, '')

api.interceptors.response.use((response) => {
  const body = response.data
  if (body && body.success === false) {
    return Promise.reject(new Error(body.message || '请求失败'))
  }
  return body?.data ?? body
})

export function uploadImage(file, bizType = 'common') {
  const form = new FormData()
  form.append('file', file)
  form.append('bizType', bizType)
  return api.post('/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function normalizeUploadUrl(uploaded) {
  if (!uploaded?.proxyUrl) {
    return uploaded?.url || ''
  }
  return uploaded.proxyUrl.startsWith('http') ? uploaded.proxyUrl : `${apiOrigin}${uploaded.proxyUrl}`
}
