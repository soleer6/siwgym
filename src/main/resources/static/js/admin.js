// ----- 1) Recuperar del localStorage -----
const userId   = localStorage.getItem('userId');
const username = localStorage.getItem('username');
const role     = localStorage.getItem('role');

// 2) Comprobar sesión/rol
if (!userId) {
    window.location.href = 'login.html';
}
if (role !== 'ADMIN') {
    alert('Access denied. Admin only.');
    window.location.href = 'login.html';
}

// 3) Mostrar usuario y rol en la cabecera
document.getElementById('usernameDisplay').textContent = username;
document.getElementById('roleDisplay').textContent   = role;

// 4) Logout
document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.clear();
    window.location.href = 'login.html';
});

// ======= Manejo de pestañas internas =======
const tabActivities = document.getElementById('tabActivities');
const tabOffers     = document.getElementById('tabOffers');
const tabReports    = document.getElementById('tabReports');

const activitiesSection = document.getElementById('activitiesSection');
const offersSection     = document.getElementById('offersSection');
const reportsSection    = document.getElementById('reportsSection');

function getCsrfToken() {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

function showSection(sectionTab) {
    // Desactivar todas las tabs
    [tabActivities, tabOffers, tabReports].forEach(btn => btn.classList.remove('active'));
    // Ocultar todas las secciones
    [activitiesSection, offersSection, reportsSection].forEach(sec => sec.classList.remove('active'));

    if (sectionTab === 'activities') {
        tabActivities.classList.add('active');
        activitiesSection.classList.add('active');
    } else if (sectionTab === 'offers') {
        tabOffers.classList.add('active');
        offersSection.classList.add('active');
    } else if (sectionTab === 'reports') {
        tabReports.classList.add('active');
        reportsSection.classList.add('active');
    }
}

tabActivities.addEventListener('click', () => showSection('activities'));
tabOffers.addEventListener('click', () => showSection('offers'));
tabReports.addEventListener('click', () => showSection('reports'));

// Iniciar mostrando “Activities”
showSection('activities');

// ======= Cargar Monitors y Rooms para desplegables =======
let allMonitors = [];
let allRooms    = [];
async function loadMonitorsAndRooms() {
    // Traer monitors
    try {
        const respM = await fetch('/api/users?role=MONITOR', {
        credentials: 'include'
        });
            if (respM.ok) {
                allMonitors = await respM.json();
                const selMon = document.getElementById('activityMonitor');
                selMon.innerHTML = '<option value="">-- Select Monitor --</option>';
                allMonitors.forEach(u => {
                    const opt = document.createElement('option');
                    opt.value = u.username; // backend espera username
                    opt.textContent = u.username;
                    selMon.appendChild(opt);
                });
            } else {
                console.error('Failed to load monitors');
            }
    } catch (err) {
        console.error('Error loading monitors:', err);
    }

    // Traer rooms
    try {
        const respR = await fetch('/api/rooms', {
            credentials: 'include'
            });
        if (respR.ok) {
            allRooms = await respR.json();  
        } else {
            console.error('Failed to load rooms');
        }
    } catch (err) {
        console.error('Error loading rooms:', err);
    }
}


document.getElementById('sessionsCount')
  .addEventListener('change', () => {
    const container = document.getElementById('sessionsContainer');
    const count = parseInt(container.previousElementSibling.value, 10);
    container.innerHTML = '';  // limpias las sesiones viejas

    for (let i = 0; i < count; i++) {
      const div = document.createElement('div');
      div.className = 'session-item';

      // inputs de fecha/hora
      const inputStart = document.createElement('input');
      inputStart.type = 'datetime-local';
      inputStart.name = `sessionStart-${i}`;
      const inputEnd = document.createElement('input');
      inputEnd.type = 'datetime-local';
      inputEnd.name = `sessionEnd-${i}`;

      // select de sala usando allRooms
      const selectRoom = document.createElement('select');
      selectRoom.name = `sessionRoom-${i}`;
      allRooms.forEach(r => {
        const opt = document.createElement('option');
        opt.value = r.name;
        opt.textContent = `${r.name} (Capacity: ${r.capacity})`;
        selectRoom.appendChild(opt);
      });

      // opcional: añade labels para claridad
      div.innerHTML = `<label>Session ${i+1} Start</label>`;
      div.appendChild(inputStart);
      div.innerHTML += `<label>Session ${i+1} End</label>`;
      div.appendChild(inputEnd);
      div.innerHTML += `<label>Session ${i+1} Room</label>`;
      div.appendChild(selectRoom);

      container.appendChild(div);
    }
  });

const sessionsContainer = document.getElementById('sessionsContainer');

function renderSessionInputs() {
    const count = parseInt(sessionsCountInput.value, 10);
    sessionsContainer.innerHTML = '';
    for (let i = 1; i <= count; i++) {
        const wrapper = document.createElement('div');
        wrapper.classList.add('session-item');

        // Fecha/hora inicio
        const lblStart = document.createElement('label');
        lblStart.textContent = `Session ${i} Start`;
        const inpStart = document.createElement('input');
        inpStart.type = 'datetime-local';
        inpStart.id   = `session${i}Start`;
        inpStart.required = true;

        // Fecha/hora fin
        const lblEnd = document.createElement('label');
        lblEnd.textContent = `Session ${i} End`;
        const inpEnd = document.createElement('input');
        inpEnd.type = 'datetime-local';
        inpEnd.id   = `session${i}End`;
        inpEnd.required = true;

        // Sala
        const lblRoom = document.createElement('label');
        lblRoom.textContent = `Session ${i} Room`;
        const selRoomSess = document.createElement('select');
        selRoomSess.id = `session${i}Room`;
        selRoomSess.required = true;
        selRoomSess.innerHTML = '<option value="">-- Select Room --</option>';
        allRooms.forEach(r => {
            const opt = document.createElement('option');
            opt.value = r.name;
            opt.textContent = r.name;
            selRoomSess.appendChild(opt);
        });

        wrapper.appendChild(lblStart);
        wrapper.appendChild(inpStart);
        wrapper.appendChild(lblEnd);
        wrapper.appendChild(inpEnd);
        wrapper.appendChild(lblRoom);
        wrapper.appendChild(selRoomSess);

        sessionsContainer.appendChild(wrapper);
    }
}

// ======= Gestión de Activities (igual que antes, pero adaptado a dropdowns y sesiones) =======
const activitiesTableBody = document.querySelector('#activitiesTable tbody');
const activityForm        = document.getElementById('activityForm');
const saveActivityBtn     = document.getElementById('saveActivityBtn');

// Campos del formulario
const fieldId          = document.getElementById('activityId');
const fieldName        = document.getElementById('activityName');
const fieldDescription = document.getElementById('activityDescription');
const fieldDateTime    = document.getElementById('activityDateTime');
const fieldMaxCapacity = document.getElementById('activityMaxCapacity');
const fieldPrice       = document.getElementById('activityPrice');
const fieldMonitor     = document.getElementById('activityMonitor');
const sessionsCountInput = document.getElementById('sessionsCount');
sessionsCountInput.addEventListener('change', renderSessionInputs);





// Modal de detalle
const modalBg   = document.getElementById('activityModalBackground');
const modalWin  = document.getElementById('activityModal');
const modalCont = document.getElementById('modalContent');

// Cargar la lista de actividades en la tabla


async function loadActivities() {
    let resp;
    try {
            resp = await fetch('/api/activities', {
            credentials: 'include'
        });
    } catch (err) {
        console.error('Error fetching /api/activities:', err);
        return;
    }

    if (resp.status === 401 || resp.status === 403) {
        window.location.href = 'login.html';
        return;
    }

    const activities = await resp.json();
    activitiesTableBody.innerHTML = ''; // limpiar tabla

    activities.forEach(act => {
        const tr = document.createElement('tr');

        const idTd = document.createElement('td');
        idTd.textContent = act.id;

        const nameTd = document.createElement('td');
        nameTd.textContent = act.name;

        const descTd = document.createElement('td');
        descTd.textContent = act.description;

        const dtTd = document.createElement('td');
        dtTd.textContent = new Date(act.dateTime).toLocaleString();

        const capTd = document.createElement('td');
        capTd.textContent = act.maxCapacity;

        const priceTd = document.createElement('td');
        priceTd.textContent = `$${act.price.toFixed(2)}`;

        // Monitor (con comprobación de null)
        const monTd = document.createElement('td');
        monTd.textContent = act.monitor && act.monitor.username
                          ? act.monitor.username
                          : '—';


        // Celda de acciones
        const actionsTd = document.createElement('td');

        const btnView = document.createElement('button');
        btnView.textContent = 'View';
        btnView.style.marginRight = '6px';
        btnView.addEventListener('click', () => viewActivity(act.id));
        actionsTd.appendChild(btnView);

        const btnEdit = document.createElement('button');
        btnEdit.textContent = 'Edit';
        btnEdit.style.marginRight = '6px';
        btnEdit.addEventListener('click', () => populateFormForEdit(act));
        actionsTd.appendChild(btnEdit);

        const btnDelete = document.createElement('button');
        btnDelete.textContent = 'Delete';
        btnDelete.addEventListener('click', () => deleteActivity(act.id));
        actionsTd.appendChild(btnDelete);

        // Añadir todas las celdas al <tr>
        [idTd, nameTd, descTd, dtTd, capTd, priceTd, monTd, actionsTd]
            .forEach(td => tr.appendChild(td));

        activitiesTableBody.appendChild(tr);
    });
}

/** Muestra el modal con la info completa de la actividad */
async function viewActivity(id) {
    let resp;
    try {
        resp = await fetch(`/api/activities/${id}`, {
            credentials: 'include'
        });
    } catch (err) {
        console.error('Error fetching activity:', err);
        return;
    }
    if (!resp.ok) {
        console.error('Activity not found. Status:', resp.status);
        return;
    }
    const act = await resp.json();

    // Construir contenido HTML del modal
    let html = `
      <h3>${act.name}</h3>
      <p>${act.description}</p>
      <p><strong>Date/Time:</strong> ${new Date(act.dateTime).toLocaleString()}</p>
      <p><strong>Capacity:</strong> ${act.maxCapacity}</p>
      <p><strong>Price:</strong> $${act.price.toFixed(2)}</p>
      <p><strong>Monitor:</strong> ${act.monitor.username}</p>
      <p><strong>Number of Sessions:</strong> ${act.sessions.length}</p>
      <hr/>
      <h4>Sessions:</h4>
      <ul>
    `;
    act.sessions.forEach(sess => {
        html += `<li>${new Date(sess.startTime).toLocaleString()} – 
                 ${new Date(sess.endTime).toLocaleString()} 
                 (Room: ${sess.room.name})</li>`;
    });
    html += `</ul>
      <h4>Attendees (${act.attendees.length}):</h4>
      <ul>
    `;
    act.attendees.forEach(u => {
        html += `<li>${u.username}</li>`;
    });
    html += `</ul>`;

    // Si tiene oferta vigente (suponemos que vienen campos discountPercentage y offerEndsAt)
    if (act.discountPercentage && act.offerEndsAt) {
        const now = new Date();
        const ends = new Date(act.offerEndsAt);
        if (ends > now) {
            html += `<p style="color:#e74c3c; font-weight:bold;">
                      <strong>Current Offer:</strong> ${act.discountPercentage}% off until 
                      ${ends.toLocaleDateString()} ${ends.toLocaleTimeString()}
                     </p>`;
        }
    }

    modalCont.innerHTML = html;
    modalBg.style.display = 'block';
    modalWin.style.display = 'block';
}

/** Cierra el modal de detalle */
function closeActivityModal() {
    modalWin.style.display = 'none';
    modalBg.style.display = 'none';
}

/** Carga datos de actividad en el formulario para editar */
function populateFormForEdit(act) {
    fieldId.value          = act.id;
    fieldName.value        = act.name;
    fieldDescription.value = act.description;

    // Convertir fecha/hora ISO a formato compatible con <input type="datetime-local">
    const dt = new Date(act.dateTime);
    const year  = dt.getFullYear();
    const month = String(dt.getMonth() + 1).padStart(2, '0');
    const day   = String(dt.getDate()).padStart(2, '0');
    const hours = String(dt.getHours()).padStart(2, '0');
    const mins  = String(dt.getMinutes()).padStart(2, '0');
    fieldDateTime.value = `${year}-${month}-${day}T${hours}:${mins}`;

    fieldMaxCapacity.value = act.maxCapacity;
    fieldPrice.value       = act.price;
    fieldMonitor.value     = act.monitor.username;

    // Si la actividad ya tiene sesiones, podemos preajustar el número de sesiones:
    if (act.sessions && act.sessions.length > 0) {
        sessionsCountInput.value = act.sessions.length;
        renderSessionInputs();
        act.sessions.forEach((sess, idx) => {
            const i = idx + 1;
            document.getElementById(`session${i}Start`).value =
              new Date(sess.startTime).toISOString().substring(0,16);
            document.getElementById(`session${i}End`).value =
              new Date(sess.endTime).toISOString().substring(0,16);
            document.getElementById(`session${i}Room`).value = sess.room.name;
        });
    } else {
        sessionsCountInput.value = 1;
        renderSessionInputs();
    }

    saveActivityBtn.textContent = 'Update Activity';
    fieldName.focus();
}

/** Elimina una actividad (DELETE /api/activities/{id}) */
async function deleteActivity(id) {
    if (!confirm('Are you sure you want to delete this activity?')) {
        return;
    }
    let resp;
    try {
       resp = await fetch(`/api/activities/admin/${id}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: { 'X-XSRF-TOKEN': getCsrfToken() }
            });
    } catch (err) {
        console.error('Error deleting activity:', err);
        return;
    }
    if (resp.status === 401 || resp.status === 403) {
        alert('You do not have permission.');
        return;
    }
    if (!resp.ok) {
        console.error('Delete failed. Status:', resp.status);
        return;
    }
    // Recargar tabla
    await loadActivities();
    // Limpiar formulario si estaba en modo edición
    clearForm();
}

/** Maneja el Submit del formulario (crear o actualizar) */
activityForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Leer valores del formulario
    const idValue       = fieldId.value;
    const nameValue     = fieldName.value.trim();
    const descValue     = fieldDescription.value.trim();
    const dtValue       = fieldDateTime.value;
    const capValue      = parseInt(fieldMaxCapacity.value, 10);
    const priceValue    = parseFloat(fieldPrice.value);
    const monitorUser   = fieldMonitor.value;
    const sessionCount  = parseInt(sessionsCountInput.value, 10);

    // Construir lista de sesiones
    const sessionsPayload = [];
    const count = parseInt(sessionsCountInput.value, 10);
    for (let i = 1; i <= count; i++) {
        const start = document.getElementById(`session${i}Start`).value;
        const end   = document.getElementById(`session${i}End`).value;
        const room  = document.getElementById(`session${i}Room`).value;
        sessionsPayload.push({ startTime: start, endTime: end, roomName: room });
    }


    // Construir payload completo
    const payload = {
        name:        nameValue,
        description: descValue,
        dateTime:    dtValue,
        maxCapacity: capValue,
        price:       priceValue,
        monitorUsername: monitorUser,
        sessions:    sessionsPayload  // backend debe aceptar array de sesiones
    };

    let resp;
    try {
        if (idValue) {
            resp = await fetch(`/api/activities/${idValue}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(payload)
            });
        } else {
            // Modo creación → POST /api/activities
            resp = await fetch('/api/activities', {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(payload)
            });
        }
    } catch (err) {
        console.error('Error saving activity:', err);
        return;
    }

    if (resp.status === 401 || resp.status === 403) {
        alert('You do not have permission.');
        return;
    }
    if (!resp.ok) {
        const txt = await resp.text();
        alert(`Error: ${txt}`);
        return;
    }

    // Éxito: recargar tabla y limpiar formulario
    await loadActivities();
    clearForm();
});

