import { useEffect, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Sparkles, AlertCircle } from 'lucide-react'
import Navbar from '../../components/Navbar.jsx'
import Footer from '../../components/Footer.jsx'
import { fetchCategories, createProfessionalService } from '../../lib/api.js'
import { useSession } from '../../lib/useSession.js'

const INITIAL_FORM = {
  categoryId: '',
  title: '',
  description: '',
  price: '',
}

export default function CreateService() {
  const session = useSession()
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState(INITIAL_FORM)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]))
  }, [])

  if (session === null) {
    return <Navigate to="/login" replace />
  }

  if (session && session.role !== 'PROFESSIONAL') {
    return <Navigate to="/servicios-profesionales" replace />
  }

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await createProfessionalService({
        categoryId: Number(form.categoryId),
        title: form.title,
        description: form.description,
        price: Number(form.price),
      })
      navigate('/servicios-profesionales')
    } catch (err) {
      setError(err.message || 'No se pudo publicar el servicio')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-dvh bg-(--color-bg-main)">
      <Navbar />
      <main className="mx-auto flex max-w-2xl flex-col px-5 py-16 sm:px-8">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="rounded-2xl border border-(--color-border) bg-(--color-bg-card) p-8"
        >
          <div className="mb-8 flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-full bg-(--color-cta)/10 text-(--color-cta)">
              <Sparkles size={20} />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-(--color-text-main)">Publicar un servicio</h1>
              <p className="text-sm text-(--color-text-muted)">Ofrece tus servicios a clientes que buscan tu especialidad</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-(--color-text-main)">Título</span>
              <input
                type="text"
                name="title"
                required
                value={form.title}
                onChange={handleChange}
                placeholder="Desarrollo de landing pages"
                className="w-full rounded-xl border border-(--color-border) bg-(--color-bg-input) px-4 py-3 text-sm text-(--color-text-main) outline-none placeholder:text-(--color-text-muted)"
              />
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-(--color-text-main)">Categoría</span>
              <select
                name="categoryId"
                required
                value={form.categoryId}
                onChange={handleChange}
                className="w-full rounded-xl border border-(--color-border) bg-(--color-bg-main) px-4 py-3 text-sm text-(--color-text-main) outline-none"
              >
                <option value="" disabled>
                  Selecciona una categoría
                </option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-(--color-text-main)">Descripción</span>
              <textarea
                name="description"
                required
                rows={4}
                value={form.description}
                onChange={handleChange}
                placeholder="Describe qué incluye tu servicio y qué te diferencia."
                className="w-full resize-none rounded-xl border border-(--color-border) bg-(--color-bg-input) px-4 py-3 text-sm text-(--color-text-main) outline-none placeholder:text-(--color-text-muted)"
              />
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-(--color-text-main)">Precio (CLP)</span>
              <input
                type="number"
                name="price"
                required
                min="0"
                value={form.price}
                onChange={handleChange}
                placeholder="250000"
                className="w-full rounded-xl border border-(--color-border) bg-(--color-bg-input) px-4 py-3 text-sm text-(--color-text-main) outline-none placeholder:text-(--color-text-muted)"
              />
            </label>

            {error && (
              <div className="flex items-center gap-2 rounded-xl border border-(--color-cta)/40 bg-(--color-cta)/10 px-4 py-3 text-sm text-(--color-cta)">
                <AlertCircle size={16} className="shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="mt-2 cursor-pointer rounded-full bg-(--color-cta) py-3 text-sm font-semibold text-white shadow-[0_0_12px_rgba(229,62,62,0.3)] transition hover:shadow-[0_0_20px_rgba(229,62,62,0.6)] active:scale-95 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? 'Publicando...' : 'Publicar servicio'}
            </button>
          </form>
        </motion.div>
      </main>
      <Footer />
    </div>
  )
}
