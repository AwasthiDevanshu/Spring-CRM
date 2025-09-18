'use client'

import { motion } from 'framer-motion'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { 
  Activity,
  User,
  MessageSquare,
  Phone,
  Mail,
  Calendar,
  FileText,
  MoreHorizontal
} from 'lucide-react'
import { useRecentActivities } from '@/hooks/use-dashboard'
import { useAuth } from '@/hooks/use-auth'
import { format, formatDistanceToNow } from 'date-fns'
import { Activity as ActivityType } from '@/types/crm'

const activityIcons = {
  call: Phone,
  email: Mail,
  meeting: Calendar,
  note: FileText,
  message: MessageSquare,
  default: Activity,
}

const activityColors = {
  call: 'text-blue-600 bg-blue-50 dark:bg-blue-950',
  email: 'text-green-600 bg-green-50 dark:bg-green-950',
  meeting: 'text-purple-600 bg-purple-50 dark:bg-purple-950',
  note: 'text-orange-600 bg-orange-50 dark:bg-orange-950',
  message: 'text-pink-600 bg-pink-50 dark:bg-pink-950',
  default: 'text-gray-600 bg-gray-50 dark:bg-gray-950',
}

function ActivityItem({ activity }: { activity: ActivityType }) {
  const Icon = activityIcons[activity.type as keyof typeof activityIcons] || activityIcons.default
  const colorClass = activityColors[activity.type as keyof typeof activityColors] || activityColors.default

  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      className="flex items-start space-x-3 p-3 rounded-lg hover:bg-muted/50 transition-colors"
    >
      <div className={`p-2 rounded-full ${colorClass}`}>
        <Icon className="h-4 w-4" />
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between">
          <p className="text-sm font-medium text-foreground truncate">
            {activity.subject || 'Activity'}
          </p>
          <span className="text-xs text-muted-foreground">
            {activity.activityDate 
              ? formatDistanceToNow(new Date(activity.activityDate), { addSuffix: true })
              : formatDistanceToNow(new Date(activity.createdAt), { addSuffix: true })
            }
          </span>
        </div>
        {activity.description && (
          <p className="text-sm text-muted-foreground mt-1 line-clamp-2">
            {activity.description}
          </p>
        )}
        {activity.outcome && (
          <Badge variant="secondary" className="mt-2 text-xs">
            {activity.outcome}
          </Badge>
        )}
      </div>
    </motion.div>
  )
}

function ActivitySkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={i} className="flex items-start space-x-3 p-3">
          <Skeleton className="h-8 w-8 rounded-full" />
          <div className="flex-1 space-y-2">
            <div className="flex items-center justify-between">
              <Skeleton className="h-4 w-32" />
              <Skeleton className="h-3 w-16" />
            </div>
            <Skeleton className="h-3 w-full" />
            <Skeleton className="h-3 w-3/4" />
          </div>
        </div>
      ))}
    </div>
  )
}

export function RecentActivity() {
  const { user } = useAuth()
  const { data: activities, isLoading, error } = useRecentActivities(Number(user?.companyId) || 1, 10)

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.3 }}
    >
      <Card className="border-0 shadow-sm">
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="flex items-center space-x-2">
              <Activity className="h-5 w-5" />
              <span>Recent Activity</span>
            </CardTitle>
            <CardDescription>
              Latest activities across your CRM
            </CardDescription>
          </div>
          <Button variant="ghost" size="sm">
            <MoreHorizontal className="h-4 w-4" />
          </Button>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <ActivitySkeleton />
          ) : error ? (
            <div className="text-center py-8">
              <Activity className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-sm text-muted-foreground">
                Failed to load activities
              </p>
            </div>
          ) : !activities || activities.length === 0 ? (
            <div className="text-center py-8">
              <Activity className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-sm text-muted-foreground">
                No recent activities
              </p>
            </div>
          ) : (
            <div className="space-y-1 max-h-96 overflow-y-auto">
              {activities.map((activity) => (
                <ActivityItem key={activity.id} activity={activity} />
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </motion.div>
  )
}
