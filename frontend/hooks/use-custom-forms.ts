'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useApi } from './use-api'

export function useCustomForms() {
  const { getCustomForms } = useApi()
  
  return useQuery({
    queryKey: ['customForms'],
    queryFn: getCustomForms,
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchOnWindowFocus: true,
  })
}

export function useCreateCustomForm() {
  const queryClient = useQueryClient()
  const { createCustomForm } = useApi()
  
  return useMutation({
    mutationFn: createCustomForm,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customForms'] })
    },
  })
}

export function useUpdateCustomForm() {
  const queryClient = useQueryClient()
  const { updateCustomForm } = useApi()
  
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: any }) => updateCustomForm(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customForms'] })
    },
  })
}

export function useDeleteCustomForm() {
  const queryClient = useQueryClient()
  const { deleteCustomForm } = useApi()
  
  return useMutation({
    mutationFn: deleteCustomForm,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customForms'] })
    },
  })
}

export function useFormAccess(formId: number) {
  const { getFormAccessHistory } = useApi()
  
  return useQuery({
    queryKey: ['formAccess', formId],
    queryFn: () => getFormAccessHistory(formId),
    enabled: !!formId,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useCreateFormAccess() {
  const queryClient = useQueryClient()
  const { createFormAccess } = useApi()
  
  return useMutation({
    mutationFn: ({ formId, data }: { formId: number; data: any }) => createFormAccess(formId, data),
    onSuccess: (_, { formId }) => {
      queryClient.invalidateQueries({ queryKey: ['formAccess', formId] })
      queryClient.invalidateQueries({ queryKey: ['customForms'] })
    },
  })
}

export function useDeactivateFormAccess() {
  const queryClient = useQueryClient()
  const { deactivateFormAccess } = useApi()
  
  return useMutation({
    mutationFn: deactivateFormAccess,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['formAccess'] })
      queryClient.invalidateQueries({ queryKey: ['customForms'] })
    },
  })
}

export function useRegenerateFormAccess() {
  const queryClient = useQueryClient()
  const { regenerateFormAccess } = useApi()
  
  return useMutation({
    mutationFn: regenerateFormAccess,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['formAccess'] })
      queryClient.invalidateQueries({ queryKey: ['customForms'] })
    },
  })
}

export function useFormByAccessToken(token: string) {
  const { getFormByAccessToken } = useApi()
  
  return useQuery({
    queryKey: ['formByAccessToken', token],
    queryFn: () => getFormByAccessToken(token),
    enabled: !!token,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useSubmitFormWithAccess() {
  const { submitFormWithAccess } = useApi()
  
  return useMutation({
    mutationFn: submitFormWithAccess,
  })
}
