/**
 * Authentication utility functions
 */

const AUTH_TOKEN_KEY = 'auth_token'

/**
 * Get the authentication token from localStorage
 * @returns The authentication token or null if not found
 */
export function getAuthToken(): string | null {
  if (typeof window === 'undefined') {
    return null // Server-side rendering
  }
  return localStorage.getItem(AUTH_TOKEN_KEY)
}

/**
 * Set the authentication token in localStorage
 * @param token The authentication token to store
 */
export function setAuthToken(token: string): void {
  if (typeof window === 'undefined') {
    return // Server-side rendering
  }
  localStorage.setItem(AUTH_TOKEN_KEY, token)
}

/**
 * Remove the authentication token from localStorage
 */
export function removeAuthToken(): void {
  if (typeof window === 'undefined') {
    return // Server-side rendering
  }
  localStorage.removeItem(AUTH_TOKEN_KEY)
}

/**
 * Get the authorization header value
 * @returns The Bearer token string or null if no token
 */
export function getAuthHeader(): string | null {
  const token = getAuthToken()
  return token ? `Bearer ${token}` : null
}

/**
 * Check if user is authenticated (has a valid token)
 * @returns True if user has a token, false otherwise
 */
export function isAuthenticated(): boolean {
  return getAuthToken() !== null
}
