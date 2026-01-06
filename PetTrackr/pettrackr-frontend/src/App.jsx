import { NavLink, Route, Routes, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Login from './pages/Login'
import Home from './pages/Home'
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

  const handleLogout = async () => {
    await logout()
  }

  return (
    <div className="shell">
      <header className="topbar">
        <div className="brand">PetTrackr</div>
        <nav className="nav">
          <NavLink to="/dashboard" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Home
          </NavLink>
        </nav>
        <div className="user-info">
          <span className="user-name">{user?.name}</span>
          <button onClick={handleLogout} className="logout-btn">Logout</button>
        </div>
      </header>

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
