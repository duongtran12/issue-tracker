import type { BackendIssue } from './api'
import { formatDueDate, isOverdue } from './dates'
import { formatDuration } from './duration'

type DisplayIssue = BackendIssue & {
  displayPriority: 'High' | 'Medium' | 'Low'
  assigneeInitials: string
}

type Props = {
  issue: DisplayIssue
  onEdit: () => void
  onMove: () => void
}

export default function IssueCard({ issue, onEdit, onMove }: Props) {
  return <article className="issue-card">
    <div className="issue-card-top"><span className="issue-id">ISSUE-{issue.id}</span><div><button type="button" aria-label={`Edit issue ${issue.id}`} onClick={onEdit}>Edit</button><button type="button" aria-label={`Move issue ${issue.id}`} onClick={onMove}>Move</button></div></div>
    <h3>{issue.title}</h3>
    {issue.labels.length > 0 && <div className="issue-label-list">{issue.labels.map((label) => <span className="issue-label" style={{ backgroundColor: label.color }} key={label.id}>{label.name}</span>)}</div>}
    {issue.dueDate && <div className={`due-date ${isOverdue(issue.dueDate, issue.status) ? 'overdue' : ''}`}>Due {formatDueDate(issue.dueDate)}</div>}
    {issue.estimateMinutes && <div className="issue-estimate">Estimate {formatDuration(issue.estimateMinutes)}</div>}
    <div className="card-footer"><span className={`priority ${issue.displayPriority.toLowerCase()}`}><i /> {issue.displayPriority}</span><span className="label">{issue.assigneeUsername ?? 'Unassigned'}</span><span className="avatar small">{issue.assigneeInitials}</span></div>
  </article>
}
