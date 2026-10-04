import type { Label } from './api'

type Props = {
  labels: Label[]
  value: number | null
  onChange: (value: number | null) => void
}

export default function LabelFilter({ labels, value, onChange }: Props) {
  return <label className="label-filter">Label
    <select value={value ?? ''} onChange={(event) => onChange(event.target.value ? Number(event.target.value) : null)}>
      <option value="">All labels</option>
      {labels.map((label) => <option value={label.id} key={label.id}>{label.name}</option>)}
    </select>
  </label>
}
