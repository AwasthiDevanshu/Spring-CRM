'use client'

import { useState, useEffect, useMemo } from 'react'
import { useAuth } from '@/hooks/use-auth'
import { useLeads, useCreateLead, useUpdateLead, useDeleteLead } from '@/hooks/use-leads'
import { useCreateDeal } from '@/hooks/use-deals'
import { useCreateActivity } from '@/hooks/use-activities'
import { useCreateContact } from '@/hooks/use-contacts'
import { useCustomForms } from '@/hooks/use-custom-forms'
import WhatsAppButton from '@/components/ui/whatsapp-button'
import { FormInput, MessageCircle } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { AppLayout } from '@/components/app-layout'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
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
  MessageSquare,
  DollarSign,
  Activity,
  CheckCircle,
  UserPlus,
  Users
} from 'lucide-react';
import { Constants }  from '@/lib/constants';
import { getAuthToken } from '@/lib/auth'

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
  const { data: leads = [], isLoading: isLoadingLeads, error } = useLeads()
  const { data: forms = [] } = useCustomForms()
  const createLeadMutation = useCreateLead()
  const updateLeadMutation = useUpdateLead()
  const deleteLeadMutation = useDeleteLead()
  const createDealMutation = useCreateDeal()
  const createActivityMutation = useCreateActivity()
  const createContactMutation = useCreateContact()
  const router = useRouter()

  // Check if user is admin
  const isAdmin = user?.isCompanyAdmin || user?.isSuperuser
  
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [sourceFilter, setSourceFilter] = useState('ALL')
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null)
  const [isTimelineDialogOpen, setIsTimelineDialogOpen] = useState(false)
  const [timelineLead, setTimelineLead] = useState<Lead | null>(null)
  const [isCreateDealDialogOpen, setIsCreateDealDialogOpen] = useState(false)
  const [isCreateActivityDialogOpen, setIsCreateActivityDialogOpen] = useState(false)
  const [isConvertToContactDialogOpen, setIsConvertToContactDialogOpen] = useState(false)
  const [isFormShareDialogOpen, setIsFormShareDialogOpen] = useState(false)
  const [dealLead, setDealLead] = useState<Lead | null>(null)
  const [activityLead, setActivityLead] = useState<Lead | null>(null)
  const [contactLead, setContactLead] = useState<Lead | null>(null)
  const [selectedForm, setSelectedForm] = useState<any>(null)

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  // Memoized filtering for better performance
  const filteredLeads = useMemo(() => {
    let filtered = leads

    if (searchTerm) {
      filtered = filtered.filter(lead => 
        lead.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        lead.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        lead.company?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(lead => lead.status === statusFilter)
    }

    if (sourceFilter !== 'ALL') {
      filtered = filtered.filter(lead => lead.source === sourceFilter)
    }

    return filtered
  }, [leads, searchTerm, statusFilter, sourceFilter])

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
      await createLeadMutation.mutateAsync(leadData)
      setIsCreateDialogOpen(false)
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
      await updateLeadMutation.mutateAsync({ id: selectedLead?.id || 0, data: leadData })
      setIsEditDialogOpen(false)
      setSelectedLead(null)
    } catch (error) {
      console.error('Error updating lead:', error)
    }
  }

  const handleDeleteLead = async (leadId: number) => {
    if (confirm('Are you sure you want to delete this lead?')) {
      try {
        await deleteLeadMutation.mutateAsync(leadId)
      } catch (error) {
        console.error('Error deleting lead:', error)
      }
    }
  }

  const handleCreateDeal = (lead: Lead) => {
    setDealLead(lead)
    setIsCreateDealDialogOpen(true)
  }

  const handleCreateActivity = (lead: Lead) => {
    setActivityLead(lead)
    setIsCreateActivityDialogOpen(true)
  }

  const handleDealSubmit = async (dealData: any) => {
    try {
      const dealWithLead = {
        ...dealData,
        leadId: dealLead?.id
      }
      await createDealMutation.mutateAsync(dealWithLead)
      setIsCreateDealDialogOpen(false)
      setDealLead(null)
    } catch (error) {
      console.error('Error creating deal:', error)
    }
  }

  const handleActivitySubmit = async (activityData: any) => {
    try {
      const activityWithLead = {
        ...activityData,
        entityType: 'LEAD',
        entityId: activityLead?.id
      }
      await createActivityMutation.mutateAsync(activityWithLead)
      setIsCreateActivityDialogOpen(false)
      setActivityLead(null)
    } catch (error) {
      console.error('Error creating activity:', error)
    }
  }

  const handleConvertToContact = (lead: Lead) => {
    setContactLead(lead)
    setIsConvertToContactDialogOpen(true)
  }

  const handleContactSubmit = async (contactData: any) => {
    try {
      const contactWithLead = {
        ...contactData,
        leadId: contactLead?.id
      }
      await createContactMutation.mutateAsync(contactWithLead)
      setIsConvertToContactDialogOpen(false)
      setContactLead(null)
    } catch (error) {
      console.error('Error converting lead to contact:', error)
    }
  }

  const handleFormShare = async (lead: Lead, form: any) => {
    setSelectedForm(form)
    setDealLead(lead) // Reuse dealLead state for form sharing
    setIsFormShareDialogOpen(true)
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

  if (error) {
    return (
      <AppLayout>
        <div className="p-6">
          <div className="text-center text-red-600">
            Error loading leads: {error.message}
          </div>
        </div>
      </AppLayout>
    )
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
              <Button variant="outline" onClick={() => {
                setSearchTerm('')
                setStatusFilter('ALL')
                setSourceFilter('ALL')
              }}>
                <Filter className="mr-2 h-4 w-4" />
                Clear Filters
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
                    <WhatsAppButton
                      contact={{
                        name: lead.fullName,
                        phone: lead.phone || '',
                        company: lead.company || '',
                        email: lead.email
                      }}
                      variant="ghost"
                      size="sm"
                      showLabel={false}
                    />
                    {forms.length > 0 && (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm" title="Share Form">
                            <FormInput className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          {forms.map((form) => (
                            <DropdownMenuItem
                              key={form.id}
                              onClick={() => handleFormShare(lead, form)}
                            >
                              <FormInput className="mr-2 h-4 w-4" />
                              {form.name}
                            </DropdownMenuItem>
                          ))}
                        </DropdownMenuContent>
                      </DropdownMenu>
                    )}
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
                        <DropdownMenuItem onClick={() => handleCreateDeal(lead)}>
                          <DollarSign className="mr-2 h-4 w-4" />
                          Create Deal
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleCreateActivity(lead)}>
                          <Activity className="mr-2 h-4 w-4" />
                          Create Activity
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleConvertToContact(lead)}>
                          <UserPlus className="mr-2 h-4 w-4" />
                          Convert to Contact
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
              />
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Create Deal Dialog */}
      <Dialog open={isCreateDealDialogOpen} onOpenChange={setIsCreateDealDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <DollarSign className="h-5 w-5" />
              Create Deal for {dealLead?.fullName}
            </DialogTitle>
            <DialogDescription>
              Create a new deal linked to this lead.
            </DialogDescription>
          </DialogHeader>
          <DealForm
            onSave={handleDealSubmit}
            onCancel={() => {
              setIsCreateDealDialogOpen(false)
              setDealLead(null)
            }}
          />
        </DialogContent>
      </Dialog>

      {/* Create Activity Dialog */}
      <Dialog open={isCreateActivityDialogOpen} onOpenChange={setIsCreateActivityDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Create Activity for {activityLead?.fullName}
            </DialogTitle>
            <DialogDescription>
              Create a new activity linked to this lead.
            </DialogDescription>
          </DialogHeader>
          <ActivityForm
            onSave={handleActivitySubmit}
            onCancel={() => {
              setIsCreateActivityDialogOpen(false)
              setActivityLead(null)
            }}
          />
        </DialogContent>
      </Dialog>

      {/* Convert to Contact Dialog */}
      <Dialog open={isConvertToContactDialogOpen} onOpenChange={setIsConvertToContactDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <UserPlus className="h-5 w-5" />
              Convert Lead to Contact - {contactLead?.fullName}
            </DialogTitle>
            <DialogDescription>
              Convert this qualified lead to a contact for ongoing relationship management.
            </DialogDescription>
          </DialogHeader>
          <ContactForm
            initialData={contactLead}
            onSave={handleContactSubmit}
            onCancel={() => {
              setIsConvertToContactDialogOpen(false)
              setContactLead(null)
            }}
          />
        </DialogContent>
      </Dialog>

      {/* Form Share Dialog */}
      <Dialog open={isFormShareDialogOpen} onOpenChange={setIsFormShareDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Share Form via WhatsApp</DialogTitle>
            <DialogDescription>
              Send the "{selectedForm?.name}" form to {dealLead?.fullName} via WhatsApp
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="p-4 bg-gray-50 rounded-lg">
              <h4 className="font-medium mb-2">Form Details:</h4>
              <p><strong>Name:</strong> {selectedForm?.name}</p>
              <p><strong>Description:</strong> {selectedForm?.description || 'No description'}</p>
              <p><strong>Fields:</strong> {selectedForm?.fields?.length || 0} field(s)</p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <h4 className="font-medium mb-2">Recipient:</h4>
              <p><strong>Name:</strong> {dealLead?.fullName}</p>
              <p><strong>Phone:</strong> {dealLead?.phone || 'No phone number'}</p>
              <p><strong>Company:</strong> {dealLead?.company || 'No company'}</p>
            </div>
            <div className="flex gap-2">
              <Button
                onClick={async () => {
                  if (dealLead?.phone && selectedForm) {
                    try {
                      // Create form access token
                      const accessData = {
                        leadId: dealLead.id,
                        expiryDays: 7,
                        maxSubmissions: 1
                      }
                      
                      const response = await fetch(`${Constants.NEXT_PUBLIC_API_URL || 'http://localhost:8080/crm/api'}/custom-forms/${selectedForm.id}/access`, {
                        method: 'POST',
                        headers: {
                          'Content-Type': 'application/json',
                          'Authorization': `Bearer ${getAuthToken()}`
                        },
                        body: JSON.stringify(accessData)
                      })
                      
                      if (response.ok) {
                        const accessResult = await response.json()
                        const formUrl = `${window.location.origin}/forms/submit/${accessResult.accessToken}`
                        const message = `Hi ${dealLead.fullName}! Please fill out this form: ${selectedForm.name}\n\n${selectedForm.description || ''}\n\nForm Link: ${formUrl}`
                        const whatsappUrl = `https://wa.me/${dealLead.phone?.replace(/[^\d]/g, '')}?text=${encodeURIComponent(message)}`
                        window.open(whatsappUrl, '_blank')
                        setIsFormShareDialogOpen(false)
                      } else {
                        console.error('Failed to create form access')
                        alert('Failed to create form access. Please try again.')
                      }
                    } catch (error) {
                      console.error('Error creating form access:', error)
                      alert('Error creating form access. Please try again.')
                    }
                  }
                }}
                disabled={!dealLead?.phone}
                className="flex-1"
              >
                <MessageCircle className="h-4 w-4 mr-2" />
                Send via WhatsApp
              </Button>
              <Button variant="outline" onClick={() => setIsFormShareDialogOpen(false)}>
                Cancel
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
      </div>
    </AppLayout>
  )
}

// Deal Form Component
interface DealFormProps {
  onSave: (data: any) => void
  onCancel: () => void
}

function DealForm({ onSave, onCancel }: DealFormProps) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    value: '',
    status: 'OPEN',
    probability: 0,
    expectedCloseDate: '',
    assignedUserId: ''
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const submitData = {
      ...formData,
      value: parseFloat(formData.value) || 0,
      probability: parseInt(formData.probability.toString()) || 0,
      assignedUserId: formData.assignedUserId ? parseInt(formData.assignedUserId) : null,
      expectedCloseDate: formData.expectedCloseDate ? `${formData.expectedCloseDate}T00:00:00` : null,
      pipelineId: 1, // Default pipeline
      stageId: 1 // Default stage
    }
    onSave(submitData)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="name">Deal Name *</Label>
        <Input
          id="name"
          value={formData.name}
          onChange={(e) => setFormData({...formData, name: e.target.value})}
          placeholder="Enter deal name"
          required
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Description</Label>
        <Textarea
          id="description"
          value={formData.description}
          onChange={(e) => setFormData({...formData, description: e.target.value})}
          placeholder="Enter deal description"
          rows={3}
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="value">Deal Value ($) *</Label>
          <Input
            id="value"
            type="number"
            step="0.01"
            value={formData.value}
            onChange={(e) => setFormData({...formData, value: e.target.value})}
            placeholder="0.00"
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="probability">Probability (%)</Label>
          <Input
            id="probability"
            type="number"
            min="0"
            max="100"
            value={formData.probability}
            onChange={(e) => setFormData({...formData, probability: parseInt(e.target.value) || 0})}
            placeholder="0"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="status">Status</Label>
          <select
            id="status"
            value={formData.status}
            onChange={(e) => setFormData({...formData, status: e.target.value})}
            className="w-full p-2 border rounded-md"
          >
            <option value="OPEN">Open</option>
            <option value="WON">Won</option>
            <option value="LOST">Lost</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
        <div className="space-y-2">
          <Label htmlFor="expectedCloseDate">Expected Close Date</Label>
          <Input
            id="expectedCloseDate"
            type="date"
            value={formData.expectedCloseDate}
            onChange={(e) => setFormData({...formData, expectedCloseDate: e.target.value})}
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="assignedUserId">Assigned User ID</Label>
        <Input
          id="assignedUserId"
          type="number"
          value={formData.assignedUserId}
          onChange={(e) => setFormData({...formData, assignedUserId: e.target.value})}
          placeholder="Enter user ID"
        />
      </div>

      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">
          Create Deal
        </Button>
      </div>
    </form>
  )
}

