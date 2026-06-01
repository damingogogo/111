import { BASE_URL } from './config.js'

const SESSION_KEY = 'WAREHOUSE_SESSION_COOKIE'
const USER_KEY = 'WAREHOUSE_LOGIN_USER'

export function buildUrl(url) {
  if (/^https?:\/\//i.test(url)) return url
  return `${BASE_URL}${url}`
}

export function getCookie() {
  return uni.getStorageSync(SESSION_KEY) || ''
}

export function getAuthHeader(extra = {}) {
  const cookie = getCookie()
  return Object.assign({}, cookie ? { Cookie: cookie } : {}, extra)
}

function normalizeSetCookie(value) {
  if (!value) return ''
  const list = Array.isArray(value)
    ? value
    : String(value).split(/,(?=\s*[^;,\s]+=)/)

  return list
    .map(item => String(item).split(';')[0].trim())
    .filter(Boolean)
    .join('; ')
}

function saveCookie(headers) {
  const setCookie = headers && (headers['Set-Cookie'] || headers['set-cookie'])
  const cookie = normalizeSetCookie(setCookie)
  if (cookie) {
    uni.setStorageSync(SESSION_KEY, cookie)
  }
}

function hideLoadingSafe() {
  try {
    uni.hideLoading()
  } catch (e) {
    // ignore
  }
}

export function clearSession() {
  uni.removeStorageSync(SESSION_KEY)
  uni.removeStorageSync(USER_KEY)
}

export function saveUser(user) {
  uni.setStorageSync(USER_KEY, user || {})
}

export function getUser() {
  return uni.getStorageSync(USER_KEY) || {}
}

export function request(options) {
  const method = (options.method || 'GET').toUpperCase()
  const cookie = getCookie()

  // 统一用 application/json，H5和小程序都兼容
  const header = getAuthHeader(options.header || {})

  // 强制设置 Content-Type（小写键，避免 uni-app 内部转换问题）
  if (method !== 'GET' && method !== 'HEAD') {
    header['content-type'] = 'application/json'
  }

  let requestData = options.data || {}
  // 显式序列化为 JSON 字符串
  if (method !== 'GET' && method !== 'HEAD' && typeof requestData !== 'string') {
    requestData = JSON.stringify(requestData)
  }

  const url = buildUrl(options.url)

  if (options.loading !== false) {
    uni.showLoading({ title: '加载中', mask: true })
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method,
      data: requestData,
      header,
      timeout: options.timeout || 15000,
      withCredentials: true,
      success(res) {
        hideLoadingSafe()
        saveCookie(res.header || {})

        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
          return
        }

        const data = res.data || {}
        let message = data.message || data.error || `请求失败(${res.statusCode})`
        if (typeof message !== 'string') message = JSON.stringify(message)

        if (res.statusCode === 401) {
          clearSession()
          uni.reLaunch({ url: '/pages/login/login' })
        }

        uni.showToast({ title: message, icon: 'none' })
        reject(new Error(message))
      },
      fail(err) {
        hideLoadingSafe()
        const message = err && err.errMsg ? err.errMsg : '网络连接失败'
        uni.showToast({ title: message, icon: 'none' })
        reject(err)
      }
    })
  })
}
