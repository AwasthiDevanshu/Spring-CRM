export interface User {
  id: number
  email: string
  username: string
  firstName: string
  lastName: string
  phone?: string
  isActive: boolean
  isSuperuser: boolean
  isCompanyAdmin: boolean
  companyId: number
  createdAt: string
  updatedAt: string
}

export interface Company {
  id: number
  name: string
  description?: string
  website?: string
  phone?: string
  email?: string
  address?: string
  city?: string
  state?: string
  country?: string
  postalCode?: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface Lead {
  id: number
  firstName: string
  lastName: string
  email?: string
  phone?: string
  company?: string
  jobTitle?: string
  source: string
  status: string
  score: number
  estimatedValue?: number
  actualValue?: number
  notes?: string
  tags?: string
  assignedToId?: number
  createdById: number
  companyId: number
  createdAt: string
  updatedAt: string
  lastContacted?: string
  nextFollowUp?: string
  externalId?: string
  integrationId?: number
  pipelineId?: number
  stageId?: number
}

export interface Contact {
  id: number
  firstName: string
  lastName: string
  email?: string
  phone?: string
  company?: string
  jobTitle?: string
  department?: string
  address?: string
  city?: string
  state?: string
  country?: string
  postalCode?: string
  website?: string
  linkedin?: string
  twitter?: string
  notes?: string
  tags?: string
  isActive: boolean
  assignedToId?: number
  createdById: number
  createdAt: string
  updatedAt: string
  lastContacted?: string
}

export interface Deal {
  id: number
  title: string
  description?: string
  value: number
  probability: number
  status: string
  expectedCloseDate?: string
  actualCloseDate?: string
  notes?: string
  tags?: string
  assignedToId: number
  createdById: number
  pipelineId: number
  stageId: number
  leadId?: number
  contactId?: number
  createdAt: string
  updatedAt: string
  lastActivity?: string
}

export interface Activity {
  id: number
  type: string
  subject?: string
  description?: string
  outcome?: string
  duration?: number
  userId: number
  leadId?: number
  contactId?: number
  dealId?: number
  companyId?: number
  activityDate?: string
  createdAt: string
  updatedAt: string
}

export interface Integration {
  id: number
  name: string
  type: string
  status: string
  apiKey?: string
  apiSecret?: string
  accessToken?: string
  refreshToken?: string
  webhookUrl?: string
  config?: string
  lastSync?: string
  syncFrequency: number
  isEnabled: boolean
  userId: number
  companyId: number
  createdAt: string
  updatedAt: string
}

export interface DashboardStats {
  totalLeads: number
  newLeadsThisMonth: number
  activeDeals: number
  dealsWonThisMonth: number
  totalRevenue: number
  revenueGrowth: number
  conversionRate: number
  conversionGrowth: number
}

export interface LeadWithDetails extends Lead {
  companyName?: string
  assignedUserName?: string
  pipelineName?: string
  stageName?: string
}

export interface DealWithDetails extends Deal {
  assignedUserName?: string
  pipelineName?: string
  stageName?: string
  leadName?: string
  contactName?: string
}
