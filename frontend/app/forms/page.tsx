'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '@/hooks/use-auth'
import { AppLayout } from '@/components/app-layout'
import { 
  useCustomForms, 
  useCreateCustomForm, 
  useUpdateCustomForm, 
  useDeleteCustomForm,
  useFormAccess,
  useCreateFormAccess,
  useDeactivateFormAccess,
  useRegenerateFormAccess
} from '@/hooks/use-custom-forms'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { 
  Plus, 
  Search, 
  Filter, 
  MoreHorizontal, 
  Edit, 
  Trash2, 
  Share2, 
  Eye,
  MessageCircle,
  Users,
  Clock,
  CheckCircle,
  AlertCircle,
  Settings
} from 'lucide-react'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import WhatsAppFormShare from '@/components/forms/whatsapp-form-share'
import { FormBuilder } from '@/components/forms/form-builder'

interface CustomForm {
  id: number
  name: string
  description?: string
  status: 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'EXPIRED'
  accessType: 'PUBLIC' | 'LEAD_SPECIFIC' | 'CONTACT_SPECIFIC'
  submissionCount: number
  maxSubmissions?: number
  expiryDate?: string
  createdAt: string
  updatedAt: string
}

interface FormAccess {
  id: number
  formId: number
  formName: string
  leadId?: number
  contactId?: number
  leadName?: string
  contactName?: string
  accessToken: string
  isActive: boolean
  submissionCount: number
  maxSubmissions?: number
  expiresAt?: string
  createdAt: string
}