/** Limpia el formulario y deja en modo “Create Activity” */
function clearForm() {
    fieldId.value = '';
    fieldName.value = '';
    fieldDescription.value = '';
    fieldDateTime.value = '';
    fieldMaxCapacity.value = '';
    fieldPrice.value = '';
    fieldMonitor.value = '';
    sessionsCountInput.value = 1;
    renderSessionInputs();
    saveActivityBtn.textContent = 'Create Activity';
}

// Al cargar, ejecutar loadMonitorsAndRooms(), renderSessionInputs() y loadActivities()
window.addEventListener('DOMContentLoaded', async () => {
    await loadMonitorsAndRooms();
    renderSessionInputs();
    await loadActivities();
});


// ======= SECCIÓN Offers =======
const offerForm      = document.getElementById('offerForm');
const offerActivity  = document.getElementById('offerActivity');
const offerDiscount  = document.getElementById('offerDiscount');
const offerEndsAt    = document.getElementById('offerEndsAt');

async function loadOfferableActivities() {
    // Trae todas las actividades y llena el dropdown
    try {
        const resp = await fetch('/api/activities', {
            credentials: 'include'
        });
        if (!resp.ok) return;
        const activities = await resp.json();
        offerActivity.innerHTML = '<option value="">-- Select Activity --</option>';
        activities.forEach(act => {
            // Solo actividades activas
            if (!act.active) return;
            const opt = document.createElement('option');
            opt.value = act.id;
            opt.textContent = `${act.name} (ID: ${act.id})`;
            offerActivity.appendChild(opt);
        });
    } catch (err) {
        console.error('Error loading activities for offers:', err);
    }
}

