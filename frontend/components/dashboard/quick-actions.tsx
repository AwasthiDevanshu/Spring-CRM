'use client'

import { motion } from 'framer-motion'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { 
  Plus,
  Users,
  UserCheck,
  Target,
  Activity,
  FileText,
  Upload,
  Download,
  Settings
} from 'lucide-react'
import { useAuth } from '@/hooks/use-auth'

const quickActions = [
  {
    title: 'Add Lead',
    description: 'Create a new lead',
    icon: Users,
    href: '/leads',
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
  },
  {
    title: 'Add Contact',
    description: 'Create a new contact',
    icon: UserCheck,
    href: '/contacts',
    color: 'text-green-600',
    bgColor: 'bg-green-50 dark:bg-green-950',
  },
  {
    title: 'Add Deal',
    description: 'Create a new deal',
    icon: Target,
    href: '/deals',
    color: 'text-purple-600',
    bgColor: 'bg-purple-50 dark:bg-purple-950',
  },
  {
    title: 'Add Activity',
    description: 'Log a new activity',
    icon: Activity,
    href: '/activities',
    color: 'text-orange-600',
    bgColor: 'bg-orange-50 dark:bg-orange-950',
  },
  {
    title: 'Import Data',
    description: 'Import leads or contacts',
    icon: Upload,
    href: '/import',
    color: 'text-cyan-600',
    bgColor: 'bg-cyan-50 dark:bg-cyan-950',
  },
  {
    title: 'Export Data',
    description: 'Export your data',
    icon: Download,
    href: '/import',
    color: 'text-indigo-600',
    bgColor: 'bg-indigo-50 dark:bg-indigo-950',
  },
  {
    title: 'Create Report',
    description: 'Generate a new report',
    icon: FileText,
    href: '/reports',
    color: 'text-pink-600',
    bgColor: 'bg-pink-50 dark:bg-pink-950',
  },
  {
    title: 'Settings',
    description: 'Configure your CRM',
    icon: Settings,
    href: '/settings',
    color: 'text-gray-600',
    bgColor: 'bg-gray-50 dark:bg-gray-950',
  },
]

export function QuickActions() {
  const { user } = useAuth()

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.4 }}
    >
      <Card className="border-0 shadow-sm">
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>
            Common tasks and shortcuts for {user?.firstName || user?.fullName || 'User'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            {quickActions.map((action, index) => {
              const Icon = action.icon
              
              return (
                <motion.div
                  key={action.title}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.3, delay: 0.5 + index * 0.1 }}
                >
                  <Button
                    variant="ghost"
                    className="h-auto p-4 w-full justify-start hover:bg-muted/50 transition-colors"
                    asChild
                  >
                    <a href={action.href}>
                      <div className="flex items-center space-x-3">
                        <div className={`p-2 rounded-lg ${action.bgColor}`}>
                          <Icon className={`h-5 w-5 ${action.color}`} />
                        </div>
                        <div className="text-left">
                          <div className="font-medium">{action.title}</div>
                          <div className="text-sm text-muted-foreground">
                            {action.description}
                          </div>
                        </div>
                      </div>
                    </a>
                  </Button>
                </motion.div>
              )
            })}
          </div>
        </CardContent>
      </Card>
    </motion.div>
  )
}
