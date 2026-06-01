import { buildUrl, getAuthHeader, clearSession } from './request.js'

function appendQuery(url, params = {}) {
  const query = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

function pickFileFromResult(res) {
  const file = res.tempFiles && res.tempFiles[0]
  if (file) {
    return {
      path: file.path || file.tempFilePath,
      name: file.name || 'import.xlsx'
    }
  }
  const path = res.tempFilePaths && res.tempFilePaths[0]
  return path ? { path, name: 'import.xlsx' } : null
}

export function chooseExcelFile() {
  return new Promise((resolve, reject) => {
    const success = res => {
      const file = pickFileFromResult(res)
      if (!file || !file.path) {
        reject(new Error('未选择文件'))
        return
      }
      resolve(file)
    }
    const fail = err => reject(err)

    if (uni.chooseFile) {
      uni.chooseFile({
        count: 1,
        type: 'all',
        extension: ['.xlsx', '.xls', 'xlsx', 'xls'],
        success,
        fail
      })
      return
    }

    // #ifdef MP-WEIXIN
    uni.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['xlsx', 'xls'],
      success,
      fail
    })
    // #endif

    uni.showToast({ title: '请在系统文件管理器中选择Excel文件', icon: 'none' })
    reject(new Error('当前环境不支持选择文件'))
  })
}

export async function uploadExcel(apiPath) {
  const file = await chooseExcelFile()
  uni.showLoading({ title: '导入中', mask: true })
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: buildUrl(apiPath),
      filePath: file.path,
      name: 'file',
      header: getAuthHeader(),
      formData: {},
      success(res) {
        if (res.statusCode === 401) {
          clearSession()
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录'))
          return
        }
        if (res.statusCode < 200 || res.statusCode >= 300) {
          uni.showToast({ title: `导入失败(${res.statusCode})`, icon: 'none' })
          reject(new Error(`导入失败(${res.statusCode})`))
          return
        }
        let data = res.data
        if (typeof data === 'string' && data) {
          try {
            data = JSON.parse(data)
          } catch (e) {
            data = {}
          }
        }
        resolve(data || {})
      },
      fail(err) {
        uni.showToast({ title: err.errMsg || '导入失败，请检查文件权限', icon: 'none' })
        reject(err)
      },
      complete() {
        uni.hideLoading()
      }
    })
  })
}

export function downloadExcel(apiPath, params = {}) {
  const url = buildUrl(appendQuery(apiPath, params))
  uni.showLoading({ title: '导出中', mask: true })
  return new Promise((resolve, reject) => {
    uni.downloadFile({
      url,
      header: getAuthHeader(),
      success(res) {
        if (res.statusCode === 401) {
          clearSession()
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录'))
          return
        }
        if (res.statusCode !== 200) {
          uni.showToast({ title: `导出失败(${res.statusCode})`, icon: 'none' })
          reject(new Error(`导出失败(${res.statusCode})`))
          return
        }
        uni.openDocument({
          filePath: res.tempFilePath,
          fileType: 'xlsx',
          showMenu: true,
          success() {
            resolve(res.tempFilePath)
          },
          fail(err) {
            // #ifdef APP-PLUS
            if (typeof plus !== 'undefined' && plus.runtime && plus.runtime.openFile) {
              plus.runtime.openFile(res.tempFilePath, {}, () => {
                uni.showToast({ title: '文件已下载，未找到可打开Excel的应用', icon: 'none' })
                reject(err)
              })
              return
            }
            // #endif
            uni.showToast({ title: '文件已下载，打开失败', icon: 'none' })
            reject(err)
          }
        })
      },
      fail(err) {
        uni.showToast({ title: err.errMsg || '导出失败', icon: 'none' })
        reject(err)
      },
      complete() {
        uni.hideLoading()
      }
    })
  })
}
