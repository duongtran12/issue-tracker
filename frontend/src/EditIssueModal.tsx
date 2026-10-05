import type { FormEvent } from 'react'
import type { BackendIssue, Label, ProjectMember } from './api'
import IssueLabelPicker from './IssueLabelPicker'

type Props = {
  issue: BackendIssue
  members: ProjectMember[]
  labels: Label[]
  loading: boolean
  onChange: (issue: BackendIssue) => void
  onClose: () => void
  onDelete: () => void
  onSubmit: (event: FormEvent) => void
}

export default function EditIssueModal({ issue, members, labels, loading, onChange, onClose, onDelete, onSubmit }: Props) {
  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <form className="issue-form" onSubmit={onSubmit}>
      <div className="issue-form-heading"><div><div className="eyebrow">ISSUE-{issue.id}</div><h2>Edit issue</h2></div><button type="button" className="icon-button" aria-label="Close edit issue dialog" onClick={onClose}>X</button></div>
      <label>Title<input value={issue.title} onChange={(event) => onChange({ ...issue, title: event.target.value })} required maxLength={200} autoFocus /></label>
      <label>Description<textarea value={issue.description ?? ''} onChange={(event) => onChange({ ...issue, description: event.target.value })} maxLength={5000} rows={4} /></label>
      <label>Status<select value={issue.status} onChange={(event) => onChange({ ...issue, status: event.target.value as BackendIssue['status'] })}><option value="TODO">Todo</option><option value="IN_PROGRESS">In progress</option><option value="DONE">Done</option></select></label>
      <label>Priority<select value={issue.priority} onChange={(event) => onChange({ ...issue, priority: event.target.value as BackendIssue['priority'] })}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option></select></label>
      <label>Assignee<select value={issue.assigneeUsername ?? ''} onChange={(event) => onChange({ ...issue, assigneeUsername: event.target.value || null })}><option value="">Unassigned</option>{members.map((member) => <option key={member.userId} value={member.username}>{member.fullName} ({member.username})</option>)}</select></label>
      <label>Due date<input type="date" value={issue.dueDate ?? ''} onChange={(event) => onChange({ ...issue, dueDate: event.target.value || null })} /></label>
      <label>Estimate (minutes)<input type="number" min="1" value={issue.estimateMinutes ?? ''} onChange={(event) => onChange({ ...issue, estimateMinutes: event.target.value ? Number(event.target.value) : null })} /></label>
      <IssueLabelPicker labels={labels} selectedIds={issue.labels.map((label) => label.id)} onChange={(ids) => onChange({ ...issue, labels: labels.filter((label) => ids.includes(label.id)) })} />
      <div className="issue-form-actions split-actions"><button type="button" className="danger-button" onClick={onDelete} disabled={loading}>Delete issue</button><span /><button type="button" className="filter" onClick={onClose}>Cancel</button><button className="create-button" type="submit" disabled={loading || !issue.title.trim()}>{loading ? 'Saving...' : 'Save changes'}</button></div>
    </form>
  </div>
}
