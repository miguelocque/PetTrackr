import { createContext, useContext, useState, useEffect } from 'react'
import { getCurrentUser, login as apiLogin, logout as apiLogout, register as apiRegister } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Check if user is already logged in on mount
    checkAuth()
  }, [])

  const checkAuth = async () => {
    try {
      const response = await getCurrentUser()
      setUser(response.data)
    } catch {
      setUser(null)
    } finally {
      setLoading(false)
    }
  }

  const login = async (email, password) => {
    const response = await apiLogin(email, password)
    setUser(response.data)
    return response.data
  }

  const logout = async () => {
    await apiLogout()
    setUser(null)
  }

  const register = async (payload) => {
    const response = await apiRegister(payload)
    // After registration, log them in
    await login(payload.email, payload.password)
    return response.data
  }

  const value = {
    user,
    loading,
    login,
    logout,
    register,
    isAuthenticated: !!user
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
