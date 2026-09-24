import { Link } from 'react-router-dom';
import '../styles/footer.css';

const FOOTER_COLUMNS = [
    {
        heading: 'Categorías',
        links: [
            'Programación & Tech', 'Diseño Gráfico', 'Marketing Digital', 'Escritura & Traducción',
            'Video & Animación', 'IA & Automatización', 'Música & Audio', 'Negocios', 'Consultoría',
        ],
    },
    {
        heading: 'Para Clientes',
        links: [
            'Cómo funciona B2BMatch', 'Historias de éxito', 'Guía de calidad', 'Guías útiles', 'Preguntas frecuentes',
        ],
    },
    {
        heading: 'Para Profesionales',
        links: [
            'Hacete profesional', 'Hacete agencia', 'Comunidad', 'Foro', 'Eventos',
        ],
    },
    {
        heading: 'Soluciones de Negocio',
        links: [
            'B2BMatch Pro', 'Gestión de proyectos', 'Selección de expertos', 'Creador de logos', 'Contacto comercial',
        ],
    },
    {
        heading: 'Empresa',
        links: [
            'Acerca de', 'Centro de ayuda', 'Seguridad y confianza', 'Carreras', 'Términos y privacidad',
        ],
    },
];

const SOCIALS = [
    { label: 'TikTok', icon: '📱' },
    { label: 'Instagram', icon: '📸' },
    { label: 'LinkedIn', icon: '💼' },
    { label: 'Facebook', icon: '📘' },
    { label: 'X', icon: '🐦' },
];

export const Footer = ({ logo }) => {
    return (
        <footer className="footer-b2b footer-mega">
            <div className="footer-mega-grid">
                <div className="footer-mega-brand">
                    <div className="footer-mega-logo">
                        {logo ? <img src={logo} alt="B2BMatch" /> : <strong>B2BMatch</strong>}
                    </div>
                    <p className="footer-mega-tagline">
                        Marketplace B2B de servicios profesionales: conectá empresas con el talento verificado de primer nivel.
                    </p>
                    <div className="footer-mega-socials">
                        {SOCIALS.map((social) => (
                            <button key={social.label} className="footer-mega-social" type="button" aria-label={social.label}>
                                <span aria-hidden="true">{social.icon}</span>
                                <span className="footer-mega-social-label">{social.label}</span>
                            </button>
                        ))}
                    </div>
                </div>

                {FOOTER_COLUMNS.map((column) => (
                    <div key={column.heading} className="footer-mega-column">
                        <h4 className="footer-mega-heading">{column.heading}</h4>
                        <ul className="footer-mega-links">
                            {column.links.map((link) => (
                                <li key={link}>
                                    <Link to="/servicios">{link}</Link>
                                </li>
                            ))}
                        </ul>
                    </div>
                ))}
            </div>

            <div className="footer-mega-bottom">
                <p className="footer-mega-legal">
                    <Link to="/servicios">Servicios</Link> · <Link to="/empleos">Empleos</Link> ·{' '}
                    <Link to="/login">Ingresar</Link>
                </p>
                <p className="footer-mega-copy">© 2026 B2BMatch. Todos los derechos reservados.</p>
                <div className="footer-mega-locale">
                    <span className="footer-mega-pill">Español</span>
                    <span className="footer-mega-pill">$ USD</span>
                </div>
            </div>
        </footer>
    );
};

export default Footer;