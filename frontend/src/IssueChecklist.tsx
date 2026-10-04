import { useEffect, useMemo, useState } from 'react'
import { createChecklistItem, deleteChecklistItem, listChecklistItems, updateChecklistItem } from './api'
import type { ChecklistItem } from './api'
import type { FormEvent } from 'react'

type Props = {
  projectId: number
  issueId: number
}

export default function IssueChecklist({ projectId, issueId }: Props) {
  const [items, setItems] = useState<ChecklistItem[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [content, setContent] = useState('')
  const completedCount = useMemo(() => items.filter((item) => item.completed).length, [items])
  const completionPercent = items.length === 0 ? 0 : Math.round((completedCount / items.length) * 100)

  useEffect(() => {
    setLoading(true)
    setError('')
    listChecklistItems(projectId, issueId)
      .then(setItems)
      .catch((reason: Error) => setError(reason.message))
      .finally(() => setLoading(false))
  }, [issueId, projectId])

  const addItem = async (event: FormEvent) => {
    event.preventDefault()
    if (!content.trim()) return
    setLoading(true)
    setError('')
    try {
      const created = await createChecklistItem(projectId, issueId, content.trim())
      setItems((current) => [...current, created])
      setContent('')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to add checklist item')
    } finally {
      setLoading(false)
    }
  }

  const toggleItem = async (item: ChecklistItem) => {
    setError('')
    try {
      const updated = await updateChecklistItem(projectId, issueId, { ...item, completed: !item.completed })
      setItems((current) => current.map((candidate) => candidate.id === updated.id ? updated : candidate))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to update checklist item')
    }
  }

  const removeItem = async (itemId: number) => {
    setError('')
    try {
      await deleteChecklistItem(projectId, issueId, itemId)
      setItems((current) => current.filter((item) => item.id !== itemId))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to delete checklist item')
    }
  }

  return <section className="checklist-panel" aria-labelledby="checklist-heading">
    <div className="checklist-heading"><h3 id="checklist-heading">Checklist</h3><span>{completedCount}/{items.length}</span></div>
    <div className="checklist-progress" role="progressbar" aria-label="Checklist progress" aria-valuemin={0} aria-valuemax={100} aria-valuenow={completionPercent}><span style={{ width: `${completionPercent}%` }} /></div>
    <form className="checklist-add" onSubmit={addItem}><input value={content} onChange={(event) => setContent(event.target.value)} placeholder="Add a checklist item" maxLength={500} /><button className="create-button" disabled={loading || !content.trim()}>Add</button></form>
    {error && <p className="activity-error">{error}</p>}
    {loading ? <p className="activity-loading">Loading checklist...</p> : <ul className="checklist-list">{items.map((item) => <li key={item.id}><input type="checkbox" checked={item.completed} onChange={() => toggleItem(item)} /><span className={item.completed ? 'completed' : ''}>{item.content}</span><button type="button" className="icon-button" aria-label={`Delete ${item.content}`} onClick={() => removeItem(item.id)}>×</button></li>)}{items.length === 0 && <li className="activity-empty">No checklist items yet.</li>}</ul>}
  </section>
}
