import { useState } from 'react'
import type { FormEvent } from 'react'
import { addProjectMember, removeProjectMember } from './api'
import type { Project, ProjectMember } from './api'

type ProjectMembersModalProps = {
  project: Project
  members: ProjectMember[]
  currentUsername: string
  onClose: () => void
  onMembersChange: (members: ProjectMember[]) => void
}

export default function ProjectMembersModal({
  project,
  members,
  currentUsername,
  onClose,
  onMembersChange,
}: ProjectMembersModalProps) {
  const [username, setUsername] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const canManage = project.ownerUsername === currentUsername

  const addMember = async (event: FormEvent) => {
    event.preventDefault()
    const normalizedUsername = username.trim()
    if (!normalizedUsername) return

    setSubmitting(true)
    setError('')
    try {
      const member = await addProjectMember(project.id, normalizedUsername)
      onMembersChange([...members, member])
      setUsername('')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to add member')
    } finally {
      setSubmitting(false)
    }
  }

  const removeMember = async (member: ProjectMember) => {
    if (!window.confirm(`Remove ${member.username} from this project?`)) return
    setSubmitting(true)
    setError('')
    try {
      await removeProjectMember(project.id, member.username)
      onMembersChange(members.filter((item) => item.userId !== member.userId))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to remove member')
    } finally {
      setSubmitting(false)
    }
  }

  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <section className="issue-form member-modal" role="dialog" aria-modal="true" aria-labelledby="member-modal-title">
      <div className="issue-form-heading">
        <div><div className="eyebrow">{project.key} / TEAM</div><h2 id="member-modal-title">Project members</h2></div>
        <button type="button" className="icon-button" aria-label="Close project members dialog" onClick={onClose}>X</button>
      </div>
      {canManage && <form className="member-add-form" onSubmit={addMember}>
        <label>Username<input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="Add an existing user" required maxLength={50} /></label>
        <button className="create-button" type="submit" disabled={submitting || !username.trim()}>{submitting ? 'Adding...' : 'Add member'}</button>
      </form>}
      {error && <div className="error-message" role="alert">{error}</div>}
      <ul className="member-list">
        {members.map((member) => <li key={member.userId}>
          <span className="avatar">{member.username.slice(0, 2).toUpperCase()}</span>
          <span><strong>{member.fullName}</strong><small>@{member.username}</small></span>
          <span className="member-role">{member.role}</span>
          {canManage && member.role !== 'OWNER' && <button className="danger-button" type="button" onClick={() => void removeMember(member)} disabled={submitting}>Remove</button>}
        </li>)}
      </ul>
      {!canManage && <p className="member-note">Only the project owner can add or remove members.</p>}
    </section>
  </div>
}
