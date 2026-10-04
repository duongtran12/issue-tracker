import type { FormEvent } from 'react'
import type { BackendIssue, Label, ProjectMember } from './api'
import IssueLabelPicker from './IssueLabelPicker'

type Props = {
  loading: boolean
  title: string
  description: string
  priority: BackendIssue['priority']
  assignee: string
  dueDate: string
  labelIds: number[]
  members: ProjectMember[]
  labels: Label[]
  onTitleChange: (value: string) => void
  onDescriptionChange: (value: string) => void
  onPriorityChange: (value: BackendIssue['priority']) => void
  onAssigneeChange: (value: string) => void
  onDueDateChange: (value: string) => void
  onLabelIdsChange: (value: number[]) => void
  onClose: () => void
  onSubmit: (event: FormEvent) => void
}

export default function CreateIssueModal(props: Props) {
  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) props.onClose() }}>
    <form className="issue-form" onSubmit={props.onSubmit}>
      <div className="issue-form-heading"><div><div className="eyebrow">NEW ISSUE</div><h2>Create an issue</h2></div><button type="button" className="icon-button" aria-label="Close create issue dialog" onClick={props.onClose}>X</button></div>
      <label>Title<input value={props.title} onChange={(event) => props.onTitleChange(event.target.value)} placeholder="What needs attention?" required maxLength={200} autoFocus /></label>
      <label>Description<textarea value={props.description} onChange={(event) => props.onDescriptionChange(event.target.value)} placeholder="Add useful context" maxLength={5000} rows={4} /></label>
      <label>Priority<select value={props.priority} onChange={(event) => props.onPriorityChange(event.target.value as BackendIssue['priority'])}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option></select></label>
      <label>Assignee<select value={props.assignee} onChange={(event) => props.onAssigneeChange(event.target.value)}><option value="">Unassigned</option>{props.members.map((member) => <option key={member.userId} value={member.username}>{member.fullName} ({member.username})</option>)}</select></label>
      <label>Due date<input type="date" value={props.dueDate} onChange={(event) => props.onDueDateChange(event.target.value)} /></label>
      <IssueLabelPicker labels={props.labels} selectedIds={props.labelIds} onChange={props.onLabelIdsChange} />
      <div className="issue-form-actions"><button type="button" className="filter" onClick={props.onClose}>Cancel</button><button className="create-button" type="submit" disabled={props.loading}>{props.loading ? 'Creating...' : 'Create issue'}</button></div>
    </form>
  </div>
}
