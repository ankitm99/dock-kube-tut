import { useState, useEffect } from 'react'
import { API } from '../api.js'

export default function PlaceOrder({ onOrderPlaced }) {
  const [products, setProducts]   = useState([])
  const [productId, setProductId] = useState('')
  const [quantity, setQuantity]   = useState(1)
  const [msg, setMsg]             = useState(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    API.products()
      .then(r => r.json())
      .then(setProducts)
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setMsg(null)
    try {
      const res = await API.createOrder({ productId: Number(productId), quantity: Number(quantity) })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const order = await res.json()
      setMsg({ type: 'success', text: `Order #${order.id} placed! Total: $${order.totalPrice.toFixed(2)}` })
      setProductId('')
      setQuantity(1)
      onOrderPlaced?.()
    } catch (err) {
      setMsg({ type: 'error', text: `Failed: ${err.message}` })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section>
      <h2>Place an Order</h2>
      <form className="order-form" onSubmit={handleSubmit}>
        <label>
          Product
          <select value={productId} onChange={e => setProductId(e.target.value)} required>
            <option value="">— select —</option>
            {products.map(p => (
              <option key={p.id} value={p.id}>{p.name} (${p.price.toFixed(2)})</option>
            ))}
          </select>
        </label>

        <label>
          Quantity
          <input
            type="number"
            min="1"
            max="99"
            value={quantity}
            onChange={e => setQuantity(e.target.value)}
            required
          />
        </label>

        <button type="submit" disabled={submitting}>
          {submitting ? 'Placing…' : 'Place Order'}
        </button>
      </form>

      {msg && <p className={`status ${msg.type === 'error' ? 'error' : 'success'}`}>{msg.text}</p>}
    </section>
  )
}
