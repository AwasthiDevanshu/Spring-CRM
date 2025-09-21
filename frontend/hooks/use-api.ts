'use client'

import { useCallback } from 'react'
import { useCompanyId } from './use-company-id'
import { buildApiUrl, API_ENDPOINTS } from '@/lib/api-config'
import { getAuthHeader } from '@/lib/auth'

/**
 * Custom hook for API calls that automatically includes company ID from token
 */
export function useApi() {
  const { isAuthenticated, isLoading } = useCompanyId()

  const apiCall = useCallback(async (
    endpoint: string,
    options: RequestInit = {},
    includeCompanyId: boolean = true
  ) => {
    if (!isAuthenticated) {
      throw new Error('User not authenticated')
    }

    // companyId is now extracted from JWT token on the backend
    const url = buildApiUrl(endpoint)

    const headers = {
      'Content-Type': 'application/json',
      ...getAuthHeader() ? { 'Authorization': getAuthHeader()! } : {},
      ...options.headers
    }

    const response = await fetch(url, {
      ...options,
      headers
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`)
    }

    return response
  }, [isAuthenticated])

  const apiGet = useCallback(async (endpoint: string, includeCompanyId: boolean = true) => {
    const response = await apiCall(endpoint, { method: 'GET' }, includeCompanyId)
    return response.json()
  }, [apiCall])

  const apiPost = useCallback(async (endpoint: string, data: any, includeCompanyId: boolean = true) => {
    const response = await apiCall(endpoint, {
      method: 'POST',
      body: JSON.stringify(data)
    }, includeCompanyId)
    return response.json()
  }, [apiCall])

  const apiPut = useCallback(async (endpoint: string, data: any, includeCompanyId: boolean = true) => {
    const response = await apiCall(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data)
    }, includeCompanyId)
    return response.json()
  }, [apiCall])

  const apiPatch = useCallback(async (endpoint: string, data: any, includeCompanyId: boolean = true) => {
    const response = await apiCall(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(data)
    }, includeCompanyId)
    return response.json()
  }, [apiCall])

  const apiDelete = useCallback(async (endpoint: string, includeCompanyId: boolean = true) => {
    const response = await apiCall(endpoint, { method: 'DELETE' }, includeCompanyId)
    return response.json()
  }, [apiCall])

  return {
    isAuthenticated,
    isLoading,
    apiCall,
    apiGet,
    apiPost,
    apiPut,
    apiPatch,
    apiDelete,
    // Convenience methods for common endpoints
    getContacts: () => apiGet(API_ENDPOINTS.CONTACTS.LIST),
    createContact: (data: any) => apiPost(API_ENDPOINTS.CONTACTS.CREATE, data),
    getContact: (id: number) => apiGet(API_ENDPOINTS.CONTACTS.GET(id), false),
    updateContact: (id: number, data: any) => apiPut(API_ENDPOINTS.CONTACTS.UPDATE(id), data),
    deleteContact: (id: number) => apiDelete(API_ENDPOINTS.CONTACTS.DELETE(id), false),
    
    getLeads: () => apiGet(API_ENDPOINTS.LEADS.LIST),
    createLead: (data: any) => apiPost(API_ENDPOINTS.LEADS.CREATE, data),
    getLead: (id: number) => apiGet(API_ENDPOINTS.LEADS.GET(id), false),
    updateLead: (id: number, data: any) => apiPut(API_ENDPOINTS.LEADS.UPDATE(id), data),
    deleteLead: (id: number) => apiDelete(API_ENDPOINTS.LEADS.DELETE(id), false),
    
    getDeals: () => apiGet(API_ENDPOINTS.DEALS.LIST),
    createDeal: (data: any) => apiPost(API_ENDPOINTS.DEALS.CREATE, data),
    getDeal: (id: number) => apiGet(API_ENDPOINTS.DEALS.GET(id), false),
    updateDeal: (id: number, data: any) => apiPut(API_ENDPOINTS.DEALS.UPDATE(id), data),
    deleteDeal: (id: number) => apiDelete(API_ENDPOINTS.DEALS.DELETE(id), false),
    
    getActivities: () => apiGet(API_ENDPOINTS.ACTIVITIES.LIST),
    createActivity: (data: any) => apiPost(API_ENDPOINTS.ACTIVITIES.CREATE, data),
    getActivity: (id: number) => apiGet(API_ENDPOINTS.ACTIVITIES.GET(id), false),
    updateActivity: (id: number, data: any) => apiPut(API_ENDPOINTS.ACTIVITIES.UPDATE(id), data),
    deleteActivity: (id: number) => apiDelete(API_ENDPOINTS.ACTIVITIES.DELETE(id), false),
    
    getReports: (endpoint: string, days: number = 30) => apiGet(`${endpoint}?days=${days}`),
    getQuickStats: () => apiGet(API_ENDPOINTS.REPORTS.QUICK_STATS),
    getLeadsPerformance: (days: number = 30) => apiGet(`${API_ENDPOINTS.REPORTS.LEADS_PERFORMANCE}?days=${days}`),
    getSalesPipeline: (days: number = 30) => apiGet(`${API_ENDPOINTS.REPORTS.SALES_PIPELINE}?days=${days}`),
    getActivitySummary: (days: number = 30) => apiGet(`${API_ENDPOINTS.REPORTS.ACTIVITY_SUMMARY}?days=${days}`),
    getRevenueForecast: () => apiGet(API_ENDPOINTS.REPORTS.REVENUE_FORECAST),
    
    // Import methods
    importCsv: (formData: FormData) => apiCall('/import/csv', {
      method: 'POST',
      body: formData
    }, false),
    
    // Custom Forms methods
    getCustomForms: () => apiGet(API_ENDPOINTS.CUSTOM_FORMS.LIST),
    createCustomForm: (data: any) => apiPost(API_ENDPOINTS.CUSTOM_FORMS.CREATE, data),
    getCustomForm: (id: number) => apiGet(API_ENDPOINTS.CUSTOM_FORMS.GET(id), false),
    updateCustomForm: (id: number, data: any) => apiPut(API_ENDPOINTS.CUSTOM_FORMS.UPDATE(id), data),
    deleteCustomForm: (id: number) => apiDelete(API_ENDPOINTS.CUSTOM_FORMS.DELETE(id), false),
    createFormAccess: (formId: number, data: any) => apiPost(API_ENDPOINTS.CUSTOM_FORMS.CREATE_ACCESS(formId), data),
    getFormAccessHistory: (formId: number) => apiGet(API_ENDPOINTS.CUSTOM_FORMS.ACCESS_HISTORY(formId)),
    getFormShareUrl: (formId: number) => apiGet(API_ENDPOINTS.CUSTOM_FORMS.SHARE_URL(formId)),
    getFormByAccessToken: (token: string) => apiGet(API_ENDPOINTS.CUSTOM_FORMS.PUBLIC_ACCESS(token), false),
    submitFormWithAccess: (data: any) => apiPost(API_ENDPOINTS.CUSTOM_FORMS.PUBLIC_SUBMIT, data, false),
    deactivateFormAccess: (accessId: number) => apiDelete(API_ENDPOINTS.CUSTOM_FORMS.DEACTIVATE_ACCESS(accessId), false),
    regenerateFormAccess: (accessId: number) => apiPost(API_ENDPOINTS.CUSTOM_FORMS.REGENERATE_ACCESS(accessId), {}),
    getFormWhatsAppUrl: (accessId: number) => apiGet(API_ENDPOINTS.CUSTOM_FORMS.WHATSAPP_URL(accessId)),
    
    // Chatbot methods
    sendChatbotQuery: (query: string) => apiPost(API_ENDPOINTS.CHATBOT.QUERY, { query }),
  }
}
