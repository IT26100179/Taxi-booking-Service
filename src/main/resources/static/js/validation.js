document.addEventListener('DOMContentLoaded', () => {
  // 1. Interactive Eye Toggle Buttons on every password input
  document.querySelectorAll('.toggle-password-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const group = btn.closest('.input-group');
      if (!group) return;
      const input = group.querySelector('input');
      if (!input) return;
      
      const isPassword = input.type === 'password';
      input.type = isPassword ? 'text' : 'password';
      
      const icon = btn.querySelector('i');
      if (icon) {
        if (isPassword) {
          icon.classList.remove('bi-eye');
          icon.classList.add('bi-eye-slash');
        } else {
          icon.classList.remove('bi-eye-slash');
          icon.classList.add('bi-eye');
        }
      }
    });
  });

  // 2. Show / Hide password checkbox toggle (if present)
  const showPw = document.getElementById('showPw');
  if (showPw) {
    showPw.addEventListener('change', () => {
      const pwInput = document.getElementById('password');
      const confirmInput = document.getElementById('confirmPassword');
      const isChecked = showPw.checked;
      const type = isChecked ? 'text' : 'password';
      
      if (pwInput) pwInput.type = type;
      if (confirmInput) confirmInput.type = type;

      // Also update eye icons on the toggle buttons
      document.querySelectorAll('.toggle-password-btn i').forEach(icon => {
        if (isChecked) {
          icon.classList.remove('bi-eye');
          icon.classList.add('bi-eye-slash');
        } else {
          icon.classList.remove('bi-eye-slash');
          icon.classList.add('bi-eye');
        }
      });
    });
  }

  // 3. Form validation enhancement
  const forms = document.querySelectorAll('form.needs-validation');
  forms.forEach(form => {
    form.addEventListener('submit', event => {
      const phoneInput = form.querySelector('input[name="phone"]');
      const pwInput = form.querySelector('input[name="password"]');
      const confirmInput = form.querySelector('input[name="confirmPassword"]');

      let valid = true;

      if (phoneInput) {
        const phoneRegex = /^0[0-9]{9}$/;
        if (!phoneRegex.test(phoneInput.value.trim())) {
          phoneInput.setCustomValidity('Phone number must be exactly 10 digits starting with 0 (e.g. 0771234567)');
          phoneInput.reportValidity();
          valid = false;
        } else {
          phoneInput.setCustomValidity('');
        }
      }

      if (valid && pwInput) {
        if (pwInput.value.length < 8) {
          pwInput.setCustomValidity('Password must be at least 8 characters long');
          pwInput.reportValidity();
          valid = false;
        } else {
          pwInput.setCustomValidity('');
        }
      }

      if (valid && confirmInput && pwInput) {
        if (pwInput.value !== confirmInput.value) {
          confirmInput.setCustomValidity('Passwords do not match');
          confirmInput.reportValidity();
          valid = false;
        } else {
          confirmInput.setCustomValidity('');
        }
      }

      if (!valid) {
        event.preventDefault();
        event.stopPropagation();
      }
    });
  });
});
