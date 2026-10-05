import { useEffect, useMemo, useState } from 'react'
import { createTimeEntry, listTimeEntries } from './api'
import type { TimeEntry } from './api'
import type { FormEvent } from 'react'
import { formatDuration } from './duration'

type Props = { projectId: number; issueId: number; username: string }

export default function IssueTimeEntries({ projectId, issueId, username }: Props) {
  const [entries, setEntries] = useState<TimeEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [minutes, setMinutes] = useState('')
  const [workDate, setWorkDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [note, setNote] = useState('')
  const totalMinutes = useMemo(() => entries.reduce((sum, entry) => sum + entry.minutes, 0), [entries])

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

  return <section className="time-panel" aria-labelledby="time-heading">
    <div className="checklist-heading"><h3 id="time-heading">Time tracking</h3><span>{formatDuration(totalMinutes)}</span></div>
    <form className="time-entry-form" onSubmit={addEntry}><input type="number" min="1" value={minutes} onChange={(event) => setMinutes(event.target.value)} placeholder="Minutes" required /><input type="date" value={workDate} onChange={(event) => setWorkDate(event.target.value)} required /><input value={note} onChange={(event) => setNote(event.target.value)} placeholder="What did you work on?" maxLength={1000} /><button className="create-button" disabled={!minutes}>Log time</button></form>
    {error && <p className="activity-error">{error}</p>}
    {loading ? <p className="activity-loading">Loading time entries...</p> : <ul className="time-entry-list">{entries.map((entry) => <li key={entry.id}><strong>{formatDuration(entry.minutes)}</strong><span>{entry.note ?? 'No note'}</span><small>{entry.username} · {entry.workDate}</small>{entry.username === username && <button className="icon-button" type="button">•••</button>}</li>)}{entries.length === 0 && <li className="activity-empty">No time logged yet.</li>}</ul>}
  </section>
}
