'use client'

import { useState, useEffect, useRef } from 'react'
import { usePathname } from 'next/navigation'
import { useChatbot } from '@/contexts/chatbot-context'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Textarea } from '@/components/ui/textarea'
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
  Settings,
  ThumbsUp,
  ThumbsDown,
  RefreshCw,
  Search,
  Filter,
  Star
} from 'lucide-react'
import { chatbotAPI, type ChatbotMessage, type FAQ } from '@/lib/chatbot-api'

interface Message extends ChatbotMessage {
  suggestions?: string[]
  metadata?: {
    page?: string
    category?: string
    confidence?: number
  }
}

export default function AdvancedChatbot() {
  const { isOpen, setIsOpen } = useChatbot()
  const [messages, setMessages] = useState<Message[]>([])
  const [inputValue, setInputValue] = useState('')
  const [isTyping, setIsTyping] = useState(false)
  const [faqs, setFaqs] = useState<FAQ[]>([])
  const [searchQuery, setSearchQuery] = useState('')
  const [showFAQs, setShowFAQs] = useState(false)
  const [selectedCategory, setSelectedCategory] = useState<string>('All')
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const pathname = usePathname()

  const categories = ['All', 'Leads', 'Contacts', 'Deals', 'Activities', 'Payments', 'Delivery', 'Account', 'Data', 'Process', 'Team']

  useEffect(() => {
    if (isOpen && messages.length === 0) {
      initializeChat()
    }
  }, [isOpen])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  useEffect(() => {
    if (isOpen) {
      loadFAQs()
    }
  }, [isOpen, selectedCategory])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const initializeChat = async () => {
    try {
      const pageHelp = await chatbotAPI.getPageHelp(pathname)
      const welcomeMessage: Message = {
        id: '1',
        type: 'bot',
        content: `Hi! I'm your CRM assistant. I can help you with questions about ${pageHelp.description.toLowerCase()}. What would you like to know?`,
        timestamp: new Date(),
        suggestions: pageHelp.suggestions
      }
      setMessages([welcomeMessage])
    } catch (error) {
      console.error('Error initializing chat:', error)
      const fallbackMessage: Message = {
        id: '1',
        type: 'bot',
        content: "Hi! I'm your CRM assistant. I can help you with any questions about using the system. What would you like to know?",
        timestamp: new Date(),
        suggestions: [
          'How do I create a new lead?',
          'How do I track my activities?',
          'How do I set up payment reminders?'
        ]
      }
      setMessages([fallbackMessage])
    }
  }

  const loadFAQs = async () => {
    try {
      const category = selectedCategory === 'All' ? undefined : selectedCategory
      const faqData = await chatbotAPI.getFAQs(category)
      setFaqs(faqData)
    } catch (error) {
      console.error('Error loading FAQs:', error)
    }
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

    try {
      const response = await chatbotAPI.sendMessage(inputValue, { 
        page: pathname,
        userId: '1' // TODO: Get from auth context
      })

      const botMessage: Message = {
        id: (Date.now() + 1).toString(),
        type: 'bot',
        content: response.answer,
        timestamp: new Date(),
        suggestions: response.suggestions,
        metadata: {
          page: pathname,
          category: response.category,
          confidence: response.confidence
        }
      }

      setMessages(prev => [...prev, botMessage])
    } catch (error) {
      console.error('Error sending message:', error)
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        type: 'bot',
        content: "I'm sorry, I'm having trouble connecting right now. Please try again later.",
        timestamp: new Date(),
        suggestions: [
          'How do I create a new lead?',
          'How do I track activities?',
          'How do I set up payment reminders?'
        ]
      }
      setMessages(prev => [...prev, errorMessage])
    } finally {
      setIsTyping(false)
    }
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

  const handleFeedback = async (messageId: string, feedback: 'helpful' | 'not_helpful') => {
    try {
      await chatbotAPI.submitFeedback(messageId, feedback)
      // TODO: Show feedback confirmation
    } catch (error) {
      console.error('Error submitting feedback:', error)
    }
  }

  const handleFAQClick = (faq: FAQ) => {
    const faqMessage: Message = {
      id: Date.now().toString(),
      type: 'user',
      content: faq.question,
      timestamp: new Date()
    }
    setMessages(prev => [...prev, faqMessage])
    setInputValue(faq.question)
  }

  const handleSearchFAQs = async () => {
    if (!searchQuery.trim()) return

    try {
      const results = await chatbotAPI.searchKnowledgeBase(searchQuery)
      setFaqs(results)
    } catch (error) {
      console.error('Error searching FAQs:', error)
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
                <p className="text-xs opacity-90 flex items-center gap-1">
                  {getPageIcon()}
                  {pathname.replace('/', '').charAt(0).toUpperCase() + pathname.slice(2) || 'General Help'}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setShowFAQs(!showFAQs)}
                className="text-white hover:bg-blue-700"
              >
                <BookOpen className="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsOpen(false)}
                className="text-white hover:bg-blue-700"
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
          </div>

          {/* FAQ Panel */}
          {showFAQs && (
            <div className="bg-gray-50 p-4 border-b border-gray-200 max-h-48 overflow-y-auto">
              <div className="space-y-3">
                <div className="flex gap-2">
                  <Input
                    placeholder="Search FAQs..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="flex-1"
                    onKeyPress={(e) => e.key === 'Enter' && handleSearchFAQs()}
                  />
                  <Button size="sm" onClick={handleSearchFAQs}>
                    <Search className="h-4 w-4" />
                  </Button>
                </div>
                
                <div className="flex flex-wrap gap-1">
                  {categories.map((category) => (
                    <Button
                      key={category}
                      variant={selectedCategory === category ? "default" : "outline"}
                      size="sm"
                      onClick={() => setSelectedCategory(category)}
                      className="text-xs"
                    >
                      {category}
                    </Button>
                  ))}
                </div>
                
                <div className="space-y-2 max-h-32 overflow-y-auto">
                  {faqs.map((faq) => (
                    <div
                      key={faq.id}
                      className="p-2 bg-white rounded border cursor-pointer hover:bg-blue-50 transition-colors"
                      onClick={() => handleFAQClick(faq)}
                    >
                      <p className="text-sm font-medium">{faq.question}</p>
                      <p className="text-xs text-gray-500">{faq.category}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

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
                      <div className="flex items-center justify-between mt-2">
                        <p className="text-xs opacity-70">
                          {message.timestamp.toLocaleTimeString()}
                        </p>
                        {message.type === 'bot' && message.metadata?.confidence && (
                          <div className="flex items-center gap-1">
                            <Star className="h-3 w-3" />
                            <span className="text-xs">
                              {Math.round(message.metadata.confidence * 100)}%
                            </span>
                          </div>
                        )}
                      </div>
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

                  {/* Feedback */}
                  {message.type === 'bot' && (
                    <div className="mt-2 flex gap-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleFeedback(message.id, 'helpful')}
                        className="text-xs p-1 h-auto hover:bg-green-100"
                      >
                        <ThumbsUp className="h-3 w-3" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleFeedback(message.id, 'not_helpful')}
                        className="text-xs p-1 h-auto hover:bg-red-100"
                      >
                        <ThumbsDown className="h-3 w-3" />
                      </Button>
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
