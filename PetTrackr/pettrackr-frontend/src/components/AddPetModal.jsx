import { useState } from 'react'
import { createPet } from '../services/api'
import { useAuth } from '../context/AuthContext'
import './addPetModal.css'

const petTypes = ['Dog', 'Cat', 'Bird', 'Rabbit', 'Hamster', 'Fish', 'Other']
const activityLevels = ['LOW', 'MEDIUM', 'HIGH']
const weightTypes = ['KG', 'LBS']

function AddPetModal({ isOpen, onClose, onPetAdded }) {
  const { user } = useAuth()
  const [formData, setFormData] = useState({
    name: '',
    type: 'Dog',
    breed: '',
    dateOfBirth: '',
    weight: '',
    weightType: 'KG',
    activityLevel: 'MEDIUM'
  })
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleImageChange = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setError('Image must be less than 5MB')
        return
      }
      if (!file.type.startsWith('image/')) {
        setError('File must be an image')
        return
      }
      setImageFile(file)
      setImagePreview(URL.createObjectURL(file))
      setError('')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      // Create pet payload (backend calculates age from dateOfBirth)
      const petPayload = {
        name: formData.name,
        type: formData.type,
        breed: formData.breed,
        dateOfBirth: formData.dateOfBirth,
        weight: parseFloat(formData.weight),
        weightType: formData.weightType,
        activityLevel: formData.activityLevel
      }

      console.log('Creating pet with payload:', petPayload)

      // Create the pet first
      const response = await createPet(user.id, petPayload)
      const newPet = response.data

      console.log('Pet created successfully:', newPet)

      // If there's an image, upload it
      if (imageFile) {
        const formDataImg = new FormData()
        formDataImg.append('photo', imageFile)

        await fetch(`http://localhost:8080/api/owners/${user.id}/pets/${newPet.id}/photo`, {
          method: 'POST',
          body: formDataImg,
          credentials: 'include'
        })
      }

      // Success - notify parent and close
      onPetAdded()
      handleClose()
    } catch (err) {
      console.error('Error creating pet:', err)
      console.error('Error response:', err.response)
      const errorMsg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to create pet'
      setError(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setFormData({
      name: '',
      type: 'Dog',
      breed: '',
      dateOfBirth: '',
      weight: '',
      weightType: 'KG',
      activityLevel: 'MEDIUM'
    })
    setImageFile(null)
    setImagePreview('')
    setError('')
    onClose()
  }

  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Add New Pet</h2>
          <button className="close-btn" onClick={handleClose}>
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="pet-form">
          <div className="form-row">
            <label className="field">
              <span>Pet Name *</span>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="Max"
                required
              />
            </label>

            <label className="field">
              <span>Type *</span>
              <select name="type" value={formData.type} onChange={handleInputChange} required>
                {petTypes.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div className="form-row">
            <label className="field">
              <span>Breed *</span>
              <input
                type="text"
                name="breed"
                value={formData.breed}
                onChange={handleInputChange}
                placeholder="Golden Retriever"
                required
              />
            </label>

            <label className="field">
              <span>Date of Birth *</span>
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleInputChange}
                max={new Date().toISOString().split('T')[0]}
                required
              />
            </label>
          </div>

          <div className="form-row">
            <label className="field">
              <span>Weight *</span>
              <input
                type="number"
                name="weight"
                value={formData.weight}
                onChange={handleInputChange}
                placeholder="25"
                step="0.1"
                min="0.1"
                required
              />
            </label>

            <label className="field">
              <span>Weight Unit *</span>
              <select name="weightType" value={formData.weightType} onChange={handleInputChange} required>
                {weightTypes.map((unit) => (
                  <option key={unit} value={unit}>
                    {unit}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <label className="field">
            <span>Activity Level *</span>
            <select name="activityLevel" value={formData.activityLevel} onChange={handleInputChange} required>
              {activityLevels.map((level) => (
                <option key={level} value={level}>
                  {level.charAt(0) + level.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Pet Photo (optional)</span>
            <input type="file" accept="image/*" onChange={handleImageChange} />
            {imagePreview && (
              <div className="image-preview">
                <img src={imagePreview} alt="Preview" />
              </div>
            )}
            <small>Leave blank for default image. Max 5MB, JPG/PNG only.</small>
          </label>

          {error && <div className="form-error">{error}</div>}

          <div className="form-actions">
            <button type="button" onClick={handleClose} className="cancel-btn">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="submit-btn">
              {loading ? 'Adding...' : 'Add Pet'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AddPetModal