// Activity Form Component (simplified version for leads page)
interface ActivityFormProps {
  onSave: (data: any) => void
  onCancel: () => void
}

function ActivityForm({ onSave, onCancel }: ActivityFormProps) {
  const [formData, setFormData] = useState({
    type: 'TASK',
    subject: '',
    description: '',
    status: 'PENDING',
    priority: 'MEDIUM',
    dueDate: '',
    assignedTo: '',
    outcome: '',
    duration: ''
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const submitData = {
      ...formData,
      assignedTo: formData.assignedTo ? parseInt(formData.assignedTo) : 1, // Default to current user
      duration: formData.duration ? parseInt(formData.duration) : null,
      dueDate: formData.dueDate ? `${formData.dueDate}T09:00:00` : null
    }
    onSave(submitData)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="type">Type</Label>
          <select
            id="type"
            value={formData.type}
            onChange={(e) => setFormData({...formData, type: e.target.value})}
            className="w-full p-2 border rounded-md"
          >
            <option value="CALL">Call</option>
            <option value="EMAIL">Email</option>
            <option value="MEETING">Meeting</option>
            <option value="TASK">Task</option>
            <option value="NOTE">Note</option>
          </select>
        </div>
        <div className="space-y-2">
          <Label htmlFor="status">Status</Label>
          <select
            id="status"
            value={formData.status}
            onChange={(e) => setFormData({...formData, status: e.target.value})}
            className="w-full p-2 border rounded-md"
          >
            <option value="PENDING">Pending</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="subject">Subject *</Label>
        <Input
          id="subject"
          value={formData.subject}
          onChange={(e) => setFormData({...formData, subject: e.target.value})}
          placeholder="Enter activity subject"
          required
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Description</Label>
        <Textarea
          id="description"
          value={formData.description}
          onChange={(e) => setFormData({...formData, description: e.target.value})}
          placeholder="Enter activity description"
          rows={3}
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="dueDate">Due Date</Label>
          <Input
            id="dueDate"
            type="date"
            value={formData.dueDate}
            onChange={(e) => setFormData({...formData, dueDate: e.target.value})}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="priority">Priority</Label>
          <select
            id="priority"
            value={formData.priority}
            onChange={(e) => setFormData({...formData, priority: e.target.value})}
            className="w-full p-2 border rounded-md"
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="assignedTo">Assigned To (User ID)</Label>
          <Input
            id="assignedTo"
            type="number"
            value={formData.assignedTo}
            onChange={(e) => setFormData({...formData, assignedTo: e.target.value})}
            placeholder="Enter user ID"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="duration">Duration (minutes)</Label>
          <Input
            id="duration"
            type="number"
            value={formData.duration}
            onChange={(e) => setFormData({...formData, duration: e.target.value})}
            placeholder="Enter duration"
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="outcome">Outcome</Label>
        <Textarea
          id="outcome"
          value={formData.outcome}
          onChange={(e) => setFormData({...formData, outcome: e.target.value})}
          placeholder="Enter activity outcome"
          rows={2}
        />
      </div>

      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">
          Create Activity
        </Button>
      </div>
    </form>
  )
}

// Contact Form Component for Lead Conversion
interface ContactFormProps {
  initialData?: Lead | null
  onSave: (data: any) => void
  onCancel: () => void
}

function ContactForm({ initialData, onSave, onCancel }: ContactFormProps) {
  const [formData, setFormData] = useState({
    firstName: initialData?.firstName || '',
    lastName: initialData?.lastName || '',
    email: initialData?.email || '',
    phone: initialData?.phone || '',
    jobTitle: initialData?.jobTitle || '',
    department: '',
    notes: initialData?.notes || '',
    isActive: true
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSave(formData)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="firstName">First Name *</Label>
          <Input
            id="firstName"
            value={formData.firstName}
            onChange={(e) => setFormData({...formData, firstName: e.target.value})}
            placeholder="Enter first name"
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="lastName">Last Name *</Label>
          <Input
            id="lastName"
            value={formData.lastName}
            onChange={(e) => setFormData({...formData, lastName: e.target.value})}
            placeholder="Enter last name"
            required
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="email">Email *</Label>
          <Input
            id="email"
            type="email"
            value={formData.email}
            onChange={(e) => setFormData({...formData, email: e.target.value})}
            placeholder="Enter email"
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="phone">Phone</Label>
          <Input
            id="phone"
            value={formData.phone}
            onChange={(e) => setFormData({...formData, phone: e.target.value})}
            placeholder="Enter phone number"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="jobTitle">Job Title</Label>
          <Input
            id="jobTitle"
            value={formData.jobTitle}
            onChange={(e) => setFormData({...formData, jobTitle: e.target.value})}
            placeholder="Enter job title"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="department">Department</Label>
          <Input
            id="department"
            value={formData.department}
            onChange={(e) => setFormData({...formData, department: e.target.value})}
            placeholder="Enter department"
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="notes">Notes</Label>
        <Textarea
          id="notes"
          value={formData.notes}
          onChange={(e) => setFormData({...formData, notes: e.target.value})}
          placeholder="Enter additional notes"
          rows={3}
        />
      </div>

      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">
          Convert to Contact
        </Button>
      </div>
    </form>
  )
}
