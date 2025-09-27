'use client'

import { useState, useEffect } from 'react'
import { useParams } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Checkbox } from '@/components/ui/checkbox'
import { Badge } from '@/components/ui/badge'
import { 
  CheckCircle, 
  AlertCircle, 
  Upload, 
  Image as ImageIcon,
  Loader2
} from 'lucide-react'

interface FormField {
  id: string
  type: 'TEXT' | 'EMAIL' | 'PHONE' | 'NUMBER' | 'TEXTAREA' | 'SELECT' | 'CHECKBOX' | 'DATE' | 'IMAGE'
  name: string
  label: string
  placeholder?: string
  required: boolean
  helpText?: string
  options?: string[]
  validation?: string
  order: number
}

interface FormData {
  id: number
  name: string
  description?: string
  fields: FormField[]
  theme: string
  settings: any
}

export default function FormAccessPage() {
  const params = useParams()
  const token = params.token as string
  
  const [form, setForm] = useState<FormData | null>(null)
  const [formData, setFormData] = useState<Record<string, any>>({})
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSubmitted, setIsSubmitted] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [uploadedFiles, setUploadedFiles] = useState<Record<string, File>>({})

  useEffect(() => {
    if (token) {
      fetchForm(token)
    }
  }, [token])

  const fetchForm = async (accessToken: string) => {
    try {
      setIsLoading(true)
      const response = await fetch(`/api/custom-forms/public/access/${accessToken}`)
      
      if (!response.ok) {
        throw new Error('Form not found or expired')
      }
      
      const data = await response.json()
      setForm(data)
      
      // Initialize form data
      const initialData: Record<string, any> = {}
      data.fields.forEach((field: FormField) => {
        if (field.type === 'CHECKBOX') {
          initialData[field.name] = false
        } else if (field.type === 'SELECT' && field.options && field.options.length > 0) {
          initialData[field.name] = field.options[0]
        } else {
          initialData[field.name] = ''
        }
      })
      setFormData(initialData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load form')
    } finally {
      setIsLoading(false)
    }
  }

  const handleInputChange = (fieldName: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [fieldName]: value
    }))
  }

  const handleFileUpload = (fieldName: string, file: File) => {
    setUploadedFiles(prev => ({
      ...prev,
      [fieldName]: file
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!form) return

    // Validate required fields
    const missingFields = form.fields
      .filter(field => field.required && !formData[field.name])
      .map(field => field.label)

    if (missingFields.length > 0) {
      setError(`Please fill in the following required fields: ${missingFields.join(', ')}`)
      return
    }

    try {
      setIsSubmitting(true)
      setError(null)

      // Process file uploads
      const processedData = { ...formData }
      for (const [fieldName, file] of Object.entries(uploadedFiles)) {
        if (file) {
          const base64 = await fileToBase64(file)
          processedData[fieldName] = {
            fileName: file.name,
            fileType: file.type,
            fileSize: file.size,
            data: base64
          }
        }
      }

      const response = await fetch('/api/custom-forms/public/submit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          accessToken: token,
          formId: form.id,
          data: processedData
        })
      })

      if (!response.ok) {
        throw new Error('Failed to submit form')
      }

      setIsSubmitted(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit form')
    } finally {
      setIsSubmitting(false)
    }
  }

  const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.readAsDataURL(file)
      reader.onload = () => resolve(reader.result as string)
      reader.onerror = error => reject(error)
    })
  }

  const renderField = (field: FormField) => {
    const value = formData[field.name] || ''
    const isRequired = field.required

    switch (field.type) {
      case 'TEXT':
        return (
          <Input
            type="text"
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            required={isRequired}
          />
        )
      
      case 'EMAIL':
        return (
          <Input
            type="email"
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            required={isRequired}
          />
        )
      
      case 'PHONE':
        return (
          <Input
            type="tel"
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            required={isRequired}
          />
        )
      
      case 'NUMBER':
        return (
          <Input
            type="number"
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            required={isRequired}
          />
        )
      
      case 'TEXTAREA':
        return (
          <Textarea
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            required={isRequired}
            rows={4}
          />
        )
      
      case 'SELECT':
        return (
          <Select value={value} onValueChange={(val) => handleInputChange(field.name, val)}>
            <SelectTrigger>
              <SelectValue placeholder="Select an option" />
            </SelectTrigger>
            <SelectContent>
              {field.options?.map((option, index) => (
                <SelectItem key={index} value={option}>
                  {option}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        )
      
      case 'CHECKBOX':
        return (
          <div className="flex items-center space-x-2">
            <Checkbox
              id={field.name}
              checked={value}
              onChange={e => handleInputChange(field.name, e.target.checked)}
            />
            <Label htmlFor={field.name} className="text-sm">
              {field.label}
            </Label>
          </div>
        )
      
      case 'DATE':
        return (
          <Input
            type="date"
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            required={isRequired}
          />
        )
      
      case 'IMAGE':
        return (
          <div className="space-y-2">
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center">
              <ImageIcon className="h-8 w-8 mx-auto text-gray-400 mb-2" />
              <p className="text-sm text-gray-500 mb-2">Click to upload image</p>
              <input
                type="file"
                accept="image/*"
                onChange={(e) => {
                  const file = e.target.files?.[0]
                  if (file) {
                    handleFileUpload(field.name, file)
                  }
                }}
                className="hidden"
                id={`file-${field.name}`}
              />
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => document.getElementById(`file-${field.name}`)?.click()}
              >
                <Upload className="h-4 w-4 mr-2" />
                Choose File
              </Button>
            </div>
            {uploadedFiles[field.name] && (
              <p className="text-sm text-green-600">
                Selected: {uploadedFiles[field.name].name}
              </p>
            )}
          </div>
        )
      
      default:
        return (
          <Input
            type="text"
            value={value}
            onChange={(e) => handleInputChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            required={isRequired}
          />
        )
    }
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
          <p>Loading form...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="max-w-md">
          <CardContent className="p-6 text-center">
            <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
            <h2 className="text-xl font-semibold mb-2">Form Not Available</h2>
            <p className="text-gray-600">{error}</p>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (isSubmitted) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="max-w-md">
          <CardContent className="p-6 text-center">
            <CheckCircle className="h-12 w-12 text-green-500 mx-auto mb-4" />
            <h2 className="text-xl font-semibold mb-2">Thank You!</h2>
            <p className="text-gray-600">
              Your form has been submitted successfully. We'll get back to you soon!
            </p>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (!form) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p>Form not found</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4">
        <Card>
          <CardHeader className="text-center">
            <CardTitle className="text-2xl">{form.name}</CardTitle>
            {form.description && (
              <CardDescription className="text-lg">{form.description}</CardDescription>
            )}
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {form.fields
                .sort((a, b) => a.order - b.order)
                .map((field) => (
                  <div key={field.id} className="space-y-2">
                    <Label htmlFor={field.name} className="text-base">
                      {field.label}
                      {field.required && <span className="text-red-500 ml-1">*</span>}
                    </Label>
                    {renderField(field)}
                    {field.helpText && (
                      <p className="text-sm text-gray-500">{field.helpText}</p>
                    )}
                  </div>
                ))}
              
              {error && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                  <div className="flex items-center">
                    <AlertCircle className="h-5 w-5 text-red-500 mr-2" />
                    <p className="text-red-700">{error}</p>
                  </div>
                </div>
              )}

              <div className="pt-4">
                <Button
                  type="submit"
                  className="w-full"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Submitting...
                    </>
                  ) : (
                    'Submit Form'
                  )}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
