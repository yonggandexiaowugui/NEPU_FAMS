const TOKEN_KEY = 'fams_token'
const USER_INFO_KEY = 'fams_user_info'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserInfo() {
  const info = localStorage.getItem(USER_INFO_KEY)
  return info ? JSON.parse(info) : null
}

export function setUserInfo(info) {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
}

export function removeUserInfo() {
  localStorage.removeItem(USER_INFO_KEY)
}

export function clearAuth() {
  removeToken()
  removeUserInfo()
}
