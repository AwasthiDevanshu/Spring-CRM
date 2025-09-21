'use client'

import { useState, useEffect, useMemo } from 'react'
import { useAuth } from '@/hooks/use-auth'
import { useContacts, useCreateContact, useUpdateContact, useDeleteContact } from '@/hooks/use-contacts'
import { useCreateDeal } from '@/hooks/use-deals'
import { useCreateActivity } from '@/hooks/use-activities'
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
  Eye,
  Edit,
  Trash2,
  Users,
  UserCheck,
  UserX,
  DollarSign,
  Activity
} from 'lucide-react'

interface Contact {
  id: number
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone?: string
  jobTitle?: string
  department?: string
  companyId: number
  leadId?: number
  isActive: boolean
  notes?: string
  assignedUserId?: number
  createdAt: string
  updatedAt: string
}

export default function ContactsPage() {
  const { user, isAuthenticated, isLoading } = useAuth()
  const { data: contacts = [], isLoading: isLoadingContacts, error } = useContacts()
  const { data: forms = [] } = useCustomForms()
  const createContactMutation = useCreateContact()
  const updateContactMutation = useUpdateContact()
  const deleteContactMutation = useDeleteContact()
  const createDealMutation = useCreateDeal()
  const createActivityMutation = useCreateActivity()
  const router = useRouter()

  // Check if user is admin
  const isAdmin = user?.isCompanyAdmin || user?.isSuperuser
  
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [selectedContact, setSelectedContact] = useState<Contact | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isCreateDealDialogOpen, setIsCreateDealDialogOpen] = useState(false)
  const [isCreateActivityDialogOpen, setIsCreateActivityDialogOpen] = useState(false)
  const [isFormShareDialogOpen, setIsFormShareDialogOpen] = useState(false)
  const [dealContact, setDealContact] = useState<Contact | null>(null)
  const [activityContact, setActivityContact] = useState<Contact | null>(null)
  const [selectedForm, setSelectedForm] = useState<any>(null)
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    jobTitle: '',
    department: '',
    notes: ''
  })

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  // Memoized filtering for better performance
  const filteredContacts = useMemo(() => {
    let filtered = contacts

    if (searchTerm) {
      filtered = filtered.filter(contact => 
        contact.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        contact.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        contact.jobTitle?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        contact.department?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (statusFilter === 'ACTIVE') {
      filtered = filtered.filter(contact => contact.isActive)
    } else if (statusFilter === 'INACTIVE') {
      filtered = filtered.filter(contact => !contact.isActive)
    }

    return filtered
  }, [contacts, searchTerm, statusFilter])

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (isSubmitting) return

    try {
      setIsSubmitting(true)
      await createContactMutation.mutateAsync(formData)
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        jobTitle: '',
        department: '',
        notes: ''
      })
      setIsCreateDialogOpen(false)
    } catch (error) {
      console.error('Error creating contact:', error)
      alert('Failed to create contact. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const getStatusBadge = (isActive: boolean) => {
    return (
      <Badge className={isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}>
        {isActive ? 'Active' : 'Inactive'}
      </Badge>
    )
  }

  const handleCreateContact = async (contactData: any) => {
    try {
      await createContactMutation.mutateAsync(contactData)
      setIsCreateDialogOpen(false)
    } catch (error) {
      console.error('Error creating contact:', error)
    }
  }

  const handleCreateDeal = (contact: Contact) => {
    setDealContact(contact)
    setIsCreateDealDialogOpen(true)
  }

  const handleCreateActivity = (contact: Contact) => {
    setActivityContact(contact)
    setIsCreateActivityDialogOpen(true)
  }

  const handleDealSubmit = async (dealData: any) => {
    try {
      const dealWithContact = {
        ...dealData,
        contactId: dealContact?.id
      }
      await createDealMutation.mutateAsync(dealWithContact)
      setIsCreateDealDialogOpen(false)
      setDealContact(null)
    } catch (error) {
      console.error('Error creating deal:', error)
    }
  }

  const handleActivitySubmit = async (activityData: any) => {
    try {
      const activityWithContact = {
        ...activityData,
        entityType: 'CONTACT',
        entityId: activityContact?.id
      }
      await createActivityMutation.mutateAsync(activityWithContact)
      setIsCreateActivityDialogOpen(false)
      setActivityContact(null)
    } catch (error) {
      console.error('Error creating activity:', error)
    }
  }

  const handleFormShare = (contact: Contact, form: any) => {
    setSelectedForm(form)
    setDealContact(contact) // Reuse dealContact state for form sharing
    setIsFormShareDialogOpen(true)
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
          <h1 className="text-3xl font-bold tracking-tight">Contacts</h1>
          <p className="text-muted-foreground">
            Manage your customer contacts and their information
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button onClick={() => setIsCreateDialogOpen(true)}>
            <Plus className="mr-2 h-4 w-4" />
            Add Contact
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <Label htmlFor="search">Search</Label>
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="search"
                  placeholder="Search contacts..."
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
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </div>
            <div className="flex items-end">
              <Button variant="outline" onClick={() => {
                setSearchTerm('')
                setStatusFilter('ALL')
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
            <CardTitle className="text-sm font-medium">Total Contacts</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{contacts.length}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Contacts</CardTitle>
            <UserCheck className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {contacts.filter(c => c.isActive).length}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Inactive Contacts</CardTitle>
            <UserX className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {contacts.filter(c => !c.isActive).length}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">From Leads</CardTitle>
            <User className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {contacts.filter(c => c.leadId).length}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Contacts Table */}
      <Card>
        <CardHeader>
          <CardTitle>Contacts ({filteredContacts.length})</CardTitle>
          <CardDescription>
            A list of all your contacts with their information and status
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoadingContacts ? (
            <div className="flex items-center justify-center h-32">Loading contacts...</div>
          ) : filteredContacts.length === 0 ? (
            <div className="text-center py-8">
              <User className="mx-auto h-12 w-12 text-muted-foreground" />
              <h3 className="mt-2 text-sm font-semibold">No contacts found</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Get started by creating a new contact.
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredContacts.map((contact) => (
                <div key={contact.id} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center space-x-4">
                    <div className="flex-shrink-0">
                      <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center">
                        <User className="h-5 w-5 text-primary" />
                      </div>
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {contact.fullName}
                        </p>
                        {getStatusBadge(contact.isActive)}
                        {contact.leadId && (
                          <Badge variant="outline">From Lead</Badge>
                        )}
                      </div>
                      <div className="flex items-center space-x-4 mt-1">
                        <div className="flex items-center text-sm text-gray-500">
                          <Mail className="h-4 w-4 mr-1" />
                          {contact.email}
                        </div>
                        {contact.phone && (
                          <div className="flex items-center text-sm text-gray-500">
                            <Phone className="h-4 w-4 mr-1" />
                            {contact.phone}
                          </div>
                        )}
                        {contact.jobTitle && (
                          <div className="flex items-center text-sm text-gray-500">
                            <Building className="h-4 w-4 mr-1" />
                            {contact.jobTitle}
                          </div>
                        )}
                      </div>
                      <div className="flex items-center space-x-4 mt-1">
                        {contact.department && (
                          <span className="text-xs text-gray-500">
                            {contact.department}
                          </span>
                        )}
                        <span className="text-xs text-gray-500">
                          Created: {new Date(contact.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button variant="ghost" size="sm">
                      <Eye className="h-4 w-4" />
                    </Button>
                    <Button variant="ghost" size="sm">
                      <Edit className="h-4 w-4" />
                    </Button>
                    <WhatsAppButton
                      contact={{
                        name: contact.fullName,
                        phone: contact.phone || '',
                        company: contact.jobTitle || '',
                        email: contact.email
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
                              onClick={() => handleFormShare(contact, form)}
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
                        <DropdownMenuItem>
                          <Edit className="mr-2 h-4 w-4" />
                          Edit
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleCreateDeal(contact)}>
                          <DollarSign className="mr-2 h-4 w-4" />
                          Create Deal
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleCreateActivity(contact)}>
                          <Activity className="mr-2 h-4 w-4" />
                          Create Activity
                        </DropdownMenuItem>
                        <DropdownMenuItem className="text-red-600">
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

      {/* Create Contact Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Create New Contact</DialogTitle>
            <DialogDescription>
              Add a new contact to your CRM system. Fill in the details below.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="firstName">First Name *</Label>
                <Input 
                  id="firstName" 
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  placeholder="Enter first name" 
                  required
                />
              </div>
              <div>
                <Label htmlFor="lastName">Last Name *</Label>
                <Input 
                  id="lastName" 
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  placeholder="Enter last name" 
                  required
                />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="email">Email *</Label>
                <Input 
                  id="email" 
                  name="email"
                  type="email" 
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="Enter email" 
                  required
                />
              </div>
              <div>
                <Label htmlFor="phone">Phone</Label>
                <Input 
                  id="phone" 
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  placeholder="Enter phone number" 
                />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="jobTitle">Job Title</Label>
                <Input 
                  id="jobTitle" 
                  name="jobTitle"
                  value={formData.jobTitle}
                  onChange={handleInputChange}
                  placeholder="Enter job title" 
                />
              </div>
              <div>
                <Label htmlFor="department">Department</Label>
                <Input 
                  id="department" 
                  name="department"
                  value={formData.department}
                  onChange={handleInputChange}
                  placeholder="Enter department" 
                />
              </div>
            </div>
            <div>
              <Label htmlFor="notes">Notes</Label>
              <Input 
                id="notes" 
                name="notes"
                value={formData.notes}
                onChange={handleInputChange}
                placeholder="Enter notes" 
              />
            </div>
            <div className="flex justify-end space-x-2">
              <Button 
                type="button" 
                variant="outline" 
                onClick={() => setIsCreateDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button 
                type="submit" 
                disabled={isSubmitting}
              >
                {isSubmitting ? 'Creating...' : 'Create Contact'}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      {/* Create Deal Dialog */}
      <Dialog open={isCreateDealDialogOpen} onOpenChange={setIsCreateDealDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <DollarSign className="h-5 w-5" />
              Create Deal for {dealContact?.fullName}
            </DialogTitle>
            <DialogDescription>
              Create a new deal linked to this contact.
            </DialogDescription>
          </DialogHeader>
          <DealForm
            onSave={handleDealSubmit}
            onCancel={() => {
              setIsCreateDealDialogOpen(false)
              setDealContact(null)
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
              Create Activity for {activityContact?.fullName}
            </DialogTitle>
            <DialogDescription>
              Create a new activity linked to this contact.
            </DialogDescription>
          </DialogHeader>
          <ActivityForm
            onSave={handleActivitySubmit}
            onCancel={() => {
              setIsCreateActivityDialogOpen(false)
              setActivityContact(null)
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
              Send the "{selectedForm?.name}" form to {dealContact?.fullName} via WhatsApp
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
              <p><strong>Name:</strong> {dealContact?.fullName}</p>
              <p><strong>Phone:</strong> {dealContact?.phone || 'No phone number'}</p>
              <p><strong>Job Title:</strong> {dealContact?.jobTitle || 'No job title'}</p>
            </div>
            <div className="flex gap-2">
              <Button
                onClick={async () => {
                  if (dealContact?.phone && selectedForm) {
                    try {
                      // Create form access token
                      const accessData = {
                        contactId: dealContact.id,
                        expiryDays: 7,
                        maxSubmissions: 1
                      }
                      
                      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/crm/api'}/custom-forms/${selectedForm.id}/access`, {
                        method: 'POST',
                        headers: {
                          'Content-Type': 'application/json',
                          'Authorization': `Bearer ${localStorage.getItem('token')}`
                        },
                        body: JSON.stringify(accessData)
                      })
                      
                      if (response.ok) {
                        const accessResult = await response.json()
                        const formUrl = `${window.location.origin}/forms/submit/${accessResult.accessToken}`
                        const message = `Hi ${dealContact.fullName}! Please fill out this form: ${selectedForm.name}\n\n${selectedForm.description || ''}\n\nForm Link: ${formUrl}`
                        const whatsappUrl = `https://wa.me/${dealContact.phone.replace(/[^\d]/g, '')}?text=${encodeURIComponent(message)}`
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
                disabled={!dealContact?.phone}
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

// Deal Form Component (reused from leads page)
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

// Activity Form Component (simplified version for contacts page)
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
