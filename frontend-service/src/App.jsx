import { useState, useEffect } from 'react'
import Products from './components/Products.jsx'
import Orders from './components/Orders.jsx'
import PlaceOrder from './components/PlaceOrder.jsx'
import './App.css'

function App() {
  const [tab, setTab] = useState('products')
  const [refreshOrders, setRefreshOrders] = useState(0)

  return (
    <div className="app">
      <header>
        <h1>🛒 Shop Dashboard</h1>
        <nav>
          <button className={tab === 'products' ? 'active' : ''} onClick={() => setTab('products')}>Products</button>
          <button className={tab === 'orders'   ? 'active' : ''} onClick={() => setTab('orders')}>Orders</button>
        </nav>
      </header>

      <main>
        {tab === 'products' && (
          <>
            <Products />
            <PlaceOrder onOrderPlaced={() => { setTab('orders'); setRefreshOrders(n => n + 1) }} />
          </>
        )}
        {tab === 'orders' && <Orders key={refreshOrders} />}
      </main>
    </div>
  )
}

export default App
