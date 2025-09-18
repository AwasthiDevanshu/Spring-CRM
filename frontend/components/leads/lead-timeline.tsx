'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { useToast } from '@/hooks/use-toast'
import { getAuthHeader } from '@/lib/auth'
import { 
  MessageSquare, 
  Plus, 
  Clock, 
  User,
  Edit,
  Trash2,
  Save,
  X
} from 'lucide-react'

interface Note {
  id: number
  content: string
  entityType: string
  entityId: number
  createdBy: number
  createdByName: string
  companyId: number
  createdAt: string
  updatedAt: string
}

interface LeadTimelineProps {
  leadId: number
  companyId: number
}

export function LeadTimeline({ leadId, companyId }: LeadTimelineProps) {
  const [notes, setNotes] = useState<Note[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [newNote, setNewNote] = useState('')
  const [editingNote, setEditingNote] = useState<number | null>(null)
  const [editContent, setEditContent] = useState('')
  const { toast } = useToast()

  useEffect(() => {
    fetchNotes()
  }, [leadId])

  const fetchNotes = async () => {
    setIsLoading(true)
    try {
      const response = await fetch(`http://localhost:8080/crm/api/notes?entityType=LEAD&entityId=${leadId}`, {
        headers: {
          'Authorization': getAuthHeader() || ''
        }
      })
      if (response.ok) {
        const data = await response.json()
        setNotes(data)
      }
    } catch (error) {
      console.error('Error fetching notes:', error)
      toast({
        title: "Error",
        description: "Failed to load notes.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleAddNote = async () => {
    if (!newNote.trim()) return

    try {
      const response = await fetch(`http://localhost:8080/crm/api/notes?companyId=${companyId}&userId=1`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': getAuthHeader() || ''
        },
        body: JSON.stringify({
          content: newNote,
          entityType: 'LEAD',
          entityId: leadId
        })
      })

      if (response.ok) {
        setNewNote('')
        fetchNotes()
        toast({
          title: "Note added",
          description: "Your note has been added successfully.",
        })
      } else {
        toast({
          title: "Error",
          description: "Failed to add note.",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error('Error adding note:', error)
      toast({
        title: "Error",
        description: "Failed to add note.",
        variant: "destructive",
      })
    }
  }

  const handleEditNote = async (noteId: number) => {
    if (!editContent.trim()) return

    try {
      const response = await fetch(`http://localhost:8080/crm/api/notes/${noteId}?companyId=${companyId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': getAuthHeader() || ''
        },
        body: JSON.stringify({
          content: editContent
        })
      })

      if (response.ok) {
        setEditingNote(null)
        setEditContent('')
        fetchNotes()
        toast({
          title: "Note updated",
          description: "Your note has been updated successfully.",
        })
      } else {
        toast({
          title: "Error",
          description: "Failed to update note.",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error('Error updating note:', error)
      toast({
        title: "Error",
        description: "Failed to update note.",
        variant: "destructive",
      })
    }
  }

  const handleDeleteNote = async (noteId: number) => {
    if (confirm('Are you sure you want to delete this note?')) {
      try {
        const response = await fetch(`http://localhost:8080/crm/api/notes/${noteId}?companyId=${companyId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': getAuthHeader() || ''
          }
        })

        if (response.ok) {
          fetchNotes()
          toast({
            title: "Note deleted",
            description: "Note has been deleted successfully.",
          })
        } else {
          toast({
            title: "Error",
            description: "Failed to delete note.",
            variant: "destructive",
          })
        }
      } catch (error) {
        console.error('Error deleting note:', error)
        toast({
          title: "Error",
          description: "Failed to delete note.",
          variant: "destructive",
        })
      }
    }
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString() + ' at ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <MessageSquare className="h-5 w-5" />
          Notes & Timeline
        </CardTitle>
        <CardDescription>
          Track conversations, updates, and important information
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Add New Note */}
        <div className="space-y-3">
          <Label htmlFor="newNote">Add a note</Label>
          <Textarea
            id="newNote"
            placeholder="Write a note about this lead..."
            value={newNote}
            onChange={(e) => setNewNote(e.target.value)}
            rows={3}
          />
          <div className="flex justify-end">
            <Button onClick={handleAddNote} disabled={!newNote.trim()}>
              <Plus className="h-4 w-4 mr-2" />
              Add Note
            </Button>
          </div>
        </div>

        <Separator />

        {/* Notes Timeline */}
        <div className="space-y-4">
          {isLoading ? (
            <div className="text-center py-4">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
            </div>
          ) : notes.length === 0 ? (
            <div className="text-center py-8">
              <MessageSquare className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
              <h3 className="text-lg font-semibold mb-2">No notes yet</h3>
              <p className="text-muted-foreground">
                Add the first note to start tracking this lead's timeline.
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              <h3 className="font-semibold flex items-center gap-2">
                <Clock className="h-4 w-4" />
                Timeline ({notes.length} notes)
              </h3>
              
              <div className="space-y-3">
                {notes.map((note) => (
                  <div key={note.id} className="border rounded-lg p-4 space-y-3">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center">
                          <User className="h-4 w-4 text-primary" />
                        </div>
                        <div>
                          <p className="font-medium text-sm">{note.createdByName}</p>
                          <p className="text-xs text-muted-foreground">
                            {formatDate(note.createdAt)}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-1">
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => {
                            setEditingNote(note.id)
                            setEditContent(note.content)
                          }}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleDeleteNote(note.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                    
                    {editingNote === note.id ? (
                      <div className="space-y-3">
                        <Textarea
                          value={editContent}
                          onChange={(e) => setEditContent(e.target.value)}
                          rows={3}
                        />
                        <div className="flex justify-end gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => {
                              setEditingNote(null)
                              setEditContent('')
                            }}
                          >
                            <X className="h-4 w-4 mr-1" />
                            Cancel
                          </Button>
                          <Button
                            size="sm"
                            onClick={() => handleEditNote(note.id)}
                          >
                            <Save className="h-4 w-4 mr-1" />
                            Save
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div className="text-sm text-gray-700 whitespace-pre-wrap">
                        {note.content}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
