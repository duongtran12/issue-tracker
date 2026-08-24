import { useMemo, useState } from 'react'
import './App.css'

type IssueStatus = 'Todo' | 'In progress' | 'Done'

type Issue = {
  id: string
  title: string
  status: IssueStatus
  priority: 'High' | 'Medium' | 'Low'
  assignee: string
  label: string
}

const initialIssues: Issue[] = [
  { id: 'ISSUE-24', title: 'Refresh token expires too early', status: 'In progress', priority: 'High', assignee: 'DT', label: 'Security' },
  { id: 'ISSUE-23', title: 'Add project member permissions', status: 'In progress', priority: 'Medium', assignee: 'LM', label: 'Backend' },
  { id: 'ISSUE-21', title: 'Search should include descriptions', status: 'Todo', priority: 'Low', assignee: 'AK', label: 'API' },
  { id: 'ISSUE-19', title: 'Empty state for project board', status: 'Todo', priority: 'Medium', assignee: 'DT', label: 'Frontend' },
  { id: 'ISSUE-18', title: 'Create issue history timeline', status: 'Done', priority: 'Medium', assignee: 'LM', label: 'Backend' },
  { id: 'ISSUE-15', title: 'Improve login validation copy', status: 'Done', priority: 'Low', assignee: 'AK', label: 'UX' },
]

function App() {
  const [issues, setIssues] = useState(initialIssues)
  const [statusFilter, setStatusFilter] = useState<'All' | IssueStatus>('All')
  const [query, setQuery] = useState('')
  const [activeProject, setActiveProject] = useState('Issue Tracker')

  const filteredIssues = useMemo(() => issues.filter((issue) => {
    const matchesStatus = statusFilter === 'All' || issue.status === statusFilter
    const matchesQuery = `${issue.id} ${issue.title} ${issue.label}`.toLowerCase().includes(query.toLowerCase())
    return matchesStatus && matchesQuery
  }), [issues, query, statusFilter])

  const moveIssue = (id: string, status: IssueStatus) => {
    setIssues((current) => current.map((issue) => issue.id === id ? { ...issue, status } : issue))
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark">IT</span><span>issue tracker</span></div>
        <div className="workspace-label">Workspace</div>
        <button className="workspace-switcher" type="button"><span className="workspace-dot" /> Acme Studio <span className="chevron">⌄</span></button>
        <nav className="main-nav" aria-label="Main navigation">
          <button className="nav-item active" type="button"><span>▦</span> Overview</button>
          <button className="nav-item" type="button"><span>◈</span> My issues <b>4</b></button>
          <button className="nav-item" type="button"><span>◷</span> Activity</button>
        </nav>
        <div className="projects-heading"><span>Projects</span><button type="button" aria-label="Add project">+</button></div>
        <div className="project-list">
          {['Issue Tracker', 'Mobile App', 'Website refresh'].map((project) => <button key={project} type="button" className={`project-item ${activeProject === project ? 'selected' : ''}`} onClick={() => setActiveProject(project)}><span className="project-icon">{project === activeProject ? '●' : '○'}</span>{project}</button>)}
        </div>
        <div className="sidebar-bottom"><button className="nav-item" type="button"><span>⚙</span> Settings</button><div className="user-chip"><span className="avatar">DT</span><span><strong>Duong Tran</strong><small>Admin</small></span><span className="chevron">⌄</span></div></div>
      </aside>

      <section className="content">
        <header className="topbar"><div className="breadcrumbs"><span>Projects</span><b>/</b><strong>{activeProject}</strong></div><div className="top-actions"><label className="search"><span>⌕</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search issues" /><kbd>⌘ K</kbd></label><button className="icon-button" type="button" aria-label="Notifications">♢</button><button className="create-button" type="button"><span>+</span> Create issue</button></div></header>
        <div className="page-heading"><div><div className="eyebrow">PROJECT / ISSUE TRACKER</div><h1>{activeProject}</h1><p>Ship with clarity. Keep every moving part visible.</p></div><div className="heading-meta"><div className="member-stack"><span className="avatar pink">LM</span><span className="avatar yellow">AK</span><span className="avatar">DT</span><span className="more-members">+3</span></div><span className="meta-divider" /><span className="updated">Updated just now</span></div></div>
        <div className="toolbar"><div className="filters"><span className="filter-label">View</span>{(['All', 'Todo', 'In progress', 'Done'] as const).map((filter) => <button key={filter} type="button" className={`filter ${statusFilter === filter ? 'active' : ''}`} onClick={() => setStatusFilter(filter)}>{filter}</button>)}</div><div className="toolbar-actions"><button type="button">☷ Group: Status</button><button type="button">↕ Sort: Updated</button></div></div>
        <div className="board">{(['Todo', 'In progress', 'Done'] as IssueStatus[]).map((status) => <section className="column" key={status}><div className="column-heading"><div><span className={`status-dot ${status.toLowerCase().replace(' ', '-')}`} /><h2>{status}</h2><span className="issue-count">{filteredIssues.filter((issue) => issue.status === status).length}</span></div><button type="button" aria-label={`Add issue to ${status}`}>+</button></div><div className="issue-list">{filteredIssues.filter((issue) => issue.status === status).map((issue) => <article className="issue-card" key={issue.id}><div className="issue-card-top"><span className="issue-id">{issue.id}</span><button type="button" aria-label={`Move ${issue.id}`} onClick={() => moveIssue(issue.id, status === 'Todo' ? 'In progress' : status === 'In progress' ? 'Done' : 'Todo')}>•••</button></div><h3>{issue.title}</h3><div className="card-footer"><span className={`priority ${issue.priority.toLowerCase()}`}><i /> {issue.priority}</span><span className="label">{issue.label}</span><span className="avatar small">{issue.assignee}</span></div></article>)}{filteredIssues.filter((issue) => issue.status === status).length === 0 && <div className="empty-column">Nothing here yet</div>}</div></section>)}</div>
        <footer className="board-footer"><span><b>{filteredIssues.length}</b> issues in view</span><span className="legend"><i className="priority high-dot" /> High priority <i className="priority medium-dot" /> Medium <i className="priority low-dot" /> Low</span></footer>
      </section>
    </main>
  )
}

export default App