// Manejar el envío del formulario de oferta
offerForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const actId       = offerActivity.value;
  const discountVal = parseInt(offerDiscount.value, 10);
  const endsAtVal   = offerEndsAt.value; // “YYYY-MM-DDTHH:mm”

  if (!actId || !discountVal || !endsAtVal) {
    alert('Please fill all fields.');
    return;
  }

  let resp;
  try {
    resp = await fetch(`/api/activities/admin/${actId}/offer`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': getCsrfToken()
        },
        body: JSON.stringify({
            discountPercentage: discountVal,
            offerEndsAt: endsAtVal
        })
    });
  } catch (err) {
    console.error('Error applying offer:', err);
    alert('Error applying offer.');
    return;
  }

  if (resp.status === 401 || resp.status === 403) {
    alert('You do not have permission.');
    return;
  }
  if (!resp.ok) {
    const txt = await resp.text();
    alert(`Error: ${txt}`);
    return;
  }

  alert('Offer applied successfully!');
  offerActivity.value  = '';
  offerDiscount.value  = '';
  offerEndsAt.value    = '';
});


// Al cargar la página, invocar loadOfferableActivities()
window.addEventListener('DOMContentLoaded', loadOfferableActivities);


// ======= SECCIÓN DE INFORMES =======
/** Carga el ingreso total (GET /api/reports/total) */
async function loadTotalRevenue() {
    let resp;
    try {
        resp = await fetch('/api/reports/total', {
            credentials: 'include'
            });
    } catch (err) {
        console.error('Error fetching total revenue:', err);
        document.getElementById('totalRevenueDisplay').textContent = 'Error';
        return;
    }
    if (!resp.ok) {
        document.getElementById('totalRevenueDisplay').textContent = 'Error';
        return;
    }
    const total = await resp.json();
    document.getElementById('totalRevenueDisplay').textContent = `$ ${total.toFixed(2)}`;
}

