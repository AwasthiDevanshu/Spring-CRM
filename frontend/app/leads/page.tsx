'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '@/hooks/use-auth'
import { useRouter } from 'next/navigation'
import { getAuthHeader } from '@/lib/auth'
import { buildApiUrl, API_ENDPOINTS } from '@/lib/api-config'
import { AppLayout } from '@/components/app-layout'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import LeadForm from '@/components/leads/lead-form'
import CustomFieldManager from '@/components/leads/custom-field-manager'
import { LeadTimeline } from '@/components/leads/lead-timeline'
import { 
  Plus, 
  Search, 
  Filter, 
  MoreHorizontal, 
  Phone, 
  Mail, 
  Building, 
  User,
  Calendar,
  TrendingUp,
  Eye,
  Edit,
  Trash2,
  MessageSquare
} from 'lucide-react'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'

interface Lead {
  id: number
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone?: string
  company?: string
  jobTitle?: string
  status: string
  source: string
  score: number
  notes?: string
  assignedUserId?: number
  companyId: number
  createdAt: string
  updatedAt: string
}

const leadStatuses = [
  { value: 'NEW', label: 'New', color: 'bg-blue-100 text-blue-800' },
  { value: 'CONTACTED', label: 'Contacted', color: 'bg-yellow-100 text-yellow-800' },
  { value: 'QUALIFIED', label: 'Qualified', color: 'bg-green-100 text-green-800' },
  { value: 'PROPOSAL', label: 'Proposal', color: 'bg-purple-100 text-purple-800' },
  { value: 'NEGOTIATION', label: 'Negotiation', color: 'bg-orange-100 text-orange-800' },
  { value: 'CLOSED_WON', label: 'Closed Won', color: 'bg-emerald-100 text-emerald-800' },
  { value: 'CLOSED_LOST', label: 'Closed Lost', color: 'bg-red-100 text-red-800' }
]

const leadSources = [
  { value: 'WEBSITE', label: 'Website' },
  { value: 'PHONE', label: 'Phone' },
  { value: 'EMAIL', label: 'Email' },
  { value: 'REFERRAL', label: 'Referral' },
  { value: 'SOCIAL_MEDIA', label: 'Social Media' },
  { value: 'ADVERTISEMENT', label: 'Advertisement' },
  { value: 'TRADE_SHOW', label: 'Trade Show' },
  { value: 'OTHER', label: 'Other' }
]

