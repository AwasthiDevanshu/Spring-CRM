import { useState, useEffect } from 'react'
import { useAuth } from '@/hooks/use-auth'
import { getAuthHeader } from '@/lib/auth'
import { buildApiUrl, API_ENDPOINTS } from '@/lib/api-config'

interface NavigationCounts {
  leads: number
  contacts: number
  deals: number
  activities: number
  isLoading: boolean
}

export function useNavigationCounts(): NavigationCounts {
  const [counts, setCounts] = useState<NavigationCounts>({
    leads: 0,
    contacts: 0,
    deals: 0,
    activities: 0,
    isLoading: true
  })
  const { user, isAuthenticated } = useAuth()

  useEffect(() => {
    if (!isAuthenticated || !user?.companyId) {
      setCounts(prev => ({ ...prev, isLoading: false }))
      return
    }

    const fetchCounts = async () => {
      try {
        // Set default counts to avoid UI breaking
        setCounts({
          leads: 0,
          contacts: 0,
          deals: 0,
          activities: 0,
          isLoading: false
        })

        const headers = {
          'Authorization': getAuthHeader() || ''
        }

        // Fetch leads count only for now to avoid breaking UI
        try {
          const leadsRes = await fetch(buildApiUrl(API_ENDPOINTS.LEADS.LIST, user.companyId), { headers })
          if (leadsRes.ok) {
            const leads = await leadsRes.json()
            setCounts(prev => ({
              ...prev,
              leads: Array.isArray(leads) ? leads.length : 0
            }))
          }
        } catch (e) {
          console.warn('Could not fetch leads count:', e)
        }

      } catch (error) {
        console.error('Failed to fetch navigation counts:', error)
        setCounts({
          leads: 0,
          contacts: 0,
          deals: 0,
          activities: 0,
          isLoading: false
        })
      }
    }

    fetchCounts()

    // Refresh counts every 30 seconds
    const interval = setInterval(fetchCounts, 30000)
    return () => clearInterval(interval)
  }, [isAuthenticated, user?.companyId])

  return counts
}
