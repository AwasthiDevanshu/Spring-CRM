'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { CheckCircle, AlertCircle, Loader2, Upload, X } from 'lucide-react'
import { useToast } from '@/hooks/use-toast'

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

interface CustomForm {
  id: number
  name: string
  description?: string
  status: string
  fields: FormField[]
  successMessage?: string
  redirectUrl?: string
}

interface FormSubmission {
  [key: string]: any
}

export default function PublicFormSubmissionPage() {
  const params = useParams()
  const router = useRouter()
  const { toast } = useToast()
  const [form, setForm] = useState<CustomForm | null>(null)
  const [submission, setSubmission] = useState<FormSubmission>({})
  const [files, setFiles] = useState<{ [key: string]: File }>({})
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSubmitted, setIsSubmitted] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const token = params.token as string

  useEffect(() => {
    if (token) {
      fetchForm()
    }
  }, [token])

  const fetchForm = async () => {
    try {
      setIsLoading(true)
      const response = await fetch(`/api/custom-forms/public/access/${token}`)
      
      if (!response.ok) {
        if (response.status === 404) {
          setError('Form not found or access token is invalid')
        } else if (response.status === 410) {
          setError('This form has expired and is no longer available')
        } else {
          setError('Failed to load form. Please try again.')
        }
        return
      }

      const formData = await response.json()
      setForm(formData)
      
      // Initialize submission object with empty values
      const initialSubmission: FormSubmission = {}
      formData.fields?.forEach((field: FormField) => {
        if (field.type === 'CHECKBOX') {
          initialSubmission[field.name] = []
        } else {
          initialSubmission[field.name] = ''
        }
      })
      setSubmission(initialSubmission)
    } catch (err) {
      console.error('Error fetching form:', err)
      setError('Failed to load form. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleInputChange = (fieldName: string, value: any) => {
    setSubmission(prev => ({
      ...prev,
      [fieldName]: value
    }))
  }

  const handleFileChange = (fieldName: string, file: File | null) => {
    if (file) {
      setFiles(prev => ({
        ...prev,
        [fieldName]: file
      }))
    } else {
      setFiles(prev => {
        const newFiles = { ...prev }
        delete newFiles[fieldName]
        return newFiles
      })
    }
  }

  const validateField = (field: FormField, value: any): string | null => {
    if (field.isRequired && (!value || (Array.isArray(value) && value.length === 0))) {
      return `${field.label} is required`
    }

    if (value && field.validation) {
      if (field.validation.minLength && value.length < field.validation.minLength) {
        return `${field.label} must be at least ${field.validation.minLength} characters`
      }
      if (field.validation.maxLength && value.length > field.validation.maxLength) {
        return `${field.label} must be no more than ${field.validation.maxLength} characters`
      }
      if (field.validation.pattern && !new RegExp(field.validation.pattern).test(value)) {
        return `${field.label} format is invalid`
      }
      if (field.validation.min && Number(value) < field.validation.min) {
        return `${field.label} must be at least ${field.validation.min}`
      }
      if (field.validation.max && Number(value) > field.validation.max) {
        return `${field.label} must be no more than ${field.validation.max}`
      }
    }

    return null
  }

  const validateForm = (): boolean => {
    if (!form) return false

    for (const field of form.fields) {
      const value = submission[field.name]
      const error = validateField(field, value)
      if (error) {
        toast({
          title: "Validation Error",
          description: error,
          variant: "destructive"
        })
        return false
      }
    }

    return true
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!form || !validateForm()) return

    try {
      setIsSubmitting(true)

      // Prepare form data with files
      const formData = new FormData()
      
      // Add form fields
      Object.entries(submission).forEach(([key, value]) => {
        if (Array.isArray(value)) {
          formData.append(key, JSON.stringify(value))
        } else {
          formData.append(key, String(value))
        }
      })

      // Add files
      Object.entries(files).forEach(([fieldName, file]) => {
        formData.append(`file_${fieldName}`, file)
      })

      const response = await fetch('/api/custom-forms/public/submit', {
        method: 'POST',
        body: formData,
        headers: {
          'X-Access-Token': token
        }
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Failed to submit form')
      }

      setIsSubmitted(true)
      toast({
        title: "Form Submitted Successfully",
        description: form.successMessage || "Thank you for your submission!",
      })

      // Redirect if specified
      if (form.redirectUrl) {
        setTimeout(() => {
          window.location.href = form.redirectUrl!
        }, 2000)
      }
    } catch (err) {
      console.error('Error submitting form:', err)
      toast({
        title: "Submission Failed",
        description: err instanceof Error ? err.message : "Failed to submit form. Please try again.",
        variant: "destructive"
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const renderField = (field: FormField) => {
    const value = submission[field.name] || ''
    const error = validateField(field, value)

    const commonProps = {
      id: field.name,
      name: field.name,
      placeholder: field.placeholder,
      required: field.isRequired,
      className: `w-full ${error ? 'border-red-500' : ''}`,
      onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        handleInputChange(field.name, e.target.value)
      }
    }

    switch (field.type) {
      case 'TEXT':
      case 'EMAIL':
      case 'PHONE':
      case 'URL':
        return (
          <div>
            <Input {...commonProps} type={field.type.toLowerCase()} value={value} />
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
        )
      
      case 'NUMBER':
        return (
          <div>
            <Input 
              {...commonProps} 
              type="number" 
              value={value}
              min={field.validation?.min}
              max={field.validation?.max}
            />
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
        )
      
      case 'TEXTAREA':
        return (
          <div>
            <Textarea 
              {...commonProps} 
              value={value}
              rows={3}
            />
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
        )
      
      case 'SELECT':
        return (
          <div>
            <select {...commonProps} value={value} className={`w-full p-2 border rounded-md ${error ? 'border-red-500' : ''}`}>
              <option value="">Select an option</option>
              {field.options?.map((option, index) => (
                <option key={index} value={option}>{option}</option>
              ))}
            </select>
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
        )
      
      case 'RADIO':
        return (
          <div>
            <div className="space-y-2">
              {field.options?.map((option, index) => (
                <label key={index} className="flex items-center space-x-2">
                  <input 
                    type="radio" 
                    name={field.name} 
                    value={option}
                    checked={value === option}
                    onChange={(e) => handleInputChange(field.name, e.target.value)}
                    className="text-blue-600"
                  />
                  <span>{option}</span>
                </label>
              ))}
            </div>
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
        )
      
      case 'CHECKBOX':
        return (
          <div>
            <div className="space-y-2">
              {field.options?.map((option, index) => (
                <label key={index} className="flex items-center space-x-2">
                  <input 
                    type="checkbox" 
                    value={option}
                    checked={Array.isArray(value) && value.includes(option)}
                    onChange={(e) => {
                      const currentValues = Array.isArray(value) ? value : []
                      if (e.target.checked) {
                        handleInputChange(field.name, [...currentValues, option])
                      } else {
                        handleInputChange(field.name, currentValues.filter(v => v !== option))
                      }
                    }}
                    className="text-blue-600"
                  />
                  <span>{option}</span>
                </label>
              ))}
            </div>
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
        )
      
      case 'DATE':
        return (
          <div>
            <Input {...commonProps} type="date" value={value} />
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
        )
      
      case 'FILE':
      case 'IMAGE':
        return (
          <div>
            <div className="flex items-center space-x-2">
              <Input
                type="file"
                accept={field.type === 'IMAGE' ? 'image/*' : '*'}
                onChange={(e) => handleFileChange(field.name, e.target.files?.[0] || null)}
                className="flex-1"
              />
              {files[field.name] && (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => handleFileChange(field.name, null)}
                >
                  <X className="h-4 w-4" />
                </Button>
              )}
            </div>
            {files[field.name] && (
              <p className="text-sm text-gray-600 mt-1">
                Selected: {files[field.name].name}
              </p>
            )}
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
        )
      
      default:
        return (
          <div>
            <Input {...commonProps} value={value} />
            {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
          </div>
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
        <Card className="w-full max-w-md">
          <CardContent className="p-6">
            <div className="text-center">
              <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
              <h2 className="text-xl font-semibold mb-2">Form Not Available</h2>
              <p className="text-gray-600 mb-4">{error}</p>
              <Button onClick={() => router.back()}>
                Go Back
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (isSubmitted) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardContent className="p-6">
            <div className="text-center">
              <CheckCircle className="h-12 w-12 text-green-500 mx-auto mb-4" />
              <h2 className="text-xl font-semibold mb-2">Form Submitted Successfully!</h2>
              <p className="text-gray-600 mb-4">
                {form?.successMessage || "Thank you for your submission!"}
              </p>
              {form?.redirectUrl && (
                <p className="text-sm text-gray-500">
                  You will be redirected shortly...
                </p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (!form) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="h-8 w-8 text-red-500 mx-auto mb-4" />
          <p>Form not found</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4">
        <Card>
          <CardHeader>
            <CardTitle className="text-2xl">{form.name}</CardTitle>
            {form.description && (
              <CardDescription className="text-base">
                {form.description}
              </CardDescription>
            )}
            <div className="flex items-center gap-2 mt-2">
              <Badge variant="secondary">{form.status}</Badge>
              <span className="text-sm text-gray-500">
                {form.fields?.length || 0} field{form.fields?.length !== 1 ? 's' : ''}
              </span>
            </div>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {form.fields
                ?.sort((a, b) => a.order - b.order)
                .map((field) => (
                  <div key={field.id} className="space-y-2">
                    <Label htmlFor={field.name} className="text-base font-medium">
                      {field.label}
                      {field.isRequired && <span className="text-red-500 ml-1">*</span>}
                    </Label>
                    {field.helpText && (
                      <p className="text-sm text-gray-600">{field.helpText}</p>
                    )}
                    {renderField(field)}
                  </div>
                ))}

              <div className="pt-6 border-t">
                <Button 
                  type="submit" 
                  className="w-full" 
                  size="lg"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin mr-2" />
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
