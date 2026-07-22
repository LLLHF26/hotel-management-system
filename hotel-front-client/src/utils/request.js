const BASE_URL = 'http://localhost:8088'
const AI_BASE_URL = 'http://localhost:8088'
const TIMEOUT = 15000
const AI_TIMEOUT = 60000

function getToken() {
  try {
    return uni.getStorageSync('token') || ''
  } catch {
    return ''
  }
}

function request(url, options = {}, useAi = false) {
  const token = getToken()
  const base = useAi ? AI_BASE_URL : BASE_URL

  return new Promise((resolve, reject) => {
    uni.request({
      url: base + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
        ...(options.header || {})
      },
      timeout: options.timeout || (useAi ? AI_TIMEOUT : TIMEOUT),
      success(res) {
        const { statusCode, data } = res
        if (statusCode === 200) {
          if (data && data.code === 200) {
            resolve(data)
          } else if (data && data.code) {
            uni.showToast({ title: data.msg || '请求失败', icon: 'none' })
            reject(data)
          } else {
            resolve(data)
          }
        } else if (statusCode === 401) {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(data)
        } else if (statusCode === 403) {
          uni.showToast({ title: '权限不足', icon: 'none' })
          reject(data)
        } else {
          // 业务异常：优先展示后端返回的 msg（如“用户名或密码错误”）
          if (data && data.msg) {
            uni.showToast({ title: data.msg, icon: 'none' })
          } else {
            uni.showToast({ title: '服务器错误(' + statusCode + ')', icon: 'none' })
          }
          reject(data)
        }
      },
      fail(err) {
        uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

function get(url, params = {}, useAi = false) {
  const qs = Object.keys(params)
    .filter(k => params[k] !== undefined && params[k] !== null && params[k] !== '')
    .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(params[k]))
    .join('&')
  const full = qs ? url + '?' + qs : url
  return request(full, { method: 'GET' }, useAi)
}

function post(url, data = {}, useAi = false) {
  return request(url, { method: 'POST', data }, useAi)
}

function put(url, data = {}, useAi = false) {
  return request(url, { method: 'PUT', data }, useAi)
}

function del(url, useAi = false) {
  return request(url, { method: 'DELETE' }, useAi)
}

function uploadFile(url, filePath, formName = 'file') {
  const token = getToken()
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + url,
      filePath,
      name: formName,
      header: {
        ...(token ? { 'Authorization': 'Bearer ' + token } : {})
      },
      success(res) {
        try {
          const data = JSON.parse(res.data)
          if (data && data.code === 200) {
            resolve(data)
          } else {
            uni.showToast({ title: (data && data.msg) || '上传失败', icon: 'none' })
            reject(data)
          }
        } catch {
          reject({ msg: '解析响应失败' })
        }
      },
      fail(err) {
        uni.showToast({ title: '上传失败，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

export { get, post, put, del, uploadFile, BASE_URL, AI_BASE_URL }
