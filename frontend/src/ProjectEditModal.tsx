import { useState } from 'react'
import type { FormEvent } from 'react'
import { updateProject } from './api'
import type { Project } from './api'

type ProjectEditModalProps = {
  project: Project
  onClose: () => void
  onUpdated: (project: Project) => void
}

export default function ProjectEditModal({ project, onClose, onUpdated }: ProjectEditModalProps) {
  const [name, setName] = useState(project.name)
  const [key, setKey] = useState(project.key)
  const [description, setDescription] = useState(project.description ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      const updated = await updateProject(project.id, {
        name: name.trim(),
        key: key.trim().toUpperCase(),
        description: description.trim() || null,
      })
      onUpdated(updated)
      onClose()
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to update project')
    } finally {
      setSaving(false)
    }
  }

  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <form className="issue-form project-form" onSubmit={submit}>
      <div className="issue-form-heading">
        <div><div className="eyebrow">PROJECT SETTINGS</div><h2>Edit project</h2></div>
        <button type="button" className="icon-button" aria-label="Close edit project dialog" onClick={onClose}>X</button>
      </div>
      <label>Name<input value={name} onChange={(event) => setName(event.target.value)} required maxLength={100} autoFocus /></label>
      <label>Key<input value={key} onChange={(event) => setKey(event.target.value.replace(/\s/g, '').toUpperCase())} required maxLength={20} pattern="[A-Z0-9_-]+" /></label>
      <label>Description<textarea value={description} onChange={(event) => setDescription(event.target.value)} maxLength={1000} rows={4} /></label>
      {error && <div className="error-message" role="alert">{error}</div>}
      <div className="issue-form-actions">
        <button type="button" className="filter" onClick={onClose}>Cancel</button>
        <button className="create-button" type="submit" disabled={saving || !name.trim() || !key.trim()}>{saving ? 'Saving...' : 'Save project'}</button>
      </div>
    </form>
  </div>
}
