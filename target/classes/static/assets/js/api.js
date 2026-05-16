/* ============================================================
   vOO Airways — api.js
   Funções de comunicação com o backend Spring Boot
   ============================================================ */

const API_BASE = 'http://localhost:8080/api';

/* ── Utilitários ─────────────────────────────────────────── */

async function apiCall(method, endpoint, body = null) {
  const options = {
    method,
    headers: { 'Content-Type': 'application/json' },
  };
  if (body) options.body = JSON.stringify(body);

  const res = await fetch(API_BASE + endpoint, options);
  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.error || 'Erro na requisição');
  }
  return data;
}

/* ── Reservas ────────────────────────────────────────────── */

async function createBooking(payload) {
  return apiCall('POST', '/bookings', payload);
}

async function getBookingByLocator(locator) {
  return apiCall('GET', `/bookings/${locator}`);
}

async function cancelBooking(locator) {
  return apiCall('PATCH', `/bookings/${locator}/cancel`);
}

/* ── Passageiros ─────────────────────────────────────────── */

async function getPassengerByCpf(cpf) {
  return apiCall('GET', `/passengers/cpf/${cpf}`);
}

async function getBookingsByPassenger(cpf) {
  return apiCall('GET', `/passengers/cpf/${cpf}/bookings`);
}

/* ── Health ──────────────────────────────────────────────── */

async function checkHealth() {
  return apiCall('GET', '/health');
}

/* ── Mapeamento de campos da UI ──────────────────────────── */

/**
 * Lê todos os campos do formulário de reserva e monta
 * o payload no formato esperado pelo backend.
 */
function buildBookingPayload() {
  const flightType = document.getElementById('voo-flight-type').value;
  const origin     = document.getElementById('voo-origin').value;
  const dest       = document.getElementById('voo-destination').value;
  const depDate    = document.getElementById('voo-dep-date').value;
  const retDate    = document.getElementById('voo-ret-date').value;
  const name       = document.getElementById('voo-passenger-name').value;
  const email      = document.getElementById('voo-passenger-email').value;
  const docNumber  = document.getElementById('voo-passport').value;
  const seat       = document.getElementById('voo-selected-seat').value;
  const flightClass = document.getElementById('voo-flight-class').value;

  return {
    flightNum:   'VO' + Math.floor(1000 + Math.random() * 9000),
    origin:      origin  || 'LHR',
    destination: dest    || 'HND',
    depDate:     depDate || new Date().toISOString().split('T')[0],
    retDate:     retDate || null,
    flightType:  flightType || 'ROUNDTRIP',
    flightClass: flightClass || 'EXECUTIVE',
    seat:        seat    || null,
    gate:        'B' + Math.floor(1 + Math.random() * 40),
    aircraft:    'Boeing 787 Dreamliner',
    departure:   '18:45',
    boarding:    '18:15',
    payMethod:   'CREDIT_CARD',
    passengerData: {
      name:      name,
      email:     email,
      docType:   'PASSPORT',
      docNumber: docNumber,
    }
  };
}

/* ── Handler principal do botão Confirm Booking ──────────── */

async function handleConfirmBooking() {
  const btn = document.getElementById('voo-confirm-btn');

  // Validação básica
  const name  = document.getElementById('voo-passenger-name').value.trim();
  const email = document.getElementById('voo-passenger-email').value.trim();
  if (!name || !email) {
    showToast('Preencha o nome e e-mail do passageiro.', 'error');
    return;
  }

  // Loading state
  btn.disabled = true;
  btn.textContent = 'Processando...';

  try {
    const payload  = buildBookingPayload();
    const response = await createBooking(payload);
    const booking  = response.data;

    // Preenche a boarding pass com os dados retornados pelo backend
    fillBoardingPass(booking);

    showToast('Reserva confirmada com sucesso!', 'success');
  } catch (err) {
    showToast(err.message || 'Erro ao confirmar reserva.', 'error');
    btn.disabled = false;
    btn.textContent = 'Confirm Booking';
  }
}

/* ── Preenche a boarding pass visualmente ────────────────── */

const CITY_NAMES = {
  JFK:'NEW YORK, US', BOS:'BOSTON, US',   YEG:'EDMONTON, CA',
  YYZ:'TORONTO, CA',  LHR:'LONDON, UK',   AUH:'ABU DHABI, AE',
  DXB:'DUBAI, AE',    SYD:'SYDNEY, AU',   WLG:'WELLINGTON, NZ',
  GIG:'RIO DE JANEIRO, BR', REC:'RECIFE, BR', KEF:'REYKJAVÍK, IS'
};

function fillBoardingPass(booking) {
  const set = (id, val) => {
    const el = document.getElementById(id);
    if (el && val !== undefined && val !== null) el.textContent = val;
  };

  set('bp-locator',     booking.locator);
  set('bp-flight-num',  booking.flightNum);
  set('bp-origin',      booking.origin);
  set('bp-origin-city', CITY_NAMES[booking.origin] || booking.origin);
  set('bp-destination', booking.destination);
  set('bp-dest-city',   CITY_NAMES[booking.destination] || booking.destination);
  set('bp-seat',        booking.seat || '—');
  set('bp-gate',        booking.gate);
  set('bp-boarding',    booking.boarding);
  set('bp-passenger',   booking.passenger?.name?.toUpperCase() || '');
  set('bp-class',       booking.flightClass === 'EXECUTIVE'       ? 'FIRST CLASS'
                      : booking.flightClass === 'PREMIUM_ECONOMY' ? 'BUSINESS PLUS'
                      : 'ECONOMY');

  // Garante que a seção está visível
  const ss = document.getElementById('voo-success-section');
  if (ss) {
    ss.classList.remove('hidden');
    setTimeout(() => ss.scrollIntoView({ behavior: 'smooth', block: 'start' }), 150);
  }
}

/* ── Toast de feedback ───────────────────────────────────── */

function showToast(message, type = 'success') {
  const existing = document.getElementById('voo-toast');
  if (existing) existing.remove();

  const bg = type === 'success' ? '#785600' : '#ba1a1a';
  const toast = document.createElement('div');
  toast.id = 'voo-toast';
  toast.style.cssText = `
    position:fixed; bottom:2rem; right:2rem; z-index:9999;
    background:${bg}; color:#fff;
    padding:1rem 1.5rem; border-radius:0.5rem;
    font-family:'Manrope',sans-serif; font-size:0.875rem; font-weight:600;
    box-shadow:0 10px 30px rgba(0,0,0,0.15);
    animation: slideIn 0.3s ease;
  `;
  toast.textContent = message;

  const style = document.createElement('style');
  style.textContent = '@keyframes slideIn { from { transform: translateY(1rem); opacity:0; } to { transform: translateY(0); opacity:1; } }';
  document.head.appendChild(style);

  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}
