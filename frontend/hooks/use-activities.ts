'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useApi } from './use-api'

export function useActivities() {
  const { getActivities } = useApi()
  
  return useQuery({
    queryKey: ['activities'],
    queryFn: getActivities,
    staleTime: 2 * 60 * 1000, // 2 minutes (activities change more frequently)
    refetchOnWindowFocus: true,
  })
}

export function useCreateActivity() {
  const queryClient = useQueryClient()
  const { createActivity } = useApi()
  
  return useMutation({
    mutationFn: createActivity,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['activities'] })
    },
  })
}

export function useUpdateActivity() {
  const queryClient = useQueryClient()
  const { updateActivity } = useApi()
  
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) => updateActivity(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['activities'] })
    },
  })
}

export function useDeleteActivity() {
  const queryClient = useQueryClient()
  const { deleteActivity } = useApi()
  
  return useMutation({
    mutationFn: deleteActivity,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['activities'] })
    },
  })
}
