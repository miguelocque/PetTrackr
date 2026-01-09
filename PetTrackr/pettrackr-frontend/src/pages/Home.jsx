import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getPets, getFeedingSchedules, getMedications } from '../services/api'
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
  const [imageBust, setImageBust] = useState(Date.now())

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
      setImageBust(Date.now())

      // If no pets, no need to fetch schedules
      if (petsData.length === 0) {
        setSchedule([])
        return
      }

      // Fetch feeding schedules for all pets
      const feedingPromises = petsData.map((pet) =>
        getFeedingSchedules(ownerId, pet.id)
          .then((res) => res.data.map((s) => ({ 
            ...s, 
            petName: pet.name, 
            petId: pet.id,
            type: 'feeding'
          })))
          .catch(() => [])
      )

      // Fetch medications for all pets
      const medicationPromises = petsData.map((pet) =>
        getMedications(ownerId, pet.id)
          .then((res) => res.data.map((m) => ({ 
            ...m, 
            petName: pet.name, 
            petId: pet.id,
            type: 'medication',
            time: m.timeToAdminister // normalize time field
          })))
          .catch(() => [])
      )

      const [allFeeding, allMedications] = await Promise.all([
        Promise.all(feedingPromises),
        Promise.all(medicationPromises)
      ])

      const flatFeeding = allFeeding.flat()
      const flatMedications = allMedications.flat()

      // Combine and sort by time
      const combined = [...flatFeeding, ...flatMedications]
      combined.sort((a, b) => (a.time || '').localeCompare(b.time || ''))

      setSchedule(combined)
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
        onPetUpdated={async () => {
          await loadDashboardData()
          setImageBust(Date.now())
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
                        ? `url(http://localhost:8080/uploads/pet-images/${pet.photoURL}?v=${imageBust})`
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
          <p className="section-title">Today&apos;s Schedule</p>
          {schedule.length === 0 ? (
            <p className="empty-msg">No schedules yet.</p>
          ) : (
            <div className="schedule-list">
              {schedule.map((item) => (
                <div 
                  key={`${item.type}-${item.petId}-${item.id}`} 
                  className={`schedule-item ${item.type === 'medication' ? 'schedule-medication' : 'schedule-feeding'}`}
                >
                  <div className={`time-circle ${item.type === 'medication' ? 'time-circle-medication' : 'time-circle-feeding'}`}>
                    {item.type === 'medication' ? '💊' : '🍽️'}
                  </div>
                  <div className="schedule-text">
                    <div className="schedule-row">
                      <span className="time">{formatTime(item.time)}</span>
                      <span className="pet">· {item.petName}</span>
                    </div>
                    {item.type === 'feeding' ? (
                      <span className="amount">
                        {item.quantity} {item.quantityUnit?.toLowerCase()} of {item.foodType.toLowerCase()}
                      </span>
                    ) : (
                      <span className="amount medication-text">
                        {item.name} - {item.dosageAmount} {item.dosageUnit?.toLowerCase()}
                      </span>
                    )}
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
