'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '@/hooks/use-auth'
import { useReports } from '@/hooks/use-reports'
import { useRouter } from 'next/navigation'
import { AppLayout } from '@/components/app-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { 
  BarChart3, 
  PieChart, 
  TrendingUp, 
  Users, 
  Target, 
  Activity,
  Download,
  Calendar,
  Filter,
  RefreshCw
} from 'lucide-react'

const reportTypes = [
  {
    id: 'leads-performance',
    title: 'Leads Performance',
    description: 'Track lead conversion rates and sources',
    icon: Users,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50',
  },
  {
    id: 'sales-pipeline',
    title: 'Sales Pipeline',
    description: 'Analyze deals and revenue trends',
    icon: Target,
    color: 'text-green-600',
    bgColor: 'bg-green-50',
  },
  {
    id: 'activity-summary',
    title: 'Activity Summary',
    description: 'Review team activities and productivity',
    icon: Activity,
    color: 'text-purple-600',
    bgColor: 'bg-purple-50',
  },
  {
    id: 'revenue-forecast',
    title: 'Revenue Forecast',
    description: 'Predict future revenue and growth',
    icon: TrendingUp,
    color: 'text-orange-600',
    bgColor: 'bg-orange-50',
  }
]

const timeRanges = [
  { value: '7d', label: 'Last 7 days' },
  { value: '30d', label: 'Last 30 days' },
  { value: '90d', label: 'Last 3 months' },
  { value: '1y', label: 'Last year' },
  { value: 'custom', label: 'Custom range' }
]

