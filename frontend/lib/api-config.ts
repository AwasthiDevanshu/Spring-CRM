/**
 * Centralized API configuration
 */

// Base configuration
export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/crm/api',
  TIMEOUT: 30000, // 30 seconds
} as const

// API Endpoints
export const API_ENDPOINTS = {
  // Authentication
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    ME: '/auth/me',
    REFRESH: '/auth/refresh',
    DEBUG_USERS: '/auth/debug/users',
  },

  // Dashboard
  DASHBOARD: {
    STATS: '/dashboard/stats',
    ACTIVITIES: '/dashboard/activities',
    RECENT_LEADS: '/dashboard/recent-leads',
    RECENT_DEALS: '/dashboard/recent-deals',
    LEADS_CHART: '/dashboard/leads-chart',
    REVENUE_CHART: '/dashboard/revenue-chart',
  },

  // Leads
  LEADS: {
    LIST: '/leads',
    CREATE: '/leads',
    GET: (id: number) => `/leads/${id}`,
    UPDATE: (id: number) => `/leads/${id}`,
    DELETE: (id: number) => `/leads/${id}`,
    STATS: '/leads/stats',
    SOURCES: '/leads/sources',
    USERS: '/leads/users',
  },

  // Contacts
  CONTACTS: {
    LIST: '/contacts',
    CREATE: '/contacts',
    GET: (id: number) => `/contacts/${id}`,
    UPDATE: (id: number) => `/contacts/${id}`,
    DELETE: (id: number) => `/contacts/${id}`,
    STATS: '/contacts/stats',
  },

  // Deals
  DEALS: {
    LIST: '/deals',
    CREATE: '/deals',
    GET: (id: number) => `/deals/${id}`,
    UPDATE: (id: number) => `/deals/${id}`,
    DELETE: (id: number) => `/deals/${id}`,
    STATS: '/deals/stats',
    PIPELINES: '/deals/pipelines',
    STAGES: '/deals/stages',
  },

  // Activities
  ACTIVITIES: {
    LIST: '/activities',
    CREATE: '/activities',
    GET: (id: number) => `/activities/${id}`,
    UPDATE: (id: number) => `/activities/${id}`,
    DELETE: (id: number) => `/activities/${id}`,
    STATS: '/activities/stats',
  },

  // Custom Fields
  CUSTOM_FIELDS: {
    LIST: '/custom-fields',
    CREATE: '/custom-fields',
    GET: (id: number) => `/custom-fields/${id}`,
    UPDATE: (id: number) => `/custom-fields/${id}`,
    DELETE: (id: number) => `/custom-fields/${id}`,
    CONFIGURATION: (entityType: string) => `/custom-fields/configuration/${entityType}`,
  },

  // Facebook Ads Integration
  FACEBOOK_ADS: {
    LIST: '/facebook-ads/integrations',
    CREATE: '/facebook-ads/integrations',
    GET: (id: number) => `/facebook-ads/integrations/${id}`,
    UPDATE: (id: number) => `/facebook-ads/integrations/${id}`,
    DELETE: (id: number) => `/facebook-ads/integrations/${id}`,
    SYNC: (id: number) => `/facebook-ads/integrations/${id}/sync`,
    LEADS: '/facebook-ads/leads',
    VALIDATE: '/facebook-ads/validate',
  },

  // Users
  USERS: {
    LIST: '/users',
    CREATE: '/users',
    GET: (id: number) => `/users/${id}`,
    UPDATE: (id: number) => `/users/${id}`,
    DELETE: (id: number) => `/users/${id}`,
  },

  // Companies
  COMPANIES: {
    LIST: '/companies',
    CREATE: '/companies',
    GET: (id: number) => `/companies/${id}`,
    UPDATE: (id: number) => `/companies/${id}`,
    DELETE: (id: number) => `/companies/${id}`,
  },

  // Database
  DATABASE: {
    FIX_SCHEMA: '/database/fix-schema',
    SEED_DATA: '/database/seed-data',
  },
} as const

// Helper function to build full URL
export function buildApiUrl(endpoint: string, companyId?: number): string {
  const baseUrl = API_CONFIG.BASE_URL
  const url = `${baseUrl}${endpoint}`
  
  if (companyId !== undefined) {
    const separator = endpoint.includes('?') ? '&' : '?'
    return `${url}${separator}companyId=${companyId}`
  }
  
  return url
}

// Helper function to build URL with query parameters
export function buildApiUrlWithParams(
  endpoint: string, 
  params: Record<string, string | number | boolean | undefined>
): string {
  const baseUrl = API_CONFIG.BASE_URL
  const url = `${baseUrl}${endpoint}`
  
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      searchParams.append(key, String(value))
    }
  })
  
  const queryString = searchParams.toString()
  return queryString ? `${url}?${queryString}` : url
}

// Common query parameters
export const COMMON_PARAMS = {
  COMPANY_ID: 'companyId',
  PAGE: 'page',
  SIZE: 'size',
  SORT: 'sort',
  SEARCH: 'search',
  STATUS: 'status',
  TYPE: 'type',
  PRIORITY: 'priority',
  ASSIGNED_USER_ID: 'assignedUserId',
  SOURCE: 'source',
  DAYS: 'days',
  MONTHS: 'months',
} as const
