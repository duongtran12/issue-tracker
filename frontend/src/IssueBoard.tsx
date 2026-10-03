import type { BackendIssue } from './api'
import IssueCard from './IssueCard'

export type DisplayStatus = 'Todo' | 'In progress' | 'Done'
export type DisplayIssue = BackendIssue & {
  displayStatus: DisplayStatus
  displayPriority: 'High' | 'Medium' | 'Low'
  assigneeInitials: string
}

type Props = {
  issues: DisplayIssue[]
  onEdit: (issue: DisplayIssue) => void
  onMove: (issue: DisplayIssue) => void
}

export default function IssueBoard({ issues, onEdit, onMove }: Props) {
  return <>
    <div className="board">{(['Todo', 'In progress', 'Done'] as DisplayStatus[]).map((status) => {
      const columnIssues = issues.filter((issue) => issue.displayStatus === status)
      return <section className="column" key={status}>
        <div className="column-heading"><div><span className={`status-dot ${status.toLowerCase().replace(' ', '-')}`} /><h2>{status}</h2><span className="issue-count">{columnIssues.length}</span></div></div>
        <div className="issue-list">{columnIssues.map((issue) => <IssueCard issue={issue} key={issue.id} onEdit={() => onEdit(issue)} onMove={() => onMove(issue)} />)}{columnIssues.length === 0 && <div className="empty-column">Nothing here yet</div>}</div>
      </section>
    })}</div>
    <footer className="board-footer"><span><b>{issues.length}</b> issues in view</span><span className="legend"><i className="priority high-dot" /> High priority <i className="priority medium-dot" /> Medium <i className="priority low-dot" /> Low</span></footer>
  </>
}
