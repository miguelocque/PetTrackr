import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import EditPetModal from './EditPetModal'
import AddVetVisitModal from './AddVetVisitModal'
import AddFeedingScheduleModal from './AddFeedingScheduleModal'
import AddMedicationModal from './AddMedicationModal'
import './petDetailModal.css'

function PetDetailModal({ pet, isOpen, onClose, onPetUpdated }) {
  const { user } = useAuth()
  const [petDetails, setPetDetails] = useState(null)
  const [vetVisits, setVetVisits] = useState([])
  const [medications, setMedications] = useState([])
  const [feedingSchedules, setFeedingSchedules] = useState([])
  const [loading, setLoading] = useState(false)
  const [isEditOpen, setIsEditOpen] = useState(false)
  const [showQRCode, setShowQRCode] = useState(false)
  const [qrCodeUrl, setQRCodeUrl] = useState('')
  const [imageBust, setImageBust] = useState(Date.now())
  const [showVetVisitsPanel, setShowVetVisitsPanel] = useState(false)
  const [isAddVetVisitOpen, setIsAddVetVisitOpen] = useState(false)
  const [editVetVisit, setEditVetVisit] = useState(null)
  const [showFeedingPanel, setShowFeedingPanel] = useState(false)
  const [isAddFeedingOpen, setIsAddFeedingOpen] = useState(false)
  const [editFeedingSchedule, setEditFeedingSchedule] = useState(null)
  const [showMedicationPanel, setShowMedicationPanel] = useState(false)
  const [isAddMedicationOpen, setIsAddMedicationOpen] = useState(false)
  const [editMedication, setEditMedication] = useState(null)
  const [deleteConfirm, setDeleteConfirm] = useState(null)

  useEffect(() => {
    if (isOpen && pet) {
      loadPetDetails()
    }
  }, [isOpen, pet])

  // Refresh image on photo change or modal open (cache-busting)
  useEffect(() => {
    if (isOpen) {
      setImageBust(Date.now())
    }
  }, [isOpen, pet?.photoURL, petDetails?.photoURL])

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

  const handleGenerateQRCode = async () => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}/qr-code`,
        { credentials: 'include' }
      )
      
      if (response.ok) {
        const blob = await response.blob()
        const url = URL.createObjectURL(blob)
        setQRCodeUrl(url)
        setShowQRCode(true)
      }
    } catch (error) {
      console.error('Error generating QR code:', error)
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

  const handleDeleteFeedingSchedule = async (scheduleId) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}/feeding-schedules/${scheduleId}`,
        { method: 'DELETE', credentials: 'include' }
      )
      if (response.ok) {
        loadPetDetails()
        setDeleteConfirm(null)
        if (onPetUpdated) onPetUpdated()
      }
    } catch (error) {
      console.error('Error deleting feeding schedule:', error)
    }
  }

  const handleDeleteMedication = async (medicationId) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/owners/${user.id}/pets/${pet.id}/medications/${medicationId}`,
        { method: 'DELETE', credentials: 'include' }
      )
      if (response.ok) {
        loadPetDetails()
        setDeleteConfirm(null)
        if (onPetUpdated) onPetUpdated()
      }
    } catch (error) {
      console.error('Error deleting medication:', error)
    }
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
  const sortedVisits = [...vetVisits].sort((a, b) => new Date(b.visitDate) - new Date(a.visitDate))
  const lastVisit = sortedVisits[0]
  // Get the next scheduled visit from the most recent visit's nextVisitDate field
  const nextVisitDate = sortedVisits.find(v => v.nextVisitDate)?.nextVisitDate

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
              ? `url(http://localhost:8080/uploads/pet-images/${displayPet.photoURL}?v=${imageBust})`
              : 'linear-gradient(135deg, #dfe7ff 0%, #f2f4f8 100%)'
          }}
        >
          <div className="pet-hero-info">
            <div className="pet-name-breed">
              <h2>{displayPet.name}</h2>
              <p>{displayPet.breed}</p>
            </div>
            <div className="hero-badges">
              <button className="lost-btn" onClick={handleGenerateQRCode}>
                Lost?
              </button>
              <span className="health-badge" style={{ backgroundColor: healthStatus.color }}>
                {healthStatus.status}
              </span>
            </div>
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
              <button 
                className="add-section-btn" 
                onClick={() => setIsAddVetVisitOpen(true)}
                title="Add vet visit"
              >
                +
              </button>
            </div>
            <div 
              className="vet-info vet-info-clickable" 
              onClick={() => setShowVetVisitsPanel(true)}
            >
              <div className="vet-item">
                <p className="vet-label">Last Visit</p>
                <p className="vet-value">{formatDate(lastVisit?.visitDate)}</p>
              </div>
              <div className="vet-item">
                <p className="vet-label">Next Visit</p>
                <p className="vet-value">{formatDate(nextVisitDate)}</p>
              </div>
              <div className="vet-item">
                <p className="vet-label">Total Visits</p>
                <p className="vet-value">{vetVisits.length}</p>
              </div>
              <span className="vet-arrow">→</span>
            </div>
          </section>

          <section className="detail-section">
            <div className="section-header">
              <span className="section-icon">💊</span>
              <h3>Medications</h3>
              <button 
                className="add-section-btn add-section-btn-purple" 
                onClick={() => setIsAddMedicationOpen(true)}
                title="Add medication"
              >
                +
              </button>
            </div>
            <div 
              className="medication-info medication-info-clickable" 
              onClick={() => setShowMedicationPanel(true)}
            >
              {medications.length === 0 ? (
                <p className="empty-state-inline">No medications</p>
              ) : (
                <>
                  <div className="medication-summary">
                    <p className="medication-count">{medications.length} medication{medications.length !== 1 ? 's' : ''}</p>
                    <p className="medication-names">
                      {medications.map(m => m.name).join(', ')}
                    </p>
                  </div>
                  <span className="medication-arrow">→</span>
                </>
              )}
            </div>
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
                {medications
                  .sort((a, b) => (a.timeToAdminister || '').localeCompare(b.timeToAdminister || ''))
                  .map((med) => (
                  <div key={med.id} className="med-schedule-item">
                    <div className="med-schedule-time">
                      <span className="schedule-time-icon">⏰</span>
                      <span className="schedule-time-text">{formatTime(med.timeToAdminister)}</span>
                    </div>
                    <div className="med-schedule-info">
                      <p className="med-schedule-name">{med.name}</p>
                      <p className="med-schedule-dosage">
                        {med.dosageAmount} {med.dosageUnit?.toLowerCase()} • {med.frequency}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="detail-section">
            <div className="section-header">
              <span className="section-icon">🍽️</span>
              <h3>Feeding Schedule</h3>
              <button 
                className="add-section-btn add-section-btn-orange" 
                onClick={() => setIsAddFeedingOpen(true)}
                title="Add feeding schedule"
              >
                +
              </button>
            </div>
            <div 
              className="feeding-info feeding-info-clickable" 
              onClick={() => setShowFeedingPanel(true)}
            >
              {feedingSchedules.length === 0 ? (
                <p className="empty-state-inline">No feeding schedule set</p>
              ) : (
                <>
                  <div className="feeding-summary">
                    <p className="feeding-count">{feedingSchedules.length} meal{feedingSchedules.length !== 1 ? 's' : ''}/day</p>
                    <p className="feeding-times">
                      {feedingSchedules
                        .sort((a, b) => a.time.localeCompare(b.time))
                        .map(s => formatTime(s.time))
                        .join(', ')}
                    </p>
                  </div>
                  <span className="feeding-arrow">→</span>
                </>
              )}
            </div>
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
            if (onPetUpdated) {
              onPetUpdated()
            }
          }}
        />

        {showQRCode && (
          <div className="qr-modal-overlay" onClick={() => setShowQRCode(false)}>
            <div className="qr-modal-content" onClick={(e) => e.stopPropagation()}>
              <button className="qr-close-btn" onClick={() => setShowQRCode(false)}>✕</button>
              <h3>Pet Emergency QR Code</h3>
              <p>Scan this code for pet information</p>
              {qrCodeUrl && <img src={qrCodeUrl} alt="Pet QR Code" className="qr-code-image" />}
              <a href={qrCodeUrl} download={`pet_${pet.id}_qr.png`} className="qr-download-btn">
                Download QR Code
              </a>
            </div>
          </div>
        )}

        <AddVetVisitModal
          isOpen={isAddVetVisitOpen}
          onClose={() => {
            setIsAddVetVisitOpen(false)
            setEditVetVisit(null)
          }}
          onVetVisitAdded={loadPetDetails}
          ownerId={user.id}
          petId={pet.id}
          editVisit={editVetVisit}
        />

        {showVetVisitsPanel && (
          <div className="vet-panel-overlay" onClick={() => setShowVetVisitsPanel(false)}>
            <div className="vet-panel" onClick={(e) => e.stopPropagation()}>
              <div className="vet-panel-header">
                <h3>Vet Visit History</h3>
                <button className="vet-panel-close" onClick={() => setShowVetVisitsPanel(false)}>✕</button>
              </div>
              <div className="vet-panel-actions">
                <button 
                  className="add-visit-btn"
                  onClick={() => {
                    setEditVetVisit(null)
                    setShowVetVisitsPanel(false)
                    setIsAddVetVisitOpen(true)
                  }}
                >
                  + Add Vet Visit
                </button>
              </div>
              <div className="vet-panel-content">
                {vetVisits.length === 0 ? (
                  <p className="empty-state">No vet visits recorded yet.</p>
                ) : (
                  <div className="vet-visits-list">
                    {vetVisits
                      .sort((a, b) => new Date(b.visitDate) - new Date(a.visitDate))
                      .map((visit) => (
                        <div key={visit.id} className="vet-visit-card">
                          <div className="vet-visit-date">
                            <span className="visit-day">{new Date(visit.visitDate).getDate()}</span>
                            <span className="visit-month">
                              {new Date(visit.visitDate).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })}
                            </span>
                          </div>
                          <div className="vet-visit-details">
                            <p className="visit-reason">{visit.reasonForVisit}</p>
                            <p className="visit-vet">{visit.vetName}</p>
                            {visit.notes && <p className="visit-notes">{visit.notes}</p>}
                            {visit.nextVisitDate && (
                              <p className="visit-next">Next: {formatDate(visit.nextVisitDate)}</p>
                            )}
                          </div>
                          <div className="card-actions">
                            <button 
                              className="card-edit-btn"
                              onClick={() => {
                                setEditVetVisit(visit)
                                setShowVetVisitsPanel(false)
                                setIsAddVetVisitOpen(true)
                              }}
                              title="Edit notes"
                            >
                              ✏️
                            </button>
                          </div>
                        </div>
                      ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        <AddFeedingScheduleModal
          isOpen={isAddFeedingOpen}
          onClose={() => {
            setIsAddFeedingOpen(false)
            setEditFeedingSchedule(null)
          }}
          onScheduleAdded={() => {
            loadPetDetails()
            if (onPetUpdated) onPetUpdated()
          }}
          ownerId={user.id}
          petId={pet.id}
          editSchedule={editFeedingSchedule}
        />

        {showFeedingPanel && (
          <div className="feeding-panel-overlay" onClick={() => setShowFeedingPanel(false)}>
            <div className="feeding-panel" onClick={(e) => e.stopPropagation()}>
              <div className="feeding-panel-header">
                <h3>Feeding Schedule</h3>
                <button className="feeding-panel-close" onClick={() => setShowFeedingPanel(false)}>✕</button>
              </div>
              <div className="feeding-panel-actions">
                <button 
                  className="add-feeding-btn"
                  onClick={() => {
                    setEditFeedingSchedule(null)
                    setShowFeedingPanel(false)
                    setIsAddFeedingOpen(true)
                  }}
                >
                  + Add Feeding Time
                </button>
              </div>
              <div className="feeding-panel-content">
                {feedingSchedules.length === 0 ? (
                  <p className="empty-state">No feeding schedule set yet.</p>
                ) : (
                  <div className="feeding-list">
                    {feedingSchedules
                      .sort((a, b) => a.time.localeCompare(b.time))
                      .map((schedule) => (
                        <div key={schedule.id} className="feeding-card">
                          <div className="feeding-card-time">
                            <span className="feeding-card-hour">{formatTime(schedule.time)}</span>
                          </div>
                          <div className="feeding-card-details">
                            <p className="feeding-card-food">{schedule.foodType}</p>
                            <p className="feeding-card-amount">
                              {schedule.quantity} {schedule.quantityUnit.toLowerCase()}
                            </p>
                          </div>
                          <div className="card-actions">
                            <button 
                              className="card-edit-btn"
                              onClick={() => {
                                setEditFeedingSchedule(schedule)
                                setShowFeedingPanel(false)
                                setIsAddFeedingOpen(true)
                              }}
                              title="Edit"
                            >
                              ✏️
                            </button>
                            <button 
                              className="card-delete-btn"
                              onClick={() => setDeleteConfirm({ type: 'feeding', id: schedule.id, name: schedule.foodType })}
                              title="Delete"
                            >
                              🗑️
                            </button>
                          </div>
                        </div>
                      ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        <AddMedicationModal
          isOpen={isAddMedicationOpen}
          onClose={() => {
            setIsAddMedicationOpen(false)
            setEditMedication(null)
          }}
          onMedicationAdded={() => {
            loadPetDetails()
            if (onPetUpdated) onPetUpdated()
          }}
          ownerId={user.id}
          petId={pet.id}
          editMedication={editMedication}
        />

        {showMedicationPanel && (
          <div className="medication-panel-overlay" onClick={() => setShowMedicationPanel(false)}>
            <div className="medication-panel" onClick={(e) => e.stopPropagation()}>
              <div className="medication-panel-header">
                <h3>Medications</h3>
                <button className="medication-panel-close" onClick={() => setShowMedicationPanel(false)}>✕</button>
              </div>
              <div className="medication-panel-actions">
                <button 
                  className="add-medication-btn"
                  onClick={() => {
                    setEditMedication(null)
                    setShowMedicationPanel(false)
                    setIsAddMedicationOpen(true)
                  }}
                >
                  + Add Medication
                </button>
              </div>
              <div className="medication-panel-content">
                {medications.length === 0 ? (
                  <p className="empty-state">No medications added yet.</p>
                ) : (
                  <div className="medication-list">
                    {medications
                      .sort((a, b) => (a.timeToAdminister || '').localeCompare(b.timeToAdminister || ''))
                      .map((med) => (
                        <div key={med.id} className="medication-card">
                          <div className="medication-card-icon">💊</div>
                          <div className="medication-card-details">
                            <p className="medication-card-name">{med.name}</p>
                            <p className="medication-card-dosage">
                              {med.dosageAmount} {med.dosageUnit?.toLowerCase()}
                            </p>
                            <p className="medication-card-schedule">
                              {formatTime(med.timeToAdminister)} • {med.frequency}
                            </p>
                            <p className="medication-card-dates">
                              {formatDate(med.startDate)}
                              {med.endDate ? ` → ${formatDate(med.endDate)}` : ' (ongoing)'}
                            </p>
                          </div>
                          <div className="card-actions">
                            <button 
                              className="card-edit-btn"
                              onClick={() => {
                                setEditMedication(med)
                                setShowMedicationPanel(false)
                                setIsAddMedicationOpen(true)
                              }}
                              title="Edit"
                            >
                              ✏️
                            </button>
                            <button 
                              className="card-delete-btn"
                              onClick={() => setDeleteConfirm({ type: 'medication', id: med.id, name: med.name })}
                              title="Delete"
                            >
                              🗑️
                            </button>
                          </div>
                        </div>
                      ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {deleteConfirm && (
          <div className="delete-confirm-overlay" onClick={() => setDeleteConfirm(null)}>
            <div className="delete-confirm-modal" onClick={(e) => e.stopPropagation()}>
              <h3>Delete {deleteConfirm.type === 'feeding' ? 'Feeding Schedule' : 'Medication'}?</h3>
              <p>Are you sure you want to delete &quot;{deleteConfirm.name}&quot;? This cannot be undone.</p>
              <div className="delete-confirm-actions">
                <button className="delete-cancel-btn" onClick={() => setDeleteConfirm(null)}>
                  Cancel
                </button>
                <button 
                  className="delete-confirm-btn"
                  onClick={() => {
                    if (deleteConfirm.type === 'feeding') {
                      handleDeleteFeedingSchedule(deleteConfirm.id)
                    } else {
                      handleDeleteMedication(deleteConfirm.id)
                    }
                  }}
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default PetDetailModal
