'use client'

import { useState, useEffect, useRef } from 'react'
import { usePathname } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { 
  MessageCircle, 
  X, 
  Send, 
  Bot, 
  User, 
  HelpCircle,
  Lightbulb,
  BookOpen,
  Phone,
  Mail,
  Calendar,
  DollarSign,
  Users,
  Activity,
  Settings
} from 'lucide-react'

interface Message {
  id: string
  type: 'user' | 'bot'
  content: string
  timestamp: Date
  suggestions?: string[]
}

interface FAQ {
  id: string
  question: string
  answer: string
  category: string
  keywords: string[]
}

const faqData: FAQ[] = [
  // General CRM Questions
  {
    id: '1',
    question: 'How do I create a new lead?',
    answer: 'To create a new lead, go to the Leads page and click the "Add Lead" button. Fill in the required information like name, email, phone, and company details.',
    category: 'Leads',
    keywords: ['create', 'lead', 'new', 'add']
  },
  {
    id: '2',
    question: 'How do I convert a lead to a contact?',
    answer: 'On the Leads page, click the three dots menu next to any lead and select "Convert to Contact". This will open a form to create a contact with the lead\'s information.',
    category: 'Leads',
    keywords: ['convert', 'contact', 'lead', 'change']
  },
  {
    id: '3',
    question: 'How do I create a deal?',
    answer: 'You can create a deal from the Deals page or from a specific lead/contact. Click "Add Deal" or use the "Create Deal" option in the lead/contact dropdown menu.',
    category: 'Deals',
    keywords: ['create', 'deal', 'new', 'add', 'sales']
  },
  {
    id: '4',
    question: 'How do I track activities?',
    answer: 'Go to the Activities page to view all activities. You can create new activities and link them to leads, contacts, or deals. Activities help track your interactions and tasks.',
    category: 'Activities',
    keywords: ['track', 'activity', 'task', 'follow', 'up']
  },
  {
    id: '5',
    question: 'How do I set up payment reminders?',
    answer: 'Payment reminders are automatically created when you create deals with EMI or recurring payment types. You can view and manage them in the Payment Reminders section.',
    category: 'Payments',
    keywords: ['payment', 'reminder', 'emi', 'recurring', 'billing']
  },
  {
    id: '6',
    question: 'How do I track delivery status?',
    answer: 'When you mark a deal as delivered, the system automatically creates delivery confirmation reminders. You can track delivery status in the deal details or activities.',
    category: 'Delivery',
    keywords: ['delivery', 'track', 'status', 'shipped', 'delivered']
  },
  // Technical Support
  {
    id: '7',
    question: 'I forgot my password, how do I reset it?',
    answer: 'Click on "Forgot Password" on the login page. Enter your email address and you\'ll receive a password reset link.',
    category: 'Account',
    keywords: ['password', 'reset', 'forgot', 'login', 'account']
  },
  {
    id: '8',
    question: 'How do I update my profile information?',
    answer: 'Go to Settings > Profile to update your personal information, contact details, and preferences.',
    category: 'Account',
    keywords: ['profile', 'update', 'edit', 'settings', 'information']
  },
  {
    id: '9',
    question: 'How do I export my data?',
    answer: 'You can export your data from the Reports page. Select the data type (leads, contacts, deals) and choose your preferred format (CSV, Excel).',
    category: 'Data',
    keywords: ['export', 'download', 'data', 'csv', 'excel', 'reports']
  },
  // Business Process Questions
  {
    id: '10',
    question: 'What is the difference between a lead and a contact?',
    answer: 'A lead is a potential customer who hasn\'t been qualified yet. A contact is a qualified person you have an ongoing relationship with. You convert leads to contacts when they\'re ready to do business.',
    category: 'Process',
    keywords: ['lead', 'contact', 'difference', 'qualify', 'relationship']
  },
  {
    id: '11',
    question: 'How do I manage my sales pipeline?',
    answer: 'Your sales pipeline is managed through deals. Each deal goes through different stages (prospecting, qualification, proposal, negotiation, closing). You can customize these stages in Settings.',
    category: 'Process',
    keywords: ['pipeline', 'sales', 'stages', 'process', 'manage']
  },
  {
    id: '12',
    question: 'How do I assign tasks to team members?',
    answer: 'When creating activities, you can assign them to specific team members by entering their User ID. You can also assign deals and leads to team members.',
    category: 'Team',
    keywords: ['assign', 'task', 'team', 'member', 'activity']
  }
]

