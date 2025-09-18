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
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { 
  Plus, 
  Search, 
  Filter, 
  MoreHorizontal, 
  Calendar,
  User,
  Phone,
  Mail,
  MessageSquare,
  FileText,
  Eye,
  Edit,
  Trash2,
  Activity,
  Clock,
  CheckCircle
} from 'lucide-react'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'

interface Activity {
  id: number
  type: string
  subject: string
  description?: string
  dueDate?: string
  status: string
  priority: string
  assignedUserId?: number
  relatedEntityType?: string
  relatedEntityId?: number
  companyId: number
  createdAt: string
  updatedAt: string
}

const activityTypes = [
  { value: 'CALL', label: 'Call', color: 'bg-blue-100 text-blue-800', icon: Phone },
  { value: 'EMAIL', label: 'Email', color: 'bg-green-100 text-green-800', icon: Mail },
  { value: 'MEETING', label: 'Meeting', color: 'bg-purple-100 text-purple-800', icon: Calendar },
  { value: 'TASK', label: 'Task', color: 'bg-orange-100 text-orange-800', icon: CheckCircle },
  { value: 'NOTE', label: 'Note', color: 'bg-gray-100 text-gray-800', icon: FileText },
  { value: 'MESSAGE', label: 'Message', color: 'bg-cyan-100 text-cyan-800', icon: MessageSquare }
]

const activityStatuses = [
  { value: 'PENDING', label: 'Pending', color: 'bg-yellow-100 text-yellow-800' },
  { value: 'IN_PROGRESS', label: 'In Progress', color: 'bg-blue-100 text-blue-800' },
  { value: 'COMPLETED', label: 'Completed', color: 'bg-green-100 text-green-800' },
  { value: 'CANCELLED', label: 'Cancelled', color: 'bg-red-100 text-red-800' }
]

const priorities = [
  { value: 'LOW', label: 'Low', color: 'bg-gray-100 text-gray-800' },
  { value: 'MEDIUM', label: 'Medium', color: 'bg-yellow-100 text-yellow-800' },
  { value: 'HIGH', label: 'High', color: 'bg-orange-100 text-orange-800' },
  { value: 'URGENT', label: 'Urgent', color: 'bg-red-100 text-red-800' }
]

