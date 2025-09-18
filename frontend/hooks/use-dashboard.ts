import { useQuery } from '@tanstack/react-query'
import { api, endpoints } from '@/lib/api'
import { DashboardStats, Lead, Deal, Activity } from '@/types/crm'

export function useDashboardStats(companyId: number) {
  return useQuery({
    queryKey: ['dashboard', 'stats', companyId],
    queryFn: async () => {
      const response = await api.get(endpoints.DASHBOARD.STATS, {
        params: { companyId }
      })
      return response.data as DashboardStats
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useRecentLeads(companyId: number, limit = 5) {
  return useQuery({
    queryKey: ['dashboard', 'recent-leads', companyId, limit],
    queryFn: async () => {
      const response = await api.get(endpoints.LEADS.LIST, {
        params: { companyId, limit, page: 0 }
      })
      return response.data.content || response.data as Lead[]
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useRecentDeals(companyId: number, limit = 5) {
  return useQuery({
    queryKey: ['dashboard', 'recent-deals', companyId, limit],
    queryFn: async () => {
      const response = await api.get(endpoints.DEALS.LIST, {
        params: { companyId, limit, page: 0 }
      })
      return response.data.content || response.data as Deal[]
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useRecentActivities(companyId: number, limit = 10) {
  return useQuery({
    queryKey: ['dashboard', 'recent-activities', companyId, limit],
    queryFn: async () => {
      const response = await api.get(endpoints.ACTIVITIES.LIST, {
        params: { companyId, limit, page: 0 }
      })
      return response.data.content || response.data as Activity[]
    },
    staleTime: 1 * 60 * 1000, // 1 minute
  })
}

export function useLeadStats(companyId: number) {
  return useQuery({
    queryKey: ['leads', 'stats', companyId],
    queryFn: async () => {
      const response = await api.get(endpoints.LEADS.STATS, {
        params: { companyId }
      })
      return response.data
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useLeadSources(companyId: number) {
  return useQuery({
    queryKey: ['leads', 'sources', companyId],
    queryFn: async () => {
      const response = await api.get(endpoints.LEADS.SOURCES, {
        params: { companyId }
      })
      return response.data
    },
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

export function useLeadUsers(companyId: number) {
  return useQuery({
    queryKey: ['leads', 'users', companyId],
    queryFn: async () => {
      const response = await api.get(endpoints.LEADS.USERS, {
        params: { companyId }
      })
      return response.data
    },
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}
