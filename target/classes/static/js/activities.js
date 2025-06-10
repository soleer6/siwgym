// src/main/resources/static/js/activities.js

// 1) Recuperamos del localStorage la información de sesión
const userId   = localStorage.getItem('userId');
const username = localStorage.getItem('username');
const role     = localStorage.getItem('role');

// 2) Si no hay userId almacenado, redirigimos al login
if (!userId) {
    window.location.href = 'login.html';
}

// 3) Colocamos el usuario y rol en pantalla
document.getElementById('usernameDisplay').textContent = username;
document.getElementById('roleDisplay').textContent   = role;

// 4) Logout: limpios localStorage y volvemos a login
document.getElementById('logout').addEventListener('click', () => {
    localStorage.clear();
    window.location.href = 'login.html';
});

/**
 * Función principal que obtiene la lista de actividades y dibuja
 * la tabla (o un mensaje de “sin permiso”) según el rol.
 */
async function loadActivities() {
    let response;
    try {
        response = await fetch('/api/activities', {credentials : 'include'});
    } catch (err) {
        console.error('Error al llamar a /api/activities:', err);
        return;
    }

    // Si el servidor responde 401/403, redirigimos al login
    if (response.status === 401 || response.status === 403) {
        window.location.href = 'login.html';
        return;
    }

    const activities = await response.json();
    const contentDiv = document.getElementById('content');
    contentDiv.innerHTML = ''; // Limpiamos contenido previo

    // Si el rol es CLIENT, mostramos la tabla de actividades
    if (role === 'CLIENT') {
        // Creamos la tabla
        const table = document.createElement('table');
        table.classList.add('activities-table');

        // Encabezado de la tabla
        const header = document.createElement('tr');
        ['ID', 'Name', 'Description', 'Date/Time', 'Enrolled', 'Price', 'Actions'].forEach(txt => {
            const th = document.createElement('th');
            th.textContent = txt;
            header.appendChild(th);
        });
        table.appendChild(header);

        // Filas: recorremos cada actividad
        activities.forEach(act => {
            if (!act.active) return; // solo mostramos las activas

            const row = document.createElement('tr');

            // Datos básicos de la actividad
            const idCell       = document.createElement('td');
            idCell.textContent = act.id;

            const nameCell     = document.createElement('td');
            nameCell.textContent = act.name;

            const descCell     = document.createElement('td');
            descCell.textContent = act.description;

            const dateCell     = document.createElement('td');
            dateCell.textContent = new Date(act.dateTime).toLocaleString();

            const enrolledCount = act.attendees.length;
            const enrolledCell  = document.createElement('td');
            enrolledCell.textContent = `${enrolledCount}/${act.maxCapacity}`;

            const priceCell    = document.createElement('td');
            priceCell.textContent = act.price;

            row.append(idCell, nameCell, descCell, dateCell, enrolledCell, priceCell);

            // Celda de acciones (View Info + Sign Up / Full)
            const actionsTd = document.createElement('td');

            // Botón "View Info"
            const viewBtn = document.createElement('button');
            viewBtn.textContent = 'View Info';
            viewBtn.addEventListener('click', () => viewInfo(act.id));
            actionsTd.appendChild(viewBtn);

            // Botón "Sign Up" o "Full"
            const signBtn = document.createElement('button');
            // Si la actividad ya está llena, deshabilitamos y cambiamos texto a "Full"
            if (enrolledCount >= act.maxCapacity) {
                signBtn.textContent = 'Full';
                signBtn.disabled = true;
            } else {
                signBtn.textContent = 'Sign Up';
                signBtn.disabled = false;
                signBtn.style.marginLeft = '8px';
                signBtn.addEventListener('click', () => signUp(act.id));
            }
            actionsTd.appendChild(signBtn);

            row.appendChild(actionsTd);
            table.appendChild(row);
        });

        contentDiv.appendChild(table);
    }
    // Si NO es CLIENT, mostramos mensaje de "sin permiso"
    else {
        const msg = document.createElement('p');
        msg.classList.add('no-permission');
        msg.textContent = 'You do not have permission to view this page.';
        contentDiv.appendChild(msg);
    }
}

