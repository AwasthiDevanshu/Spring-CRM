'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '@/hooks/use-auth'
import { useRouter } from 'next/navigation'
import { getAuthHeader } from '@/lib/auth'
import { AppLayout } from '@/components/app-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { 
  Upload, 
  Download, 
  FileText, 
  Users, 
  Building, 
  Target,
  CheckCircle,
  AlertCircle,
  XCircle,
  FileSpreadsheet,
  Database,
  CloudUpload
} from 'lucide-react'
import { useToast } from '@/hooks/use-toast'

interface ImportResult {
  id: string
  fileName: string
  type: 'leads' | 'contacts' | 'deals'
  status: 'pending' | 'processing' | 'completed' | 'failed'
  totalRecords: number
  processedRecords: number
  successRecords: number
  errorRecords: number
  errors: string[]
  createdAt: string
  completedAt?: string
}

const importTypes = [
  {
    id: 'leads',
    name: 'Leads',
    description: 'Import leads from CSV, Excel, or other sources',
    icon: Users,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
    supportedFormats: ['CSV', 'Excel', 'JSON']
  },
  {
    id: 'contacts',
    name: 'Contacts',
    description: 'Import contact information and details',
    icon: Building,
    color: 'text-green-600',
    bgColor: 'bg-green-50 dark:bg-green-950',
    supportedFormats: ['CSV', 'Excel', 'JSON']
  },
  {
    id: 'deals',
    name: 'Deals',
    description: 'Import sales opportunities and deals',
    icon: Target,
    color: 'text-purple-600',
    bgColor: 'bg-purple-50 dark:bg-purple-950',
    supportedFormats: ['CSV', 'Excel', 'JSON']
  }
]

