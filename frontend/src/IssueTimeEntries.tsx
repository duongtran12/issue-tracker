import { useEffect, useMemo, useState } from 'react'
import { listTimeEntries } from './api'
import type { TimeEntry } from './api'
import { formatDuration } from './duration'

type Props = { projectId: number; issueId: number; username: string }

export default function IssueTimeEntries({ projectId, issueId, username }: Props) {
  const [entries, setEntries] = useState<TimeEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const totalMinutes = useMemo(() => entries.reduce((sum, entry) => sum + entry.minutes, 0), [entries])

  useEffect(() => {
    listTimeEntries(projectId, issueId)
      .then(setEntries)
      .catch((reason: Error) => setError(reason.message))
      .finally(() => setLoading(false))
  }, [issueId, projectId])

  return <section className="time-panel" aria-labelledby="time-heading">
    <div className="checklist-heading"><h3 id="time-heading">Time tracking</h3><span>{formatDuration(totalMinutes)}</span></div>
    {error && <p className="activity-error">{error}</p>}
    {loading ? <p className="activity-loading">Loading time entries...</p> : <ul className="time-entry-list">{entries.map((entry) => <li key={entry.id}><strong>{formatDuration(entry.minutes)}</strong><span>{entry.note ?? 'No note'}</span><small>{entry.username} · {entry.workDate}</small>{entry.username === username && <button className="icon-button" type="button">•••</button>}</li>)}{entries.length === 0 && <li className="activity-empty">No time logged yet.</li>}</ul>}
  </section>
}
