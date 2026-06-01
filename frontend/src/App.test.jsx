import React from 'react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import App from './App'
import axios from 'axios'

vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    isAxiosError: vi.fn(() => false),
  },
}))

describe('App', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    global.fetch = vi.fn()
  })

  it('shows validation error when city is empty', async () => {
    render(<App />)

    fireEvent.click(screen.getByRole('button', { name: /fetch weather/i }))

    expect(await screen.findByText(/please enter a city name/i)).toBeInTheDocument()
  })

  it('renders stored readings after successful fetch', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      text: async () => '',
    })

    axios.get.mockResolvedValueOnce({
      data: [
        {
          id: 1,
          city: 'London',
          temperature: 22.4,
          windSpeed: 14.2,
          weatherDescription: 'Overcast',
          fetchedAt: '2026-05-31T10:00:00',
        },
      ],
    })

    render(<App />)

    fireEvent.change(screen.getByPlaceholderText(/enter city/i), {
      target: { value: 'London' },
    })
    fireEvent.click(screen.getByRole('button', { name: /fetch weather/i }))

    await waitFor(() => {
      expect(screen.getByText(/stored readings/i)).toBeInTheDocument()
      expect(screen.getByText(/London/)).toBeInTheDocument()
      expect(screen.getByText(/22.4/)).toBeInTheDocument()
    })
  })

  it('shows error message when city is not found', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      status: 404,
      text: async () => JSON.stringify({ error: 'City not found: xyz' }),
    })

    render(<App />)

    fireEvent.change(screen.getByPlaceholderText(/enter city/i), {
      target: { value: 'xyz' },
    })
    fireEvent.click(screen.getByRole('button', { name: /fetch weather/i }))

    await waitFor(() => {
      expect(screen.getByText(/city not found: xyz/i)).toBeInTheDocument()
    })
  })
})
