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
import { Plus, Trash2, Settings } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { getAuthHeader } from "@/lib/auth"
import { buildApiUrl, API_ENDPOINTS } from "@/lib/api-config"

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
}

interface CoreField {
  id: string
  fieldName: string
  fieldLabel: string
  fieldType: string
  isRequired: boolean
  options?: string[]
}

interface FieldConfiguration {
  entityType: string
  coreFields: CoreField[]
  customFields: CustomField[]
  totalFields: number
}

interface LeadFormProps {
  initialData?: any
  onSave: (data: any) => void
  onCancel: () => void
  companyId: number
}

const LeadForm = ({ initialData, onSave, onCancel, companyId }: LeadFormProps) => {
  const [fieldConfig, setFieldConfig] = useState<FieldConfiguration | null>(null)
  const [loading, setLoading] = useState(true)
  const { toast } = useToast()

  useEffect(() => {
    fetchFieldConfiguration()
  }, [companyId])

  const fetchFieldConfiguration = async () => {
    try {
      const response = await fetch(buildApiUrl(API_ENDPOINTS.CUSTOM_FIELDS.CONFIGURATION('LEAD'), companyId), {
        headers: {
          'Authorization': getAuthHeader() || ''
        }
      })
      if (response.ok) {
        const config = await response.json()
        setFieldConfig(config)
      }
    } catch (error) {
      console.error('Failed to fetch field configuration:', error)
      toast({
        title: "Error",
        description: "Failed to load field configuration",
        variant: "destructive"
      })
    } finally {
      setLoading(false)
    }
  }

  const createValidationSchema = () => {
    if (!fieldConfig) return z.object({})

    const schemaFields: Record<string, z.ZodTypeAny> = {}

    // Add core fields
    fieldConfig.coreFields.forEach(field => {
      let fieldSchema: z.ZodTypeAny = z.string()
      
      if (field.fieldType === 'EMAIL') {
        fieldSchema = z.string().email()
      } else if (field.fieldType === 'PHONE') {
        fieldSchema = z.string().regex(/^[\+]?[1-9][\d]{0,15}$/, "Invalid phone number")
      } else if (field.fieldType === 'NUMBER') {
        fieldSchema = z.string().transform(val => parseFloat(val))
      } else if (field.fieldType === 'BOOLEAN') {
        fieldSchema = z.boolean()
      }

      if (field.isRequired) {
        if (fieldSchema instanceof z.ZodString) {
          fieldSchema = fieldSchema.min(1, `${field.fieldLabel} is required`)
        }
      } else {
        fieldSchema = fieldSchema.optional()
      }

      schemaFields[field.fieldName] = fieldSchema
    })

    // Add custom fields
    fieldConfig.customFields.forEach(field => {
      let fieldSchema: z.ZodTypeAny = z.string()
      
      if (field.fieldType === 'EMAIL') {
        fieldSchema = z.string().email()
      } else if (field.fieldType === 'PHONE') {
        fieldSchema = z.string().regex(/^[\+]?[1-9][\d]{0,15}$/, "Invalid phone number")
      } else if (field.fieldType === 'NUMBER') {
        fieldSchema = z.string().transform(val => parseFloat(val))
      } else if (field.fieldType === 'BOOLEAN') {
        fieldSchema = z.boolean()
      }

      if (field.isRequired) {
        if (fieldSchema instanceof z.ZodString) {
          fieldSchema = fieldSchema.min(1, `${field.fieldLabel} is required`)
        }
      } else {
        fieldSchema = fieldSchema.optional()
      }

      schemaFields[`custom_${field.id}`] = fieldSchema
    })

    return z.object(schemaFields)
  }

  const schema = createValidationSchema()
  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: initialData || {}
  })

  const onSubmit = (data: any) => {
    // Separate core fields from custom fields
    const coreData: any = {}
    const customFields: any[] = []

    Object.entries(data).forEach(([key, value]) => {
      if (key.startsWith('custom_')) {
        const fieldId = parseInt(key.replace('custom_', ''))
        customFields.push({
          fieldId,
          fieldValue: value?.toString() || ''
        })
      } else {
        coreData[key] = value
      }
    })

    onSave({
      ...coreData,
      customFields
    })
  }

  const renderField = (field: CoreField | CustomField, isCustom = false) => {
    const fieldKey = isCustom ? `custom_${field.id}` : field.fieldName
    const fieldValue = form.watch(fieldKey)

    const commonProps = {
      id: fieldKey,
      ...form.register(fieldKey),
      placeholder: `Enter ${field.fieldLabel.toLowerCase()}`,
      required: field.isRequired
    }

    switch (field.fieldType) {
      case 'TEXT':
      case 'EMAIL':
      case 'PHONE':
        return (
          <Input
            {...commonProps}
            type={field.fieldType === 'EMAIL' ? 'email' : field.fieldType === 'PHONE' ? 'tel' : 'text'}
          />
        )
      
      case 'TEXTAREA':
        return (
          <Textarea
            {...commonProps}
            rows={3}
          />
        )
      
      case 'NUMBER':
      case 'CURRENCY':
      case 'PERCENTAGE':
        return (
          <Input
            {...commonProps}
            type="number"
            step={field.fieldType === 'CURRENCY' ? '0.01' : field.fieldType === 'PERCENTAGE' ? '0.1' : '1'}
          />
        )
      
      case 'DATE':
        return (
          <Input
            {...commonProps}
            type="date"
          />
        )
      
      case 'DATETIME':
        return (
          <Input
            {...commonProps}
            type="datetime-local"
          />
        )
      
      case 'BOOLEAN':
        return (
          <div className="flex items-center space-x-2">
            <Switch
              id={fieldKey}
              checked={fieldValue || false}
              onCheckedChange={(checked) => form.setValue(fieldKey, checked)}
            />
            <Label htmlFor={fieldKey}>{field.fieldLabel}</Label>
          </div>
        )
      
      case 'SELECT':
      case 'DROPDOWN':
      case 'RADIO':
        const options = (field as CustomField).options || (field as CoreField).options || []
        const optionsArray = Array.isArray(options) ? options : []
        if (field.fieldType === 'SELECT' || field.fieldType === 'DROPDOWN') {
          return (
            <Select
              value={fieldValue || ''}
              onValueChange={(value) => form.setValue(fieldKey, value)}
            >
              <SelectTrigger>
                <SelectValue placeholder={`Select ${field.fieldLabel.toLowerCase()}`} />
              </SelectTrigger>
              <SelectContent>
                {optionsArray.map((option, index) => (
                  <SelectItem key={index} value={option}>
                    {option}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )
        } else {
          return (
            <div className="space-y-2">
              {optionsArray.map((option, index) => (
                <div key={index} className="flex items-center space-x-2">
                  <input
                    type="radio"
                    id={`${fieldKey}_${index}`}
                    value={option}
                    {...form.register(fieldKey)}
                    className="h-4 w-4"
                  />
                  <Label htmlFor={`${fieldKey}_${index}`}>{option}</Label>
                </div>
              ))}
            </div>
          )
        }
      
      case 'URL':
        return (
          <Input
            {...commonProps}
            type="url"
          />
        )
      
      default:
        return (
          <Input
            {...commonProps}
            type="text"
          />
        )
    }
  }

  if (loading) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="animate-pulse space-y-4">
            <div className="h-4 bg-gray-200 rounded w-1/4"></div>
            <div className="h-10 bg-gray-200 rounded"></div>
            <div className="h-4 bg-gray-200 rounded w-1/4"></div>
            <div className="h-10 bg-gray-200 rounded"></div>
          </div>
        </CardContent>
      </Card>
    )
  }

  if (!fieldConfig) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center text-gray-500">
            Failed to load field configuration
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold">
            {initialData ? 'Edit Lead' : 'Create New Lead'}
          </h2>
          <p className="text-sm text-muted-foreground">
            {fieldConfig.totalFields} total fields ({fieldConfig.coreFields.length} core, {fieldConfig.customFields.length} custom)
          </p>
        </div>
        <Badge variant="outline" className="flex items-center gap-1">
          <Settings className="h-3 w-3" />
          Dynamic Fields
        </Badge>
      </div>

      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        {/* Core Fields */}
        <div className="space-y-4">
          <h3 className="text-lg font-medium border-b pb-2">Core Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {fieldConfig.coreFields.map((field) => (
              <div key={field.fieldName} className="space-y-2">
                <Label htmlFor={field.fieldName} className="text-sm font-medium">
                  {field.fieldLabel}
                  {field.isRequired && <span className="text-red-500 ml-1">*</span>}
                </Label>
                {renderField(field)}
              </div>
            ))}
          </div>
        </div>

        {/* Custom Fields */}
        {fieldConfig.customFields.length > 0 && (
          <div className="space-y-4">
            <h3 className="text-lg font-medium border-b pb-2">Additional Information</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {fieldConfig.customFields.map((field) => (
                <div key={field.id} className="space-y-2">
                  <Label htmlFor={`custom_${field.id}`} className="text-sm font-medium">
                    {field.fieldLabel}
                    {field.isRequired && <span className="text-red-500 ml-1">*</span>}
                  </Label>
                  {renderField(field, true)}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Form Actions - Sticky at bottom */}
        <div className="sticky bottom-0 bg-background border-t pt-4 mt-6">
          <div className="flex justify-end space-x-2">
            <Button type="button" variant="outline" onClick={onCancel}>
              Cancel
            </Button>
            <Button type="submit">
              {initialData ? 'Update Lead' : 'Create Lead'}
            </Button>
          </div>
        </div>
      </form>
    </div>
  )
}

export default LeadForm
