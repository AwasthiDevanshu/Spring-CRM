'use client'

import { useAuth } from './use-auth'

/**
 * Hook to get the company ID from the authenticated user's token
 * This eliminates the need to pass companyId as a parameter in API calls
 */
export function useCompanyId() {
  const { user, isAuthenticated, isLoading } = useAuth()

  return {
    companyId: user?.companyId || null,
    isAuthenticated,
    isLoading,
    hasCompanyId: !!user?.companyId
  }
}
