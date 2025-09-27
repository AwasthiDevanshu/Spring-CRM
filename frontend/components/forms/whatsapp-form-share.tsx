'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { 
  MessageCircle, 
  Send, 
  Copy, 
  CheckCircle,
  ExternalLink,
  Phone,
  Clock,
  AlertCircle
} from 'lucide-react'

interface WhatsAppFormShareProps {
  accessId: number
  formName: string
  leadName?: string
  contactName?: string
  phone?: string
  onShare?: () => void
}

export function WhatsAppFormShare({ 
  accessId, 
  formName, 
  leadName, 
  contactName, 
  phone,
  onShare 
}: WhatsAppFormShareProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [whatsAppUrl, setWhatsAppUrl] = useState<string>('')
  const [isLoading, setIsLoading] = useState(false)
  const [copied, setCopied] = useState(false)
  const [customMessage, setCustomMessage] = useState('')

  const entityName = leadName || contactName || 'Customer'

  const handleGenerateWhatsAppUrl = async () => {
    setIsLoading(true)
    try {
      const response = await fetch(`/api/custom-forms/access/${accessId}/whatsapp-url`)
      const data = await response.json()
      
      if (data.whatsAppUrl) {
        setWhatsAppUrl(data.whatsAppUrl)
      } else {
        setWhatsAppUrl('')
      }
    } catch (error) {
      console.error('Error generating WhatsApp URL:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleCopyUrl = () => {
    if (whatsAppUrl) {
      navigator.clipboard.writeText(whatsAppUrl)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  const handleOpenWhatsApp = () => {
    if (whatsAppUrl) {
      window.open(whatsAppUrl, '_blank', 'noopener,noreferrer')
      onShare?.()
    }
  }

  const defaultMessage = `👋 Hi ${entityName}!

📋 *${formName}*

Please fill out this form at your convenience. This will help us better understand your needs and provide you with the best possible service.

If you have any questions, feel free to reach out!

Best regards,
Your Sales Team`

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" className="flex items-center gap-2">
          <MessageCircle className="h-4 w-4" />
          Share via WhatsApp
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <MessageCircle className="h-5 w-5" />
            Share Form via WhatsApp
          </DialogTitle>
          <DialogDescription>
            Send the form to {entityName} via WhatsApp
          </DialogDescription>
        </DialogHeader>
        
        <div className="space-y-4">
          {/* Contact Info */}
          <div className="bg-gray-50 p-3 rounded-lg">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium">{entityName}</p>
                {phone && (
                  <p className="text-sm text-gray-600 flex items-center gap-1">
                    <Phone className="h-3 w-3" />
                    {phone}
                  </p>
                )}
              </div>
              <Badge variant="outline">
                {leadName ? 'Lead' : 'Contact'}
              </Badge>
            </div>
          </div>

          {/* Message Preview */}
          <div className="space-y-2">
            <Label>Message Preview</Label>
            <div className="bg-green-50 p-3 rounded-lg border">
              <Textarea
                value={customMessage || defaultMessage}
                onChange={(e) => setCustomMessage(e.target.value)}
                placeholder="Enter your custom message..."
                rows={8}
                className="border-0 bg-transparent resize-none"
              />
            </div>
          </div>

          {/* Actions */}
          <div className="flex flex-col gap-2">
            {!whatsAppUrl ? (
              <Button 
                onClick={handleGenerateWhatsAppUrl}
                disabled={isLoading}
                className="w-full"
              >
                {isLoading ? (
                  <>
                    <Clock className="h-4 w-4 mr-2 animate-spin" />
                    Generating...
                  </>
                ) : (
                  <>
                    <MessageCircle className="h-4 w-4 mr-2" />
                    Generate WhatsApp Link
                  </>
                )}
              </Button>
            ) : (
              <div className="space-y-2">
                <div className="flex gap-2">
                  <Button 
                    onClick={handleOpenWhatsApp}
                    className="flex-1 bg-green-600 hover:bg-green-700"
                  >
                    <Send className="h-4 w-4 mr-2" />
                    Open WhatsApp
                  </Button>
                  <Button 
                    onClick={handleCopyUrl}
                    variant="outline"
                    className="flex-1"
                  >
                    {copied ? (
                      <>
                        <CheckCircle className="h-4 w-4 mr-2" />
                        Copied!
                      </>
                    ) : (
                      <>
                        <Copy className="h-4 w-4 mr-2" />
                        Copy Link
                      </>
                    )}
                  </Button>
                </div>
                
                <div className="text-xs text-gray-500 bg-gray-50 p-2 rounded">
                  <p className="flex items-center gap-1">
                    <ExternalLink className="h-3 w-3" />
                    WhatsApp will open with the pre-filled message
                  </p>
                </div>
              </div>
            )}

            {!phone && (
              <div className="flex items-center gap-2 text-amber-600 bg-amber-50 p-2 rounded">
                <AlertCircle className="h-4 w-4" />
                <span className="text-sm">No phone number available for WhatsApp sharing</span>
              </div>
            )}
          </div>

          {/* Form Info */}
          <div className="text-xs text-gray-500 space-y-1">
            <p>• Form will be accessible via the shared link</p>
            <p>• Lead/Contact will receive a personalized message</p>
            <p>• Form submission will be tracked automatically</p>
            <p>• Follow-up activities will be created</p>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}

export default WhatsAppFormShare
