import type { Label } from './api'

type Props = {
  labels: Label[]
  selectedIds: number[]
  onChange: (ids: number[]) => void
}

export default function IssueLabelPicker({ labels, selectedIds, onChange }: Props) {
  const toggle = (labelId: number) => {
    onChange(selectedIds.includes(labelId)
      ? selectedIds.filter((id) => id !== labelId)
      : [...selectedIds, labelId])
  }

  return <fieldset className="label-picker">
    <legend>Labels</legend>
    <div className="label-checkboxes">
      {labels.map((label) => <label key={label.id}>
        <input type="checkbox" checked={selectedIds.includes(label.id)} onChange={() => toggle(label.id)} />
        <span className="issue-label" style={{ backgroundColor: label.color }}>{label.name}</span>
      </label>)}
      {labels.length === 0 && <span className="label-picker-empty">Create a project label first.</span>}
    </div>
  </fieldset>
}
