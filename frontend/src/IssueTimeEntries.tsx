import { useEffect, useMemo, useState } from 'react'
import { createTimeEntry, deleteTimeEntry, listTimeEntries, updateTimeEntry } from './api'
import type { TimeEntry } from './api'
import type { FormEvent } from 'react'
import { formatDuration } from './duration'
import { localDateInputValue } from './dates'

type Props = { projectId: number; issueId: number; username: string; estimateMinutes: number | null }

export default function IssueTimeEntries({ projectId, issueId, username, estimateMinutes }: Props) {
  const [entries, setEntries] = useState<TimeEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [minutes, setMinutes] = useState('')
  const [workDate, setWorkDate] = useState(localDateInputValue)
  const today = localDateInputValue()
  const [note, setNote] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editMinutes, setEditMinutes] = useState('')
  const [editNote, setEditNote] = useState('')
  const totalMinutes = useMemo(() => entries.reduce((sum, entry) => sum + entry.minutes, 0), [entries])
  const remainingMinutes = estimateMinutes === null ? null : estimateMinutes - totalMinutes

  useEffect(() => {
    listTimeEntries(projectId, issueId)
      .then(setEntries)
      .catch((reason: Error) => setError(reason.message))
      .finally(() => setLoading(false))
  }, [issueId, projectId])

  const addEntry = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    try {
      const created = await createTimeEntry(projectId, issueId, Number(minutes), workDate, note)
      setEntries((current) => [created, ...current])
      setMinutes('')
      setNote('')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to log time')
    }
  }

  const removeEntry = async (entryId: number) => {
    setError('')
    try {
      await deleteTimeEntry(projectId, issueId, entryId)
      setEntries((current) => current.filter((entry) => entry.id !== entryId))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to delete time entry')
    }
  }

  const saveEntry = async (entry: TimeEntry) => {
    setError('')
    try {
      const updated = await updateTimeEntry(projectId, issueId, {
        ...entry, minutes: Number(editMinutes), note: editNote.trim() || null,
      })
      setEntries((current) => current.map((item) => item.id === updated.id ? updated : item))
      setEditingId(null)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to update time entry')
    }
  }

  const startEditing = (entry: TimeEntry) => {
    setEditingId(entry.id)
    setEditMinutes(String(entry.minutes))
    setEditNote(entry.note ?? '')
  }

  return <section className="time-panel" aria-labelledby="time-heading">
    <div className="checklist-heading"><h3 id="time-heading">Time tracking</h3><span>{formatDuration(totalMinutes)}</span></div>
    {estimateMinutes !== null && <p className={`time-variance ${remainingMinutes !== null && remainingMinutes < 0 ? 'over-budget' : ''}`}>{remainingMinutes !== null && remainingMinutes >= 0 ? `${formatDuration(remainingMinutes)} remaining` : `${formatDuration(Math.abs(remainingMinutes ?? 0))} over estimate`}</p>}
    <form className="time-entry-form" onSubmit={addEntry}><input type="number" min="1" value={minutes} onChange={(event) => setMinutes(event.target.value)} placeholder="Minutes" required /><input type="date" max={today} value={workDate} onChange={(event) => setWorkDate(event.target.value)} required /><input value={note} onChange={(event) => setNote(event.target.value)} placeholder="What did you work on?" maxLength={1000} /><button className="create-button" disabled={!minutes}>Log time</button></form>
    {error && <p className="activity-error">{error}</p>}
    {loading ? <p className="activity-loading">Loading time entries...</p> : <ul className="time-entry-list">{entries.map((entry) => <li key={entry.id}>{editingId === entry.id ? <div className="time-entry-edit"><input type="number" min="1" value={editMinutes} onChange={(event) => setEditMinutes(event.target.value)} /><input value={editNote} onChange={(event) => setEditNote(event.target.value)} maxLength={1000} /><button type="button" className="create-button" onClick={() => saveEntry(entry)}>Save</button></div> : <><strong>{formatDuration(entry.minutes)}</strong><span>{entry.note ?? 'No note'}</span><small>{entry.username} · {entry.workDate}</small>{entry.username === username && <span className="time-entry-actions"><button className="filter" type="button" onClick={() => startEditing(entry)}>Edit</button><button className="icon-button" type="button" aria-label={`Delete ${entry.minutes} minute time entry`} onClick={() => removeEntry(entry.id)}>×</button></span>}</>}</li>)}{entries.length === 0 && <li className="activity-empty">No time logged yet.</li>}</ul>}
  </section>
}
