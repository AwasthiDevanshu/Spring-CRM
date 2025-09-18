'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { 
  Search,
  Users,
  UserCheck,
  Target,
  Activity,
  BarChart3,
  MessageSquare,
  Settings,
  FileText,
  Plus
} from 'lucide-react'
import { useAuth } from '@/hooks/use-auth'

const commandItems = [
  {
    group: 'Navigation',
    items: [
      { name: 'Dashboard', href: '/dashboard', icon: BarChart3 },
      { name: 'Leads', href: '/leads', icon: Users },
      { name: 'Contacts', href: '/contacts', icon: UserCheck },
      { name: 'Deals', href: '/deals', icon: Target },
      { name: 'Activities', href: '/activities', icon: Activity },
      { name: 'Reports', href: '/reports', icon: BarChart3 },
      { name: 'Integrations', href: '/integrations', icon: MessageSquare },
      { name: 'Settings', href: '/settings', icon: Settings },
    ]
  },
  {
    group: 'Quick Actions',
    items: [
      { name: 'Add Lead', href: '/leads', icon: Plus },
      { name: 'Add Contact', href: '/contacts', icon: Plus },
      { name: 'Add Deal', href: '/deals', icon: Plus },
      { name: 'Add Activity', href: '/activities', icon: Plus },
    ]
  }
]

export function CommandMenu() {
  const [open, setOpen] = useState(false)
  const [search, setSearch] = useState('')
  const router = useRouter()
  const { user } = useAuth()

  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
        e.preventDefault()
        setOpen((open) => !open)
      }
    }

    document.addEventListener('keydown', down)
    return () => document.removeEventListener('keydown', down)
  }, [])

  const handleSelect = (href: string) => {
    setOpen(false)
    router.push(href)
  }

  const filteredItems = commandItems.map(group => ({
    ...group,
    items: group.items.filter(item => 
      item.name.toLowerCase().includes(search.toLowerCase())
    )
  })).filter(group => group.items.length > 0)

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button
          variant="outline"
          className="relative h-9 w-full justify-start text-sm text-muted-foreground sm:pr-12 md:w-40 lg:w-64"
        >
          <Search className="mr-2 h-4 w-4" />
          <span className="hidden lg:inline-flex">Search...</span>
          <span className="inline-flex lg:hidden">Search</span>
          <kbd className="pointer-events-none absolute right-1.5 top-1.5 hidden h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium opacity-100 sm:flex">
            <span className="text-xs">⌘</span>K
          </kbd>
        </Button>
      </DialogTrigger>
      <DialogContent className="overflow-hidden p-0 shadow-lg">
        <Command className="[&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-muted-foreground [&_[cmdk-group]:not([hidden])_~[cmdk-group]]:pt-0 [&_[cmdk-group]]:px-2 [&_[cmdk-input-wrapper]_svg]:h-5 [&_[cmdk-input-wrapper]_svg]:w-5 [&_[cmdk-input]]:h-12 [&_[cmdk-item]]:px-2 [&_[cmdk-item]]:py-3 [&_[cmdk-item]_svg]:h-5 [&_[cmdk-item]_svg]:w-5">
          <CommandInput
            placeholder="Search for anything..."
            value={search}
            onValueChange={setSearch}
          />
          <CommandList>
            <CommandEmpty>No results found.</CommandEmpty>
            {filteredItems.map((group) => (
              <CommandGroup key={group.group} heading={group.group}>
                {group.items.map((item) => {
                  const Icon = item.icon
                  return (
                    <CommandItem
                      key={item.name}
                      value={item.name}
                      onSelect={() => handleSelect(item.href)}
                      className="flex items-center space-x-2"
                    >
                      <Icon className="h-4 w-4" />
                      <span>{item.name}</span>
                      {item.name.includes('Add') && (
                        <Badge variant="outline" className="ml-auto">
                          New
                        </Badge>
                      )}
                    </CommandItem>
                  )
                })}
              </CommandGroup>
            ))}
          </CommandList>
        </Command>
      </DialogContent>
    </Dialog>
  )
}
