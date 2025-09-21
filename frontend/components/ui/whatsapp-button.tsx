'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { 
  MessageCircle, 
  Phone, 
  CheckCircle, 
  XCircle, 
  AlertCircle,
  Send,
  Edit3
} from 'lucide-react'
import { 
  openWhatsAppChat, 
  getWhatsAppStatus, 
  formatPhoneDisplay, 
  type WhatsAppContact 
} from '@/lib/whatsapp-utils'

interface WhatsAppButtonProps {
  contact: WhatsAppContact
  variant?: 'default' | 'outline' | 'ghost' | 'link' | 'destructive' | 'secondary'
  size?: 'default' | 'sm' | 'lg' | 'icon'
  showLabel?: boolean
  className?: string
}

export function WhatsAppButton({ 
  contact, 
  variant = 'outline', 
  size = 'sm', 
  showLabel = true,
  className = ''
}: WhatsAppButtonProps) {
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [customMessage, setCustomMessage] = useState('')
  const [messageType, setMessageType] = useState<'greeting' | 'followup' | 'custom'>('greeting')

  const whatsappStatus = getWhatsAppStatus(contact.phone)

  const handleQuickChat = (type: 'greeting' | 'followup') => {
    openWhatsAppChat(contact, type)
  }

  const handleCustomChat = () => {
    openWhatsAppChat(contact, 'custom', customMessage)
    setIsDialogOpen(false)
    setCustomMessage('')
  }

  const getStatusIcon = () => {
    switch (whatsappStatus) {
      case 'available':
        return <CheckCircle className="h-4 w-4 text-green-500" />
      case 'invalid':
        return <XCircle className="h-4 w-4 text-red-500" />
      case 'no_phone':
        return <AlertCircle className="h-4 w-4 text-yellow-500" />
      default:
        return <AlertCircle className="h-4 w-4 text-gray-500" />
    }
  }

  const getStatusText = () => {
    switch (whatsappStatus) {
      case 'available':
        return 'WhatsApp Available'
      case 'invalid':
        return 'Invalid Phone Number'
      case 'no_phone':
        return 'No Phone Number'
      default:
        return 'Unknown Status'
    }
  }

  const getStatusColor = () => {
    switch (whatsappStatus) {
      case 'available':
        return 'bg-green-100 text-green-800'
      case 'invalid':
        return 'bg-red-100 text-red-800'
      case 'no_phone':
        return 'bg-yellow-100 text-yellow-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  if (whatsappStatus === 'no_phone') {
    return (
      <Button
        variant="ghost"
        size={size}
        disabled
        className={`opacity-50 ${className}`}
        title="No phone number available"
      >
        <MessageCircle className="h-4 w-4" />
        {showLabel && <span className="ml-2">WhatsApp</span>}
      </Button>
    )
  }

  return (
    <div className="flex items-center gap-2">
      {/* Single WhatsApp Button with Dropdown */}
      {whatsappStatus === 'available' && (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant={variant}
              size={size}
              className={`${className}`}
              title="WhatsApp Actions"
            >
              <MessageCircle className="h-4 w-4" />
              {showLabel && <span className="ml-2">WhatsApp</span>}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleQuickChat('greeting')}>
              <MessageCircle className="mr-2 h-4 w-4" />
              Send Greeting
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => handleQuickChat('followup')}>
              <Send className="mr-2 h-4 w-4" />
              Send Follow-up
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setIsDialogOpen(true)}>
              <Edit3 className="mr-2 h-4 w-4" />
              Custom Message
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      )}

      {/* Custom Message Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <MessageCircle className="h-5 w-5" />
              WhatsApp Message
            </DialogTitle>
            <DialogDescription>
              Send a custom message to {contact.name}
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4">
            {/* Contact Info */}
            <div className="bg-gray-50 p-3 rounded-lg">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium">{contact.name}</p>
                  <p className="text-sm text-gray-600">{contact.company}</p>
                  <p className="text-sm text-gray-500 flex items-center gap-1">
                    <Phone className="h-3 w-3" />
                    {formatPhoneDisplay(contact.phone)}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  {getStatusIcon()}
                  <Badge className={getStatusColor()}>
                    {getStatusText()}
                  </Badge>
                </div>
              </div>
            </div>

            {/* Message Type Selection */}
            <div className="space-y-2">
              <Label>Message Type</Label>
              <div className="flex gap-2">
                <Button
                  variant={messageType === 'greeting' ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setMessageType('greeting')}
                >
                  Greeting
                </Button>
                <Button
                  variant={messageType === 'followup' ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setMessageType('followup')}
                >
                  Follow-up
                </Button>
                <Button
                  variant={messageType === 'custom' ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setMessageType('custom')}
                >
                  Custom
                </Button>
              </div>
            </div>

            {/* Custom Message Input */}
            {messageType === 'custom' && (
              <div className="space-y-2">
                <Label htmlFor="customMessage">Custom Message</Label>
                <Textarea
                  id="customMessage"
                  value={customMessage}
                  onChange={(e) => setCustomMessage(e.target.value)}
                  placeholder="Type your custom message here..."
                  rows={4}
                />
              </div>
            )}

            {/* Preview */}
            {messageType !== 'custom' && (
              <div className="space-y-2">
                <Label>Message Preview</Label>
                <div className="bg-blue-50 p-3 rounded-lg">
                  <p className="text-sm">
                    {messageType === 'greeting' 
                      ? `Hi ${contact.name}! I hope you're doing well. I wanted to reach out regarding our conversation about ${contact.company || 'your business needs'}.`
                      : `Hi ${contact.name}! I wanted to follow up on our previous discussion about ${contact.company || 'your requirements'}. Do you have a moment to chat?`
                    }
                  </p>
                </div>
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                onClick={() => setIsDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleCustomChat}
                disabled={messageType === 'custom' && !customMessage.trim()}
              >
                <MessageCircle className="h-4 w-4 mr-2" />
                Send Message
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}

export default WhatsAppButton
