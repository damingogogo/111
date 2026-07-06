const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:18763/api'

export function request(path, options = {}) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${path}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'content-type': 'application/json',
        ...(options.header || {})
      },
      success: (res) => {
        const body = res.data
        if (body && body.success === false) {
          uni.showToast({ title: body.message || '请求失败', icon: 'none' })
          reject(new Error(body.message || '请求失败'))
        } else {
          resolve(body?.data ?? body)
        }
      },
      fail: (error) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(error)
      }
    })
  })
}

export function employeeId() {
  return uni.getStorageSync('employeeId')
}

export function requireEmployee() {
  const id = employeeId()
  if (!id) {
    uni.reLaunch({ url: '/pages/login/login' })
    return null
  }
  return id
}
