'use client'

import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { 
  Plus, 
  Trash2, 
  GripVertical, 
  Eye, 
  Settings,
  Text,
  Mail,
  Phone,
  Hash,
  Calendar,
  Upload,
  CheckSquare,
  Circle,
  List
} from 'lucide-react'

interface FormField {
  id: string
  name: string
  label: string
  type: string
  isRequired: boolean
  placeholder?: string
  helpText?: string
  options?: string[]
  validation?: {
    minLength?: number
    maxLength?: number
    pattern?: string
    min?: number
    max?: number
  }
  order: number
}

interface FormBuilderProps {
  formId?: number
  fields: FormField[]
  onFieldsChange: (fields: FormField[]) => void
  onSave: () => void
  onCancel: () => void
}

const fieldTypes = [
  { value: 'TEXT', label: 'Text Input', icon: Text },
  { value: 'EMAIL', label: 'Email', icon: Mail },
  { value: 'PHONE', label: 'Phone', icon: Phone },
  { value: 'NUMBER', label: 'Number', icon: Hash },
  { value: 'TEXTAREA', label: 'Text Area', icon: Text },
  { value: 'SELECT', label: 'Dropdown', icon: List },
  { value: 'RADIO', label: 'Radio Buttons', icon: Circle },
  { value: 'CHECKBOX', label: 'Checkboxes', icon: CheckSquare },
  { value: 'DATE', label: 'Date', icon: Calendar },
  { value: 'FILE', label: 'File Upload', icon: Upload },
  { value: 'URL', label: 'URL', icon: Text },
  { value: 'IMAGE', label: 'Image Upload', icon: Upload },
]

