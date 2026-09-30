import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { createIssueComment, listIssueComments, listIssueHistory } from './api'
import type { IssueComment, IssueHistory } from './api'

type IssueOption = {
  id: number
  title: string
}

type IssueActivityPanelProps = {
  projectId: number | null
  issues: IssueOption[]
}

type IssueActivity = {
  issueId: number
  comments: IssueComment[]
  history: IssueHistory[]
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export default function IssueActivityPanel({ projectId, issues }: IssueActivityPanelProps) {
  const [selectedIssueId, setSelectedIssueId] = useState<number | null>(null)
  const [activity, setActivity] = useState<IssueActivity | null>(null)
  const [loadError, setLoadError] = useState<{ issueId: number; message: string } | null>(null)
  const [comment, setComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const activeIssueId = issues.some((issue) => issue.id === selectedIssueId)
    ? selectedIssueId
    : issues[0]?.id ?? null
  const selectedIssue = issues.find((issue) => issue.id === activeIssueId)
  const currentActivity = activity?.issueId === activeIssueId ? activity : null
  const currentError = loadError?.issueId === activeIssueId ? loadError : null

  useEffect(() => {
    if (!projectId || activeIssueId === null) return
    let current = true
    Promise.all([
      listIssueComments(projectId, activeIssueId),
      listIssueHistory(projectId, activeIssueId),
    ]).then(([comments, history]) => {
      if (!current) return
      setActivity({ issueId: activeIssueId, comments, history })
      setLoadError(null)
    }).catch((reason: unknown) => {
      if (!current) return
      setLoadError({
        issueId: activeIssueId,
        message: reason instanceof Error ? reason.message : 'Unable to load issue activity',
      })
    })
    return () => { current = false }
  }, [projectId, activeIssueId])

  const submitComment = async (event: FormEvent) => {
    event.preventDefault()
    const body = comment.trim()
    if (!projectId || !selectedIssue || !body) return

    setSubmitting(true)
    setLoadError(null)
    try {
      const created = await createIssueComment(projectId, selectedIssue.id, body)
      setActivity((current) => current?.issueId === selectedIssue.id
        ? { ...current, comments: [...current.comments, created] }
        : current)
      setComment('')
    } catch (reason) {
      setLoadError({
        issueId: selectedIssue.id,
        message: reason instanceof Error ? reason.message : 'Unable to add comment',
      })
    } finally {
      setSubmitting(false)
    }
  }

  if (!projectId || issues.length === 0) return null

  return <section className="activity-panel" aria-labelledby="activity-heading">
    <div className="activity-heading">
      <div>
        <div className="eyebrow">ISSUE DETAILS</div>
        <h2 id="activity-heading">Comments & history</h2>
      </div>
      <label className="activity-issue-select">
        <span>Issue</span>
        <select
          value={selectedIssue?.id ?? ''}
          onChange={(event) => setSelectedIssueId(Number(event.target.value))}
        >
          {issues.map((issue) => <option key={issue.id} value={issue.id}>ISSUE-{issue.id}: {issue.title}</option>)}
        </select>
      </label>
    </div>

    {currentError && <p className="activity-error" role="alert">{currentError.message}</p>}
    {!currentActivity && !currentError
      ? <p className="activity-loading" role="status">Loading activity...</p>
      : currentActivity && <div className="activity-columns">
        <section aria-labelledby="comments-heading">
          <h3 id="comments-heading">Comments <span>{currentActivity.comments.length}</span></h3>
          <form className="comment-form" onSubmit={submitComment}>
            <label className="visually-hidden" htmlFor="issue-comment">Add a comment</label>
            <textarea
              id="issue-comment"
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              placeholder="Write a comment..."
              maxLength={5000}
              rows={3}
            />
            <button className="create-button" type="submit" disabled={submitting || !comment.trim()}>
              {submitting ? 'Posting...' : 'Post comment'}
            </button>
          </form>
          <ol className="activity-list">
            {currentActivity.comments.map((item) => <li key={item.id}>
              <div className="activity-item-meta"><strong>{item.authorUsername}</strong><time dateTime={item.createdAt}>{formatDate(item.createdAt)}</time></div>
              <p>{item.body}</p>
            </li>)}
            {currentActivity.comments.length === 0 && <li className="activity-empty">No comments yet.</li>}
          </ol>
        </section>
        <section aria-labelledby="history-heading">
          <h3 id="history-heading">History <span>{currentActivity.history.length}</span></h3>
          <ol className="activity-list">
            {currentActivity.history.map((item) => <li key={item.id}>
              <div className="activity-item-meta"><strong>{item.actorUsername ?? 'System'}</strong><time dateTime={item.createdAt}>{formatDate(item.createdAt)}</time></div>
              <p>{item.eventType.replaceAll('_', ' ').toLowerCase()}{item.fieldName ? `: ${item.fieldName}` : ''}</p>
              {(item.oldValue || item.newValue) && <small>{item.oldValue ?? '—'} → {item.newValue ?? '—'}</small>}
            </li>)}
            {currentActivity.history.length === 0 && <li className="activity-empty">No history yet.</li>}
          </ol>
        </section>
      </div>}
  </section>
}
