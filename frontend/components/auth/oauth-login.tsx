"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { useToast } from "@/hooks/use-toast"
import { useRouter } from "next/navigation"
import { Chrome, Mail } from "lucide-react"

interface OAuthProvider {
  name: string
  url: string
  icon: string
}

const OAuthLogin = () => {
  const [loading, setLoading] = useState<string | null>(null)
  const { toast } = useToast()
  const router = useRouter()

  const handleOAuthLogin = async (provider: string) => {
    try {
      setLoading(provider)
      
      // Redirect to OAuth provider
      window.location.href = `/oauth2/authorization/${provider}`
    } catch (error) {
      console.error('OAuth login error:', error)
      toast({
        title: "Error",
        description: "Failed to initiate OAuth login",
        variant: "destructive"
      })
    } finally {
      setLoading(null)
    }
  }

  const providers: OAuthProvider[] = [
    {
      name: "Google",
      url: "/oauth2/authorization/google",
      icon: "https://developers.google.com/identity/images/g-logo.png"
    },
    {
      name: "Microsoft",
      url: "/oauth2/authorization/microsoft", 
      icon: "https://upload.wikimedia.org/wikipedia/commons/4/44/Microsoft_logo.svg"
    }
  ]

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="text-center">
        <CardTitle className="text-2xl font-bold">Welcome to CRM</CardTitle>
        <CardDescription>
          Sign in with your enterprise account to continue
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {providers.map((provider) => (
          <Button
            key={provider.name}
            variant="outline"
            className="w-full h-12"
            onClick={() => handleOAuthLogin(provider.name.toLowerCase())}
            disabled={loading === provider.name.toLowerCase()}
          >
            {loading === provider.name.toLowerCase() ? (
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-900 mr-2" />
            ) : (
              <img
                src={provider.icon}
                alt={provider.name}
                className="h-4 w-4 mr-2"
              />
            )}
            Continue with {provider.name}
          </Button>
        ))}
        
        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <span className="w-full border-t" />
          </div>
          <div className="relative flex justify-center text-xs uppercase">
            <span className="bg-background px-2 text-muted-foreground">
              Enterprise Ready
            </span>
          </div>
        </div>
        
        <div className="text-center text-sm text-muted-foreground">
          <p>Secure authentication powered by OAuth 2.0 + OIDC</p>
          <p className="mt-1">SSO integration with Google and Microsoft</p>
        </div>
      </CardContent>
    </Card>
  )
}

export default OAuthLogin
