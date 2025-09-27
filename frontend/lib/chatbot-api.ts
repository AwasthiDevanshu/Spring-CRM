interface ChatbotMessage {
  id: string
  type: 'user' | 'bot'
  content: string
  timestamp: Date
  suggestions?: string[]
  metadata?: {
    page?: string
    category?: string
    confidence?: number
  }
}

interface FAQ {
  id: string
  question: string
  answer: string
  category: string
  keywords: string[]
  page?: string
}

interface ChatbotResponse {
  answer: string
  suggestions: string[]
  confidence: number
  category?: string
  relatedQuestions?: string[]
}

class ChatbotAPI {
  private baseUrl = '/api/chatbot'

  async sendMessage(message: string, context?: { page: string; userId?: string }): Promise<ChatbotResponse> {
    try {
      const response = await fetch(`${this.baseUrl}/message`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message,
          context
        })
      })

      if (!response.ok) {
        throw new Error('Failed to send message')
      }

      return await response.json()
    } catch (error) {
      console.error('Error sending message to chatbot:', error)
      return {
        answer: "I'm sorry, I'm having trouble connecting right now. Please try again later.",
        suggestions: [
          'How do I create a new lead?',
          'How do I track activities?',
          'How do I set up payment reminders?'
        ],
        confidence: 0
      }
    }
  }

  async getFAQs(category?: string): Promise<FAQ[]> {
    try {
      const url = category ? `${this.baseUrl}/faqs?category=${category}` : `${this.baseUrl}/faqs`
      const response = await fetch(url)

      if (!response.ok) {
        throw new Error('Failed to fetch FAQs')
      }

      return await response.json()
    } catch (error) {
      console.error('Error fetching FAQs:', error)
      return []
    }
  }

  async getPageHelp(page: string): Promise<{ suggestions: string[]; description: string }> {
    try {
      const response = await fetch(`${this.baseUrl}/page-help?page=${page}`)

      if (!response.ok) {
        throw new Error('Failed to fetch page help')
      }

      return await response.json()
    } catch (error) {
      console.error('Error fetching page help:', error)
      return {
        suggestions: [
          'How do I create a new lead?',
          'How do I track activities?',
          'How do I set up payment reminders?'
        ],
        description: 'General help for this page'
      }
    }
  }

  async searchKnowledgeBase(query: string): Promise<FAQ[]> {
    try {
      const response = await fetch(`${this.baseUrl}/search?q=${encodeURIComponent(query)}`)

      if (!response.ok) {
        throw new Error('Failed to search knowledge base')
      }

      return await response.json()
    } catch (error) {
      console.error('Error searching knowledge base:', error)
      return []
    }
  }

  async submitFeedback(messageId: string, feedback: 'helpful' | 'not_helpful', comment?: string): Promise<void> {
    try {
      await fetch(`${this.baseUrl}/feedback`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          messageId,
          feedback,
          comment
        })
      })
    } catch (error) {
      console.error('Error submitting feedback:', error)
    }
  }
}

export const chatbotAPI = new ChatbotAPI()
export type { ChatbotMessage, FAQ, ChatbotResponse }
