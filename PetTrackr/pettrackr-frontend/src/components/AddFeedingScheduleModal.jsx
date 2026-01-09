import { useState, useEffect } from 'react'
import './addFeedingScheduleModal.css'

const QUANTITY_UNITS = ['CUPS', 'GRAMS', 'OUNCES', 'CANS']

function AddFeedingScheduleModal({ isOpen, onClose, onScheduleAdded, ownerId, petId, editSchedule }) {
  const [formData, setFormData] = useState({
    time: '',
    foodType: '',
    quantity: '',
    quantityUnit: 'CUPS'
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const isEditMode = !!editSchedule

  useEffect(() => {
    if (editSchedule) {
      setFormData({
        time: editSchedule.time || '',
        foodType: editSchedule.foodType || '',
        quantity: editSchedule.quantity?.toString() || '',
        quantityUnit: editSchedule.quantityUnit || 'CUPS'
      })
    } else {
      setFormData({
        time: '',
        foodType: '',
        quantity: '',
        quantityUnit: 'CUPS'
      })
    }
  }, [editSchedule, isOpen])

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
      ? `http://localhost:8080/api/owners/${ownerId}/pets/${petId}/feeding-schedules/${editSchedule.id}`
      : `http://localhost:8080/api/owners/${ownerId}/pets/${petId}/feeding-schedules`

    try {
      const response = await fetch(url, {
        method: isEditMode ? 'PATCH' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          time: formData.time,
          foodType: formData.foodType,
          quantity: parseFloat(formData.quantity),
          quantityUnit: formData.quantityUnit
        })
      })

      if (response.ok) {
        setFormData({
          time: '',
          foodType: '',
          quantity: '',
          quantityUnit: 'CUPS'
        })
        onScheduleAdded()
        onClose()
      } else {
        const data = await response.json()
        setError(data.message || `Failed to ${isEditMode ? 'update' : 'add'} feeding schedule`)
      }
    } catch (err) {
      setError('Unable to connect to server')
    } finally {
      setLoading(false)
    }
  }

  if (!isOpen) return null

  return (
    <div className="feed-modal-overlay" onClick={onClose}>
      <div className="feed-modal" onClick={(e) => e.stopPropagation()}>
        <div className="feed-modal-header">
          <h2>{isEditMode ? 'Edit Feeding Schedule' : 'Add Feeding Schedule'}</h2>
          <button className="feed-close-btn" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="feed-form">
          {error && <div className="feed-error">{error}</div>}

          <div className="feed-form-row">
            <div className="feed-form-group">
              <label htmlFor="time">Feeding Time *</label>
              <input
                type="time"
                id="time"
                name="time"
                value={formData.time}
                onChange={handleChange}
                required
              />
            </div>
            <div className="feed-form-group">
              <label htmlFor="foodType">Food Type *</label>
              <input
                type="text"
                id="foodType"
                name="foodType"
                value={formData.foodType}
                onChange={handleChange}
                placeholder="Dry kibble, wet food..."
                required
              />
            </div>
          </div>

          <div className="feed-form-row">
            <div className="feed-form-group">
              <label htmlFor="quantity">Quantity *</label>
              <input
                type="number"
                id="quantity"
                name="quantity"
                value={formData.quantity}
                onChange={handleChange}
                placeholder="1.5"
                step="0.1"
                min="0.1"
                required
              />
            </div>
            <div className="feed-form-group">
              <label htmlFor="quantityUnit">Unit *</label>
              <select
                id="quantityUnit"
                name="quantityUnit"
                value={formData.quantityUnit}
                onChange={handleChange}
                required
              >
                {QUANTITY_UNITS.map(unit => (
                  <option key={unit} value={unit}>
                    {unit.charAt(0) + unit.slice(1).toLowerCase()}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="feed-form-actions">
            <button type="button" className="feed-cancel-btn" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="feed-submit-btn" disabled={loading}>
              {loading ? (isEditMode ? 'Updating...' : 'Adding...') : (isEditMode ? 'Update Schedule' : 'Add Schedule')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AddFeedingScheduleModal