/** Carga los ingresos mensuales (GET /api/reports/monthly) y pinta la tabla */
async function loadMonthlyRevenue() {
    let resp;
    try {
        resp = await fetch('/api/reports/monthly',
            {credentials: 'include'

            });
    } catch (err) {
        console.error('Error fetching monthly revenue:', err);
        return;
    }
    if (!resp.ok) {
        return;
    }
    const data = await resp.json(); // { "2025-06": 150.0, ... }

    const tbody = document.querySelector('#monthlyRevenueTable tbody');
    tbody.innerHTML = '';
    Object.entries(data).forEach(([month, val]) => {
        const tr = document.createElement('tr');
        const tdMonth = document.createElement('td');
        tdMonth.textContent = month;
        const tdVal = document.createElement('td');
        tdVal.textContent = `$ ${val.toFixed(2)}`;
        tr.appendChild(tdMonth);
        tr.appendChild(tdVal);
        tbody.appendChild(tr);
    });
}

/** Carga los ingresos por actividad (GET /api/reports/by-activity) y pinta la tabla */
async function loadActivityRevenue() {
    let resp;
    try {
        resp = await fetch('/api/reports/by-activity', {credentials: 'include'});
    } catch (err) {
        console.error('Error fetching activity revenue:', err);
        return;
    }
    if (!resp.ok) {
        return;
    }
    const data = await resp.json(); // { "Yoga": 80.0, ... }

    const tbody = document.querySelector('#activityRevenueTable tbody');
    tbody.innerHTML = '';
    Object.entries(data).forEach(([activityName, val]) => {
        const tr = document.createElement('tr');
        const tdAct = document.createElement('td');
        tdAct.textContent = activityName;
        const tdVal = document.createElement('td');
        tdVal.textContent = `$ ${val.toFixed(2)}`;
        tr.appendChild(tdAct);
        tr.appendChild(tdVal);
        tbody.appendChild(tr);
    });
}

// Al cargar la página, invocar las funciones de informe
window.addEventListener('DOMContentLoaded', () => {
    loadTotalRevenue();
    loadMonthlyRevenue();
    loadActivityRevenue();
});
