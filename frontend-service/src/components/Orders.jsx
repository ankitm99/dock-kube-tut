import { useState, useEffect } from 'react'
import { API } from '../api.js'

const STATUS_COLOR = {
  CONFIRMED: '#22c55e',
  PENDING:   '#f59e0b',
  CANCELLED: '#ef4444',
}

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState(null)

  useEffect(() => {
    API.orders()
      .then(r => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`)
        return r.json()
      })
      .then(setOrders)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="status">Loading orders…</p>
  if (error)   return <p className="status error">Error: {error}</p>
  if (!orders.length) return <p className="status">No orders yet. Place one from the Products tab!</p>

  return (
    <section>
      <h2>Orders</h2>
      <table className="orders-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Product</th>
            <th>Qty</th>
            <th>Unit Price</th>
            <th>Total</th>
            <th>Status</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(o => (
            <tr key={o.id}>
              <td>{o.id}</td>
              <td>{o.productName}</td>
              <td>{o.quantity}</td>
              <td>${o.unitPrice.toFixed(2)}</td>
              <td>${o.totalPrice.toFixed(2)}</td>
              <td>
                <span className="badge" style={{ background: STATUS_COLOR[o.status] ?? '#6b7280' }}>
                  {o.status}
                </span>
              </td>
              <td>{new Date(o.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
