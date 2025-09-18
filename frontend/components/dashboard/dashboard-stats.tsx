'use client'

import { motion } from 'framer-motion'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { 
  Users, 
  UserPlus, 
  DollarSign, 
  TrendingUp, 
  Target,
  Activity,
  ArrowUpRight,
  ArrowDownRight
} from 'lucide-react'
import { useDashboardStats } from '@/hooks/use-dashboard'
import { useAuth } from '@/hooks/use-auth'

const statConfigs = [
  {
    key: 'totalLeads',
    title: 'Total Leads',
    icon: Users,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
    formatter: (value: number) => value.toLocaleString(),
  },
  {
    key: 'newLeadsThisMonth',
    title: 'New Leads',
    icon: UserPlus,
    color: 'text-green-600',
    bgColor: 'bg-green-50 dark:bg-green-950',
    formatter: (value: number) => value.toLocaleString(),
  },
  {
    key: 'activeDeals',
    title: 'Active Deals',
    icon: Target,
    color: 'text-purple-600',
    bgColor: 'bg-purple-50 dark:bg-purple-950',
    formatter: (value: number) => value.toLocaleString(),
  },
  {
    key: 'totalRevenue',
    title: 'Revenue',
    icon: DollarSign,
    color: 'text-emerald-600',
    bgColor: 'bg-emerald-50 dark:bg-emerald-950',
    formatter: (value: number) => `$${value.toLocaleString()}`,
  },
  {
    key: 'conversionRate',
    title: 'Conversion Rate',
    icon: TrendingUp,
    color: 'text-orange-600',
    bgColor: 'bg-orange-50 dark:bg-orange-950',
    formatter: (value: number) => `${value.toFixed(1)}%`,
  },
  {
    key: 'totalActivities',
    title: 'Activities',
    icon: Activity,
    color: 'text-red-600',
    bgColor: 'bg-red-50 dark:bg-red-950',
    formatter: (value: number) => value.toLocaleString(),
  },
]

function StatCard({ 
  config, 
  value, 
  change, 
  changeType, 
  description 
}: {
  config: typeof statConfigs[0]
  value: number
  change?: number
  changeType?: 'positive' | 'negative' | 'neutral'
  description?: string
}) {
  const Icon = config.icon

  return (
    <Card className="relative overflow-hidden border-0 shadow-sm hover:shadow-md transition-all duration-200">
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {config.title}
        </CardTitle>
        <div className={`p-2 rounded-lg ${config.bgColor}`}>
          <Icon className={`h-4 w-4 ${config.color}`} />
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-1">
          <div className="text-2xl font-bold">
            {config.formatter(value)}
          </div>
          {change !== undefined && changeType && (
            <div className="flex items-center space-x-1 text-xs">
              {changeType === 'positive' ? (
                <ArrowUpRight className="h-3 w-3 text-green-600" />
              ) : changeType === 'negative' ? (
                <ArrowDownRight className="h-3 w-3 text-red-600" />
              ) : null}
              <span className={
                changeType === 'positive' 
                  ? 'text-green-600' 
                  : changeType === 'negative' 
                  ? 'text-red-600' 
                  : 'text-muted-foreground'
              }>
                {change > 0 ? '+' : ''}{change.toFixed(1)}%
              </span>
              {description && (
                <span className="text-muted-foreground">{description}</span>
              )}
            </div>
          )}
        </div>
      </CardContent>
      
      {/* Gradient overlay */}
      <div className="absolute inset-0 bg-gradient-to-r from-transparent via-transparent to-primary/5 pointer-events-none" />
    </Card>
  )
}

function StatCardSkeleton() {
  return (
    <Card className="border-0 shadow-sm">
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <Skeleton className="h-4 w-20" />
        <Skeleton className="h-8 w-8 rounded-lg" />
      </CardHeader>
      <CardContent>
        <div className="space-y-1">
          <Skeleton className="h-8 w-16" />
          <Skeleton className="h-3 w-24" />
        </div>
      </CardContent>
    </Card>
  )
}

export function DashboardStats() {
  const { user } = useAuth()
  const { data: stats, isLoading, error } = useDashboardStats(Number(user?.companyId) || 1)

  if (error) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
        {statConfigs.map((config) => (
          <Card key={config.key} className="border-destructive/20">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {config.title}
              </CardTitle>
              <div className={`p-2 rounded-lg ${config.bgColor}`}>
                <config.icon className={`h-4 w-4 ${config.color}`} />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-destructive">
                Error
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.1 }}
      className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6"
    >
      {statConfigs.map((config, index) => (
        <motion.div
          key={config.key}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 + index * 0.1 }}
        >
          {isLoading ? (
            <StatCardSkeleton />
          ) : (
            <StatCard
              config={config}
              value={stats?.[config.key as keyof typeof stats] || 0}
              change={stats?.[`${config.key}Change` as keyof typeof stats] as number}
              changeType={
                stats?.[`${config.key}Change` as keyof typeof stats] > 0 
                  ? 'positive' 
                  : stats?.[`${config.key}Change` as keyof typeof stats] < 0 
                  ? 'negative' 
                  : 'neutral'
              }
              description={String(stats?.[`${config.key}Description` as keyof typeof stats] || '')}
            />
          )}
        </motion.div>
      ))}
    </motion.div>
  )
}