export function FormBuilder({ formId, fields, onFieldsChange, onSave, onCancel }: FormBuilderProps) {
  const [selectedField, setSelectedField] = useState<FormField | null>(null)
  const [isPreviewMode, setIsPreviewMode] = useState(false)

  // Keep selectedField in sync with the actual field data
  useEffect(() => {
    if (selectedField) {
      const currentField = fields.find(f => f.id === selectedField.id)
      if (currentField) {
        setSelectedField(currentField)
      }
    }
  }, [fields, selectedField?.id])

  const addField = (type: string) => {
    const newField: FormField = {
      id: `field_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      name: `field_${fields.length + 1}`,
      label: `Field ${fields.length + 1}`,
      type,
      isRequired: false,
      placeholder: '',
      helpText: '',
      options: type === 'SELECT' || type === 'RADIO' || type === 'CHECKBOX' ? ['Option 1', 'Option 2'] : undefined,
      validation: {},
      order: fields.length
    }
    console.log('Adding new field:', newField)
    onFieldsChange([...fields, newField])
  }

  const updateField = (fieldId: string, updates: Partial<FormField>) => {
    console.log('Updating field:', fieldId, updates)
    const updatedFields = fields.map(field => 
      field.id === fieldId ? { ...field, ...updates } : field
    )
    console.log('Updated fields:', updatedFields)
    onFieldsChange(updatedFields)
    
    // Update selectedField if it's the field being updated
    if (selectedField?.id === fieldId) {
      const updatedSelectedField = { ...selectedField, ...updates }
      setSelectedField(updatedSelectedField)
    }
  }

  const deleteField = (fieldId: string) => {
    onFieldsChange(fields.filter(field => field.id !== fieldId))
    if (selectedField?.id === fieldId) {
      setSelectedField(null)
    }
  }

  const moveField = (fieldId: string, direction: 'up' | 'down') => {
    const currentIndex = fields.findIndex(f => f.id === fieldId)
    if (currentIndex === -1) return

    const newIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1
    if (newIndex < 0 || newIndex >= fields.length) return

    const newFields = [...fields]
    const [movedField] = newFields.splice(currentIndex, 1)
    newFields.splice(newIndex, 0, movedField)
    
    // Update order
    newFields.forEach((field, index) => {
      field.order = index
    })
    
    onFieldsChange(newFields)
  }

  const renderFieldPreview = (field: FormField) => {
    const commonProps = {
      placeholder: field.placeholder,
      required: field.isRequired,
      className: "w-full"
    }

    switch (field.type) {
      case 'TEXT':
      case 'EMAIL':
      case 'PHONE':
      case 'URL':
        return <Input {...commonProps} type={field.type.toLowerCase()} />
      
      case 'NUMBER':
        return <Input {...commonProps} type="number" />
      
      case 'TEXTAREA':
        return <Textarea {...commonProps} rows={3} />
      
      case 'SELECT':
        return (
          <select {...commonProps} className="w-full p-2 border rounded-md">
            <option value="">Select an option</option>
            {field.options?.map((option, index) => (
              <option key={index} value={option}>{option}</option>
            ))}
          </select>
        )
      
      case 'RADIO':
        return (
          <div className="space-y-2">
            {field.options?.map((option, index) => (
              <label key={index} className="flex items-center space-x-2">
                <input type="radio" name={field.name} value={option} />
                <span>{option}</span>
              </label>
            ))}
          </div>
        )
      
      case 'CHECKBOX':
        return (
          <div className="space-y-2">
            {field.options?.map((option, index) => (
              <label key={index} className="flex items-center space-x-2">
                <input type="checkbox" value={option} />
                <span>{option}</span>
              </label>
            ))}
          </div>
        )
      
      case 'DATE':
        return <Input {...commonProps} type="date" />
      
      case 'FILE':
      case 'IMAGE':
        return <Input {...commonProps} type="file" />
      
      default:
        return <Input {...commonProps} />
    }
  }

  return (
    <div className="flex h-screen">
      {/* Field Library */}
      <div className="w-64 bg-gray-50 p-4 border-r overflow-y-auto max-h-[75vh]">
        <h3 className="font-semibold mb-4">Field Types</h3>
        <div className="space-y-2">
          {fieldTypes.map((fieldType) => {
            const Icon = fieldType.icon
            return (
              <Button
                key={fieldType.value}
                variant="outline"
                className="w-full justify-start"
                onClick={() => addField(fieldType.value)}
              >
                <Icon className="h-4 w-4 mr-2" />
                {fieldType.label}
              </Button>
            )
          })}
        </div>
      </div>

      {/* Form Builder */}
      <div className="flex-1 flex overflow-y-auto max-h-[80vh]">
        {/* Form Canvas */}
        <div className="flex-1 p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold">Form Builder</h2>
            <div className="flex gap-2">
              <Button
                variant="outline"
                onClick={() => setIsPreviewMode(!isPreviewMode)}
              >
                <Eye className="h-4 w-4 mr-2" />
                {isPreviewMode ? 'Edit' : 'Preview'}
              </Button>
              <Button onClick={onSave}>
                Save Form
              </Button>
              <Button variant="outline" onClick={onCancel}>
                Cancel
              </Button>
            </div>
          </div>

          {fields.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <p>No fields added yet. Click on a field type to get started.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {fields
                .sort((a, b) => a.order - b.order)
                .map((field) => (
                  <Card 
                    key={field.id} 
                    className={`cursor-pointer transition-colors ${
                      selectedField?.id === field.id ? 'ring-2 ring-blue-500' : ''
                    }`}
                    onClick={() => setSelectedField(field)}
                  >
                    <CardContent className="p-4">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <Label className="font-medium">
                              {field.label}
                              {field.isRequired && <span className="text-red-500 ml-1">*</span>}
                            </Label>
                            <Badge variant="secondary">{field.type}</Badge>
                          </div>
                          {field.helpText && (
                            <p className="text-sm text-gray-600 mb-2">{field.helpText}</p>
                          )}
                          {isPreviewMode ? (
                            renderFieldPreview(field)
                          ) : (
                            <div className="text-gray-400 italic">
                              {field.placeholder || `Enter ${field.label.toLowerCase()}`}
                            </div>
                          )}
                        </div>
                        <div className="flex items-center gap-1 ml-4">
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={(e) => {
                              e.stopPropagation()
                              moveField(field.id, 'up')
                            }}
                            disabled={field.order === 0}
                          >
                            ↑
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={(e) => {
                              e.stopPropagation()
                              moveField(field.id, 'down')
                            }}
                            disabled={field.order === fields.length - 1}
                          >
                            ↓
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={(e) => {
                              e.stopPropagation()
                              deleteField(field.id)
                            }}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
            </div>
          )}
        </div>

        {/* Field Properties Panel */}
        {selectedField && (
          <div className="w-80 bg-gray-50 p-4 border-l overflow-y-auto max-h-[75vh]">
            <h3 className="font-semibold mb-4">Field Properties</h3>
            <div className="space-y-4">
              <div>
                <Label>Field Name</Label>
                <Input
                  value={selectedField.name}
                  onChange={(e) => updateField(selectedField.id, { name: e.target.value })}
                />
              </div>
              
              <div>
                <Label>Label</Label>
                <Input
                  value={selectedField.label}
                  onChange={(e) => updateField(selectedField.id, { label: e.target.value })}
                />
              </div>
              
              <div>
                <Label>Placeholder</Label>
                <Input
                  value={selectedField.placeholder || ''}
                  onChange={(e) => updateField(selectedField.id, { placeholder: e.target.value })}
                />
              </div>
              
              <div>
                <Label>Help Text</Label>
                <Textarea
                  value={selectedField.helpText || ''}
                  onChange={(e) => updateField(selectedField.id, { helpText: e.target.value })}
                  rows={2}
                />
              </div>
              
              <div className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  id="required"
                  checked={selectedField.isRequired}
                  onChange={(e) => updateField(selectedField.id, { isRequired: e.target.checked })}
                />
                <Label htmlFor="required">Required field</Label>
              </div>
              
              {(selectedField.type === 'SELECT' || selectedField.type === 'RADIO' || selectedField.type === 'CHECKBOX') && (
                <div>
                  <Label>Options (one per line)</Label>
                  <Textarea
                    value={selectedField.options?.join('\n') || ''}
                    onChange={(e) => {
                      const options = e.target.value
                        .split('\n')
                        .map(opt => opt.trim())
                        .filter(opt => opt.length > 0)
                      updateField(selectedField.id, { options })
                    }}
                    rows={4}
                    placeholder="Option 1&#10;Option 2&#10;Option 3"
                  />
                  <p className="text-xs text-gray-500 mt-1">
                    Enter each option on a new line
                  </p>
                </div>
              )}
              
              <div>
                <Label>Validation Rules</Label>
                <div className="space-y-2">
                  <div className="flex gap-2">
                    <Input
                      type="number"
                      placeholder="Min length"
                      value={selectedField.validation?.minLength || ''}
                      onChange={(e) => updateField(selectedField.id, {
                        validation: { ...selectedField.validation, minLength: parseInt(e.target.value) || undefined }
                      })}
                    />
                    <Input
                      type="number"
                      placeholder="Max length"
                      value={selectedField.validation?.maxLength || ''}
                      onChange={(e) => updateField(selectedField.id, {
                        validation: { ...selectedField.validation, maxLength: parseInt(e.target.value) || undefined }
                      })}
                    />
                  </div>
                  <Input
                    placeholder="Regex pattern"
                    value={selectedField.validation?.pattern || ''}
                    onChange={(e) => updateField(selectedField.id, {
                      validation: { ...selectedField.validation, pattern: e.target.value || undefined }
                    })}
                  />
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}