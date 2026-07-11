document.addEventListener('DOMContentLoaded', function () {
    setupAutoDismissToasts();
    setupLogoutModal();
    setupDeleteConfirmModal();
    setupAvatarValidation();
});

function setupAutoDismissToasts() {
    window.setTimeout(function () {
        document.querySelectorAll('.toast').forEach(function (toast) {
            toast.classList.add('toast-hide');
            window.setTimeout(function () { toast.remove(); }, 350);
        });
    }, 5500);
}

function setupLogoutModal() {
    const modal = document.getElementById('logoutModal');
    if (!modal) {
        return;
    }

    const openers = document.querySelectorAll('[data-logout-open]');
    const cancelButton = modal.querySelector('[data-logout-cancel]');

    function openModal(event) {
        event.preventDefault();
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
        if (cancelButton) {
            cancelButton.focus();
        }
    }

    function closeModal() {
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
    }

    openers.forEach(function (opener) {
        opener.addEventListener('click', openModal);
    });

    if (cancelButton) {
        cancelButton.addEventListener('click', closeModal);
    }

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modal.classList.contains('is-open')) {
            closeModal();
        }
    });
}

function setupDeleteConfirmModal() {
    const modal = document.getElementById('deleteConfirmModal');
    if (!modal) {
        return;
    }

    const confirmButton = modal.querySelector('[data-delete-confirm]');
    const cancelButton = modal.querySelector('[data-delete-cancel]');
    let pendingForm = null;

    function openModal(form) {
        pendingForm = form;
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
        if (confirmButton) {
            confirmButton.focus();
        }
    }

    function closeModal() {
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
        pendingForm = null;
    }

    // Intercetta i form con data-delete-confirm
    document.querySelectorAll('[data-delete-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            openModal(form);
        });
    });

    if (confirmButton) {
        confirmButton.addEventListener('click', function () {
            if (pendingForm) {
                pendingForm.submit();
            }
            closeModal();
        });
    }

    if (cancelButton) {
        cancelButton.addEventListener('click', closeModal);
    }

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modal.classList.contains('is-open')) {
            closeModal();
        }
    });
}

function setupAvatarValidation() {
    const avatarInput = document.getElementById('avatar');
    const avatarForm = document.querySelector('.avatar-upload-form');
    const maxAvatarSize = 2 * 1024 * 1024;

    if (!avatarInput || !avatarForm) {
        return;
    }

    function validateAvatar() {
        const file = avatarInput.files && avatarInput.files[0];
        if (!file) {
            return true;
        }

        if (file.size > maxAvatarSize) {
            avatarInput.value = '';
            showToast('Dimensione immagine eccessiva. Max 2 MB.', true);
            return false;
        }

        return true;
    }

    avatarInput.addEventListener('change', validateAvatar);

    avatarForm.addEventListener('submit', function (event) {
        if (!validateAvatar()) {
            event.preventDefault();
        }
    });
}

function showToast(message, isError) {
    const toast = document.createElement('div');
    toast.className = 'toast ' + (isError ? 'toast-error' : 'toast-success');
    toast.textContent = message;
    document.body.appendChild(toast);

    window.setTimeout(function () {
        toast.classList.add('toast-hide');
        window.setTimeout(function () { toast.remove(); }, 350);
    }, 5500);
}
