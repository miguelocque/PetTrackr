import { useState, useEffect } from 'react'
import './addMedicationModal.css'

const DOSAGE_UNITS = ['MG', 'ML', 'TABLETS', 'CAPSULES', 'DROPS', 'UNITS', 'TEASPOONS']

function AddMedicationModal({ isOpen, onClose, onMedicationAdded, ownerId, petId, editMedication }) {
  const [formData, setFormData] = useState({
    name: '',
    dosageAmount: '',
    dosageUnit: 'MG',
    frequency: '',
    timeToAdminister: '',
    startDate: '',
    endDate: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const isEditMode = !!editMedication

  useEffect(() => {
    if (editMedication) {
      setFormData({
        name: editMedication.name || '',
        dosageAmount: editMedication.dosageAmount?.toString() || '',
        dosageUnit: editMedication.dosageUnit || 'MG',
        frequency: editMedication.frequency || '',
        timeToAdminister: editMedication.timeToAdminister || '',
        startDate: editMedication.startDate || '',
        endDate: editMedication.endDate || ''
      })
    } else {
      setFormData({
        name: '',
        dosageAmount: '',
        dosageUnit: 'MG',
        frequency: '',
        timeToAdminister: '',
        startDate: '',
        endDate: ''
      })
    }
  }, [editMedication, isOpen])

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
      ? `http://localhost:8080/api/owners/${ownerId}/pets/${petId}/medications/${editMedication.id}`
      : `http://localhost:8080/api/owners/${ownerId}/pets/${petId}/medications`

    try {
      const response = await fetch(url, {
        method: isEditMode ? 'PATCH' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          name: formData.name,
          dosageAmount: parseFloat(formData.dosageAmount),
          dosageUnit: formData.dosageUnit,
          frequency: formData.frequency,
          timeToAdminister: formData.timeToAdminister,
          startDate: formData.startDate,
          endDate: formData.endDate || null
        })
      })

      if (response.ok) {
        setFormData({
          name: '',
          dosageAmount: '',
          dosageUnit: 'MG',
          frequency: '',
          timeToAdminister: '',
          startDate: '',
          endDate: ''
        })
        onMedicationAdded()
        onClose()
      } else {
        const data = await response.json()
        setError(data.message || `Failed to ${isEditMode ? 'update' : 'add'} medication`)
      }
    } catch (err) {
      setError('Unable to connect to server')
    } finally {
      setLoading(false)
    }
  }

  const formatUnitLabel = (unit) => {
    const labels = {
      'MG': 'mg',
      'ML': 'ml',
      'TABLETS': 'Tablets',
      'CAPSULES': 'Capsules',
      'DROPS': 'Drops',
      'UNITS': 'Units',
      'TEASPOONS': 'Teaspoons'
    }
    return labels[unit] || unit
  }

  if (!isOpen) return null

  return (
    <div className="med-modal-overlay" onClick={onClose}>
      <div className="med-modal" onClick={(e) => e.stopPropagation()}>
        <div className="med-modal-header">
          <h2>{isEditMode ? 'Edit Medication' : 'Add Medication'}</h2>
          <button className="med-close-btn" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="med-form">
          {error && <div className="med-error">{error}</div>}

          <div className="med-form-group">
            <label htmlFor="name">Medication Name *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g., Heartworm Prevention"
              required
            />
          </div>

          <div className="med-form-row">
            <div className="med-form-group">
              <label htmlFor="dosageAmount">Dosage *</label>
              <input
                type="number"
                id="dosageAmount"
                name="dosageAmount"
                value={formData.dosageAmount}
                onChange={handleChange}
                placeholder="1"
                step="0.1"
                min="0.1"
                required
              />
            </div>
            <div className="med-form-group">
              <label htmlFor="dosageUnit">Unit *</label>
              <select
                id="dosageUnit"
                name="dosageUnit"
                value={formData.dosageUnit}
                onChange={handleChange}
                required
              >
                {DOSAGE_UNITS.map(unit => (
                  <option key={unit} value={unit}>
                    {formatUnitLabel(unit)}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="med-form-row">
            <div className="med-form-group">
              <label htmlFor="frequency">Frequency *</label>
              <input
                type="text"
                id="frequency"
                name="frequency"
                value={formData.frequency}
                onChange={handleChange}
                placeholder="Once daily, Twice daily..."
                required
              />
            </div>
            <div className="med-form-group">
              <label htmlFor="timeToAdminister">Time *</label>
              <input
                type="time"
                id="timeToAdminister"
                name="timeToAdminister"
                value={formData.timeToAdminister}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="med-form-row">
            <div className="med-form-group">
              <label htmlFor="startDate">Start Date *</label>
              <input
                type="date"
                id="startDate"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                required
              />
            </div>
            <div className="med-form-group">
              <label htmlFor="endDate">End Date</label>
              <input
                type="date"
                id="endDate"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                placeholder="Leave blank if ongoing"
              />
              <span className="field-hint">Leave blank if ongoing</span>
            </div>
          </div>

          <div className="med-form-actions">
            <button type="button" className="med-cancel-btn" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="med-submit-btn" disabled={loading}>
              {loading ? (isEditMode ? 'Updating...' : 'Adding...') : (isEditMode ? 'Update Medication' : 'Add Medication')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AddMedicationModal
