"use client"

import { useEffect, useState, Suspense } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { useToast } from "@/hooks/use-toast"
import { Card, CardContent } from "@/components/ui/card"
import { CheckCircle, XCircle, Loader2 } from "lucide-react"

function AuthCallbackContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { toast } = useToast()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('')

  useEffect(() => {
    const token = searchParams.get('token')
    const error = searchParams.get('error')

    if (error) {
      setStatus('error')
      setMessage('Authentication failed. Please try again.')
      toast({
        title: "Authentication Error",
        description: error,
        variant: "destructive"
      })
      return
    }

    if (token) {
      // Store token and redirect to dashboard
      localStorage.setItem('token', token)
      
      // Get user info
      fetchUserInfo(token)
    } else {
      setStatus('error')
      setMessage('No authentication token received.')
    }
  }, [searchParams, toast])

  const fetchUserInfo = async (token: string) => {
    try {
      const response = await fetch('/crm/api/oauth2/user', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const user = await response.json()
        localStorage.setItem('user', JSON.stringify(user))
        localStorage.setItem('companyId', user.companyId.toString())
        
        setStatus('success')
        setMessage('Authentication successful! Redirecting...')
        
        toast({
          title: "Welcome!",
          description: `Hello ${user.firstName} ${user.lastName}`,
        })
        
        // Redirect to dashboard after a short delay
        setTimeout(() => {
          router.push('/dashboard')
        }, 2000)
      } else {
        throw new Error('Failed to fetch user info')
      }
    } catch (error) {
      console.error('Error fetching user info:', error)
      setStatus('error')
      setMessage('Failed to complete authentication.')
      
      toast({
        title: "Error",
        description: "Failed to complete authentication",
        variant: "destructive"
      })
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="w-full max-w-md">
        <CardContent className="p-6 text-center">
          {status === 'loading' && (
            <>
              <Loader2 className="h-12 w-12 animate-spin mx-auto mb-4 text-blue-600" />
              <h2 className="text-xl font-semibold mb-2">Authenticating...</h2>
              <p className="text-gray-600">Please wait while we complete your login.</p>
            </>
          )}
          
          {status === 'success' && (
            <>
              <CheckCircle className="h-12 w-12 mx-auto mb-4 text-green-600" />
              <h2 className="text-xl font-semibold mb-2 text-green-600">Success!</h2>
              <p className="text-gray-600">{message}</p>
            </>
          )}
          
          {status === 'error' && (
            <>
              <XCircle className="h-12 w-12 mx-auto mb-4 text-red-600" />
              <h2 className="text-xl font-semibold mb-2 text-red-600">Error</h2>
              <p className="text-gray-600">{message}</p>
              <button
                onClick={() => router.push('/login')}
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                Try Again
              </button>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

export default function AuthCallback() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="w-full max-w-md">
          <CardContent className="p-6 text-center">
            <Loader2 className="h-12 w-12 animate-spin mx-auto mb-4 text-blue-600" />
            <h2 className="text-xl font-semibold mb-2">Loading...</h2>
            <p className="text-gray-600">Please wait...</p>
          </CardContent>
        </Card>
      </div>
    }>
      <AuthCallbackContent />
    </Suspense>
  )
}
