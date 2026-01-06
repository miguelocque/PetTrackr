import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import './editPetModal.css'

function EditPetModal({ pet, isOpen, onClose, onPetUpdated }) {
  const { user } = useAuth()
  const [formData, setFormData] = useState({
    name: '',
    type: '',
    breed: '',
    dateOfBirth: '',
    weight: '',
    weightType: 'LBS',
    activityLevel: 'MEDIUM'
  })
  const [image, setImage] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (pet && isOpen) {
      setFormData({
        name: pet.name || '',
        type: pet.type || '',
        breed: pet.breed || '',
        dateOfBirth: pet.dateOfBirth || '',
        weight: pet.weight || '',
        weightType: pet.weightType || 'LBS',
        activityLevel: pet.activityLevel || 'MEDIUM'
      })
      setImage(null)
      setError('')
    }
  }, [pet, isOpen])

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleImageChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setImage(e.target.files[0])
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      // Update pet details - convert weight to number
      const updatePayload = {
        ...formData,
        weight: formData.weight ? parseFloat(formData.weight) : null
      }

      console.log('Updating pet with payload:', updatePayload)

      const response = await fetch(
        `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}`,
        {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'include',
          body: JSON.stringify(updatePayload)
        }
      )

      console.log('Update response status:', response.status)

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        console.log('Error response:', errorData)
        throw new Error(errorData.message || 'Failed to update pet')
      }

      // Upload new photo if provided
      if (image) {
        console.log('Uploading photo:', image.name, image.size, 'bytes')
        const photoFormData = new FormData()
        photoFormData.append('photo', image)

        const photoResponse = await fetch(
          `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}/photo`,
          {
            method: 'POST',
            credentials: 'include',
            body: photoFormData
          }
        )

        console.log('Photo upload response status:', photoResponse.status)

        if (!photoResponse.ok) {
          const photoError = await photoResponse.json().catch(() => ({}))
          console.log('Photo upload error:', photoError)
          throw new Error(photoError.message || 'Failed to upload photo')
        }
      }

      onPetUpdated()
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="edit-pet-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Edit Pet</h2>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="edit-pet-form">
          <div className="form-group">
            <label htmlFor="name">Pet Name *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="type">Type *</label>
              <select
                id="type"
                name="type"
                value={formData.type}
                onChange={handleChange}
                required
              >
                <option value="">Select type</option>
                <option value="Dog">Dog</option>
                <option value="Cat">Cat</option>
                <option value="Rabbit">Rabbit</option>
                <option value="Bird">Bird</option>
                <option value="Other">Other</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="breed">Breed</label>
              <input
                type="text"
                id="breed"
                name="breed"
                value={formData.breed}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="dateOfBirth">Date of Birth *</label>
            <input
              type="date"
              id="dateOfBirth"
              name="dateOfBirth"
              value={formData.dateOfBirth}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="weight">Weight</label>
              <input
                type="number"
                id="weight"
                name="weight"
                value={formData.weight}
                onChange={handleChange}
                step="0.1"
              />
            </div>

            <div className="form-group">
              <label htmlFor="weightType">Weight Unit</label>
              <select
                id="weightType"
                name="weightType"
                value={formData.weightType}
                onChange={handleChange}
              >
                <option value="LBS">lbs</option>
                <option value="KG">kg</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="activityLevel">Activity Level</label>
            <select
              id="activityLevel"
              name="activityLevel"
              value={formData.activityLevel}
              onChange={handleChange}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="photo">Update Photo (optional)</label>
            <input
              type="file"
              id="photo"
              accept="image/jpeg,image/png"
              onChange={handleImageChange}
            />
            {image && <p className="file-selected">{image.name}</p>}
          </div>

          <div className="form-actions">
            <button type="button" onClick={onClose} className="cancel-btn" disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="save-btn" disabled={loading}>
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default EditPetModal
