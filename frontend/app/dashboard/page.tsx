'use client'

import { Suspense } from 'react'
import { AppLayout } from '@/components/app-layout'
import { DashboardHeader } from '@/components/dashboard/dashboard-header'
import { DashboardStats } from '@/components/dashboard/dashboard-stats'
import { DashboardCharts } from '@/components/dashboard/dashboard-charts'
import { RecentActivity } from '@/components/dashboard/recent-activity'
import { QuickActions } from '@/components/dashboard/quick-actions'
import { DashboardSkeleton } from '@/components/dashboard/dashboard-skeleton'
import { ErrorBoundary } from '@/components/error-boundary'
import { useAuth } from '@/hooks/use-auth'
import { useRouter } from 'next/navigation'

export default function DashboardPage() {
  const { user, isLoading, isAuthenticated } = useAuth()
  const router = useRouter()

  if (isLoading) {
    return <DashboardSkeleton />
  }

  if (!isAuthenticated) {
    router.push('/login')
    return null
  }

  return (
    <AppLayout>
      <div className="space-y-6 p-6 pt-4">
        <ErrorBoundary>
          <Suspense fallback={<DashboardSkeleton />}>
            <DashboardHeader />
            <DashboardStats />
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-7">
              <div className="col-span-4">
                <DashboardCharts />
              </div>
              <div className="col-span-3">
                <RecentActivity />
              </div>
            </div>
            <QuickActions />
          </Suspense>
        </ErrorBoundary>
      </div>
    </AppLayout>
  )
}