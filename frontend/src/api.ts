const API_BASE_URL = import.meta.env.VITE_API_URL ?? '/api'

export type Project = {
  id: number
  name: string
  key: string
  description: string | null
  ownerUsername: string
}

export type BackendIssue = {
  id: number
  projectId: number
  title: string
  description: string | null
  status: 'TODO' | 'IN_PROGRESS' | 'DONE'
  priority: 'LOW' | 'MEDIUM' | 'HIGH'
  reporterUsername: string
  assigneeUsername: string | null
  createdAt: string
  updatedAt: string
}

export type IssuePage = {
  content: BackendIssue[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export type LoginResponse = {
  accessToken: string
  tokenType: string
  expiresIn: number
}

type ApiErrorBody = {
  message?: string
  errors?: Record<string, string>
}

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('issue_tracker_token')
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })
  if (!response.ok) {
    const body = await response.json().catch(() => null) as ApiErrorBody | null
    const fieldErrors = body?.errors
      ? Object.entries(body.errors).map(([field, message]) => `${field}: ${message}`).join('; ')
      : ''
    const message = body?.message ?? `Request failed with status ${response.status}`
    throw new ApiError(fieldErrors ? `${message}: ${fieldErrors}` : message, response.status)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export async function login(username: string, password: string) {
  const result = await apiFetch<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  localStorage.setItem('issue_tracker_token', result.accessToken)
  return result
}

export function logout() {
  localStorage.removeItem('issue_tracker_token')
}
