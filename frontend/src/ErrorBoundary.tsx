import { Component } from 'react'
import type { ErrorInfo, ReactNode } from 'react'

type ErrorBoundaryProps = {
  children: ReactNode
}

type ErrorBoundaryState = {
  failed: boolean
}

export default class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { failed: false }

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { failed: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Application render failed', error, info)
  }

  render() {
    if (this.state.failed) {
      return <main className="fatal-error" role="alert">
        <h1>Something went wrong.</h1>
        <p>Reload the page to start a fresh session.</p>
        <button type="button" onClick={() => window.location.reload()}>Reload</button>
      </main>
    }
    return this.props.children
  }
}
