import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080/api'

const client = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // Send cookies for session auth
})

// Auth endpoints
export const login = (email, password) =>
  client.post('/auth/login', { email, password })

export const logout = () =>
  client.post('/auth/logout')

export const getCurrentUser = () =>
  client.get('/auth/me')

export const register = (payload) =>
  client.post('/owners/register', payload)

// Owner endpoints
export const getOwner = (ownerId) =>
  client.get(`/owners/${ownerId}`)

// Pet endpoints
export const getPets = (ownerId) =>
  client.get(`/owners/${ownerId}/pets`)

export const getPet = (ownerId, petId) =>
  client.get(`/owners/${ownerId}/pets/${petId}`)

export const createPet = (ownerId, payload) =>
  client.post(`/owners/${ownerId}/pets`, payload)

export const updatePet = (ownerId, petId, payload) =>
  client.patch(`/owners/${ownerId}/pets/${petId}`, payload)

export const deletePet = (ownerId, petId) =>
  client.delete(`/owners/${ownerId}/pets/${petId}`)

// Feeding schedule endpoints
export const getFeedingSchedules = (ownerId, petId) =>
  client.get(`/owners/${ownerId}/pets/${petId}/feeding-schedules`)

export const addFeedingSchedule = (ownerId, petId, payload) =>
  client.post(`/owners/${ownerId}/pets/${petId}/feeding-schedules`, payload)

export const updateFeedingSchedule = (ownerId, petId, scheduleId, payload) =>
  client.patch(`/owners/${ownerId}/pets/${petId}/feeding-schedules/${scheduleId}`, payload)

export const deleteFeedingSchedule = (ownerId, petId, scheduleId) =>
  client.delete(`/owners/${ownerId}/pets/${petId}/feeding-schedules/${scheduleId}`)

export default client
