import '../styles/image-placeholder.css';

export const ImagePlaceholder = ({
  label = 'Imagen',
  hint = 'Agregá la imagen aquí',
  className = '',
  style = {},
}) => {
  return (
    <div className={`img-placeholder ${className}`} style={style} role="img" aria-label={label}>
      <svg
        className="img-placeholder-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <rect x="3" y="3" width="18" height="18" rx="2" />
        <circle cx="8.5" cy="8.5" r="1.5" />
        <path d="m21 15-5-5L5 21" />
      </svg>
      <span className="img-placeholder-label">{label}</span>
      <span className="img-placeholder-hint">{hint}</span>
    </div>
  );
};

export default ImagePlaceholder;