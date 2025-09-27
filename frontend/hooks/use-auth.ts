'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getAuthToken, setAuthToken, removeAuthToken } from '@/lib/auth'
import { buildApiUrl, API_ENDPOINTS } from '@/lib/api-config'

// Global state to prevent multiple API calls
let globalAuthState: AuthState = {
  user: null,
  isLoading: true,
  isAuthenticated: false
}

let authCheckPromise: Promise<void> | null = null

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
  const [authState, setAuthState] = useState<AuthState>(globalAuthState)
  const router = useRouter()

  useEffect(() => {
    // Check if user is authenticated
    const checkAuth = async () => {
      // If there's already an auth check in progress, wait for it
      if (authCheckPromise) {
        await authCheckPromise
        setAuthState(globalAuthState)
        return
      }

      // If we already have a valid auth state, don't check again
      if (globalAuthState.isAuthenticated && globalAuthState.user) {
        setAuthState(globalAuthState)
        return
      }

      // Start a new auth check
      authCheckPromise = (async () => {
        try {
          const token = getAuthToken()
          if (!token) {
            globalAuthState = {
              user: null,
              isLoading: false,
              isAuthenticated: false
            }
            setAuthState(globalAuthState)
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
            globalAuthState = {
              user,
              isLoading: false,
              isAuthenticated: true
            }
          } else {
            removeAuthToken()
            globalAuthState = {
              user: null,
              isLoading: false,
              isAuthenticated: false
            }
          }
        } catch (error) {
          console.error('Auth check failed:', error)
          globalAuthState = {
            user: null,
            isLoading: false,
            isAuthenticated: false
          }
        } finally {
          setAuthState(globalAuthState)
          authCheckPromise = null
        }
      })()

      await authCheckPromise
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
        globalAuthState = {
          user,
          isLoading: false,
          isAuthenticated: true
        }
        setAuthState(globalAuthState)
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

  const logout = async () => {
    try {
      // Call backend logout endpoint
      const token = getAuthToken()
      if (token) {
        await fetch(buildApiUrl(API_ENDPOINTS.AUTH.LOGOUT), {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })
      }
    } catch (error) {
      console.error('Logout API call failed:', error)
      // Continue with logout even if API call fails
    } finally {
      // Always clear local state and redirect
      removeAuthToken()
      globalAuthState = {
        user: null,
        isLoading: false,
        isAuthenticated: false
      }
      setAuthState(globalAuthState)
      router.push('/login')
    }
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