export default function FormsPage() {
  const { user } = useAuth()
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedForm, setSelectedForm] = useState<CustomForm | null>(null)
  const [isCreateFormOpen, setIsCreateFormOpen] = useState(false)
  const [isCreateAccessOpen, setIsCreateAccessOpen] = useState(false)
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    status: 'DRAFT',
    accessType: 'PUBLIC'
  })
  const [formFields, setFormFields] = useState<any[]>([])
  const [isFormBuilderOpen, setIsFormBuilderOpen] = useState(false)
  const [isEditFormOpen, setIsEditFormOpen] = useState(false)
  const [editingForm, setEditingForm] = useState<CustomForm | null>(null)

  // Check if user is admin
  const isAdmin = user?.isCompanyAdmin || user?.isSuperuser

  // API hooks
  const { data: forms = [], isLoading: formsLoading, error: formsError } = useCustomForms()
  const { data: formAccesses = [], isLoading: accessLoading } = useFormAccess(2)
  const createFormMutation = useCreateCustomForm()
  const updateFormMutation = useUpdateCustomForm()
  const deleteFormMutation = useDeleteCustomForm()
  const createAccessMutation = useCreateFormAccess()
  const deactivateAccessMutation = useDeactivateFormAccess()
  const regenerateAccessMutation = useRegenerateFormAccess()

  const isLoading = formsLoading || accessLoading

  // Handle form creation
  const handleCreateForm = async (formData: any) => {
    try {
      await createFormMutation.mutateAsync(formData)
      setIsCreateFormOpen(false)
      setFormData({ name: '', description: '', status: 'DRAFT', accessType: 'PUBLIC' })
    } catch (error) {
      console.error('Error creating form:', error)
    }
  }

  // Reset form data when dialog opens
  const handleOpenCreateForm = () => {
    setFormData({ name: '', description: '', status: 'DRAFT', accessType: 'PUBLIC' })
    setIsCreateFormOpen(true)
  }

  // Handle form update
  const handleUpdateForm = async (id: number, formData: any) => {
    try {
      await updateFormMutation.mutateAsync({ id, data: formData })
    } catch (error) {
      console.error('Error updating form:', error)
    }
  }

  // Handle form deletion
  const handleDeleteForm = async (id: number) => {
    if (confirm('Are you sure you want to delete this form?')) {
      try {
        await deleteFormMutation.mutateAsync(id)
      } catch (error) {
        console.error('Error deleting form:', error)
      }
    }
  }

  // Handle access creation
  const handleCreateAccess = async (accessData: any) => {
    if (!selectedForm) return
    
    try {
      await createAccessMutation.mutateAsync({ 
        formId: selectedForm.id, 
        data: accessData 
      })
      setIsCreateAccessOpen(false)
    } catch (error) {
      console.error('Error creating form access:', error)
    }
  }

  // Handle form editing
  const handleEditForm = (form: CustomForm) => {
    setEditingForm(form)
    setFormData({
      name: form.name,
      description: form.description || '',
      status: form.status,
      accessType: form.accessType
    })
    setIsEditFormOpen(true)
  }

  const handleEditFormFields = (form: CustomForm) => {
    setEditingForm(form)
    // TODO: Load existing form fields from API
    setFormFields([])
    setIsFormBuilderOpen(true)
  }

  // Handle access deactivation
  const handleDeactivateAccess = async (accessId: number) => {
    if (confirm('Are you sure you want to deactivate this access?')) {
      try {
        await deactivateAccessMutation.mutateAsync(accessId)
      } catch (error) {
        console.error('Error deactivating access:', error)
      }
    }
  }

  // Handle access regeneration
  const handleRegenerateAccess = async (accessId: number) => {
    try {
      await regenerateAccessMutation.mutateAsync(accessId)
    } catch (error) {
      console.error('Error regenerating access:', error)
    }
  }

  const filteredForms = forms.filter(form =>
    form.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    form.description?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800'
      case 'DRAFT': return 'bg-yellow-100 text-yellow-800'
      case 'INACTIVE': return 'bg-gray-100 text-gray-800'
      case 'EXPIRED': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getAccessTypeColor = (accessType: string) => {
    switch (accessType) {
      case 'PUBLIC': return 'bg-blue-100 text-blue-800'
      case 'LEAD_SPECIFIC': return 'bg-purple-100 text-purple-800'
      case 'CONTACT_SPECIFIC': return 'bg-orange-100 text-orange-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  if (isLoading) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
      </AppLayout>
    )
  }

  // Show access denied for non-admin users
  if (!isAdmin) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <Card className="max-w-md">
            <CardContent className="p-6 text-center">
              <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
              <h2 className="text-xl font-semibold mb-2">Access Denied</h2>
              <p className="text-gray-600">
                Form management is restricted to administrators only.
              </p>
              <p className="text-sm text-gray-500 mt-2">
                Contact your administrator if you need access to form management.
              </p>
            </CardContent>
          </Card>
        </div>
      </AppLayout>
    )
  }

  return (
    <AppLayout>
      <div className="space-y-6 p-6 pt-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Custom Forms</h1>
          <p className="text-gray-600">Create and manage customer-facing forms</p>
        </div>
        <Button onClick={handleOpenCreateForm}>
          <Plus className="h-4 w-4 mr-2" />
          Create Form
        </Button>
      </div>

      {/* Search and Filters */}
      <div className="flex items-center gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
          <Input
            placeholder="Search forms..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <Button variant="outline">
          <Filter className="h-4 w-4 mr-2" />
          Filter
        </Button>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="forms" className="space-y-4">
        <TabsList>
          <TabsTrigger value="forms">Forms</TabsTrigger>
          <TabsTrigger value="access">Form Access</TabsTrigger>
          <TabsTrigger value="submissions">Submissions</TabsTrigger>
        </TabsList>

        {/* Forms Tab */}
        <TabsContent value="forms" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {filteredForms.map((form) => (
              <Card key={form.id} className="hover:shadow-md transition-shadow">
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="space-y-1">
                      <CardTitle className="text-lg">{form.name}</CardTitle>
                      <CardDescription>{form.description}</CardDescription>
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="sm">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem>
                          <Eye className="h-4 w-4 mr-2" />
                          View
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleEditForm(form)}>
                          <Edit className="h-4 w-4 mr-2" />
                          Edit Form
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleEditFormFields(form)}>
                          <Settings className="h-4 w-4 mr-2" />
                          Edit Fields
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => {
                          setSelectedForm(form)
                          setIsCreateAccessOpen(true)
                        }}>
                          <Share2 className="h-4 w-4 mr-2" />
                          Create Access
                        </DropdownMenuItem>
                        <DropdownMenuItem 
                          className="text-red-600"
                          onClick={() => handleDeleteForm(form.id)}
                        >
                          <Trash2 className="h-4 w-4 mr-2" />
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <Badge className={getStatusColor(form.status)}>
                        {form.status}
                      </Badge>
                      <Badge className={getAccessTypeColor(form.accessType)}>
                        {form.accessType.replace('_', ' ')}
                      </Badge>
                    </div>
                    
                    <div className="flex items-center justify-between text-sm text-gray-600">
                      <span className="flex items-center gap-1">
                        <Users className="h-4 w-4" />
                        {form.submissionCount} submissions
                      </span>
                      {form.maxSubmissions && (
                        <span>Max: {form.maxSubmissions}</span>
                      )}
                    </div>

                    <div className="text-xs text-gray-500">
                      Created: {new Date(form.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        {/* Form Access Tab */}
        <TabsContent value="access" className="space-y-4">
          <div className="space-y-4">
            {formAccesses.map((access) => (
              <Card key={access.id} className="hover:shadow-md transition-shadow">
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <h3 className="font-medium">{access.formName}</h3>
                        <Badge variant={access.isActive ? "default" : "secondary"}>
                          {access.isActive ? 'Active' : 'Inactive'}
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600">
                        {access.leadName ? `Lead: ${access.leadName}` : `Contact: ${access.contactName}`}
                      </p>
                      <p className="text-xs text-gray-500">
                        Token: {access.accessToken.substring(0, 8)}...
                      </p>
                    </div>
                    
                    <div className="flex items-center gap-2">
                      <WhatsAppFormShare
                        accessId={access.id}
                        formName={access.formName}
                        leadName={access.leadName}
                        contactName={access.contactName}
                        phone="+1234567890" // Mock phone - replace with actual data
                      />
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4 mr-2" />
                        View
                      </Button>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => handleRegenerateAccess(access.id)}>
                            <Share2 className="h-4 w-4 mr-2" />
                            Regenerate Token
                          </DropdownMenuItem>
                          <DropdownMenuItem 
                            className="text-red-600"
                            onClick={() => handleDeactivateAccess(access.id)}
                          >
                            <Trash2 className="h-4 w-4 mr-2" />
                            Deactivate
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                  </div>
                  
                  <div className="mt-3 flex items-center justify-between text-sm text-gray-600">
                    <span className="flex items-center gap-1">
                      <CheckCircle className="h-4 w-4" />
                      {access.submissionCount} submissions
                    </span>
                    {access.expiresAt && (
                      <span className="flex items-center gap-1">
                        <Clock className="h-4 w-4" />
                        Expires: {new Date(access.expiresAt).toLocaleDateString()}
                      </span>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        {/* Submissions Tab */}
        <TabsContent value="submissions" className="space-y-4">
          <div className="text-center py-8 text-gray-500">
            <AlertCircle className="h-12 w-12 mx-auto mb-4 text-gray-400" />
            <p>Form submissions will appear here</p>
            <p className="text-sm">Submissions are automatically tracked when forms are submitted</p>
          </div>
        </TabsContent>
      </Tabs>

      {/* Create Form Dialog */}
      <Dialog open={isCreateFormOpen} onOpenChange={setIsCreateFormOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Create New Form</DialogTitle>
            <DialogDescription>
              Create a custom form for your customers to fill out
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium">Form Name *</label>
              <Input 
                placeholder="Enter form name..." 
                value={formData.name}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
              />
            </div>
            <div>
              <label className="text-sm font-medium">Description</label>
              <Input 
                placeholder="Enter form description..." 
                value={formData.description || ''}
                onChange={(e) => setFormData({...formData, description: e.target.value})}
              />
            </div>
            <div>
              <label className="text-sm font-medium">Status</label>
              <select 
                className="w-full p-2 border rounded-md"
                value={formData.status}
                onChange={(e) => setFormData({...formData, status: e.target.value})}
              >
                <option value="DRAFT">Draft</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium">Access Type</label>
              <select 
                className="w-full p-2 border rounded-md"
                value={formData.accessType}
                onChange={(e) => setFormData({...formData, accessType: e.target.value})}
              >
                <option value="PUBLIC">Public</option>
                <option value="LEAD_SPECIFIC">Lead Specific</option>
                <option value="CONTACT_SPECIFIC">Contact Specific</option>
              </select>
            </div>
            <div className="flex gap-2">
              <Button 
                variant="outline"
                onClick={() => {
                  setIsCreateFormOpen(false)
                  setIsFormBuilderOpen(true)
                }}
                disabled={!formData.name.trim()}
              >
                Build Form Fields
              </Button>
              <Button 
                className="flex-1" 
                onClick={() => handleCreateForm(formData)}
                disabled={createFormMutation.isPending || !formData.name.trim()}
              >
                {createFormMutation.isPending ? 'Creating...' : 'Create Form'}
              </Button>
              <Button variant="outline" onClick={() => setIsCreateFormOpen(false)}>
                Cancel
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Create Access Dialog */}
      <Dialog open={isCreateAccessOpen} onOpenChange={setIsCreateAccessOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Form Access</DialogTitle>
            <DialogDescription>
              Generate access for a lead or contact to fill out this form
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium">Select Lead/Contact</label>
              <Input placeholder="Search leads and contacts..." />
            </div>
            <div className="flex gap-2">
              <Button 
                className="flex-1" 
                onClick={() => handleCreateAccess({})}
                disabled={createAccessMutation.isPending}
              >
                {createAccessMutation.isPending ? 'Creating...' : 'Create Access'}
              </Button>
              <Button variant="outline" onClick={() => setIsCreateAccessOpen(false)}>
                Cancel
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Form Dialog */}
      <Dialog open={isEditFormOpen} onOpenChange={setIsEditFormOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Form</DialogTitle>
            <DialogDescription>
              Update the form details and settings.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="edit-name">Form Name</Label>
              <Input
                id="edit-name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter form name"
              />
            </div>
            <div>
              <Label htmlFor="edit-description">Description</Label>
              <Textarea
                id="edit-description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter form description"
                rows={3}
              />
            </div>
            <div>
              <Label htmlFor="edit-status">Status</Label>
              <select
                id="edit-status"
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                className="w-full p-2 border rounded-md"
              >
                <option value="DRAFT">Draft</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </div>
            <div>
              <Label htmlFor="edit-access-type">Access Type</Label>
              <select
                id="edit-access-type"
                value={formData.accessType}
                onChange={(e) => setFormData({ ...formData, accessType: e.target.value })}
                className="w-full p-2 border rounded-md"
              >
                <option value="PUBLIC">Public</option>
                <option value="LEAD_SPECIFIC">Lead Specific</option>
                <option value="CONTACT_SPECIFIC">Contact Specific</option>
              </select>
            </div>
            <div className="flex gap-2">
              <Button 
                className="flex-1" 
                onClick={() => handleUpdateForm(editingForm!.id, formData)}
                disabled={updateFormMutation.isPending || !formData.name.trim()}
              >
                {updateFormMutation.isPending ? 'Updating...' : 'Update Form'}
              </Button>
              <Button variant="outline" onClick={() => setIsEditFormOpen(false)}>
                Cancel
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Form Builder Dialog */}
      <Dialog open={isFormBuilderOpen} onOpenChange={setIsFormBuilderOpen}>
        <DialogContent className="max-w-full h-[90vh] w-[95vw]">
          <DialogHeader>
            <DialogTitle>Form Builder - {formData.name}</DialogTitle>
            <DialogDescription>
              Drag and drop fields to build your form. Configure validation rules and field properties.
            </DialogDescription>
          </DialogHeader>
          <FormBuilder
            formId={editingForm?.id}
            fields={formFields}
            onFieldsChange={(newFields) => {
              console.log('Form fields changed:', newFields)
              setFormFields(newFields)
            }}
            onSave={() => {
              if (editingForm) {
                // Update existing form with fields
                handleUpdateForm(editingForm.id, { ...formData, fields: formFields })
              } else {
                // Create new form with fields
                handleCreateForm({ ...formData, fields: formFields })
              }
              setIsFormBuilderOpen(false)
            }}
            onCancel={() => {
              setIsFormBuilderOpen(false)
              setEditingForm(null)
            }}
          />
        </DialogContent>
      </Dialog>
      </div>
    </AppLayout>
  )
}

