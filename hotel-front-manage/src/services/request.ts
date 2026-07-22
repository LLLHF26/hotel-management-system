const BASE = 'http://localhost:8088'

function getToken() {
    return 'eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJcdTVmMjBcdTRlMDkiLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE3Nzk5NzEyNjEsImV4cCI6MjA5NTMzMTI2MX0.QfP5Dha1HqitVZze8yVTNI7WH-CMooSdKT25J3GVM5mcjtLm7Q3Ae79xDc18_lncaOJclTchaB_MsvqC7aVFvA'
}

export async function authFetch(input: string, init: RequestInit = {}) {
  const token = getToken()
  return fetch(`${BASE}${input}`, {
    ...init,
    headers: {
      ...(init.headers || {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    }
  })
}
