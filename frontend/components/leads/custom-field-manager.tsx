"use client"

import { useState, useEffect } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { Switch } from "@/components/ui/switch"
import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Plus, Edit, Trash2, Settings, Eye } from "lucide-react"
import { useToast } from "@/hooks/use-toast"

interface CustomField {
  id: number
  fieldName: string
  fieldLabel: string
  fieldType: string
  isRequired: boolean
  defaultValue?: string
  options?: string[]
  validationRules?: Record<string, any>
  displayOrder: number
  isActive: boolean
  entityType?: string
}

const fieldTypes = [
  { value: 'TEXT', label: 'Text' },
  { value: 'EMAIL', label: 'Email' },
  { value: 'PHONE', label: 'Phone' },
  { value: 'NUMBER', label: 'Number' },
  { value: 'DATE', label: 'Date' },
  { value: 'DATETIME', label: 'Date & Time' },
  { value: 'BOOLEAN', label: 'Yes/No' },
  { value: 'DROPDOWN', label: 'Dropdown' },
  { value: 'RADIO', label: 'Radio Buttons' },
  { value: 'CHECKBOX', label: 'Checkbox' },
  { value: 'TEXTAREA', label: 'Long Text' },
  { value: 'URL', label: 'Website URL' },
  { value: 'CURRENCY', label: 'Currency' },
  { value: 'PERCENTAGE', label: 'Percentage' }
]

const createFieldSchema = z.object({
  entityType: z.string().min(1, "Entity type is required"),
  fieldName: z.string().min(1, "Field name is required").regex(/^[a-zA-Z][a-zA-Z0-9_]*$/, "Field name must start with a letter and contain only letters, numbers, and underscores"),
  fieldLabel: z.string().min(1, "Field label is required"),
  fieldType: z.string().min(1, "Field type is required"),
  isRequired: z.boolean().default(false),
  defaultValue: z.string().optional(),
  options: z.array(z.string()).optional(),
  validationRules: z.record(z.any()).optional(),
  displayOrder: z.number().min(0).default(0)
})

interface CustomFieldManagerProps {
  entityType: string
  companyId: number
}

