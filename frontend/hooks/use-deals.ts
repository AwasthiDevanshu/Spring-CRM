'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useApi } from './use-api'

export function useDeals() {
  const { getDeals } = useApi()
  
  return useQuery({
    queryKey: ['deals'],
    queryFn: getDeals,
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchOnWindowFocus: true,
  })
}

export function useCreateDeal() {
  const queryClient = useQueryClient()
  const { createDeal } = useApi()
  
  return useMutation({
    mutationFn: createDeal,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['deals'] })
    },
  })
}

export function useUpdateDeal() {
  const queryClient = useQueryClient()
  const { updateDeal } = useApi()
  
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) => updateDeal(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['deals'] })
    },
  })
}

export function useDeleteDeal() {
  const queryClient = useQueryClient()
  const { deleteDeal } = useApi()
  
  return useMutation({
    mutationFn: deleteDeal,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['deals'] })
    },
  })
}