export default function ActivitiesPage() {
  const { user, isAuthenticated, isLoading } = useAuth()
  const router = useRouter()
  const [activities, setActivities] = useState<Activity[]>([])
  const [filteredActivities, setFilteredActivities] = useState<Activity[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [isLoadingActivities, setIsLoadingActivities] = useState(true)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [selectedActivity, setSelectedActivity] = useState<Activity | null>(null)

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  useEffect(() => {
    if (isAuthenticated) {
      fetchActivities()
    }
  }, [isAuthenticated])

  useEffect(() => {
    filterActivities()
  }, [activities, searchTerm, typeFilter, statusFilter])

  const fetchActivities = async () => {
    try {
      setIsLoadingActivities(true)
      const response = await fetch(buildApiUrl(API_ENDPOINTS.ACTIVITIES.LIST, user?.companyId || 1), {
        headers: {
          'Authorization': getAuthHeader() || ''
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        setActivities(data)
      }
    } catch (error) {
      console.error('Error fetching activities:', error)
    } finally {
      setIsLoadingActivities(false)
    }
  }

  const filterActivities = () => {
    let filtered = activities

    if (searchTerm) {
      filtered = filtered.filter(activity =>
        activity.subject.toLowerCase().includes(searchTerm.toLowerCase()) ||
        activity.description?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (typeFilter !== 'ALL') {
      filtered = filtered.filter(activity => activity.type === typeFilter)
    }

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(activity => activity.status === statusFilter)
    }

    setFilteredActivities(filtered)
  }

  const handleCreateActivity = async (activityData: any) => {
    try {
      const response = await fetch(buildApiUrl(API_ENDPOINTS.ACTIVITIES.LIST, user?.companyId || 1), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': getAuthHeader() || ''
        },
        body: JSON.stringify(activityData)
      })

      if (response.ok) {
        await fetchActivities()
        setIsCreateDialogOpen(false)
      }
    } catch (error) {
      console.error('Error creating activity:', error)
    }
  }

  const handleUpdateActivity = async (activityData: any) => {
    try {
      const response = await fetch(buildApiUrl(API_ENDPOINTS.ACTIVITIES.UPDATE(selectedActivity?.id || 0), user?.companyId || 1), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': getAuthHeader() || ''
        },
        body: JSON.stringify(activityData)
      })

      if (response.ok) {
        await fetchActivities()
        setSelectedActivity(null)
      }
    } catch (error) {
      console.error('Error updating activity:', error)
    }
  }

  const handleDeleteActivity = async (activityId: number) => {
    if (confirm('Are you sure you want to delete this activity?')) {
      try {
        const response = await fetch(buildApiUrl(API_ENDPOINTS.ACTIVITIES.DELETE(activityId), user?.companyId || 1), {
          method: 'DELETE',
          headers: {
            'Authorization': getAuthHeader() || ''
          }
        })

        if (response.ok) {
          await fetchActivities()
        }
      } catch (error) {
        console.error('Error deleting activity:', error)
      }
    }
  }

  const getActivityType = (type: string) => {
    return activityTypes.find(t => t.value === type) || activityTypes[0]
  }

  const getActivityStatus = (status: string) => {
    return activityStatuses.find(s => s.value === status) || activityStatuses[0]
  }

  const getPriority = (priority: string) => {
    return priorities.find(p => p.value === priority) || priorities[1]
  }

  if (isLoading) {
    return <div className="flex items-center justify-center h-64">Loading...</div>
  }

  if (!isAuthenticated) {
    return <div className="flex items-center justify-center h-64">Redirecting to login...</div>
  }

  return (
    <AppLayout>
      <div className="container mx-auto p-6 pt-4 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Activities</h1>
          <p className="text-muted-foreground">
            Manage your activities, tasks, and interactions
          </p>
        </div>
        <Button onClick={() => setIsCreateDialogOpen(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Add Activity
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
                <Input
                  placeholder="Search activities..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <Select value={typeFilter} onValueChange={setTypeFilter}>
              <SelectTrigger className="w-full sm:w-48">
                <SelectValue placeholder="Filter by type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Types</SelectItem>
                {activityTypes.map((type) => (
                  <SelectItem key={type.value} value={type.value}>
                    {type.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-full sm:w-48">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Statuses</SelectItem>
                {activityStatuses.map((status) => (
                  <SelectItem key={status.value} value={status.value}>
                    {status.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Activities List */}
      <div className="space-y-4">
        {isLoadingActivities ? (
          <div className="flex items-center justify-center h-32">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          </div>
        ) : filteredActivities.length === 0 ? (
          <Card>
            <CardContent className="p-8 text-center">
              <Activity className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
              <h3 className="text-lg font-semibold mb-2">No activities found</h3>
              <p className="text-muted-foreground mb-4">
                {searchTerm || typeFilter !== 'ALL' || statusFilter !== 'ALL'
                  ? 'Try adjusting your filters to see more activities.'
                  : 'Get started by creating your first activity.'}
              </p>
              <Button onClick={() => setIsCreateDialogOpen(true)}>
                <Plus className="h-4 w-4 mr-2" />
                Add Activity
              </Button>
            </CardContent>
          </Card>
        ) : (
          filteredActivities.map((activity) => {
            const typeInfo = getActivityType(activity.type)
            const statusInfo = getActivityStatus(activity.status)
            const priorityInfo = getPriority(activity.priority)

            return (
              <Card key={activity.id}>
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start space-x-4 flex-1">
                      <div className={`p-2 rounded-lg ${typeInfo.color}`}>
                        <typeInfo.icon className="h-5 w-5" />
                      </div>
                      <div className="flex-1 space-y-2">
                        <div className="flex items-center gap-2">
                          <h3 className="font-semibold">{activity.subject}</h3>
                          <Badge className={statusInfo.color}>
                            {statusInfo.label}
                          </Badge>
                          <Badge variant="outline" className={priorityInfo.color}>
                            {priorityInfo.label}
                          </Badge>
                        </div>
                        {activity.description && (
                          <p className="text-muted-foreground text-sm">
                            {activity.description}
                          </p>
                        )}
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <div className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            {new Date(activity.createdAt).toLocaleDateString()}
                          </div>
                          {activity.dueDate && (
                            <div className="flex items-center gap-1">
                              <Calendar className="h-4 w-4" />
                              Due: {new Date(activity.dueDate).toLocaleDateString()}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="sm">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => setSelectedActivity(activity)}>
                          <Eye className="mr-2 h-4 w-4" />
                          View
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => setSelectedActivity(activity)}>
                          <Edit className="mr-2 h-4 w-4" />
                          Edit
                        </DropdownMenuItem>
                        <DropdownMenuItem 
                          className="text-red-600" 
                          onClick={() => handleDeleteActivity(activity.id)}
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </CardContent>
              </Card>
            )
          })
        )}
      </div>

      {/* Create Activity Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Create New Activity</DialogTitle>
            <DialogDescription>
              Add a new activity, task, or interaction to your CRM.
            </DialogDescription>
          </DialogHeader>
          <ActivityForm
            onSave={handleCreateActivity}
            onCancel={() => setIsCreateDialogOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* Edit Activity Dialog */}
      <Dialog open={!!selectedActivity} onOpenChange={() => setSelectedActivity(null)}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Edit Activity</DialogTitle>
            <DialogDescription>
              Update the activity information below.
            </DialogDescription>
          </DialogHeader>
          <ActivityForm
            initialData={selectedActivity}
            onSave={handleUpdateActivity}
            onCancel={() => setSelectedActivity(null)}
          />
        </DialogContent>
      </Dialog>
      </div>
    </AppLayout>
  )
}

// Activity Form Component
interface ActivityFormProps {
  initialData?: Activity | null
  onSave: (data: any) => void
  onCancel: () => void
}

function ActivityForm({ initialData, onSave, onCancel }: ActivityFormProps) {
  const [formData, setFormData] = useState({
    type: initialData?.type || 'TASK',
    subject: initialData?.subject || '',
    description: initialData?.description || '',
    dueDate: initialData?.dueDate ? new Date(initialData.dueDate).toISOString().split('T')[0] : '',
    dueTime: initialData?.dueDate ? new Date(initialData.dueDate).toISOString().split('T')[1].substring(0, 5) : '',
    status: initialData?.status || 'PENDING',
    priority: initialData?.priority || 'MEDIUM'
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    
    // Combine date and time into ISO datetime string
    const submitData = { ...formData }
    if (formData.dueDate) {
      if (formData.dueTime) {
        // Combine date and time
        submitData.dueDate = `${formData.dueDate}T${formData.dueTime}:00`
      } else {
        // Default to 9 AM if no time specified
        submitData.dueDate = `${formData.dueDate}T09:00:00`
      }
    }
    
    // Remove dueTime from the data sent to API
    const { dueTime, ...apiData } = submitData
    onSave(apiData)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="type">Type</Label>
          <Select value={formData.type} onValueChange={(value) => setFormData({...formData, type: value})}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {activityTypes.map((type) => (
                <SelectItem key={type.value} value={type.value}>
                  {type.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-2">
          <Label htmlFor="status">Status</Label>
          <Select value={formData.status} onValueChange={(value) => setFormData({...formData, status: value})}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {activityStatuses.map((status) => (
                <SelectItem key={status.value} value={status.value}>
                  {status.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
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

      <div className="grid grid-cols-3 gap-4">
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
          <Label htmlFor="dueTime">Due Time</Label>
          <Input
            id="dueTime"
            type="time"
            value={formData.dueTime || ''}
            onChange={(e) => setFormData({...formData, dueTime: e.target.value})}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="priority">Priority</Label>
          <Select value={formData.priority} onValueChange={(value) => setFormData({...formData, priority: value})}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {priorities.map((priority) => (
                <SelectItem key={priority.value} value={priority.value}>
                  {priority.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">
          {initialData ? 'Update Activity' : 'Create Activity'}
        </Button>
      </div>
    </form>
  )
}
