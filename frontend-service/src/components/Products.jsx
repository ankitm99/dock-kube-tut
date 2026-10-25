import { useState, useEffect } from 'react'
import { API } from '../api.js'

export default function Products() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState(null)

  useEffect(() => {
    API.products()
      .then(r => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`)
        return r.json()
      })
      .then(setProducts)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="status">Loading products…</p>
  if (error)   return <p className="status error">Error: {error}</p>

  return (
    <section>
      <h2>Products</h2>
      <div className="card-grid">
        {products.map(p => (
          <div key={p.id} className="card">
            <h3>{p.name}</h3>
            <p>{p.description}</p>
            <span className="price">${p.price.toFixed(2)}</span>
          </div>
        ))}
      </div>
    </section>
  )
}
