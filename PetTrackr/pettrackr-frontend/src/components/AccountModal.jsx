import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { updateOwner, deleteOwner } from '../services/api'
import './accountModal.css'

function AccountModal({ isOpen, onClose }) {
  const { user, logout } = useAuth()
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phoneNumber: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  useEffect(() => {
    if (isOpen && user) {
      setFormData({
        name: user.name || '',
        email: user.email || '',
        phoneNumber: user.phoneNumber || ''
      })
      setError('')
      setSuccess('')
      setShowDeleteConfirm(false)
    }
  }, [isOpen, user])

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setError('')
    setSuccess('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    setSuccess('')

    try {
      await updateOwner(user.id, {
        name: formData.name,
        email: formData.email,
        phoneNumber: formData.phoneNumber
      })
      setSuccess('Account updated successfully!')
      // Refresh will happen on next page load or re-auth
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update account')
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteAccount = async () => {
    setLoading(true)
    setError('')

    try {
      await deleteOwner(user.id)
      await logout()
      // User will be redirected to login by AuthContext
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete account')
      setLoading(false)
    }
  }

  if (!isOpen) return null

  return (
    <div className="account-modal-overlay" onClick={onClose}>
      <div className="account-modal" onClick={(e) => e.stopPropagation()}>
        <div className="account-modal-header">
          <h2>Account Settings</h2>
          <button className="account-close-btn" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="account-form">
          {error && <div className="account-error">{error}</div>}
          {success && <div className="account-success">{success}</div>}

          <div className="account-form-group">
            <label htmlFor="name">Name</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Your name"
              required
            />
          </div>

          <div className="account-form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="your@email.com"
              required
            />
          </div>

          <div className="account-form-group">
            <label htmlFor="phoneNumber">Phone Number</label>
            <input
              type="tel"
              id="phoneNumber"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              placeholder="1234567890"
              required
            />
          </div>

          <button 
            type="submit" 
            className="account-save-btn"
            disabled={loading}
          >
            {loading ? 'Saving...' : 'Save Changes'}
          </button>
        </form>

        <div className="account-danger-zone">
          <h3>Danger Zone</h3>
          <p>Deleting your account will permanently remove all your data, including all pets, vet visits, medications, and feeding schedules.</p>
          <button 
            className="account-delete-btn"
            onClick={() => setShowDeleteConfirm(true)}
            disabled={loading}
          >
            Delete Account
          </button>
        </div>

        {showDeleteConfirm && (
          <div className="delete-account-overlay" onClick={() => setShowDeleteConfirm(false)}>
            <div className="delete-account-modal" onClick={(e) => e.stopPropagation()}>
              <h3>Delete Account?</h3>
              <p>
                Are you sure you want to delete your account? This will permanently delete:
              </p>
              <ul>
                <li>Your account and profile</li>
                <li>All your pets</li>
                <li>All vet visits, medications, and feeding schedules</li>
              </ul>
              <p className="delete-warning">This action cannot be undone.</p>
              <div className="delete-account-actions">
                <button 
                  className="delete-cancel-btn" 
                  onClick={() => setShowDeleteConfirm(false)}
                >
                  Cancel
                </button>
                <button 
                  className="delete-confirm-btn"
                  onClick={handleDeleteAccount}
                  disabled={loading}
                >
                  {loading ? 'Deleting...' : 'Delete My Account'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default AccountModal
