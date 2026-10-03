import { useState } from 'react'
import type { FormEvent } from 'react'
import { createProjectLabel, deleteProjectLabel, updateProjectLabel } from './api'
import type { Label, Project } from './api'

type Props = {
  project: Project
  labels: Label[]
  onClose: () => void
  onLabelsChange: (labels: Label[]) => void
}

export default function ProjectLabelsModal({ project, labels, onClose, onLabelsChange }: Props) {
  const [name, setName] = useState('')
  const [color, setColor] = useState('#2563EB')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)

  const addLabel = async (event: FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const created = await createProjectLabel(project.id, name.trim(), color)
      onLabelsChange([...labels, created].sort((left, right) => left.name.localeCompare(right.name)))
      setName('')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to create label')
    } finally {
      setBusy(false)
    }
  }

  const removeLabel = async (label: Label) => {
    if (!window.confirm(`Delete label "${label.name}"?`)) return
    setBusy(true)
    setError('')
    try {
      await deleteProjectLabel(project.id, label.id)
      onLabelsChange(labels.filter((item) => item.id !== label.id))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to delete label')
    } finally {
      setBusy(false)
    }
  }

  const startEditing = (label: Label) => {
    setEditingId(label.id)
    setName(label.name)
    setColor(label.color)
  }

  const saveLabel = async (event: FormEvent) => {
    event.preventDefault()
    if (editingId === null) return
    setBusy(true)
    setError('')
    try {
      const updated = await updateProjectLabel(project.id, editingId, name.trim(), color)
      onLabelsChange(labels.map((label) => label.id === updated.id ? updated : label)
        .sort((left, right) => left.name.localeCompare(right.name)))
      setEditingId(null)
      setName('')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to update label')
    } finally {
      setBusy(false)
    }
  }

  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <section className="issue-form labels-modal" role="dialog" aria-modal="true" aria-label="Project labels">
      <div className="issue-form-heading"><div><div className="eyebrow">PROJECT SETTINGS</div><h2>Labels</h2></div><button type="button" className="icon-button" onClick={onClose}>X</button></div>
      {error && <div className="api-error">{error}</div>}
      <form className="label-create-form" onSubmit={editingId === null ? addLabel : saveLabel}><input value={name} onChange={(event) => setName(event.target.value)} placeholder="Label name" required maxLength={50} /><input type="color" value={color} onChange={(event) => setColor(event.target.value)} aria-label="Label color" /><button className="create-button" disabled={busy || !name.trim()}>{editingId === null ? 'Add' : 'Save'}</button></form>
      <div className="label-list">{labels.map((label) => <div className="label-row" key={label.id}><span className="issue-label" style={{ backgroundColor: label.color }}>{label.name}</span><span className="label-actions"><button className="filter" type="button" onClick={() => startEditing(label)} disabled={busy}>Edit</button><button className="danger-button" type="button" onClick={() => removeLabel(label)} disabled={busy}>Delete</button></span></div>)}{labels.length === 0 && <p>No labels yet.</p>}</div>
    </section>
  </div>
}