export default function ReportsPage() {
  const { user, isLoading, isAuthenticated } = useAuth()
  const router = useRouter()
  const [selectedTimeRange, setSelectedTimeRange] = useState('30d')
  const [selectedReportType, setSelectedReportType] = useState('leads-performance')
  
  const days = parseInt(selectedTimeRange.replace('d', ''))
  const companyId = user?.companyId
  const { 
    leadsPerformance, 
    salesPipeline, 
    activitySummary, 
    revenueForecast, 
    quickStats, 
    isLoading: reportsLoading, 
    error 
  } = useReports(days)

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  const loadReports = async (days: number = 30) => {
    if (!companyId) return
    
    // The useReports hook automatically handles loading states with React Query
    // Update the selectedTimeRange to trigger a refetch
    const timeRange = `${days}d`
    setSelectedTimeRange(timeRange)
  }

  useEffect(() => {
    if (companyId) {
      loadReports()
    }
  }, [companyId])

  const handleTimeRangeChange = (value: string) => {
    setSelectedTimeRange(value)
    const days = value === '7d' ? 7 : value === '30d' ? 30 : value === '90d' ? 90 : value === '1y' ? 365 : 30
    loadReports(days)
  }

  if (isLoading || reportsLoading) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-muted-foreground">Loading reports...</p>
          </div>
        </div>
      </AppLayout>
    )
  }

  if (!isAuthenticated) {
    return <div className="flex items-center justify-center h-64">Redirecting to login...</div>
  }

  if (error) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <p className="text-red-600 mb-4">Error loading reports: {error?.message || 'Unknown error'}</p>
            <Button onClick={() => loadReports()}>
              <RefreshCw className="h-4 w-4 mr-2" />
              Retry
            </Button>
          </div>
        </div>
      </AppLayout>
    )
  }

  return (
    <AppLayout>
      <div className="space-y-6 p-6 pt-4">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Reports & Analytics</h1>
            <p className="text-muted-foreground">
              Generate insights and track your CRM performance
            </p>
          </div>
          <div className="flex items-center gap-2">
            <Select value={selectedTimeRange} onValueChange={handleTimeRangeChange}>
              <SelectTrigger className="w-48">
                <Calendar className="h-4 w-4 mr-2" />
                <SelectValue placeholder="Select time range" />
              </SelectTrigger>
              <SelectContent>
                {timeRanges.map((range) => (
                  <SelectItem key={range.value} value={range.value}>
                    {range.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button variant="outline" onClick={() => loadReports()}>
              <RefreshCw className={`h-4 w-4 mr-2 ${reportsLoading ? 'animate-spin' : ''}`} />
              Refresh
            </Button>
            <Button>
              <Download className="h-4 w-4 mr-2" />
              Export
            </Button>
          </div>
        </div>

        {/* Report Types */}
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {reportTypes.map((report) => {
            const Icon = report.icon
            const isSelected = selectedReportType === report.id
            
            return (
              <Card 
                key={report.id}
                className={`cursor-pointer transition-all hover:shadow-md ${
                  isSelected ? 'ring-2 ring-primary' : ''
                }`}
                onClick={() => setSelectedReportType(report.id)}
              >
                <CardContent className="p-6">
                  <div className="flex items-center space-x-4">
                    <div className={`p-3 rounded-lg ${report.bgColor}`}>
                      <Icon className={`h-6 w-6 ${report.color}`} />
                    </div>
                    <div>
                      <h3 className="font-semibold">{report.title}</h3>
                      <p className="text-sm text-muted-foreground">
                        {report.description}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )
          })}
        </div>

        {/* Report Content */}
        <Tabs value={selectedReportType} onValueChange={setSelectedReportType}>
          <TabsContent value="leads-performance">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Users className="h-5 w-5" />
                  Leads Performance Report
                </CardTitle>
                <CardDescription>
                  Analysis of lead generation and conversion for {timeRanges.find(r => r.value === selectedTimeRange)?.label.toLowerCase()}
                </CardDescription>
              </CardHeader>
              <CardContent>
                {reportsLoading ? (
                  <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                    {[1, 2, 3, 4].map((i) => (
                      <div key={i} className="space-y-2">
                        <div className="h-4 bg-muted rounded animate-pulse"></div>
                        <div className="h-8 bg-muted rounded animate-pulse"></div>
                        <div className="h-6 bg-muted rounded animate-pulse"></div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                    <div className="space-y-2">
                      <p className="text-sm text-muted-foreground">Total Leads</p>
                      <p className="text-2xl font-bold">{leadsPerformance?.data.totalLeads || 0}</p>
                      <Badge variant="outline" className={leadsPerformance?.data?.periodComparison?.changeType === 'increase' ? 'text-green-600' : 'text-red-600'}>
                        {leadsPerformance?.data?.periodComparison?.changePercent ? `${leadsPerformance.data.periodComparison.changePercent > 0 ? '+' : ''}${leadsPerformance.data.periodComparison.changePercent.toFixed(1)}%` : '0%'} from last period
                      </Badge>
                    </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Conversion Rate</p>
                    <p className="text-2xl font-bold">{leadsPerformance?.data?.conversionRate?.toFixed(1) || 0}%</p>
                    <Badge variant="outline" className="text-blue-600">
                      Real-time data
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Qualified Leads</p>
                    <p className="text-2xl font-bold">{leadsPerformance?.data?.qualifiedLeads || 0}</p>
                    <Badge variant="outline" className="text-purple-600">
                      {leadsPerformance?.data && leadsPerformance.data.totalLeads > 0 ? `${((leadsPerformance.data.qualifiedLeads / leadsPerformance.data.totalLeads) * 100).toFixed(1)}%` : '0%'} of total
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Avg. Lead Score</p>
                    <p className="text-2xl font-bold">{leadsPerformance?.data?.averageLeadScore?.toFixed(0) || 0}</p>
                    <Badge variant="outline" className="text-orange-600">
                      Out of 100
                    </Badge>
                  </div>
                </div>
                )}
                
                <div className="mt-6 p-6 bg-muted/50 rounded-lg text-center">
                  <BarChart3 className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                  <p className="text-muted-foreground">
                    Interactive charts and detailed analytics will be available in the next version.
                  </p>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="sales-pipeline">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Target className="h-5 w-5" />
                  Sales Pipeline Report
                </CardTitle>
                <CardDescription>
                  Deal progression and revenue analysis for {timeRanges.find(r => r.value === selectedTimeRange)?.label.toLowerCase()}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Total Deals</p>
                    <p className="text-2xl font-bold">{salesPipeline?.data.totalDeals || 0}</p>
                    <Badge variant="outline" className={salesPipeline?.data.periodComparison?.changeType === 'increase' ? 'text-green-600' : 'text-red-600'}>
                      {salesPipeline?.data.periodComparison?.changePercent ? `${salesPipeline.data.periodComparison.changePercent > 0 ? '+' : ''}${salesPipeline.data.periodComparison.changePercent.toFixed(1)}%` : '0%'} from last period
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Won Deals</p>
                    <p className="text-2xl font-bold">{salesPipeline?.data.wonDeals || 0}</p>
                    <Badge variant="outline" className="text-blue-600">
                      {salesPipeline?.data.totalDeals > 0 ? `${((salesPipeline.data.wonDeals / salesPipeline.data.totalDeals) * 100).toFixed(1)}%` : '0%'} win rate
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Total Revenue</p>
                    <p className="text-2xl font-bold">${salesPipeline?.data.totalRevenue ? (salesPipeline.data.totalRevenue / 1000).toFixed(0) + 'K' : '0'}</p>
                    <Badge variant="outline" className="text-purple-600">
                      Real-time data
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Avg. Deal Size</p>
                    <p className="text-2xl font-bold">${salesPipeline?.data.averageDealSize ? (salesPipeline.data.averageDealSize / 1000).toFixed(1) + 'K' : '0'}</p>
                    <Badge variant="outline" className="text-orange-600">
                      Average value
                    </Badge>
                  </div>
                </div>
                
                <div className="mt-6 p-6 bg-muted/50 rounded-lg text-center">
                  <PieChart className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                  <p className="text-muted-foreground">
                    Pipeline visualization and deal progression charts coming soon.
                  </p>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="activity-summary">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Activity className="h-5 w-5" />
                  Activity Summary Report
                </CardTitle>
                <CardDescription>
                  Team productivity and activity metrics for {timeRanges.find(r => r.value === selectedTimeRange)?.label.toLowerCase()}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Total Activities</p>
                    <p className="text-2xl font-bold">{activitySummary?.data.totalActivities || 0}</p>
                    <Badge variant="outline" className={activitySummary?.data.periodComparison?.changeType === 'increase' ? 'text-green-600' : 'text-red-600'}>
                      {activitySummary?.data.periodComparison?.changePercent ? `${activitySummary.data.periodComparison.changePercent > 0 ? '+' : ''}${activitySummary.data.periodComparison.changePercent.toFixed(1)}%` : '0%'} from last period
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Calls Made</p>
                    <p className="text-2xl font-bold">{activitySummary?.data.callsMade || 0}</p>
                    <Badge variant="outline" className="text-blue-600">
                      {activitySummary?.data.totalActivities > 0 ? `${((activitySummary.data.callsMade / activitySummary.data.totalActivities) * 100).toFixed(1)}%` : '0%'} of total
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Emails Sent</p>
                    <p className="text-2xl font-bold">{activitySummary?.data.emailsSent || 0}</p>
                    <Badge variant="outline" className="text-purple-600">
                      {activitySummary?.data.totalActivities > 0 ? `${((activitySummary.data.emailsSent / activitySummary.data.totalActivities) * 100).toFixed(1)}%` : '0%'} of total
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Meetings Held</p>
                    <p className="text-2xl font-bold">{activitySummary?.data.meetingsHeld || 0}</p>
                    <Badge variant="outline" className="text-orange-600">
                      {activitySummary?.data.totalActivities > 0 ? `${((activitySummary.data.meetingsHeld / activitySummary.data.totalActivities) * 100).toFixed(1)}%` : '0%'} of total
                    </Badge>
                  </div>
                </div>
                
                <div className="mt-6 p-6 bg-muted/50 rounded-lg text-center">
                  <Activity className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                  <p className="text-muted-foreground">
                    Detailed activity breakdowns and team performance metrics coming soon.
                  </p>
                </div>
                
                {/* Activity Breakdown */}
                <div className="mt-6 grid gap-4 md:grid-cols-2">
                  <Card>
                    <CardHeader className="pb-3">
                      <CardTitle className="text-sm">Activity Distribution</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Calls</span>
                          <div className="flex items-center gap-2">
                            <div className="w-20 bg-muted rounded-full h-2">
                              <div 
                                className="bg-blue-600 h-2 rounded-full" 
                                style={{ 
                                  width: `${activitySummary ? (activitySummary.data.callsMade / activitySummary.data.totalActivities) * 100 : 0}%` 
                                }}
                              />
                            </div>
                            <span className="text-sm font-medium">
                              {activitySummary && activitySummary.data.totalActivities > 0 ? `${((activitySummary.data.callsMade / activitySummary.data.totalActivities) * 100).toFixed(1)}%` : '0%'}
                            </span>
                          </div>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Emails</span>
                          <div className="flex items-center gap-2">
                            <div className="w-20 bg-muted rounded-full h-2">
                              <div 
                                className="bg-purple-600 h-2 rounded-full" 
                                style={{ 
                                  width: `${activitySummary ? (activitySummary.data.emailsSent / activitySummary.data.totalActivities) * 100 : 0}%` 
                                }}
                              />
                            </div>
                            <span className="text-sm font-medium">
                              {activitySummary && activitySummary.data.totalActivities > 0 ? `${((activitySummary.data.emailsSent / activitySummary.data.totalActivities) * 100).toFixed(1)}%` : '0%'}
                            </span>
                          </div>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Meetings</span>
                          <div className="flex items-center gap-2">
                            <div className="w-20 bg-muted rounded-full h-2">
                              <div 
                                className="bg-orange-600 h-2 rounded-full" 
                                style={{ 
                                  width: `${activitySummary ? (activitySummary.data.meetingsHeld / activitySummary.data.totalActivities) * 100 : 0}%` 
                                }}
                              />
                            </div>
                            <span className="text-sm font-medium">
                              {activitySummary && activitySummary.data.totalActivities > 0 ? `${((activitySummary.data.meetingsHeld / activitySummary.data.totalActivities) * 100).toFixed(1)}%` : '0%'}
                            </span>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                  
                  <Card>
                    <CardHeader className="pb-3">
                      <CardTitle className="text-sm">Activity Trends</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Daily Average</span>
                          <span className="font-semibold">
                            {activitySummary && activitySummary.data.totalActivities ? Math.round(activitySummary.data.totalActivities / 30) : 0}
                          </span>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Most Active Type</span>
                          <span className="font-semibold">
                            {activitySummary ? 
                              (activitySummary.data.callsMade > activitySummary.data.emailsSent && activitySummary.data.callsMade > activitySummary.data.meetingsHeld ? 'Calls' :
                               activitySummary.data.emailsSent > activitySummary.data.meetingsHeld ? 'Emails' : 'Meetings') : 'N/A'
                            }
                          </span>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Activity Rate</span>
                          <span className="font-semibold text-green-600">
                            {activitySummary?.data.periodComparison?.changeType === 'increase' ? '+' : ''}{activitySummary?.data.periodComparison?.changePercent?.toFixed(1) || 0}%
                          </span>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="revenue-forecast">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5" />
                  Revenue Forecast Report
                </CardTitle>
                <CardDescription>
                  Projected revenue and growth predictions based on current pipeline
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Forecasted Revenue</p>
                    <p className="text-2xl font-bold">${revenueForecast?.data.forecastedRevenue ? (revenueForecast.data.forecastedRevenue / 1000000).toFixed(1) + 'M' : '0'}</p>
                    <Badge variant="outline" className="text-green-600">
                      Next 3 months
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Pipeline Value</p>
                    <p className="text-2xl font-bold">${revenueForecast?.data.pipelineValue ? (revenueForecast.data.pipelineValue / 1000000).toFixed(1) + 'M' : '0'}</p>
                    <Badge variant="outline" className="text-blue-600">
                      Total potential
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Win Probability</p>
                    <p className="text-2xl font-bold">{revenueForecast?.data.winProbability?.toFixed(0) || 0}%</p>
                    <Badge variant="outline" className="text-purple-600">
                      Weighted average
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">Growth Rate</p>
                    <p className="text-2xl font-bold">{revenueForecast?.data.growthRate ? `${revenueForecast.data.growthRate > 0 ? '+' : ''}${revenueForecast.data.growthRate.toFixed(0)}%` : '0%'}</p>
                    <Badge variant="outline" className="text-orange-600">
                      Year over year
                    </Badge>
                  </div>
                </div>
                
                <div className="mt-6 p-6 bg-muted/50 rounded-lg text-center">
                  <TrendingUp className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                  <p className="text-muted-foreground">
                    Advanced forecasting models and predictive analytics coming soon.
                  </p>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>

        {/* Quick Stats */}
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-base">This Month</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">New Leads</span>
                  <span className="font-semibold">{quickStats?.data.thisMonth?.newLeads || 0}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Deals Closed</span>
                  <span className="font-semibold">{quickStats?.data.thisMonth?.dealsClosed || 0}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Revenue</span>
                  <span className="font-semibold">${quickStats?.data.thisMonth?.revenue ? (quickStats.data.thisMonth.revenue / 1000).toFixed(0) + 'K' : '0'}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-base">Team Performance</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Top Performer</span>
                  <span className="font-semibold">{quickStats?.data.teamPerformance?.topPerformer || 'N/A'}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Activities Logged</span>
                  <span className="font-semibold">{quickStats?.data.teamPerformance?.activitiesLogged || 0}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Response Rate</span>
                  <span className="font-semibold">{quickStats?.data.teamPerformance?.responseRate?.toFixed(0) || 0}%</span>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-base">Lead Sources</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Website</span>
                  <span className="font-semibold">{quickStats?.data.leadSources?.website?.toFixed(0) || 0}%</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Referrals</span>
                  <span className="font-semibold">{quickStats?.data.leadSources?.referrals?.toFixed(0) || 0}%</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Social Media</span>
                  <span className="font-semibold">{quickStats?.data.leadSources?.socialMedia?.toFixed(0) || 0}%</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </AppLayout>
  )
}
