import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { apiFetch, login, logout } from './api'
import type { BackendIssue, IssuePage, Project } from './api'
import './App.css'

type IssueStatus = 'Todo' | 'In progress' | 'Done'
type Issue = BackendIssue & { displayStatus: IssueStatus; displayPriority: 'High' | 'Medium' | 'Low'; assigneeInitials: string }

const statusMap: Record<BackendIssue['status'], IssueStatus> = { TODO: 'Todo', IN_PROGRESS: 'In progress', DONE: 'Done' }
const priorityMap: Record<BackendIssue['priority'], Issue['displayPriority']> = { LOW: 'Low', MEDIUM: 'Medium', HIGH: 'High' }

function initials(username: string | null) {
  if (!username) return '—'
  return username.slice(0, 2).toUpperCase()
}

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('issue_tracker_token'))
  const [username, setUsername] = useState('duong')
  const [password, setPassword] = useState('Password123!')
  const [projects, setProjects] = useState<Project[]>([])
  const [activeProjectId, setActiveProjectId] = useState<number | null>(null)
  const [issues, setIssues] = useState<BackendIssue[]>([])
  const [statusFilter, setStatusFilter] = useState<'All' | IssueStatus>('All')
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [isCreateIssueOpen, setIsCreateIssueOpen] = useState(false)
  const [newIssueTitle, setNewIssueTitle] = useState('')
  const [newIssueDescription, setNewIssueDescription] = useState('')
  const [newIssuePriority, setNewIssuePriority] = useState<BackendIssue['priority']>('MEDIUM')

  const activeProject = projects.find((project) => project.id === activeProjectId) ?? projects[0]

  useEffect(() => {
    const createButton = document.querySelector<HTMLButtonElement>('.top-actions .create-button')
    if (!createButton) return
    const openDialog = () => setIsCreateIssueOpen(true)
    createButton.addEventListener('click', openDialog)
    return () => createButton.removeEventListener('click', openDialog)
  }, [activeProjectId])
  useEffect(() => {
    if (!token) return
    setLoading(true)
    setError('')
    apiFetch<Project[]>('/projects')
      .then((result) => {
        setProjects(result)
        setActiveProjectId((current) => current ?? result[0]?.id ?? null)
      })
      .catch((reason: Error) => { setError(reason.message); setToken(null); logout() })
      .finally(() => setLoading(false))
  }, [token])

  useEffect(() => {
    if (!activeProjectId || !token) return
    setLoading(true)
    apiFetch<IssuePage>(`/projects/${activeProjectId}/issues?size=100&sort=createdAt,desc`)
      .then((result) => setIssues(result.content))
      .catch((reason: Error) => setError(reason.message))
      .finally(() => setLoading(false))
  }, [activeProjectId, token])

  const visibleIssues = useMemo<Issue[]>(() => issues.map((issue) => ({
    ...issue,
    displayStatus: statusMap[issue.status],
    displayPriority: priorityMap[issue.priority],
    assigneeInitials: initials(issue.assigneeUsername),
  })).filter((issue) => {
    const matchesStatus = statusFilter === 'All' || issue.displayStatus === statusFilter
    const matchesQuery = `${issue.id} ${issue.title} ${issue.description ?? ''}`.toLowerCase().includes(query.toLowerCase())
    return matchesStatus && matchesQuery
  }), [issues, query, statusFilter])

  const handleLogin = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      await login(username, password)
      setToken(localStorage.getItem('issue_tracker_token'))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to login')
    } finally { setLoading(false) }
  }

  const moveIssue = async (issue: Issue) => {
    const nextStatus: BackendIssue['status'] = issue.status === 'TODO' ? 'IN_PROGRESS' : issue.status === 'IN_PROGRESS' ? 'DONE' : 'TODO'
    try {
      const updated = await apiFetch<BackendIssue>(`/projects/${issue.projectId}/issues/${issue.id}`, {
        method: 'PUT',
        body: JSON.stringify({ title: issue.title, description: issue.description, status: nextStatus, priority: issue.priority, assigneeUsername: issue.assigneeUsername }),
      })
      setIssues((current) => current.map((item) => item.id === updated.id ? updated : item))
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'Unable to update issue') }
  }

  const createIssue = async (event: FormEvent) => {
    event.preventDefault()
    if (!activeProjectId || !newIssueTitle.trim()) return

    setLoading(true)
    setError('')
    try {
      const created = await apiFetch<BackendIssue>(`/projects/${activeProjectId}/issues`, {
        method: 'POST',
        body: JSON.stringify({
          title: newIssueTitle.trim(),
          description: newIssueDescription.trim() || null,
          status: 'TODO',
          priority: newIssuePriority,
          assigneeUsername: null,
        }),
      })
      setIssues((current) => [created, ...current])
      setNewIssueTitle('')
      setNewIssueDescription('')
      setNewIssuePriority('MEDIUM')
      setIsCreateIssueOpen(false)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to create issue')
    } finally { setLoading(false) }
  }

  if (!token) return <main className="auth-shell"><form className="login-card" onSubmit={handleLogin}><div className="brand"><span className="brand-mark">IT</span><span>issue tracker</span></div><div className="eyebrow">ACME STUDIO / WORKSPACE</div><h1>Welcome back.</h1><p>Sign in to see what needs shipping next.</p><label>Username<input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" /></label><label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" /></label>{error && <div className="error-message">{error}</div>}<button className="create-button login-button" disabled={loading}>{loading ? 'Signing in...' : 'Sign in'}</button><small>Use a registered account from the backend.</small></form></main>

  return <main className="app-shell">
    <aside className="sidebar"><div className="brand"><span className="brand-mark">IT</span><span>issue tracker</span></div><div className="workspace-label">Workspace</div><button className="workspace-switcher" type="button"><span className="workspace-dot" /> Acme Studio <span className="chevron">⌄</span></button><nav className="main-nav"><button className="nav-item active" type="button"><span>▦</span> Overview</button><button className="nav-item" type="button"><span>◈</span> My issues</button><button className="nav-item" type="button"><span>◷</span> Activity</button></nav><div className="projects-heading"><span>Projects</span></div><div className="project-list">{projects.map((project) => <button key={project.id} type="button" className={`project-item ${activeProject?.id === project.id ? 'selected' : ''}`} onClick={() => setActiveProjectId(project.id)}><span className="project-icon">{activeProject?.id === project.id ? '●' : '○'}</span>{project.name}</button>)}</div><div className="sidebar-bottom"><button className="nav-item" type="button" onClick={() => { logout(); setToken(null) }}><span>↪</span> Sign out</button><div className="user-chip"><span className="avatar">{initials(username)}</span><span><strong>{username}</strong><small>Authenticated</small></span></div></div></aside>
    <section className="content"><header className="topbar"><div className="breadcrumbs"><span>Projects</span><b>/</b><strong>{activeProject?.name ?? 'Loading...'}</strong></div><div className="top-actions"><label className="search"><span>⌕</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search issues" /><kbd>⌘ K</kbd></label><button className="create-button" type="button"><span>+</span> Create issue</button></div></header><div className="page-heading"><div><div className="eyebrow">PROJECT / {activeProject?.key ?? '...'}</div><h1>{activeProject?.name ?? 'Your projects'}</h1><p>{activeProject?.description ?? 'Ship with clarity. Keep every moving part visible.'}</p></div><div className="heading-meta"><span className="updated">{loading ? 'Syncing...' : `${issues.length} issues loaded`}</span></div></div>{error && <div className="api-error">{error}</div>}<div className="toolbar"><div className="filters"><span className="filter-label">View</span>{(['All', 'Todo', 'In progress', 'Done'] as const).map((filter) => <button key={filter} type="button" className={`filter ${statusFilter === filter ? 'active' : ''}`} onClick={() => setStatusFilter(filter)}>{filter}</button>)}</div><div className="toolbar-actions"><span>Live API data</span></div></div><div className="board">{(['Todo', 'In progress', 'Done'] as IssueStatus[]).map((status) => <section className="column" key={status}><div className="column-heading"><div><span className={`status-dot ${status.toLowerCase().replace(' ', '-')}`} /><h2>{status}</h2><span className="issue-count">{visibleIssues.filter((issue) => issue.displayStatus === status).length}</span></div></div><div className="issue-list">{visibleIssues.filter((issue) => issue.displayStatus === status).map((issue) => <article className="issue-card" key={issue.id}><div className="issue-card-top"><span className="issue-id">ISSUE-{issue.id}</span><button type="button" aria-label={`Move issue ${issue.id}`} onClick={() => moveIssue(issue)}>•••</button></div><h3>{issue.title}</h3><div className="card-footer"><span className={`priority ${issue.displayPriority.toLowerCase()}`}><i /> {issue.displayPriority}</span><span className="label">{issue.assigneeUsername ?? 'Unassigned'}</span><span className="avatar small">{issue.assigneeInitials}</span></div></article>)}{visibleIssues.filter((issue) => issue.displayStatus === status).length === 0 && <div className="empty-column">Nothing here yet</div>}</div></section>)}</div><footer className="board-footer"><span><b>{visibleIssues.length}</b> issues in view</span><span className="legend"><i className="priority high-dot" /> High priority <i className="priority medium-dot" /> Medium <i className="priority low-dot" /> Low</span></footer></section>
    {isCreateIssueOpen && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setIsCreateIssueOpen(false) }}><form className="issue-form" onSubmit={createIssue}><div className="issue-form-heading"><div><div className="eyebrow">NEW ISSUE</div><h2>Create an issue</h2></div><button type="button" className="icon-button" aria-label="Close create issue dialog" onClick={() => setIsCreateIssueOpen(false)}>X</button></div><label>Title<input value={newIssueTitle} onChange={(event) => setNewIssueTitle(event.target.value)} placeholder="What needs attention?" required maxLength={200} autoFocus /></label><label>Description<textarea value={newIssueDescription} onChange={(event) => setNewIssueDescription(event.target.value)} placeholder="Add useful context" maxLength={5000} rows={4} /></label><label>Priority<select value={newIssuePriority} onChange={(event) => setNewIssuePriority(event.target.value as BackendIssue['priority'])}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option></select></label><div className="issue-form-actions"><button type="button" className="filter" onClick={() => setIsCreateIssueOpen(false)}>Cancel</button><button className="create-button" type="submit" disabled={loading}>{loading ? 'Creating...' : 'Create issue'}</button></div></form></div>}
  </main>
}

export default App