const pageHelp = {
  '/dashboard': {
    title: 'Dashboard Help',
    description: 'Your dashboard shows key metrics and quick actions',
    suggestions: [
      'How do I view my sales performance?',
      'What are the quick actions available?',
      'How do I see recent activities?'
    ]
  },
  '/leads': {
    title: 'Leads Management',
    description: 'Manage your potential customers and sales prospects',
    suggestions: [
      'How do I create a new lead?',
      'How do I convert a lead to contact?',
      'How do I filter leads by status?'
    ]
  },
  '/contacts': {
    title: 'Contacts Management',
    description: 'Manage your qualified contacts and customer relationships',
    suggestions: [
      'How do I create a new contact?',
      'How do I create a deal from a contact?',
      'How do I track contact activities?'
    ]
  },
  '/deals': {
    title: 'Deals Management',
    description: 'Track your sales opportunities and revenue',
    suggestions: [
      'How do I create a new deal?',
      'How do I set up payment reminders?',
      'How do I track deal progress?'
    ]
  },
  '/activities': {
    title: 'Activities Management',
    description: 'Track your tasks, calls, meetings, and interactions',
    suggestions: [
      'How do I create a new activity?',
      'How do I link activities to leads/contacts?',
      'How do I track activity completion?'
    ]
  },
  '/reports': {
    title: 'Reports & Analytics',
    description: 'View insights and export your data',
    suggestions: [
      'How do I generate sales reports?',
      'How do I export my data?',
      'What metrics are available?'
    ]
  }
}

