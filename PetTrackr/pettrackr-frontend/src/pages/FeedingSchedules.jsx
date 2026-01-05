import { useState } from 'react'
import {
  getFeedingSchedules,
  addFeedingSchedule,
  updateFeedingSchedule,
  deleteFeedingSchedule
} from '../services/api'
import './feeding.css'

const quantityUnits = ['CUPS', 'GRAMS', 'OUNCES', 'CANS']

function FeedingSchedules() {
  const [ownerId, setOwnerId] = useState('')
  const [petId, setPetId] = useState('')
  const [schedules, setSchedules] = useState([])
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    time: '08:00',
    foodType: 'Dry food',
    quantity: '1',
    quantityUnit: 'CUPS',
    scheduleId: '' // for updates/deletes
  })

  const showError = (msg) => {
    setError(msg)
    setMessage('')
  }

  const showMessage = (msg) => {
    setMessage(msg)
    setError('')
  }

  const validateIds = () => {
    if (!ownerId || !petId) {
      showError('Enter both ownerId and petId')
      return false
    }
    return true
  }

  const handleLoad = async () => {
    if (!validateIds()) return
    try {
      setLoading(true)
      const { data } = await getFeedingSchedules(ownerId, petId)
      setSchedules(data)
      showMessage('Loaded feeding schedules')
    } catch (e) {
      showError(e.response?.data?.message || 'Failed to load schedules')
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = async () => {
    if (!validateIds()) return
    try {
      setLoading(true)
      const payload = {
        time: form.time,
        foodType: form.foodType,
        quantity: Number(form.quantity),
        quantityUnit: form.quantityUnit
      }
      await addFeedingSchedule(ownerId, petId, payload)
      await handleLoad()
      showMessage('Created feeding schedule')
    } catch (e) {
      showError(e.response?.data?.message || 'Create failed')
      setLoading(false)
    }
  }

  const handleUpdate = async () => {
    if (!validateIds()) return
    if (!form.scheduleId) {
      showError('Provide scheduleId to update')
      return
    }
    try {
      setLoading(true)
      const payload = {
        time: form.time,
        foodType: form.foodType,
        quantity: form.quantity ? Number(form.quantity) : undefined,
        quantityUnit: form.quantityUnit
      }
      await updateFeedingSchedule(ownerId, petId, form.scheduleId, payload)
      await handleLoad()
      showMessage('Updated feeding schedule')
    } catch (e) {
      showError(e.response?.data?.message || 'Update failed')
      setLoading(false)
    }
  }

  const handleDelete = async (scheduleId) => {
    if (!validateIds()) return
    try {
      setLoading(true)
      await deleteFeedingSchedule(ownerId, petId, scheduleId)
      await handleLoad()
      showMessage('Deleted feeding schedule')
    } catch (e) {
      showError(e.response?.data?.message || 'Delete failed')
      setLoading(false)
    }
  }

  const updateForm = (key, value) => setForm((prev) => ({ ...prev, [key]: value }))

  return (
    <div className="layout">
      <section className="panel">
        <div className="panel-head">
          <div>
            <p className="eyebrow">Connect</p>
            <h2>Feeding schedules</h2>
          </div>
          <button className="ghost" onClick={handleLoad} disabled={loading}>Refresh</button>
        </div>

        <div className="grid">
          <label className="field">
            <span>Owner ID</span>
            <input
              type="number"
              value={ownerId}
              onChange={(e) => setOwnerId(e.target.value)}
              placeholder="e.g. 1"
            />
          </label>
          <label className="field">
            <span>Pet ID</span>
            <input
              type="number"
              value={petId}
              onChange={(e) => setPetId(e.target.value)}
              placeholder="e.g. 2"
            />
          </label>
          <label className="field">
            <span>Schedule ID (for update/delete)</span>
            <input
              type="number"
              value={form.scheduleId}
              onChange={(e) => updateForm('scheduleId', e.target.value)}
              placeholder="existing id"
            />
          </label>
        </div>

        <div className="grid">
          <label className="field">
            <span>Time</span>
            <input
              type="time"
              value={form.time}
              onChange={(e) => updateForm('time', e.target.value)}
            />
          </label>
          <label className="field">
            <span>Food type</span>
            <input
              value={form.foodType}
              onChange={(e) => updateForm('foodType', e.target.value)}
              placeholder="Dry food"
            />
          </label>
          <label className="field">
            <span>Quantity</span>
            <input
              type="number"
              min="0"
              step="0.1"
              value={form.quantity}
              onChange={(e) => updateForm('quantity', e.target.value)}
              placeholder="1"
            />
          </label>
          <label className="field">
            <span>Quantity unit</span>
            <select
              value={form.quantityUnit}
              onChange={(e) => updateForm('quantityUnit', e.target.value)}
            >
              {quantityUnits.map((unit) => (
                <option key={unit} value={unit}>{unit}</option>
              ))}
            </select>
          </label>
        </div>

        <div className="actions">
          <button onClick={handleCreate} disabled={loading}>Create</button>
          <button onClick={handleUpdate} disabled={loading}>Update</button>
        </div>

        {message && <div className="notice success">{message}</div>}
        {error && <div className="notice error">{error}</div>}
      </section>

      <section className="panel list-panel">
        <div className="panel-head">
          <div>
            <p className="eyebrow">Results</p>
            <h2>Schedules</h2>
          </div>
          <button className="ghost" onClick={handleLoad} disabled={loading}>Reload</button>
        </div>

        {loading && <p className="muted">Loading...</p>}
        {!loading && schedules.length === 0 && (
          <p className="muted">No schedules yet. Hit Refresh after setting IDs.</p>
        )}

        <div className="list">
          {schedules.map((s) => (
            <div key={s.id} className="tile">
              <div>
                <p className="eyebrow">#{s.id}</p>
                <p className="title">{s.foodType}</p>
                <p className="muted">{s.time} · {s.quantity} {s.quantityUnit}</p>
              </div>
              <button className="ghost" onClick={() => handleDelete(s.id)} disabled={loading}>Delete</button>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export default FeedingSchedules