const CustomFieldManager = ({ entityType, companyId }: CustomFieldManagerProps) => {
  const [fields, setFields] = useState<CustomField[]>([])
  const [loading, setLoading] = useState(true)
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [editingField, setEditingField] = useState<CustomField | null>(null)
  const { toast } = useToast()

  const form = useForm({
    resolver: zodResolver(createFieldSchema),
    defaultValues: {
      entityType,
      fieldName: '',
      fieldLabel: '',
      fieldType: 'TEXT',
      isRequired: false,
      defaultValue: '',
      options: [],
      validationRules: {},
      displayOrder: 0
    }
  })

  useEffect(() => {
    fetchFields()
  }, [entityType, companyId])

  const fetchFields = async () => {
    try {
      const response = await fetch(`/crm/api/custom-fields?entityType=${entityType}`, {
        headers: {
          'X-Company-Id': companyId.toString()
        }
      })
      if (response.ok) {
        const data = await response.json()
        setFields(data)
      }
    } catch (error) {
      console.error('Failed to fetch custom fields:', error)
      toast({
        title: "Error",
        description: "Failed to load custom fields",
        variant: "destructive"
      })
    } finally {
      setLoading(false)
    }
  }

  const onSubmit = async (data: any) => {
    try {
      const url = editingField 
        ? `/crm/api/custom-fields/${editingField.id}`
        : '/crm/api/custom-fields'
      
      const method = editingField ? 'PUT' : 'POST'
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'X-Company-Id': companyId.toString(),
          'X-User-Id': '1' // In real app, get from auth context
        },
        body: JSON.stringify(data)
      })

      if (response.ok) {
        toast({
          title: "Success",
          description: `Custom field ${editingField ? 'updated' : 'created'} successfully`
        })
        fetchFields()
        setIsCreateOpen(false)
        setEditingField(null)
        form.reset()
      } else {
        throw new Error('Failed to save custom field')
      }
    } catch (error) {
      console.error('Failed to save custom field:', error)
      toast({
        title: "Error",
        description: "Failed to save custom field",
        variant: "destructive"
      })
    }
  }

  const handleEdit = (field: CustomField) => {
    setEditingField(field)
    form.reset({
      entityType: field.entityType,
      fieldName: field.fieldName,
      fieldLabel: field.fieldLabel,
      fieldType: field.fieldType,
      isRequired: field.isRequired,
      defaultValue: field.defaultValue || '',
      options: field.options || [],
      validationRules: field.validationRules || {},
      displayOrder: field.displayOrder
    })
    setIsCreateOpen(true)
  }

  const handleDelete = async (fieldId: number) => {
    if (!confirm('Are you sure you want to delete this custom field?')) return

    try {
      const response = await fetch(`/crm/api/custom-fields/${fieldId}`, {
        method: 'DELETE',
        headers: {
          'X-Company-Id': companyId.toString()
        }
      })

      if (response.ok) {
        toast({
          title: "Success",
          description: "Custom field deleted successfully"
        })
        fetchFields()
      } else {
        throw new Error('Failed to delete custom field')
      }
    } catch (error) {
      console.error('Failed to delete custom field:', error)
      toast({
        title: "Error",
        description: "Failed to delete custom field",
        variant: "destructive"
      })
    }
  }

  const addOption = () => {
    const currentOptions = form.getValues('options') || []
    form.setValue('options', [...currentOptions, ''])
  }

  const removeOption = (index: number) => {
    const currentOptions = form.getValues('options') || []
    form.setValue('options', currentOptions.filter((_, i) => i !== index))
  }

  const updateOption = (index: number, value: string) => {
    const currentOptions = form.getValues('options') || []
    const newOptions = [...currentOptions]
    newOptions[index] = value
    form.setValue('options', newOptions)
  }

  if (loading) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="animate-pulse space-y-4">
            <div className="h-4 bg-gray-200 rounded w-1/4"></div>
            <div className="h-10 bg-gray-200 rounded"></div>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Custom Fields for {entityType}</h2>
          <p className="text-gray-600">Manage custom fields for {entityType.toLowerCase()}s</p>
        </div>
        <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
          <DialogTrigger asChild>
            <Button onClick={() => {
              setEditingField(null)
              form.reset({
                entityType,
                fieldName: '',
                fieldLabel: '',
                fieldType: 'TEXT',
                isRequired: false,
                defaultValue: '',
                options: [],
                validationRules: {},
                displayOrder: fields.length
              })
            }}>
              <Plus className="h-4 w-4 mr-2" />
              Add Custom Field
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>
                {editingField ? 'Edit Custom Field' : 'Create Custom Field'}
              </DialogTitle>
              <DialogDescription>
                Add a new custom field to {entityType.toLowerCase()}s
              </DialogDescription>
            </DialogHeader>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="fieldName">Field Name</Label>
                  <Input
                    id="fieldName"
                    {...form.register('fieldName')}
                    placeholder="e.g., company_size"
                    disabled={!!editingField}
                  />
                  <p className="text-xs text-gray-500">
                    Used in API and database. Cannot be changed after creation.
                  </p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="fieldLabel">Field Label</Label>
                  <Input
                    id="fieldLabel"
                    {...form.register('fieldLabel')}
                    placeholder="e.g., Company Size"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="fieldType">Field Type</Label>
                  <Select
                    value={form.watch('fieldType')}
                    onValueChange={(value) => form.setValue('fieldType', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select field type" />
                    </SelectTrigger>
                    <SelectContent>
                      {fieldTypes.map((type) => (
                        <SelectItem key={type.value} value={type.value}>
                          {type.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="displayOrder">Display Order</Label>
                  <Input
                    id="displayOrder"
                    type="number"
                    {...form.register('displayOrder', { valueAsNumber: true })}
                    min="0"
                  />
                </div>
              </div>

              <div className="flex items-center space-x-2">
                <Switch
                  id="isRequired"
                  checked={form.watch('isRequired')}
                  onCheckedChange={(checked) => form.setValue('isRequired', checked)}
                />
                <Label htmlFor="isRequired">Required field</Label>
              </div>

              <div className="space-y-2">
                <Label htmlFor="defaultValue">Default Value</Label>
                <Input
                  id="defaultValue"
                  {...form.register('defaultValue')}
                  placeholder="Optional default value"
                />
              </div>

              {(form.watch('fieldType') === 'DROPDOWN' || form.watch('fieldType') === 'RADIO' || form.watch('fieldType') === 'CHECKBOX') && (
                <div className="space-y-2">
                  <Label>Options</Label>
                  <div className="space-y-2">
                    {(form.watch('options') || []).map((option, index) => (
                      <div key={index} className="flex items-center space-x-2">
                        <Input
                          value={option}
                          onChange={(e) => updateOption(index, e.target.value)}
                          placeholder={`Option ${index + 1}`}
                        />
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => removeOption(index)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    ))}
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={addOption}
                    >
                      <Plus className="h-4 w-4 mr-2" />
                      Add Option
                    </Button>
                  </div>
                </div>
              )}

              <div className="flex justify-end space-x-2">
                <Button type="button" variant="outline" onClick={() => setIsCreateOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit">
                  {editingField ? 'Update Field' : 'Create Field'}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {fields.map((field) => (
          <Card key={field.id}>
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg">{field.fieldLabel}</CardTitle>
                <Badge variant={field.isActive ? "default" : "secondary"}>
                  {field.isActive ? "Active" : "Inactive"}
                </Badge>
              </div>
              <CardDescription>
                {field.fieldName} • {fieldTypes.find(t => t.value === field.fieldType)?.label}
                {field.isRequired && " • Required"}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {field.options && field.options.length > 0 && (
                  <div>
                    <p className="text-sm font-medium">Options:</p>
                    <div className="flex flex-wrap gap-1 mt-1">
                      {field.options.map((option, index) => (
                        <Badge key={index} variant="outline" className="text-xs">
                          {option}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}
                {field.defaultValue && (
                  <div>
                    <p className="text-sm font-medium">Default:</p>
                    <p className="text-sm text-gray-600">{field.defaultValue}</p>
                  </div>
                )}
              </div>
              <div className="flex justify-end space-x-2 mt-4">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleEdit(field)}
                >
                  <Edit className="h-4 w-4" />
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleDelete(field.id)}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {fields.length === 0 && (
        <Card>
          <CardContent className="p-6 text-center">
            <Settings className="h-12 w-12 mx-auto text-gray-400 mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No custom fields yet</h3>
            <p className="text-gray-600 mb-4">
              Create custom fields to capture additional information for {entityType.toLowerCase()}s
            </p>
            <Button onClick={() => setIsCreateOpen(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Create Your First Custom Field
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

export default CustomFieldManager
