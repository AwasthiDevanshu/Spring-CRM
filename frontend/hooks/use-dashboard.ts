import { useQuery } from '@tanstack/react-query'
import { useApi } from './use-api'

export function useDashboardStats() {
  const { getQuickStats } = useApi()
  return useQuery({
    queryKey: ['dashboardStats'],
    queryFn: getQuickStats,
    staleTime: 60 * 1000, // 1 minute
  })
}

export function useRecentActivities() {
  const { getActivities } = useApi()
  return useQuery({
    queryKey: ['recentActivities'],
    queryFn: async () => {
      const data = await getActivities()
      return data.slice(0, 10) // Limit to 10 recent activities
    },
    staleTime: 30 * 1000, // 30 seconds
  })
}

export function useNavigationCounts() {
  const { getLeads, getContacts, getDeals, getActivities } = useApi()
  return useQuery({
    queryKey: ['navigationCounts'],
    queryFn: async () => {
      const [leads, contacts, deals, activities] = await Promise.all([
        getLeads(),
        getContacts(),
        getDeals(),
        getActivities(),
      ])
      return {
        leads: leads.length,
        contacts: contacts.length,
        deals: deals.length,
        activities: activities.length,
      }
    },
    staleTime: 60 * 1000, // 1 minute
  })
}
