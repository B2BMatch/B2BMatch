document.addEventListener('DOMContentLoaded', () => {
  // ELEMENTOS DEL DOM
  const loginForm = document.getElementById('login-form');
  const nameGroup = document.getElementById('name-group');
  const nameInput = document.getElementById('name');
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  
  // Elementos de Títulos y Botones
  const cardTitle = document.getElementById('login-card-title');
  const cardSubtitle = document.getElementById('login-card-subtitle');
  const btnSubmit = document.getElementById('btn-submit');
  const toggleText = document.getElementById('toggle-text');
  const toggleModeBtn = document.getElementById('toggle-mode-btn');

  // Elementos de Error
  const nameError = document.getElementById('name-error');
  const emailError = document.getElementById('email-error');
  const passwordError = document.getElementById('password-error');
  const generalError = document.getElementById('general-error');

  // Modo actual: 'login' o 'register'
  const urlParams = new URLSearchParams(window.location.search);
  let currentMode = urlParams.get('mode') === 'register' ? 'register' : 'login';

  // Base de datos simulada en localStorage
  const defaultUsers = {
    'test@example.com': { name: 'Usuario Demo', email: 'test@example.com', password: '123456', role: 'usuario' },
    'trabajador@test.com': { name: 'Trabajador Demo', email: 'trabajador@test.com', password: '123456', role: 'trabajador' },
    'empresa@test.com': { name: 'Empresa Demo', email: 'empresa@test.com', password: '123456', role: 'empresa' },
    'admin@test.com': { name: 'Administrador', email: 'admin@test.com', password: 'admin123', role: 'admin' }
  };

  let users = JSON.parse(localStorage.getItem('expertConnect_users') || '[]');

  // Mantener usuarios registrados que NO sean demo, y reiniciar los demo
  users = users.filter(u => !defaultUsers[u.email.toLowerCase()]);
  Object.values(defaultUsers).forEach(demo => users.push(demo));

  localStorage.setItem('expertConnect_users', JSON.stringify(users));

  function renderMode() {
    const roleGroup = document.getElementById('role-group');

    if (currentMode === 'register') {
      cardTitle.textContent = 'Crear Cuenta';
      cardSubtitle.textContent = 'Regístrate gratis en ExpertConnect';
      nameGroup.style.display = 'flex';
      nameInput.required = true;
      roleGroup.style.display = 'flex';
      btnSubmit.textContent = 'Registrarse';
      toggleText.innerHTML = '¿Ya tienes una cuenta? <a href="#" id="toggle-mode-btn">Inicia sesión aquí</a>';
    } else {
      cardTitle.textContent = 'Bienvenido de nuevo';
      cardSubtitle.textContent = 'Ingresa a tu cuenta de ExpertConnect';
      nameGroup.style.display = 'none';
      nameInput.required = false;
      roleGroup.style.display = 'none';
      btnSubmit.textContent = 'Iniciar Sesión';
      toggleText.innerHTML = '¿No tienes una cuenta? <a href="#" id="toggle-mode-btn">Regístrate aquí</a>';
    }
  }

  function switchMode() {
    nameError.style.display = 'none';
    emailError.style.display = 'none';
    passwordError.style.display = 'none';
    generalError.style.display = 'none';
    loginForm.reset();
    currentMode = currentMode === 'login' ? 'register' : 'login';
    renderMode();
  }

  renderMode();

  document.addEventListener('click', (e) => {
    if (e.target && e.target.id === 'toggle-mode-btn') {
      e.preventDefault();
      switchMode();
    }
  });

  // Procesar envío del formulario
  loginForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    // Ocultar mensajes de error previos
    nameError.style.display = 'none';
    emailError.style.display = 'none';
    passwordError.style.display = 'none';
    generalError.style.display = 'none';
    
    let isValid = true;
    
    // 1. Validar Nombre (solo en modo registro)
    const nameValue = nameInput.value.trim();
    if (currentMode === 'register' && nameValue.length < 3) {
      nameError.style.display = 'block';
      isValid = false;
    }

    // 2. Validar Correo Electrónico
    const emailValue = emailInput.value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(emailValue)) {
      emailError.style.display = 'block';
      isValid = false;
    }
    
    // 3. Validar Contraseña
    const passwordValue = passwordInput.value.trim();
    if (passwordValue.length < 6) {
      passwordError.style.display = 'block';
      isValid = false;
    }
    
    if (!isValid) return;

    // Obtener usuarios guardados en localStorage
    const users = JSON.parse(localStorage.getItem('expertConnect_users') || '[]');

    if (currentMode === 'register') {
      // PROCESAR REGISTRO
      
      // Verificar si el correo ya existe
      const userExists = users.some(u => u.email.toLowerCase() === emailValue.toLowerCase());
      if (userExists) {
        generalError.textContent = 'Este correo electrónico ya está registrado.';
        generalError.style.display = 'block';
        return;
      }

      const roleValue = document.getElementById('role-select').value;

      users.push({
        name: nameValue,
        email: emailValue,
        password: passwordValue,
        role: roleValue
      });
      localStorage.setItem('expertConnect_users', JSON.stringify(users));

      alert('¡Cuenta creada con éxito! Ahora puedes iniciar sesión con tus credenciales.');
      
      // Volver a modo Login
      switchMode();
      
    } else {
      // PROCESAR INICIO DE SESIÓN
      
      // Buscar coincidencia en la base de datos simulada
      const foundUser = users.find(u => 
        u.email.toLowerCase() === emailValue.toLowerCase() && 
        u.password === passwordValue
      );

      if (foundUser) {
        const userRole = foundUser.role || 'usuario';

        localStorage.setItem('expertConnect_userEmail', foundUser.email);
        localStorage.setItem('expertConnect_userName', foundUser.name);
        localStorage.setItem('expertConnect_isLoggedIn', 'true');
        localStorage.setItem('userRole', userRole);
        localStorage.setItem('userName', foundUser.name);
        
        const redirectMap = {
          admin: 'admin/editor-empresas.html',
          empresa: 'empresa/perfil.html',
          trabajador: 'trabajador/perfil.html',
          usuario: 'usuario/perfil.html'
        };
        window.location.href = redirectMap[userRole] || 'usuario/perfil.html';
      } else {
        // Credenciales inválidas
        generalError.textContent = 'Correo o contraseña incorrectos.';
        generalError.style.display = 'block';
      }
    }
  });
});

/* HAMBURGUESA mobile toggle */
document.querySelector('.hamburger')?.addEventListener('click', function(){
  document.querySelector('.nav-links')?.classList.toggle('open');
});
