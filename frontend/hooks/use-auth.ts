'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getAuthToken, setAuthToken, removeAuthToken } from '@/lib/auth'
import { buildApiUrl, API_ENDPOINTS } from '@/lib/api-config'

interface User {
  id: number
  email: string
  username: string
  firstName: string
  lastName: string
  fullName: string
  phone?: string
  isActive: boolean
  isSuperuser: boolean
  isCompanyAdmin: boolean
  companyId: number
  lastLogin?: string
}

interface AuthState {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
}

export function useAuth() {
  const [authState, setAuthState] = useState<AuthState>({
    user: null,
    isLoading: true,
    isAuthenticated: false
  })
  const router = useRouter()

  useEffect(() => {
    // Check if user is authenticated
    const checkAuth = async () => {
      try {
        const token = getAuthToken()
        if (!token) {
          setAuthState({
            user: null,
            isLoading: false,
            isAuthenticated: false
          })
          return
        }

        // Validate token with backend
        const response = await fetch(buildApiUrl(API_ENDPOINTS.AUTH.ME), {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        if (response.ok) {
          const user = await response.json()
          setAuthState({
            user,
            isLoading: false,
            isAuthenticated: true
          })
        } else {
          removeAuthToken()
          setAuthState({
            user: null,
            isLoading: false,
            isAuthenticated: false
          })
        }
      } catch (error) {
        console.error('Auth check failed:', error)
        setAuthState({
          user: null,
          isLoading: false,
          isAuthenticated: false
        })
      }
    }

    checkAuth()
  }, [])

  const login = async (email: string, password: string) => {
    try {
      const response = await fetch(buildApiUrl(API_ENDPOINTS.AUTH.LOGIN), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
      })

      if (response.ok) {
        const { token, user } = await response.json()
        setAuthToken(token)
        setAuthState({
          user,
          isLoading: false,
          isAuthenticated: true
        })
        return { success: true }
      } else {
        const error = await response.json()
        return { success: false, error: error.message }
      }
    } catch (error) {
      console.error('Login failed:', error)
      return { success: false, error: 'Login failed' }
    }
  }

  const logout = () => {
    removeAuthToken()
    setAuthState({
      user: null,
      isLoading: false,
      isAuthenticated: false
    })
    router.push('/login')
  }

  const redirectToLogin = () => {
    router.push('/login')
  }

  return {
    ...authState,
    login,
    logout,
    redirectToLogin
  }
}