export default function LeadsPage() {
  const { user, isAuthenticated, isLoading } = useAuth()
  const router = useRouter()
  const [leads, setLeads] = useState<Lead[]>([])
  const [filteredLeads, setFilteredLeads] = useState<Lead[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [sourceFilter, setSourceFilter] = useState('ALL')
  const [isLoadingLeads, setIsLoadingLeads] = useState(true)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null)
  const [isTimelineDialogOpen, setIsTimelineDialogOpen] = useState(false)
  const [timelineLead, setTimelineLead] = useState<Lead | null>(null)

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  useEffect(() => {
    if (isAuthenticated) {
      fetchLeads()
    }
  }, [isAuthenticated])

  useEffect(() => {
    filterLeads()
  }, [leads, searchTerm, statusFilter, sourceFilter])

  const fetchLeads = async () => {
    try {
      setIsLoadingLeads(true)
      const response = await fetch(buildApiUrl(API_ENDPOINTS.LEADS.LIST, user?.companyId || 1), {
        headers: {
          'Authorization': getAuthHeader() || ''
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        setLeads(data)
      }
    } catch (error) {
      console.error('Error fetching leads:', error)
    } finally {
      setIsLoadingLeads(false)
    }
  }

  const filterLeads = () => {
    let filtered = leads

    if (searchTerm) {
      filtered = filtered.filter(lead => 
        lead.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        lead.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        lead.company?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(lead => lead.status === statusFilter)
    }

    if (sourceFilter !== 'ALL') {
      filtered = filtered.filter(lead => lead.source === sourceFilter)
    }

    setFilteredLeads(filtered)
  }

  const getStatusBadge = (status: string) => {
    const statusConfig = leadStatuses.find(s => s.value === status)
    return (
      <Badge className={statusConfig?.color || 'bg-gray-100 text-gray-800'}>
        {statusConfig?.label || status}
      </Badge>
    )
  }

  const getSourceLabel = (source: string) => {
    const sourceConfig = leadSources.find(s => s.value === source)
    return sourceConfig?.label || source
  }

  const handleCreateLead = async (leadData: any) => {
    try {
      const response = await fetch(buildApiUrl(API_ENDPOINTS.LEADS.CREATE, user?.companyId || 1), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getAuthHeader() || ''}`
        },
        body: JSON.stringify(leadData)
      })

      if (response.ok) {
        await fetchLeads()
        setIsCreateDialogOpen(false)
      }
    } catch (error) {
      console.error('Error creating lead:', error)
    }
  }

  const handleEditLead = (lead: Lead) => {
    setSelectedLead(lead)
    setIsEditDialogOpen(true)
  }

  const handleUpdateLead = async (leadData: any) => {
    try {
      const response = await fetch(buildApiUrl(API_ENDPOINTS.LEADS.UPDATE(selectedLead?.id || 0), user?.companyId || 1), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getAuthHeader() || ''}`
        },
        body: JSON.stringify(leadData)
      })

      if (response.ok) {
        await fetchLeads()
        setIsEditDialogOpen(false)
        setSelectedLead(null)
      }
    } catch (error) {
      console.error('Error updating lead:', error)
    }
  }

  const handleDeleteLead = async (leadId: number) => {
    if (confirm('Are you sure you want to delete this lead?')) {
      try {
        const response = await fetch(buildApiUrl(API_ENDPOINTS.LEADS.DELETE(leadId), user?.companyId || 1), {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${getAuthHeader() || ''}`
          }
        })

        if (response.ok) {
          await fetchLeads()
        }
      } catch (error) {
        console.error('Error deleting lead:', error)
      }
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'NEW':
        return 'bg-blue-100 text-blue-800'
      case 'CONTACTED':
        return 'bg-yellow-100 text-yellow-800'
      case 'QUALIFIED':
        return 'bg-green-100 text-green-800'
      case 'PROPOSAL':
        return 'bg-purple-100 text-purple-800'
      case 'NEGOTIATION':
        return 'bg-orange-100 text-orange-800'
      case 'CLOSED_WON':
        return 'bg-green-100 text-green-800'
      case 'CLOSED_LOST':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  if (isLoading) {
    return <div className="flex items-center justify-center h-64">Loading...</div>
  }

  if (!isAuthenticated) {
    return <div className="flex items-center justify-center h-64">Redirecting to login...</div>
  }

  return (
    <AppLayout>
      <div className="flex-1 space-y-6 p-6 pt-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Leads</h1>
          <p className="text-muted-foreground">
            Manage your sales leads and track their progress through the pipeline
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button onClick={() => setIsCreateDialogOpen(true)}>
            <Plus className="mr-2 h-4 w-4" />
            Add Lead
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <Label htmlFor="search">Search</Label>
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="search"
                  placeholder="Search leads..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <div>
              <Label htmlFor="status">Status</Label>
              <select
                id="status"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="w-full p-2 border rounded-md"
              >
                <option value="ALL">All Statuses</option>
                {leadStatuses.map(status => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <Label htmlFor="source">Source</Label>
              <select
                id="source"
                value={sourceFilter}
                onChange={(e) => setSourceFilter(e.target.value)}
                className="w-full p-2 border rounded-md"
              >
                <option value="ALL">All Sources</option>
                {leadSources.map(source => (
                  <option key={source.value} value={source.value}>
                    {source.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex items-end">
              <Button variant="outline" onClick={fetchLeads}>
                <Filter className="mr-2 h-4 w-4" />
                Apply Filters
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Leads</CardTitle>
            <User className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{leads.length}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">New Leads</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {leads.filter(l => l.status === 'NEW').length}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Qualified</CardTitle>
            <Building className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {leads.filter(l => l.status === 'QUALIFIED').length}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Conversion Rate</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {leads.length > 0 ? Math.round((leads.filter(l => l.status === 'CLOSED_WON').length / leads.length) * 100) : 0}%
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Leads Table */}
      <Card>
        <CardHeader>
          <CardTitle>Leads ({filteredLeads.length})</CardTitle>
          <CardDescription>
            A list of all your leads with their current status and details
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoadingLeads ? (
            <div className="flex items-center justify-center h-32">Loading leads...</div>
          ) : filteredLeads.length === 0 ? (
            <div className="text-center py-8">
              <User className="mx-auto h-12 w-12 text-muted-foreground" />
              <h3 className="mt-2 text-sm font-semibold">No leads found</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Get started by creating a new lead.
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredLeads.map((lead) => (
                <div key={lead.id} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center space-x-4">
                    <div className="flex-shrink-0">
                      <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center">
                        <User className="h-5 w-5 text-primary" />
                      </div>
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {lead.fullName}
                        </p>
                        {getStatusBadge(lead.status)}
                        <Badge variant="outline">Score: {lead.score}</Badge>
                      </div>
                      <div className="flex items-center space-x-4 mt-1">
                        <div className="flex items-center text-sm text-gray-500">
                          <Mail className="h-4 w-4 mr-1" />
                          {lead.email}
                        </div>
                        {lead.phone && (
                          <div className="flex items-center text-sm text-gray-500">
                            <Phone className="h-4 w-4 mr-1" />
                            {lead.phone}
                          </div>
                        )}
                        {lead.company && (
                          <div className="flex items-center text-sm text-gray-500">
                            <Building className="h-4 w-4 mr-1" />
                            {lead.company}
                          </div>
                        )}
                      </div>
                      <div className="mt-1">
                        <span className="text-xs text-gray-500">
                          Source: {getSourceLabel(lead.source)}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button variant="ghost" size="sm">
                      <Eye className="h-4 w-4" />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleEditLead(lead)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="sm">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => handleEditLead(lead)}>
                          <Edit className="mr-2 h-4 w-4" />
                          Edit
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => {
                          setTimelineLead(lead)
                          setIsTimelineDialogOpen(true)
                        }}>
                          <MessageSquare className="mr-2 h-4 w-4" />
                          View Timeline
                        </DropdownMenuItem>
                        <DropdownMenuItem className="text-red-600" onClick={() => handleDeleteLead(lead.id)}>
                          <Trash2 className="mr-2 h-4 w-4" />
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Create Lead Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
          <DialogHeader className="flex-shrink-0">
            <DialogTitle>Create New Lead</DialogTitle>
            <DialogDescription>
              Add a new lead to your CRM system. Fill in the details below.
            </DialogDescription>
          </DialogHeader>
          <div className="flex-1 overflow-y-auto pr-2">
            <LeadForm 
              onSave={handleCreateLead} 
              onCancel={() => setIsCreateDialogOpen(false)}
              companyId={Number(user?.companyId) || 1}
            />
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Lead Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
          <DialogHeader className="flex-shrink-0">
            <DialogTitle>Edit Lead</DialogTitle>
            <DialogDescription>
              Update the lead information below.
            </DialogDescription>
          </DialogHeader>
          <div className="flex-1 overflow-y-auto pr-2">
            <LeadForm 
              initialData={selectedLead}
              onSave={handleUpdateLead} 
              onCancel={() => {
                setIsEditDialogOpen(false)
                setSelectedLead(null)
              }}
              companyId={Number(user?.companyId) || 1}
            />
          </div>
        </DialogContent>
      </Dialog>

      {/* Timeline Dialog */}
      <Dialog open={isTimelineDialogOpen} onOpenChange={setIsTimelineDialogOpen}>
        <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <MessageSquare className="h-5 w-5" />
              Lead Timeline - {timelineLead?.fullName}
            </DialogTitle>
            <DialogDescription>
              View notes, activities, and timeline for {timelineLead?.email}
            </DialogDescription>
          </DialogHeader>
          {timelineLead && (
            <div className="space-y-6">
              {/* Lead Summary */}
              <Card>
                <CardContent className="p-4">
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div>
                      <p className="text-sm text-muted-foreground">Status</p>
                      <Badge className={getStatusColor(timelineLead.status)}>
                        {timelineLead.status}
                      </Badge>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Source</p>
                      <p className="font-medium">{timelineLead.source}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Score</p>
                      <p className="font-medium">{timelineLead.score}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Company</p>
                      <p className="font-medium">{timelineLead.company || 'N/A'}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Timeline */}
              <LeadTimeline 
                leadId={timelineLead.id} 
                companyId={user?.companyId || 1} 
              />
            </div>
          )}
        </DialogContent>
      </Dialog>
      </div>
    </AppLayout>
  )
}
