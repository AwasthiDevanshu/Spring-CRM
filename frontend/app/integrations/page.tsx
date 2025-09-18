'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '@/hooks/use-auth'
import { useRouter } from 'next/navigation'
import { AppLayout } from '@/components/app-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Switch } from '@/components/ui/switch'
import { useToast } from '@/hooks/use-toast'
import { 
  MessageSquare,
  Facebook,
  Mail,
  Phone,
  Globe,
  Zap,
  Settings,
  Plus,
  ExternalLink,
  CheckCircle,
  XCircle,
  AlertCircle
} from 'lucide-react'

const integrations = [
  {
    id: 'facebook-ads',
    name: 'Facebook Ads',
    description: 'Import leads directly from Facebook Ad campaigns',
    icon: Facebook,
    status: 'available',
    enabled: false,
    category: 'Lead Generation',
    features: ['Lead Import', 'Auto-sync', 'Campaign Tracking']
  },
  {
    id: 'google-ads',
    name: 'Google Ads',
    description: 'Sync leads from Google Ads campaigns',
    icon: Globe,
    status: 'coming-soon',
    enabled: false,
    category: 'Lead Generation',
    features: ['Lead Import', 'Conversion Tracking', 'Campaign Analytics']
  },
  {
    id: 'mailchimp',
    name: 'Mailchimp',
    description: 'Sync contacts and email campaigns',
    icon: Mail,
    status: 'coming-soon',
    enabled: false,
    category: 'Email Marketing',
    features: ['Contact Sync', 'Campaign Management', 'Analytics']
  },
  {
    id: 'whatsapp',
    name: 'WhatsApp Business',
    description: 'Send messages and track conversations',
    icon: MessageSquare,
    status: 'coming-soon',
    enabled: false,
    category: 'Communication',
    features: ['Message Sending', 'Chat History', 'Automation']
  },
  {
    id: 'zapier',
    name: 'Zapier',
    description: 'Connect with 5000+ apps via Zapier',
    icon: Zap,
    status: 'coming-soon',
    enabled: false,
    category: 'Automation',
    features: ['Workflow Automation', 'Data Sync', 'Custom Triggers']
  },
  {
    id: 'twilio',
    name: 'Twilio',
    description: 'SMS and voice communication',
    icon: Phone,
    status: 'coming-soon',
    enabled: false,
    category: 'Communication',
    features: ['SMS Sending', 'Voice Calls', 'Call Recording']
  }
]

export default function IntegrationsPage() {
  const { user, isLoading, isAuthenticated } = useAuth()
  const router = useRouter()
  const { toast } = useToast()
  const [enabledIntegrations, setEnabledIntegrations] = useState<Record<string, boolean>>({})

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  if (isLoading) {
    return <div className="flex items-center justify-center h-64">Loading...</div>
  }

  if (!isAuthenticated) {
    return <div className="flex items-center justify-center h-64">Redirecting to login...</div>
  }

  const handleToggleIntegration = async (integrationId: string, enabled: boolean) => {
    if (integrations.find(i => i.id === integrationId)?.status !== 'available') {
      toast({
        title: "Coming Soon",
        description: "This integration is not yet available.",
        variant: "destructive",
      })
      return
    }

    setEnabledIntegrations(prev => ({
      ...prev,
      [integrationId]: enabled
    }))

    toast({
      title: enabled ? "Integration Enabled" : "Integration Disabled",
      description: `${integrations.find(i => i.id === integrationId)?.name} has been ${enabled ? 'enabled' : 'disabled'}.`,
    })
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'available':
        return <CheckCircle className="h-4 w-4 text-green-600" />
      case 'coming-soon':
        return <AlertCircle className="h-4 w-4 text-yellow-600" />
      default:
        return <XCircle className="h-4 w-4 text-red-600" />
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'available':
        return <Badge variant="outline" className="text-green-600">Available</Badge>
      case 'coming-soon':
        return <Badge variant="outline" className="text-yellow-600">Coming Soon</Badge>
      default:
        return <Badge variant="outline" className="text-red-600">Unavailable</Badge>
    }
  }

  const categories = [...new Set(integrations.map(i => i.category))]

  return (
    <AppLayout>
      <div className="space-y-6 p-6 pt-4">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Integrations</h1>
            <p className="text-muted-foreground">
              Connect your CRM with external services and platforms
            </p>
          </div>
          <Button>
            <Plus className="h-4 w-4 mr-2" />
            Request Integration
          </Button>
        </div>

        {/* Integration Categories */}
        {categories.map((category) => (
          <div key={category} className="space-y-4">
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-semibold">{category}</h2>
              <Badge variant="secondary">
                {integrations.filter(i => i.category === category).length} integrations
              </Badge>
            </div>
            
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {integrations
                .filter(integration => integration.category === category)
                .map((integration) => {
                  const Icon = integration.icon
                  const isEnabled = enabledIntegrations[integration.id] || false
                  
                  return (
                    <Card key={integration.id} className="relative">
                      <CardHeader className="pb-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <div className="p-2 rounded-lg bg-muted">
                              <Icon className="h-6 w-6" />
                            </div>
                            <div>
                              <CardTitle className="text-lg">{integration.name}</CardTitle>
                              {getStatusBadge(integration.status)}
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            {getStatusIcon(integration.status)}
                            <Switch
                              checked={isEnabled}
                              onCheckedChange={(checked) => handleToggleIntegration(integration.id, checked)}
                              disabled={integration.status !== 'available'}
                            />
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <CardDescription>
                          {integration.description}
                        </CardDescription>
                        
                        <div className="space-y-2">
                          <h4 className="text-sm font-medium">Features:</h4>
                          <div className="flex flex-wrap gap-1">
                            {integration.features.map((feature, index) => (
                              <Badge key={index} variant="outline" className="text-xs">
                                {feature}
                              </Badge>
                            ))}
                          </div>
                        </div>
                        
                        {integration.status === 'available' && (
                          <div className="flex gap-2 pt-2">
                            <Button size="sm" variant="outline" className="flex-1">
                              <Settings className="h-4 w-4 mr-1" />
                              Configure
                            </Button>
                            <Button size="sm" variant="outline">
                              <ExternalLink className="h-4 w-4" />
                            </Button>
                          </div>
                        )}
                        
                        {integration.status === 'coming-soon' && (
                          <div className="pt-2">
                            <Button size="sm" variant="outline" className="w-full" disabled>
                              Coming Soon
                            </Button>
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  )
                })}
            </div>
          </div>
        ))}

        {/* Integration Stats */}
        <Card>
          <CardHeader>
            <CardTitle>Integration Overview</CardTitle>
            <CardDescription>
              Summary of your active integrations and data sync status
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-6 md:grid-cols-3">
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">Active Integrations</p>
                <p className="text-2xl font-bold">
                  {Object.values(enabledIntegrations).filter(Boolean).length}
                </p>
              </div>
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">Available Integrations</p>
                <p className="text-2xl font-bold">
                  {integrations.filter(i => i.status === 'available').length}
                </p>
              </div>
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">Last Sync</p>
                <p className="text-2xl font-bold">
                  {Object.values(enabledIntegrations).some(Boolean) ? 'Just now' : 'Never'}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
