/**
 * WhatsApp utility functions for opening chat windows
 */

export interface WhatsAppContact {
  name: string
  phone: string
  company?: string
  email?: string
}

/**
 * Format phone number for WhatsApp (remove all non-digits and ensure proper format)
 */
export function formatPhoneForWhatsApp(phone: string): string {
  // Remove all non-digit characters
  const digitsOnly = phone.replace(/\D/g, '')
  
  // If phone starts with country code, use as is
  if (digitsOnly.length >= 10) {
    return digitsOnly
  }
  
  // If phone is missing country code, assume US (+1)
  if (digitsOnly.length === 10) {
    return `1${digitsOnly}`
  }
  
  return digitsOnly
}

/**
 * Generate WhatsApp message with contact context
 */
export function generateWhatsAppMessage(contact: WhatsAppContact, messageType: 'greeting' | 'followup' | 'custom' = 'greeting'): string {
  const { name, company } = contact
  
  const baseMessage = `Hi ${name}!`
  
  switch (messageType) {
    case 'greeting':
      return `${baseMessage} I hope you're doing well. I wanted to reach out regarding our conversation about ${company || 'your business needs'}.`
    
    case 'followup':
      return `${baseMessage} I wanted to follow up on our previous discussion about ${company || 'your requirements'}. Do you have a moment to chat?`
    
    case 'custom':
      return baseMessage
    
    default:
      return baseMessage
  }
}

/**
 * Open WhatsApp chat in new tab
 */
export function openWhatsAppChat(contact: WhatsAppContact, messageType: 'greeting' | 'followup' | 'custom' = 'greeting', customMessage?: string): void {
  const formattedPhone = formatPhoneForWhatsApp(contact.phone)
  
  if (!formattedPhone) {
    console.error('Invalid phone number for WhatsApp')
    return
  }
  
  let message = customMessage || generateWhatsAppMessage(contact, messageType)
  
  // Encode the message for URL
  const encodedMessage = encodeURIComponent(message)
  
  // Create WhatsApp URL
  const whatsappUrl = `https://wa.me/${formattedPhone}?text=${encodedMessage}`
  
  // Open in new tab
  window.open(whatsappUrl, '_blank', 'noopener,noreferrer')
}

/**
 * Check if phone number is valid for WhatsApp
 */
export function isValidPhoneForWhatsApp(phone: string): boolean {
  const digitsOnly = phone.replace(/\D/g, '')
  return digitsOnly.length >= 10
}

/**
 * Get WhatsApp status (available, invalid, no phone)
 */
export function getWhatsAppStatus(phone?: string): 'available' | 'invalid' | 'no_phone' {
  if (!phone) return 'no_phone'
  if (!isValidPhoneForWhatsApp(phone)) return 'invalid'
  return 'available'
}

/**
 * Format phone number for display
 */
export function formatPhoneDisplay(phone: string): string {
  const digitsOnly = phone.replace(/\D/g, '')
  
  if (digitsOnly.length === 10) {
    // US format: (123) 456-7890
    return `(${digitsOnly.slice(0, 3)}) ${digitsOnly.slice(3, 6)}-${digitsOnly.slice(6)}`
  } else if (digitsOnly.length === 11 && digitsOnly.startsWith('1')) {
    // US format with country code: +1 (123) 456-7890
    return `+1 (${digitsOnly.slice(1, 4)}) ${digitsOnly.slice(4, 7)}-${digitsOnly.slice(7)}`
  } else if (digitsOnly.length > 10) {
    // International format: +1234567890
    return `+${digitsOnly}`
  }
  
  return phone // Return original if can't format
}
