import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, endpoints } from '@/lib/api'
import { Company } from '@/types/crm'
import { toast } from 'sonner'

export function useCompany(companyId: number) {
  return useQuery({
    queryKey: ['company', companyId],
    queryFn: async () => {
      const response = await api.get(endpoints.COMPANIES.GET(companyId))
      return response.data as Company
    },
    enabled: !!companyId,
  })
}

export function useCompanies() {
  return useQuery({
    queryKey: ['companies'],
    queryFn: async () => {
      const response = await api.get(endpoints.COMPANIES.LIST)
      return response.data as Company[]
    },
  })
}

export function useCreateCompany() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: async (company: Omit<Company, 'id' | 'createdAt' | 'updatedAt'>) => {
      const response = await api.post(endpoints.COMPANIES.CREATE, company)
      return response.data as Company
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['companies'] })
      toast.success('Company created successfully')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create company')
    },
  })
}

export function useUpdateCompany() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: async ({ id, ...company }: Company) => {
      const response = await api.put(endpoints.COMPANIES.UPDATE(id), company)
      return response.data as Company
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['companies'] })
      queryClient.invalidateQueries({ queryKey: ['company', data.id] })
      toast.success('Company updated successfully')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update company')
    },
  })
}

export function useDeleteCompany() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(endpoints.COMPANIES.DELETE(id))
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['companies'] })
      toast.success('Company deleted successfully')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete company')
    },
  })
}
