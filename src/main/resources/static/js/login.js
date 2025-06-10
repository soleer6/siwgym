// src/main/resources/static/js/login.js

document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  // Ocultamos mensaje de error por defecto
  const errorMsg = document.getElementById('errorMsg');
  errorMsg.style.display = 'none';

  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      credentials: 'include',  
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!response.ok) {
      // Mostrar mensaje de error
      errorMsg.style.display = 'block';
      return;
    }

    // Si todo ok, guardamos en localStorage
    const user = await response.json();
    localStorage.setItem('userId', user.id);
    localStorage.setItem('username', user.username);
    localStorage.setItem('role', user.role);

    // Redirigir según rol
    if (user.role === 'CLIENT') {
      window.location.href = 'activities.html';
    } else if (user.role === 'MONITOR') {
      window.location.href = 'monitor.html';
    } else if (user.role === 'ADMIN') {
      window.location.href = 'admin.html';
    } else {
      // Por defecto, al login
      window.location.href = 'login.html';
    }
  } catch (err) {
    console.error('Login error:', err);
    errorMsg.textContent = 'Network error. Please try again.';
    errorMsg.style.display = 'block';
  }
});
