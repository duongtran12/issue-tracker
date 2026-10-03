import { useState } from 'react'
import type { FormEvent } from 'react'
import { createProjectLabel, deleteProjectLabel } from './api'
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

  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <section className="issue-form labels-modal" role="dialog" aria-modal="true" aria-label="Project labels">
      <div className="issue-form-heading"><div><div className="eyebrow">PROJECT SETTINGS</div><h2>Labels</h2></div><button type="button" className="icon-button" onClick={onClose}>X</button></div>
      {error && <div className="api-error">{error}</div>}
      <form className="label-create-form" onSubmit={addLabel}><input value={name} onChange={(event) => setName(event.target.value)} placeholder="Label name" required maxLength={50} /><input type="color" value={color} onChange={(event) => setColor(event.target.value)} aria-label="Label color" /><button className="create-button" disabled={busy || !name.trim()}>Add</button></form>
      <div className="label-list">{labels.map((label) => <div className="label-row" key={label.id}><span className="issue-label" style={{ backgroundColor: label.color }}>{label.name}</span><button className="danger-button" type="button" onClick={() => removeLabel(label)} disabled={busy}>Delete</button></div>)}{labels.length === 0 && <p>No labels yet.</p>}</div>
    </section>
  </div>
}