/**
 * viewInfo(id)
 * Abre el modal mostrando todos los datos completos de la actividad con ID = id
 */
async function viewInfo(id) {
    let resp;
    try {
        resp = await fetch('/api/activities/' + id, {credentials: 'include'});
    } catch (err) {
        console.error('Error fetching actividad con id ' + id + ':', err);
        return;
    }
    if (!resp.ok) {
        console.error('Actividad no encontrada. Status:', resp.status);
        return;
    }

    const act = await resp.json();

    // Referencias a elementos del DOM del modal
    const modalBg = document.getElementById('infoModalBackground');
    const modal   = document.getElementById('infoModal');
    const content = document.getElementById('modalContent');

    // Construimos el HTML interno del modal
    let html = `
        <h3>${act.name}</h3>
        <p>${act.description}</p>
        <p><strong>Date/Time:</strong> ${new Date(act.dateTime).toLocaleString()}</p>
        <p><strong>Price:</strong> ${act.price}</p>
        <p><strong>Enrolled:</strong> ${act.attendees.length}/${act.maxCapacity}</p>
        <p><strong>Monitor:</strong> ${act.monitor.username}</p>
        <p><strong>Number of Sessions:</strong> ${act.sessions.length}</p>
        <hr/>
        <h4>Sessions:</h4>
        <ul>
    `;
    // Listado de cada sesión
    act.sessions.forEach(sess => {
        html += `
            <li>
                ${new Date(sess.startTime).toLocaleString()} – 
                ${new Date(sess.endTime).toLocaleString()} (Room: ${sess.room.name})
            </li>
        `;
    });
    html += `</ul>
        <h4>Attendees (${act.attendees.length}):</h4>
        <ul>
    `;
    // Listado de cada usuario inscrito
    act.attendees.forEach(u => {
        html += `<li>${u.username}</li>`;
    });
    html += `</ul>
        <div style="margin-top: 12px;">
    `;

    // Si está lleno, no ponemos botón; si no, permitimos “Sign Up”
    if (act.attendees.length < act.maxCapacity) {
        html += `<button class="sign-up-btn" id="modalSignUpBtn">Sign Up</button>`;
    } else {
        html += `<em>This activity is already full.</em>`;
    }
    html += `</div>`;

    content.innerHTML = html;

    // Asignamos comportamiento al botón "Sign Up" dentro del modal (si existe)
    const modalSignBtn = document.getElementById('modalSignUpBtn');
    if (modalSignBtn) {
        modalSignBtn.onclick = async () => {
            await signUp(id);
            // Tras apuntarse, recargamos la tabla y cerramos el modal
            await loadActivities();
            closeModal();
        };
    }

    // Mostramos el fondo y el modal
    modalBg.style.display = 'block';
    modal.style.display   = 'block';
}

/** Cierra el modal */
function closeModal() {
    document.getElementById('infoModal').style.display = 'none';
    document.getElementById('infoModalBackground').style.display = 'none';
}

/**
 * signUp(id)
 * Hace POST a /api/activities/{id}/signup?userId={userId}
 * para inscribir al usuario en la actividad. Si funciona, muestra alerta.
 */
async function signUp(id) {
    let resp;
    try {
        resp = await fetch(`/api/activities/${id}/signup?userId=${userId}`, {
            credentials: 'include',
            method: 'POST'
        });
    } catch (err) {
        console.error('Error al hacer signup:', err);
        alert('Error signing up.');
        return;
    }
    
    if (resp.ok) {
        alert('Successfully signed up!');
    } else {
        // Si la respuesta no es 200, obtenemos el texto de error (si lo hay)
        let msg = 'Error signing up.';
        try {
            const txt = await resp.text();
            if (txt) msg = txt;
        } catch (_) { /* no hacemos nada */ }

        alert(msg);
    }
}

// Al cargar el script, invocamos loadActivities() para que pinte todo
loadActivities();
