import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getPets, getFeedingSchedules } from '../services/api'
import AddPetModal from '../components/AddPetModal'
import PetDetailModal from '../components/PetDetailModal'
import './home.css'

function Home() {
  const { user } = useAuth()
  const ownerId = user?.id
  const [pets, setPets] = useState([])
  const [schedule, setSchedule] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [selectedPet, setSelectedPet] = useState(null)
  const [isPetDetailOpen, setIsPetDetailOpen] = useState(false)

  useEffect(() => {
    if (ownerId) {
      loadDashboardData()
    }
  }, [ownerId])

  const petTypeIcon = (type = '') => {
    const t = type.toLowerCase()
    if (t.includes('dog')) return '🦴'
    if (t.includes('cat')) return '🐾'
    if (t.includes('rabbit')) return '🐰'
    if (t.includes('bird')) return '🪶'
    return '🐾'
  }

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      setError('')

      // Fetch all pets for owner
      const petsResponse = await getPets(ownerId)
      const petsData = petsResponse.data || []

      setPets(petsData)

      // If no pets, no need to fetch feeding schedules
      if (petsData.length === 0) {
        setSchedule([])
        return
      }

      // Fetch feeding schedules for all pets and combine
      const schedulePromises = petsData.map((pet) =>
        getFeedingSchedules(ownerId, pet.id)
          .then((res) => res.data.map((s) => ({ ...s, petName: pet.name, petId: pet.id })))
          .catch(() => [])
      )

      const allSchedules = await Promise.all(schedulePromises)
      const flatSchedules = allSchedules.flat()

      // Sort by time
      flatSchedules.sort((a, b) => a.time.localeCompare(b.time))

      setSchedule(flatSchedules)
    } catch (err) {
      // Don't show error for 404 (no pets found) - that's expected for new users
      if (err.response?.status === 404) {
        setPets([])
        setSchedule([])
        return
      }
      // Handle auth errors
      if (err.response?.status === 401) {
        setError('Session expired. Please refresh and log in again.')
        return
      }
      setError(err.response?.data?.message || 'Unable to connect to server. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const calculateAge = (dateOfBirth) => {
    if (!dateOfBirth) return 'Unknown age'
    const birth = new Date(dateOfBirth)
    const now = new Date()
    const years = now.getFullYear() - birth.getFullYear()
    return years === 1 ? '1 year old' : `${years} years old`
  }

  const formatTime = (time24) => {
    if (!time24) return ''
    const [hours, minutes] = time24.split(':')
    const hour = parseInt(hours, 10)
    const ampm = hour >= 12 ? 'PM' : 'AM'
    const hour12 = hour % 12 || 12
    return `${hour12}:${minutes} ${ampm}`
  }

  if (loading) {
    return (
      <div className="home-screen">
        <p className="loading-msg">Loading dashboard...</p>
      </div>
    )
  }

  return (
    <div className="home-screen">
      <div className="home-header">
        <div className="brand-group">
          <div className="brand-icon">🐾</div>
          <div>
            <h1>Pet Dashboard</h1>
            <p className="subhead">Manage and track your furry friends</p>
          </div>
        </div>
        <button className="add-btn" onClick={() => setIsModalOpen(true)}>+ Add Pet</button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <AddPetModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onPetAdded={loadDashboardData}
      />

      <PetDetailModal
        pet={selectedPet}
        isOpen={isPetDetailOpen}
        onClose={() => {
          setIsPetDetailOpen(false)
          setSelectedPet(null)
        }}
      />

      <div className="home-layout">
        <section className="pet-column">
          <p className="section-title">Your Pets</p>
          {pets.length === 0 ? (
            <p className="empty-msg">No pets yet. Add your first pet!</p>
          ) : (
            <div className="pet-grid">
              {pets.map((pet) => (
                <article
                  key={pet.id}
                  className="pet-card"
                  onClick={() => {
                    setSelectedPet(pet)
                    setIsPetDetailOpen(true)
                  }}
                >
                  <div
                    className="pet-image"
                    style={{
                      backgroundImage: pet.photoURL
                        ? `url(http://localhost:8080/uploads/pet-images/${pet.photoURL})`
                        : 'linear-gradient(135deg, #dfe7ff 0%, #f2f4f8 100%)'
                    }}
                  >
                    <span className="pet-chip">{petTypeIcon(pet.type)}</span>
                  </div>
                  <div className="pet-info">
                    <div className="pet-top">
                      <div>
                        <p className="pet-name">{pet.name}</p>
                        <p className="pet-breed">{pet.breed || pet.type}</p>
                      </div>
                      <span className="pet-age-tag">{calculateAge(pet.dateOfBirth)}</span>
                    </div>
                    <p className="pet-age">{pet.type}</p>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="schedule-column">
          <p className="section-title">Today&apos;s Feeding Schedule</p>
          {schedule.length === 0 ? (
            <p className="empty-msg">No feeding schedules yet.</p>
          ) : (
            <div className="schedule-list">
              {schedule.map((item) => (
                <div key={`${item.petId}-${item.id}`} className="schedule-item">
                  <div className="time-circle">⏰</div>
                  <div className="schedule-text">
                    <div className="schedule-row">
                      <span className="time">{formatTime(item.time)}</span>
                      <span className="pet">· {item.petName}</span>
                    </div>
                    <span className="amount">
                      {item.quantity} {item.quantityUnit.toLowerCase()} {item.foodType}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

export default Home
