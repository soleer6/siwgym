// src/main/resources/static/js/monitor.js

// --------------- Datos de usuario en localStorage ---------------
const userId = localStorage.getItem('userId');
const username = localStorage.getItem('username');
const role = localStorage.getItem('role');

// Si no hay sesión, volvemos a login
if (!userId) {
  window.location.href = 'login.html';
}

// Mostramos en pantalla
document.getElementById('usernameDisplay').textContent = username;
document.getElementById('roleDisplay').textContent = role;

// Botones de Top Bar
document.getElementById('logoutBtn').addEventListener('click', () => {
  localStorage.clear();
  window.location.href = 'login.html';
});

// Al pulsar “View Notifications (N)”
document.getElementById('notifBtn').addEventListener('click', () => {
  window.location.href = 'notifications.html';
});

// Elementos del DOM donde inyectaremos contenido
const activitiesTbody = document.querySelector('#activitiesTable tbody');
const noActivitiesMsg = document.getElementById('noActivitiesMsg');
const sessionsContainer = document.getElementById('sessionsContainer');
const noSessionsMsg = document.getElementById('noSessionsMsg');
const notifCountSpan = document.getElementById('notifCount');

// Función principal para inicializar la pantalla
async function initMonitorPanel() {
  try {
    // 1) Cargar actividades y filtrar por monitor
    const activitiesResp = await fetch('/api/activities', {credentials : 'include'});
    if (!activitiesResp.ok) {
      console.error('Error loading activities:', activitiesResp.status);
      return;
    }
    const allActivities = await activitiesResp.json();
    // Filtramos aquellas donde el monitor coincide con nuestro username
    const myActivities = allActivities.filter(act => 
      act.monitor && act.monitor.username === username
    );
    renderActivitiesTable(myActivities);
    document.getElementById('totalActivities').textContent = myActivities.length;


    // 2) Cargar sesiones (mini-calendar)
    // Podemos reusar las mismas actividades, pues cada una trae su array “sessions”
    const allSessions = [];
    myActivities.forEach(act => {
      if (Array.isArray(act.sessions)) {
        act.sessions.forEach(sess => {
            // Le añadimos el nombre de la actividad y sala para usar luego
            allSessions.push({
                activityName: act.name,
                roomName: sess.room ? sess.room.name : 'N/A',
                startTime: sess.startTime,
                endTime: sess.endTime
            });
        const now = new Date();
            // ...
            if (new Date(sess.startTime) < now) {
                card.style.opacity = '0.6';
                // o card.classList.add('past-session');
            }
        });
      }
    });
    renderSessionsCalendar(allSessions);

    // 3) Cargar contador de notificaciones sin leer
    await fetchNotificationCount();

  } catch (err) {
    console.error('Init monitor panel error:', err);
  }
}

/**
 *  Renderiza la tabla de Assigned Activities
 *  @param {Array} activities → lista de actividades donde el monitor es this.user
 */
function renderActivitiesTable(activities) {
  activitiesTbody.innerHTML = '';  // Limpiamos
  if (activities.length === 0) {
    noActivitiesMsg.style.display = 'block';
    return;
  }
  noActivitiesMsg.style.display = 'none';

  activities.forEach(act => {
    const tr = document.createElement('tr');

    // ID
    const tdId = document.createElement('td');
    tdId.textContent = act.id;
    tr.appendChild(tdId);

    // Name
    const tdName = document.createElement('td');
    tdName.textContent = act.name;
    tr.appendChild(tdName);

    // Description
    const tdDesc = document.createElement('td');
    tdDesc.textContent = act.description;
    tr.appendChild(tdDesc);

    // Date/Time (solo la fecha base de la actividad)
    const tdDate = document.createElement('td');
    tdDate.textContent = new Date(act.dateTime).toLocaleString();
    tr.appendChild(tdDate);

    // Price
    const tdPrice = document.createElement('td');
    tdPrice.textContent = `$${act.price}`;
    tr.appendChild(tdPrice);

    // Capacity actual / max
    const tdCap = document.createElement('td');
    const currentCount = Array.isArray(act.attendees) ? act.attendees.length : 0;
    tdCap.textContent = `${currentCount} / ${act.maxCapacity}`;
    tr.appendChild(tdCap);

    // Cantidad de Attendees
    const tdAtt = document.createElement('td');
    tdAtt.textContent = currentCount;
    tr.appendChild(tdAtt);

    activitiesTbody.appendChild(tr);
  });
}

/**
 *  Renderiza el “mini-calendario” de sesiones
 *  Recibe un array de objetos:
 *    { activityName, roomName, startTime, endTime }
 */
function renderSessionsCalendar(sessions) {
  sessionsContainer.innerHTML = '';

  if (sessions.length === 0) {
    noSessionsMsg.style.display = 'block';
    return;
  }
  noSessionsMsg.style.display = 'none';

  // Ordenamos cronológicamente
  sessions.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

  sessions.forEach(sess => {
    const card = document.createElement('div');
    card.classList.add('session-card');

    // Título: nombre de la actividad
    const h3 = document.createElement('h3');
    h3.textContent = sess.activityName;
    card.appendChild(h3);

    // Fecha (solo la parte de día/mes/año)
    const dateP = document.createElement('p');
    dateP.textContent = `Date: ${new Date(sess.startTime).toLocaleDateString()}`;
    card.appendChild(dateP);

    // Hora de inicio – fin
    const timeP = document.createElement('p');
    timeP.textContent = `Time: ${new Date(sess.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} ‒ ${new Date(sess.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
    card.appendChild(timeP);

    // Sala
    const roomP = document.createElement('p');
    roomP.textContent = `Room: ${sess.roomName}`;
    card.appendChild(roomP);

    sessionsContainer.appendChild(card);
  });
}

/**
 *  Carga el número de notificaciones sin leer y actualiza el botón
 */
async function fetchNotificationCount() {
  try {
    const resp = await fetch(`/api/notifications/${userId}`);
    if (!resp.ok) {
      console.error('Error fetching notifications:', resp.status);
      return;
    }
    const notifs = await resp.json();
    const unreadCount = notifs.filter(n => !n.read).length;
    notifCountSpan.textContent = unreadCount;
  } catch (err) {
    console.error('Error fetching notif count:', err);
  }
}

// Iniciamos la carga de datos al abrir la página
initMonitorPanel();
