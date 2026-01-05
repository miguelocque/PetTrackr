import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getPets, getFeedingSchedules } from '../services/api'
import './home.css'

function Home() {
  const { user } = useAuth()
  const ownerId = user?.id
  const [pets, setPets] = useState([])
  const [schedule, setSchedule] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (ownerId) {
      loadDashboardData()
    }
  }, [ownerId])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      setError('')

      // Fetch all pets for owner
      const petsResponse = await getPets(ownerId)
      const petsData = petsResponse.data

      setPets(petsData)

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
      setError(err.response?.data?.message || 'Failed to load dashboard data')
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
        <button className="add-btn">+ Add Pet</button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="home-layout">
        <section className="pet-column">
          <p className="section-title">Your Pets</p>
          {pets.length === 0 ? (
            <p className="empty-msg">No pets yet. Add your first pet!</p>
          ) : (
            <div className="pet-grid">
              {pets.map((pet) => (
                <article key={pet.id} className="pet-card">
                  <div
                    className="pet-image"
                    style={{
                      backgroundImage: pet.photoURL
                        ? `url(${pet.photoURL})`
                        : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                    }}
                  />
                  <div className="pet-info">
                    <div className="pet-top">
                      <div>
                        <p className="pet-name">{pet.name}</p>
                        <p className="pet-breed">{pet.breed || pet.type}</p>
                      </div>
                      <button className="icon-btn" aria-label="Favorite">
                        ☆
                      </button>
                    </div>
                    <p className="pet-age">{calculateAge(pet.dateOfBirth)}</p>
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
