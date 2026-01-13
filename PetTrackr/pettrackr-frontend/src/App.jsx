import { NavLink, Route, Routes, Navigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from './context/AuthContext'
import Login from './pages/Login'
import Home from './pages/Home'
import AccountModal from './components/AccountModal'
import './App.css'

// Protected route wrapper
function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return children
}

// Layout with navigation for authenticated pages
function AppLayout() {
  const { user, logout } = useAuth()
  const [isAccountOpen, setIsAccountOpen] = useState(false)

  const handleLogout = async () => {
    await logout()
  }

  return (
    <div className="shell">
      <header className="topbar">
        <div className="brand">PetTrackr</div>
        <div className="user-info">
          <span className="user-name">{user.name}</span>
          <button onClick={() => setIsAccountOpen(true)} className="account-btn" title="Account Settings">
            ⚙️
          </button>
          <button onClick={handleLogout} className="logout-btn">Logout</button>
        </div>
      </header>

      <AccountModal
        isOpen={isAccountOpen}
        onClose={() => setIsAccountOpen(false)}
      />

      <main className="content">
        <Routes>
          <Route path="/dashboard" element={<Home />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </main>
    </div>
  )
}

function App() {
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <Login />}
      />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}

export default App
