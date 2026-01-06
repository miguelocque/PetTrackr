import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import EditPetModal from './EditPetModal'
import './petDetailModal.css'

function PetDetailModal({ pet, isOpen, onClose }) {
  const { user } = useAuth()
  const [petDetails, setPetDetails] = useState(null)
  const [vetVisits, setVetVisits] = useState([])
  const [medications, setMedications] = useState([])
  const [feedingSchedules, setFeedingSchedules] = useState([])
  const [loading, setLoading] = useState(false)
  const [isEditOpen, setIsEditOpen] = useState(false)

  useEffect(() => {
    if (isOpen && pet) {
      loadPetDetails()
    }
  }, [isOpen, pet])

  const loadPetDetails = async () => {
    setLoading(true)
    try {
      // Fetch full pet details (includes weight, activityLevel, etc.)
      const petResponse = await fetch(
        `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}`,
        { credentials: 'include' }
      )
      if (petResponse.ok) {
        const petData = await petResponse.json()
        setPetDetails(petData)
      }

      // Fetch vet visits
      const vetResponse = await fetch(
        `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}/vet-visits`,
        { credentials: 'include' }
      )
      if (vetResponse.ok) {
        const vetData = await vetResponse.json()
        setVetVisits(vetData)
      }

      // Fetch medications
      const medResponse = await fetch(
        `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}/medications`,
        { credentials: 'include' }
      )
      if (medResponse.ok) {
        const medData = await medResponse.json()
        setMedications(medData)
      }

      // Fetch feeding schedules
      const feedResponse = await fetch(
        `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}/feeding-schedules`,
        { credentials: 'include' }
      )
      if (feedResponse.ok) {
        const feedData = await feedResponse.json()
        setFeedingSchedules(feedData)
      }
    } catch (error) {
      console.error('Error loading pet details:', error)
    } finally {
      setLoading(false)
    }
  }

  const calculateAge = (dateOfBirth) => {
    if (!dateOfBirth) return 'Unknown'
    const birth = new Date(dateOfBirth)
    const now = new Date()
    const years = now.getFullYear() - birth.getFullYear()
    return `${years} year${years !== 1 ? 's' : ''}`
  }

  const formatDate = (dateString) => {
    if (!dateString) return 'Not scheduled'
    const date = new Date(dateString)
    return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })
  }

  const formatTime = (time24) => {
    if (!time24) return ''
    const [hours, minutes] = time24.split(':')
    const hour = parseInt(hours, 10)
    const ampm = hour >= 12 ? 'PM' : 'AM'
    const hour12 = hour % 12 || 12
    return `${hour12}:${minutes} ${ampm}`
  }

  const getHealthStatus = () => {
    // Logic to determine health status based on recent vet visits
    const recentVisit = vetVisits.sort((a, b) => new Date(b.visitDate) - new Date(a.visitDate))[0]
    if (!recentVisit) return { status: 'Unknown', color: '#94a3b8' }
    
    const daysSinceVisit = Math.floor((new Date() - new Date(recentVisit.visitDate)) / (1000 * 60 * 60 * 24))
    if (daysSinceVisit < 180) return { status: 'Excellent', color: '#10b981' }
    if (daysSinceVisit < 365) return { status: 'Good', color: '#3b82f6' }
    return { status: 'Check-up Due', color: '#f59e0b' }
  }

  if (!isOpen || !pet) return null

  // Use petDetails (full data) if loaded, otherwise fall back to pet (summary)
  const displayPet = petDetails || pet

  const healthStatus = getHealthStatus()
  const lastVisit = vetVisits.sort((a, b) => new Date(b.visitDate) - new Date(a.visitDate))[0]
  const nextVisit = vetVisits.find(v => new Date(v.visitDate) > new Date())

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="pet-detail-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-actions">
          <button className="action-btn" aria-label="Edit" onClick={() => setIsEditOpen(true)}>
            ✏️
          </button>
          <button className="action-btn" aria-label="Delete">
            🗑️
          </button>
          <button className="action-btn close-btn" onClick={onClose}>
            ✕
          </button>
        </div>

        <div
          className="pet-hero-image"
          style={{
            backgroundImage: displayPet.photoURL
              ? `url(http://localhost:8080/uploads/pet-images/${displayPet.photoURL})`
              : 'linear-gradient(135deg, #dfe7ff 0%, #f2f4f8 100%)'
          }}
        >
          <div className="pet-hero-info">
            <div className="pet-name-breed">
              <h2>{displayPet.name}</h2>
              <p>{displayPet.breed}</p>
            </div>
            <span className="health-badge" style={{ backgroundColor: healthStatus.color }}>
              {healthStatus.status}
            </span>
          </div>
        </div>

        <div className="pet-detail-content">
          <div className="pet-stats">
            <div className="stat-item">
              <span className="stat-icon">📅</span>
              <div>
                <p className="stat-label">Age</p>
                <p className="stat-value">{calculateAge(displayPet.dateOfBirth)}</p>
              </div>
            </div>
            <div className="stat-item">
              <span className="stat-icon">⚖️</span>
              <div>
                <p className="stat-label">Weight</p>
                <p className="stat-value">{displayPet.weight ? `${displayPet.weight} ${displayPet.weightType?.toLowerCase() || ''}` : 'Not set'}</p>
              </div>
            </div>
            <div className="stat-item">
              <span className="stat-icon">⚡</span>
              <div>
                <p className="stat-label">Activity</p>
                <p className="stat-value">{displayPet.activityLevel || 'Not set'}</p>
              </div>
            </div>
            <div className="stat-item">
              <span className="stat-icon">❤️</span>
              <div>
                <p className="stat-label">Type</p>
                <p className="stat-value">{displayPet.type}</p>
              </div>
            </div>
          </div>

          <section className="detail-section">
            <div className="section-header">
              <span className="section-icon">🏥</span>
              <h3>Veterinary Care</h3>
            </div>
            <div className="vet-info">
              <div className="vet-item">
                <p className="vet-label">Last Visit</p>
                <p className="vet-value">{formatDate(lastVisit?.visitDate)}</p>
              </div>
              <div className="vet-item">
                <p className="vet-label">Next Visit</p>
                <p className="vet-value">{formatDate(nextVisit?.visitDate)}</p>
              </div>
            </div>
          </section>

          <section className="detail-section">
            <div className="section-header">
              <span className="section-icon">💊</span>
              <h3>Medications</h3>
            </div>
            {medications.length === 0 ? (
              <p className="empty-state">No medications</p>
            ) : (
              <div className="medications-list">
                {medications.map((med) => (
                  <div key={med.id} className="medication-item">
                    <span className="med-icon">ⓘ</span>
                    <span className="med-name">{med.name}</span>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="detail-section">
            <div className="section-header">
              <span className="section-icon">💊</span>
              <h3>Daily Medication Schedule</h3>
            </div>
            {medications.length === 0 ? (
              <p className="empty-state">No medication schedule</p>
            ) : (
              <div className="med-schedule-list">
                {medications.map((med) => (
                  <div key={med.id} className="med-schedule-item">
                    <div className="med-schedule-info">
                      <p className="med-schedule-name">{med.name}</p>
                      <p className="med-schedule-dosage">{med.dosage}</p>
                    </div>
                    <button className="edit-btn">✏️</button>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="detail-section">
            <div className="section-header">
              <span className="section-icon">🍽️</span>
              <h3>Feeding Schedule</h3>
            </div>
            {feedingSchedules.length === 0 ? (
              <p className="empty-state">No feeding schedule</p>
            ) : (
              <div className="feeding-schedule-list">
                {feedingSchedules.sort((a, b) => a.time.localeCompare(b.time)).map((schedule) => (
                  <div key={schedule.id} className="feeding-schedule-item">
                    <span className="feeding-time">{formatTime(schedule.time)}</span>
                    <span className="feeding-amount">
                      {schedule.quantity} {schedule.quantityUnit.toLowerCase()}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>

        <EditPetModal
          pet={displayPet}
          isOpen={isEditOpen}
          onClose={() => setIsEditOpen(false)}
          onPetUpdated={() => {
            loadPetDetails()
            // Trigger parent refresh
            onClose()
          }}
        />
      </div>
    </div>
  )
}

export default PetDetailModal
