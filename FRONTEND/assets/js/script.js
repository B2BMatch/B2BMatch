const CO=[
  {init:'TW',type:'empresa',name:'TechWeb Chile',role:'Desarrollo web y apps móviles',tags:['React','Node.js','AWS'],score:'97%'},
  {init:'D3',type:'empresa',name:'Diseño360 Studio',role:'Branding, diseño UX/UI y marketing',tags:['Figma','Adobe CC','Branding'],score:'94%'},
  {init:'CA',type:'empresa',name:'ContaAsesores SpA',role:'Contabilidad, tributaria y finanzas PYME',tags:['SAP','Excel','Tributaria'],score:'96%'},
  {init:'MM',type:'empresa',name:'Marketing Masters',role:'Marketing digital, SEO y redes sociales',tags:['Google Ads','Meta','SEO'],score:'92%'},
];
const WO=[
  {init:'JP',type:'usuario',name:'Juan Pérez',role:'Desarrollador web freelance · 8 años exp.',tags:['HTML','CSS','JavaScript'],score:'95%'},
  {init:'AL',type:'usuario',name:'Andrea Lagos',role:'Diseñadora gráfica independiente',tags:['Illustrator','Figma','Canva'],score:'98%'},
  {init:'RM',type:'usuario',name:'Roberto Muñoz',role:'Gasfíter certificado · Disponible esta semana',tags:['Residencial','Comercial','Urgencias'],score:'91%'},
  {init:'VP',type:'usuario',name:'Valeria Pinto',role:'Contadora independiente · PYMES',tags:['Contabilidad','IVA','Renta'],score:'93%'},
];
function setQuery(t){document.getElementById('search-input').value=t;document.getElementById('search-input').focus()}
function triggerSearch(){
  const q=document.getElementById('search-input').value.trim();
  if(!q)return;
  document.getElementById('ia-section').classList.add('visible');
  document.getElementById('results-list').style.display='none';
  document.getElementById('ai-q-txt').textContent='Encontré resultados relacionados con "'+q+'". ¿Qué tipo de resultado te interesa más?';
  ['btn-co','btn-wo','btn-all'].forEach(id=>document.getElementById(id).classList.remove('act'));
  document.getElementById('below-fold').scrollIntoView({behavior:'smooth'});
}
function showResults(type){
  document.getElementById('results-list').style.display='block';
  const q=document.getElementById('search-input').value||'tu búsqueda';
  document.getElementById('r-query').textContent=q;
  const data=type==='empresa'?CO:type==='usuario'?WO:[...CO,...WO];
  document.getElementById('r-count').textContent=data.length;
  ['btn-co','btn-wo','btn-all'].forEach(id=>document.getElementById(id).classList.remove('act'));
  if(type==='empresa')document.getElementById('btn-co').classList.add('act');
  else if(type==='usuario')document.getElementById('btn-wo').classList.add('act');
  else document.getElementById('btn-all').classList.add('act');
  document.getElementById('card-list').innerHTML=data.map(d=>{
    const co=d.type==='empresa';
    return`<div class="result-card">
      <div class="ava ${co?'ava-co':'ava-wo'}">${d.init}</div>
      <div>
        <div class="card-name">${d.name}<span class="type-badge ${co?'badge-co':'badge-wo'}">${co?'empresa':'freelance'}</span></div>
        <div class="card-role">${d.role}</div>
        <div class="card-tags">${d.tags.map(t=>`<span class="tag ${co?'tag-co':'tag-wo'}">${t}</span>`).join('')}<span class="trust">✓ ${d.score}</span></div>
      </div>
      <button class="card-action">Ver perfil →</button>
    </div>`;
  }).join('');
}
document.getElementById('search-btn')?.addEventListener('click',triggerSearch);
document.getElementById('search-input')?.addEventListener('keydown',e=>{if(e.key==='Enter')triggerSearch()});
document.addEventListener('DOMContentLoaded', () => {
  const authContainer = document.getElementById('nav-auth-container');
  if (authContainer && localStorage.getItem('expertConnect_isLoggedIn') === 'true') {
    const userRole = localStorage.getItem('userRole');
    const perfilMap = {
      admin: 'admin/editor-empresas.html',
      empresa: 'empresa/perfil.html',
      trabajador: 'trabajador/perfil.html',
      usuario: 'usuario/perfil.html'
    };
    const perfilUrl = perfilMap[userRole] || 'usuario/perfil.html';
    authContainer.innerHTML = `
      <button class="btn-n-solid" onclick="window.location.href='${perfilUrl}'">Mi Perfil</button>
      <button class="btn-n-ghost" onclick="cerrarSesionDesdeInicio()">Cerrar sesión</button>
    `;
  }
});

function cerrarSesionDesdeInicio() {
  localStorage.clear();
  window.location.reload();
}

/* HAMBURGUESA mobile toggle */
document.querySelector('.hamburger')?.addEventListener('click', function(){
  document.querySelector('.nav-links')?.classList.toggle('open');
});