export default function Chatbot() {
  const [isOpen, setIsOpen] = useState(false)
  const [messages, setMessages] = useState<Message[]>([])
  const [inputValue, setInputValue] = useState('')
  const [isTyping, setIsTyping] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const pathname = usePathname()

  const currentPageHelp = pageHelp[pathname as keyof typeof pageHelp]

  useEffect(() => {
    if (isOpen && messages.length === 0) {
      // Initialize with welcome message and page-specific help
      const welcomeMessage: Message = {
        id: '1',
        type: 'bot',
        content: currentPageHelp 
          ? `Hi! I'm your CRM assistant. I can help you with questions about ${currentPageHelp.title.toLowerCase()}. What would you like to know?`
          : "Hi! I'm your CRM assistant. I can help you with any questions about using the system. What would you like to know?",
        timestamp: new Date(),
        suggestions: currentPageHelp?.suggestions || [
          'How do I create a new lead?',
          'How do I track my activities?',
          'How do I set up payment reminders?'
        ]
      }
      setMessages([welcomeMessage])
    }
  }, [isOpen, pathname])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const findBestAnswer = (query: string): string => {
    const queryLower = query.toLowerCase()
    
    // Find FAQ with best keyword match
    let bestMatch = faqData[0]
    let bestScore = 0
    
    for (const faq of faqData) {
      let score = 0
      for (const keyword of faq.keywords) {
        if (queryLower.includes(keyword.toLowerCase())) {
          score++
        }
      }
      if (score > bestScore) {
        bestScore = score
        bestMatch = faq
      }
    }
    
    return bestScore > 0 ? bestMatch.answer : 
      "I'm not sure about that. Could you try rephrasing your question or ask about creating leads, contacts, deals, or activities?"
  }

  const handleSendMessage = async () => {
    if (!inputValue.trim()) return

    const userMessage: Message = {
      id: Date.now().toString(),
      type: 'user',
      content: inputValue,
      timestamp: new Date()
    }

    setMessages(prev => [...prev, userMessage])
    setInputValue('')
    setIsTyping(true)

    // Simulate typing delay
    setTimeout(() => {
      const botResponse = findBestAnswer(inputValue)
      const botMessage: Message = {
        id: (Date.now() + 1).toString(),
        type: 'bot',
        content: botResponse,
        timestamp: new Date(),
        suggestions: [
          'How do I create a new lead?',
          'How do I track activities?',
          'How do I set up payment reminders?'
        ]
      }
      setMessages(prev => [...prev, botMessage])
      setIsTyping(false)
    }, 1000)
  }

  const handleSuggestionClick = (suggestion: string) => {
    setInputValue(suggestion)
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  const getPageIcon = () => {
    switch (pathname) {
      case '/dashboard': return <Activity className="h-4 w-4" />
      case '/leads': return <Users className="h-4 w-4" />
      case '/contacts': return <Users className="h-4 w-4" />
      case '/deals': return <DollarSign className="h-4 w-4" />
      case '/activities': return <Activity className="h-4 w-4" />
      case '/reports': return <BookOpen className="h-4 w-4" />
      default: return <HelpCircle className="h-4 w-4" />
    }
  }

  return (
    <>
      {/* Chatbot Toggle Button */}
      <div className="fixed bottom-6 right-6 z-50">
        <Button
          onClick={() => setIsOpen(!isOpen)}
          className="h-14 w-14 rounded-full shadow-lg hover:shadow-xl transition-all duration-200"
          size="lg"
        >
          {isOpen ? <X className="h-6 w-6" /> : <MessageCircle className="h-6 w-6" />}
        </Button>
      </div>

      {/* Chatbot Window */}
      {isOpen && (
        <div className="fixed bottom-24 right-6 z-50 w-96 h-[600px] bg-white rounded-lg shadow-2xl border border-gray-200 flex flex-col">
          {/* Header */}
          <div className="bg-blue-600 text-white p-4 rounded-t-lg flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Bot className="h-5 w-5" />
              <div>
                <h3 className="font-semibold">CRM Assistant</h3>
                <p className="text-xs opacity-90">
                  {currentPageHelp?.title || 'General Help'}
                </p>
              </div>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setIsOpen(false)}
              className="text-white hover:bg-blue-700"
            >
              <X className="h-4 w-4" />
            </Button>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`flex ${message.type === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[80%] rounded-lg p-3 ${
                    message.type === 'user'
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-900'
                  }`}
                >
                  <div className="flex items-start gap-2">
                    {message.type === 'bot' && <Bot className="h-4 w-4 mt-0.5 flex-shrink-0" />}
                    {message.type === 'user' && <User className="h-4 w-4 mt-0.5 flex-shrink-0" />}
                    <div className="flex-1">
                      <p className="text-sm">{message.content}</p>
                      <p className="text-xs opacity-70 mt-1">
                        {message.timestamp.toLocaleTimeString()}
                      </p>
                    </div>
                  </div>
                  
                  {/* Suggestions */}
                  {message.suggestions && message.suggestions.length > 0 && (
                    <div className="mt-3 space-y-2">
                      <p className="text-xs font-medium opacity-80">Quick suggestions:</p>
                      <div className="flex flex-wrap gap-1">
                        {message.suggestions.map((suggestion, index) => (
                          <button
                            key={index}
                            onClick={() => handleSuggestionClick(suggestion)}
                            className="text-xs bg-white bg-opacity-20 hover:bg-opacity-30 px-2 py-1 rounded-full transition-colors"
                          >
                            {suggestion}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
            
            {isTyping && (
              <div className="flex justify-start">
                <div className="bg-gray-100 rounded-lg p-3 flex items-center gap-2">
                  <Bot className="h-4 w-4" />
                  <div className="flex space-x-1">
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                  </div>
                </div>
              </div>
            )}
            
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div className="p-4 border-t border-gray-200">
            <div className="flex gap-2">
              <Input
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Ask me anything about the CRM..."
                className="flex-1"
              />
              <Button
                onClick={handleSendMessage}
                disabled={!inputValue.trim() || isTyping}
                size="sm"
              >
                <Send className="h-4 w-4" />
              </Button>
            </div>
            
            {/* Quick Actions */}
            <div className="mt-3 flex flex-wrap gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleSuggestionClick('How do I create a new lead?')}
                className="text-xs"
              >
                <Users className="h-3 w-3 mr-1" />
                Create Lead
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleSuggestionClick('How do I track activities?')}
                className="text-xs"
              >
                <Activity className="h-3 w-3 mr-1" />
                Track Activities
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleSuggestionClick('How do I set up payment reminders?')}
                className="text-xs"
              >
                <DollarSign className="h-3 w-3 mr-1" />
                Payment Reminders
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
