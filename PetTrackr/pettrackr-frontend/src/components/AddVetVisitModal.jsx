import { useState, useEffect } from 'react'
import './addVetVisitModal.css'

function AddVetVisitModal({ isOpen, onClose, onVetVisitAdded, ownerId, petId, editVisit }) {
  const [formData, setFormData] = useState({
    visitDate: '',
    nextVisitDate: '',
    vetName: '',
    reasonForVisit: '',
    notes: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const isEditMode = !!editVisit

  useEffect(() => {
    if (editVisit) {
      setFormData({
        visitDate: editVisit.visitDate || '',
        nextVisitDate: editVisit.nextVisitDate || '',
        vetName: editVisit.vetName || '',
        reasonForVisit: editVisit.reasonForVisit || '',
        notes: editVisit.notes || ''
      })
    } else {
      setFormData({
        visitDate: '',
        nextVisitDate: '',
        vetName: '',
        reasonForVisit: '',
        notes: ''
      })
    }
  }, [editVisit, isOpen])

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    const url = isEditMode
      ? `http://localhost:8080/api/owners/${ownerId}/pets/${petId}/vet-visits/${editVisit.id}`
      : `http://localhost:8080/api/owners/${ownerId}/pets/${petId}/vet-visits`

    try {
      const response = await fetch(url, {
        method: isEditMode ? 'PATCH' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          visitDate: formData.visitDate,
          nextVisitDate: formData.nextVisitDate || null,
          vetName: formData.vetName,
          reasonForVisit: formData.reasonForVisit,
          notes: formData.notes || null
        })
      })

      if (response.ok) {
        setFormData({
          visitDate: '',
          nextVisitDate: '',
          vetName: '',
          reasonForVisit: '',
          notes: ''
        })
        onVetVisitAdded()
        onClose()
      } else {
        const data = await response.json()
        setError(data.message || `Failed to ${isEditMode ? 'update' : 'add'} vet visit`)
      }
    } catch (err) {
      setError('Unable to connect to server')
    } finally {
      setLoading(false)
    }
  }

  if (!isOpen) return null

  return (
    <div className="vet-modal-overlay" onClick={onClose}>
      <div className="vet-modal" onClick={(e) => e.stopPropagation()}>
        <div className="vet-modal-header">
          <h2>{isEditMode ? 'Edit Vet Visit' : 'Add Vet Visit'}</h2>
          <button className="vet-close-btn" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="vet-form">
          {error && <div className="vet-error">{error}</div>}

          <div className="vet-form-row">
            <div className="vet-form-group">
              <label htmlFor="visitDate">Visit Date *</label>
              <input
                type="date"
                id="visitDate"
                name="visitDate"
                value={formData.visitDate}
                onChange={handleChange}
                disabled={isEditMode}
                required
              />
            </div>
            <div className="vet-form-group">
              <label htmlFor="nextVisitDate">Next Visit Date</label>
              <input
                type="date"
                id="nextVisitDate"
                name="nextVisitDate"
                value={formData.nextVisitDate}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="vet-form-group">
            <label htmlFor="vetName">Veterinarian Name *</label>
            <input
              type="text"
              id="vetName"
              name="vetName"
              value={formData.vetName}
              onChange={handleChange}
              placeholder="Dr. Smith"
              disabled={isEditMode}
              required
            />
          </div>

          <div className="vet-form-group">
            <label htmlFor="reasonForVisit">Reason for Visit *</label>
            <input
              type="text"
              id="reasonForVisit"
              name="reasonForVisit"
              value={formData.reasonForVisit}
              onChange={handleChange}
              placeholder="Annual checkup, vaccination, etc."
              disabled={isEditMode}
              required
            />
          </div>

          <div className="vet-form-group">
            <label htmlFor="notes">Notes {isEditMode && '(editable)'}</label>
            <textarea
              id="notes"
              name="notes"
              value={formData.notes}
              onChange={handleChange}
              placeholder="Additional notes about the visit..."
              rows="3"
            />
          </div>

          <div className="vet-form-actions">
            <button type="button" className="vet-cancel-btn" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="vet-submit-btn" disabled={loading}>
              {loading ? (isEditMode ? 'Updating...' : 'Adding...') : (isEditMode ? 'Update Visit' : 'Add Visit')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AddVetVisitModal
