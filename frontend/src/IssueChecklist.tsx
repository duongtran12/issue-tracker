import { useEffect, useMemo, useState } from 'react'
import { listChecklistItems } from './api'
import type { ChecklistItem } from './api'

type Props = {
  projectId: number
  issueId: number
}

export default function IssueChecklist({ projectId, issueId }: Props) {
  const [items, setItems] = useState<ChecklistItem[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const completedCount = useMemo(() => items.filter((item) => item.completed).length, [items])

  useEffect(() => {
    setLoading(true)
    setError('')
    listChecklistItems(projectId, issueId)
      .then(setItems)
      .catch((reason: Error) => setError(reason.message))
      .finally(() => setLoading(false))
  }, [issueId, projectId])

  return <section className="checklist-panel" aria-labelledby="checklist-heading">
    <div className="checklist-heading"><h3 id="checklist-heading">Checklist</h3><span>{completedCount}/{items.length}</span></div>
    {error && <p className="activity-error">{error}</p>}
    {loading ? <p className="activity-loading">Loading checklist...</p> : <ul className="checklist-list">{items.map((item) => <li key={item.id}><input type="checkbox" checked={item.completed} readOnly /><span className={item.completed ? 'completed' : ''}>{item.content}</span></li>)}{items.length === 0 && <li className="activity-empty">No checklist items yet.</li>}</ul>}
  </section>
}
