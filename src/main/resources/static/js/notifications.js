// src/main/resources/static/js/notifications.js

// Recuperamos los datos básicos del usuario
const userId = localStorage.getItem('userId');
const username = localStorage.getItem('username');
const role = localStorage.getItem('role');

// Si no hay userId, volvemos al login
if (!userId) {
    window.location.href = 'login.html';
}

// Mostramos username y role en el top-bar
document.getElementById('usernameDisplay').textContent = username;
document.getElementById('roleDisplay').textContent = role;

// Botón “Back to Activities”
backBtn.addEventListener('click', () => {
  if (role === 'CLIENT') {
    window.location.href = 'activities.html';
  } else if (role === 'MONITOR') {
    window.location.href = 'monitor.html';
  } else if (role === 'ADMIN') {
    window.location.href = 'users.html';
  } else {
    window.location.href = 'login.html';
  }
});

// Botón “Logout”
document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.clear();
    window.location.href = 'login.html';
});

// Elementos del DOM para listas y mensajes “no hay nada”
const unreadListDiv = document.getElementById('unreadList');
const readListDiv = document.getElementById('readList');
const noUnreadPara = document.getElementById('noUnread');
const noReadPara = document.getElementById('noRead');

// Función principal que carga y dibuja las notificaciones
async function loadNotifications() {
    let resp;
    try {
        resp = await fetch(`/api/notifications/${userId}`);
    } catch (err) {
        console.error('Error fetching /api/notifications:', err);
        return;
    }

    if (!resp.ok) {
        console.error('Error obteniendo notificaciones, status:', resp.status);
        return;
    }

    const notifications = await resp.json();

    // Limpiamos ambas listas
    unreadListDiv.innerHTML = '';
    readListDiv.innerHTML = '';

    // Separamos por isRead
    const unread = notifications.filter(n => !n.read);
    const readed = notifications.filter(n => n.read);

    // Si no hay ninguna sin leer, mostramos el párrafo “You have no unread…”
    if (unread.length === 0) {
        noUnreadPara.style.display = 'block';
    } else {
        noUnreadPara.style.display = 'none';
        unread.forEach(n => {
            const notifDiv = createNotificationDiv(n, false);
            unreadListDiv.appendChild(notifDiv);
        });
    }

    // Si no hay ninguna leída, mostramos el párrafo “You have no read…”
    if (readed.length === 0) {
        noReadPara.style.display = 'block';
    } else {
        noReadPara.style.display = 'none';
        readed.forEach(n => {
            const notifDiv = createNotificationDiv(n, true);
            readListDiv.appendChild(notifDiv);
        });
    }
}

// Crea el `<div class="notification">` para cada notificación
function createNotificationDiv(notif, alreadyRead) {
    // contenedor principal
    const div = document.createElement('div');
    div.classList.add('notification');
    div.classList.add(alreadyRead ? 'read' : 'unread');

    // texto y timestamp
    const textContainer = document.createElement('div');
    textContainer.classList.add('notif-text');

    const descP = document.createElement('p');
    descP.textContent = notif.description;
    textContainer.appendChild(descP);

    const metaP = document.createElement('p');
    metaP.classList.add('notif-meta');
    // Mostrar fecha/hora de forma local (adjust a tu zona horaria si hace falta)
    metaP.textContent = new Date(notif.timestamp).toLocaleString();
    textContainer.appendChild(metaP);

    div.appendChild(textContainer);

    // Si aún no está leído, añadimos un botón “Mark as Read”
    if (!alreadyRead) {
        const markBtn = document.createElement('button');
        markBtn.classList.add('mark-button');
        markBtn.textContent = 'Mark as Read';
        markBtn.onclick = () => markAsRead(notif.id, div);
        div.appendChild(markBtn);
    }

    return div;
}

// Cuando el usuario cliquea “Mark as Read”:
async function markAsRead(notifId, notifDiv) {
    let resp;
    try {
        resp = await fetch(`/api/notifications/${userId}/mark-read/${notifId}`, {
            method: 'POST'
        });
    } catch (err) {
        console.error('Error al marcar como leído:', err);
        alert('Error marking as read.');
        return;
    }

    if (!resp.ok) {
        console.error('Mark-read respondió con status:', resp.status);
        alert('Error marking as read.');
        return;
    }

    // Si todo OK, movemos la notificación de “unreadListDiv” a “readListDiv”
    // 1) Cambiamos clases y quitamos el botón
    notifDiv.classList.remove('unread');
    notifDiv.classList.add('read');
    // Quitamos el botón “Mark as Read”
    const btn = notifDiv.querySelector('.mark-button');
    if (btn) { btn.remove(); }

    // 2) Lo removemos de la sección unread
    unreadListDiv.removeChild(notifDiv);

    // 3) Si ahora “unreadListDiv” está vacío, mostramos el mensaje “noUnreadPara”
    if (unreadListDiv.children.length === 0) {
        noUnreadPara.style.display = 'block';
    }

    // 4) Lo añadimos a la sección de leídas
    readListDiv.appendChild(notifDiv);

    // 5) Si había mensaje “noReadPara”, lo ocultamos
    noReadPara.style.display = 'none';
}

// Al cargar la página, ejecutamos loadNotifications()
loadNotifications();