export default function ImportPage() {
  const { user, isAuthenticated, isLoading } = useAuth()
  const router = useRouter()
  const { toast } = useToast()
  const [activeTab, setActiveTab] = useState('leads')
  const [isUploading, setIsUploading] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [importResults, setImportResults] = useState<ImportResult[]>([])
  const [dragActive, setDragActive] = useState(false)

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  useEffect(() => {
    if (isAuthenticated) {
      fetchImportHistory()
    }
  }, [isAuthenticated])

  const fetchImportHistory = async () => {
    try {
      // Mock data for now - replace with actual API call
      const mockResults: ImportResult[] = [
        {
          id: '1',
          fileName: 'leads_import_2024.csv',
          type: 'leads',
          status: 'completed',
          totalRecords: 150,
          processedRecords: 150,
          successRecords: 147,
          errorRecords: 3,
          errors: ['Invalid email format on row 23', 'Missing required field on row 45', 'Duplicate entry on row 78'],
          createdAt: '2024-01-15T10:30:00Z',
          completedAt: '2024-01-15T10:32:15Z'
        },
        {
          id: '2',
          fileName: 'contacts_bulk_import.xlsx',
          type: 'contacts',
          status: 'processing',
          totalRecords: 500,
          processedRecords: 250,
          successRecords: 245,
          errorRecords: 5,
          errors: [],
          createdAt: '2024-01-15T11:00:00Z'
        }
      ]
      setImportResults(mockResults)
    } catch (error) {
      console.error('Error fetching import history:', error)
    }
  }

  const downloadTemplate = (type: string) => {
    const templateFileName = `${type}_template.csv`
    const templatePath = `/templates/${templateFileName}`
    
    // Create a temporary link element to trigger download
    const link = document.createElement('a')
    link.href = templatePath
    link.download = templateFileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    toast({
      title: 'Template Downloaded',
      description: `${templateFileName} has been downloaded successfully.`,
    })
  }

  const handleFileUpload = async (file: File, type: string) => {
    if (!file) return

    setIsUploading(true)
    setUploadProgress(0)

    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('entityType', type === 'leads' ? 'LEAD' : type === 'contacts' ? 'CONTACT' : 'DEAL')
      formData.append('companyId', String(user?.companyId || 1))

      const response = await fetch('http://localhost:8080/crm/api/import/csv', {
        method: 'POST',
        headers: {
          'Authorization': getAuthHeader() || ''
        },
        body: formData
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Upload failed')
      }

      const result = await response.json()
      setUploadProgress(100)

      const newImport: ImportResult = {
        id: result.id,
        fileName: result.fileName,
        type: type as 'leads' | 'contacts' | 'deals',
        status: result.status,
        totalRecords: result.totalRecords,
        processedRecords: result.processedRecords,
        successRecords: result.successRecords,
        errorRecords: result.errorRecords,
        errors: result.errors,
        createdAt: result.createdAt,
        completedAt: result.completedAt
      }

      setImportResults(prev => [newImport, ...prev])

      toast({
        title: "Import completed",
        description: `Successfully imported ${result.successRecords} out of ${result.totalRecords} records.`,
      })

      if (result.errorRecords > 0) {
        toast({
          title: "Import completed with errors",
          description: `${result.errorRecords} records failed to import. Check the details below.`,
          variant: "destructive",
        })
      }

    } catch (error) {
      console.error('Upload failed:', error)
      toast({
        title: "Upload failed",
        description: "There was an error uploading your file. Please try again.",
        variant: "destructive"
      })
    } finally {
      setIsUploading(false)
      setUploadProgress(0)
    }
  }

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true)
    } else if (e.type === 'dragleave') {
      setDragActive(false)
    }
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileUpload(e.dataTransfer.files[0], activeTab)
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="h-4 w-4 text-green-600" />
      case 'processing':
        return <AlertCircle className="h-4 w-4 text-yellow-600" />
      case 'failed':
        return <XCircle className="h-4 w-4 text-red-600" />
      default:
        return <AlertCircle className="h-4 w-4 text-gray-600" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800'
      case 'processing':
        return 'bg-yellow-100 text-yellow-800'
      case 'failed':
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
      <div className="container mx-auto p-6 pt-4 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Import Data</h1>
          <p className="text-muted-foreground">
            Import leads, contacts, and deals from CSV, Excel, or other file formats
          </p>
        </div>
        <Button 
          variant="outline" 
          size="sm"
          onClick={() => downloadTemplate(activeTab)}
        >
          <Download className="h-4 w-4 mr-2" />
          Download Template
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="grid w-full grid-cols-3">
          {importTypes.map((type) => (
            <TabsTrigger key={type.id} value={type.id} className="flex items-center gap-2">
              <type.icon className="h-4 w-4" />
              {type.name}
            </TabsTrigger>
          ))}
        </TabsList>

        {importTypes.map((type) => (
          <TabsContent key={type.id} value={type.id} className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <type.icon className={`h-5 w-5 ${type.color}`} />
                  Import {type.name}
                </CardTitle>
                <CardDescription>
                  {type.description}. Supported formats: {type.supportedFormats.join(', ')}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div
                  className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
                    dragActive ? 'border-primary bg-primary/5' : 'border-muted-foreground/25'
                  }`}
                  onDragEnter={handleDrag}
                  onDragLeave={handleDrag}
                  onDragOver={handleDrag}
                  onDrop={handleDrop}
                >
                  <CloudUpload className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                  <h3 className="text-lg font-semibold mb-2">Drop your file here</h3>
                  <p className="text-muted-foreground mb-4">
                    or click to browse and select a file
                  </p>
                  <Button
                    onClick={() => {
                      const input = document.createElement('input')
                      input.type = 'file'
                      input.accept = '.csv,.xlsx,.xls,.json'
                      input.onchange = (e) => {
                        const file = (e.target as HTMLInputElement).files?.[0]
                        if (file) handleFileUpload(file, type.id)
                      }
                      input.click()
                    }}
                    disabled={isUploading}
                  >
                    <Upload className="h-4 w-4 mr-2" />
                    Choose File
                  </Button>
                </div>

                {isUploading && (
                  <div className="mt-4 space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span>Uploading...</span>
                      <span>{uploadProgress}%</span>
                    </div>
                    <Progress value={uploadProgress} className="w-full" />
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Import History */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Database className="h-5 w-5" />
                  Import History
                </CardTitle>
                <CardDescription>
                  Recent import activities and their status
                </CardDescription>
              </CardHeader>
              <CardContent>
                {importResults.filter(result => result.type === type.id).length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">
                    No imports yet for {type.name.toLowerCase()}
                  </div>
                ) : (
                  <div className="space-y-4">
                    {importResults
                      .filter(result => result.type === type.id)
                      .map((result) => (
                        <div key={result.id} className="border rounded-lg p-4 space-y-3">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                              <FileSpreadsheet className="h-5 w-5 text-muted-foreground" />
                              <div>
                                <h4 className="font-medium">{result.fileName}</h4>
                                <p className="text-sm text-muted-foreground">
                                  {new Date(result.createdAt).toLocaleString()}
                                </p>
                              </div>
                            </div>
                            <Badge className={getStatusColor(result.status)}>
                              <span className="flex items-center gap-1">
                                {getStatusIcon(result.status)}
                                {result.status.charAt(0).toUpperCase() + result.status.slice(1)}
                              </span>
                            </Badge>
                          </div>

                          {result.status === 'completed' && (
                            <div className="grid grid-cols-3 gap-4 text-sm">
                              <div className="text-center">
                                <div className="font-semibold text-green-600">{result.successRecords}</div>
                                <div className="text-muted-foreground">Successful</div>
                              </div>
                              <div className="text-center">
                                <div className="font-semibold text-red-600">{result.errorRecords}</div>
                                <div className="text-muted-foreground">Failed</div>
                              </div>
                              <div className="text-center">
                                <div className="font-semibold">{result.totalRecords}</div>
                                <div className="text-muted-foreground">Total</div>
                              </div>
                            </div>
                          )}

                          {result.errors.length > 0 && (
                            <div className="space-y-2">
                              <h5 className="text-sm font-medium text-red-600">Errors:</h5>
                              <ul className="text-sm text-muted-foreground space-y-1">
                                {result.errors.map((error, index) => (
                                  <li key={index} className="flex items-start gap-2">
                                    <XCircle className="h-3 w-3 text-red-500 mt-0.5 flex-shrink-0" />
                                    {error}
                                  </li>
                                ))}
                              </ul>
                            </div>
                          )}
                        </div>
                      ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        ))}
      </Tabs>
      </div>
    </AppLayout>
  )
}
