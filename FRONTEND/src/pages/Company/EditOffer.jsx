import { useEffect, useState } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Briefcase, AlertCircle } from 'lucide-react'
import Navbar from '../../components/Navbar.jsx'
import Footer from '../../components/Footer.jsx'
import { fetchCategories } from '../../lib/api.js'
import { fetchMyOffers, updateOffer } from '../../lib/ofertas.js'
import { useSession } from '../../lib/useSession.js'

export default function EditOffer() {
  const session = useSession()
  const navigate = useNavigate()
  const { id } = useParams()
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [loadingOffer, setLoadingOffer] = useState(true)

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]))
  }, [])

  useEffect(() => {
    if (session?.role !== 'COMPANY') return
    fetchMyOffers()
      .then((offers) => {
        const offer = offers.find((o) => String(o.id) === id)
        if (offer) {
          setForm({
            categoryId: String(offer.categoryId),
            title: offer.title,
            description: offer.description,
            budget: String(offer.budget),
            deadline: offer.deadline,
          })
        } else {
          setError('No se encontró esa oferta o no te pertenece')
        }
      })
      .catch((err) => setError(err.message || 'No se pudo cargar la oferta'))
      .finally(() => setLoadingOffer(false))
  }, [session, id])

  if (session === null) {
    return <Navigate to="/login" replace />
  }

  if (session && session.role !== 'COMPANY') {
    return <Navigate to="/ofertas" replace />
  }

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await updateOffer(id, {
        categoryId: Number(form.categoryId),
        title: form.title,
        description: form.description,
        budget: Number(form.budget),
        deadline: form.deadline,
      })
      navigate('/empresa/mis-ofertas')
    } catch (err) {
      setError(err.message || 'No se pudo actualizar la oferta')
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
              <Briefcase size={20} />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-(--color-text-main)">Editar oferta</h1>
              <p className="text-sm text-(--color-text-muted)">Actualiza los detalles de tu oferta de trabajo</p>
            </div>
          </div>

          {loadingOffer && <p className="text-(--color-text-muted)">Cargando...</p>}

          {form && (
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              <label className="flex flex-col gap-1.5">
                <span className="text-sm font-medium text-(--color-text-main)">Título</span>
                <input
                  type="text"
                  name="title"
                  required
                  value={form.title}
                  onChange={handleChange}
                  className="w-full rounded-xl border border-(--color-border) bg-(--color-bg-input) px-4 py-3 text-sm text-(--color-text-main) outline-none"
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
                  className="w-full resize-none rounded-xl border border-(--color-border) bg-(--color-bg-input) px-4 py-3 text-sm text-(--color-text-main) outline-none"
                />
              </label>

              <div className="grid grid-cols-2 gap-3">
                <label className="flex flex-col gap-1.5">
                  <span className="text-sm font-medium text-(--color-text-main)">Presupuesto (CLP)</span>
                  <input
                    type="number"
                    name="budget"
                    required
                    min="0"
                    value={form.budget}
                    onChange={handleChange}
                    className="w-full rounded-xl border border-(--color-border) bg-(--color-bg-input) px-4 py-3 text-sm text-(--color-text-main) outline-none"
                  />
                </label>
                <label className="flex flex-col gap-1.5">
                  <span className="text-sm font-medium text-(--color-text-main)">Fecha límite</span>
                  <input
                    type="date"
                    name="deadline"
                    required
                    value={form.deadline}
                    onChange={handleChange}
                    className="w-full rounded-xl border border-(--color-border) bg-(--color-bg-input) px-4 py-3 text-sm text-(--color-text-main) outline-none"
                  />
                </label>
              </div>

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
                {loading ? 'Guardando...' : 'Guardar cambios'}
              </button>
            </form>
          )}

          {!form && !loadingOffer && error && (
            <div className="flex items-center gap-2 rounded-xl border border-(--color-cta)/40 bg-(--color-cta)/10 px-4 py-3 text-sm text-(--color-cta)">
              <AlertCircle size={16} className="shrink-0" />
              <span>{error}</span>
            </div>
          )}
        </motion.div>
      </main>
      <Footer />
    </div>
  )
}
