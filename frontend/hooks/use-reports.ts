import { useQuery } from '@tanstack/react-query'
import { useApi } from './use-api'

export function useReports(days: number = 30) {
  const { getLeadsPerformance, getSalesPipeline, getActivitySummary, getRevenueForecast, getQuickStats } = useApi()
  
  const leadsPerformance = useQuery({
    queryKey: ['reports', 'leadsPerformance', days],
    queryFn: () => getLeadsPerformance(days),
    staleTime: 5 * 60 * 1000, // 5 minutes
  })

  const salesPipeline = useQuery({
    queryKey: ['reports', 'salesPipeline', days],
    queryFn: () => getSalesPipeline(days),
    staleTime: 5 * 60 * 1000, // 5 minutes
  })

  const activitySummary = useQuery({
    queryKey: ['reports', 'activitySummary', days],
    queryFn: () => getActivitySummary(days),
    staleTime: 5 * 60 * 1000, // 5 minutes
  })

  const revenueForecast = useQuery({
    queryKey: ['reports', 'revenueForecast', days],
    queryFn: () => getRevenueForecast(days),
    staleTime: 5 * 60 * 1000, // 5 minutes
  })

  const quickStats = useQuery({
    queryKey: ['reports', 'quickStats', days],
    queryFn: () => getQuickStats(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  })

  return {
    leadsPerformance,
    salesPipeline,
    activitySummary,
    revenueForecast,
    quickStats,
    isLoading: leadsPerformance.isLoading || salesPipeline.isLoading || activitySummary.isLoading || revenueForecast.isLoading || quickStats.isLoading,
    error: leadsPerformance.error || salesPipeline.error || activitySummary.error || revenueForecast.error || quickStats.error,
  }
}
