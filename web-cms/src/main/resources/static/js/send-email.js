const emailInput = document.getElementById('emailTo');
const suggestionsBox = document.getElementById('emailSuggestions');
const sendButton = document.getElementById('sendEmailBtn');

emailInput.addEventListener('input', async () => {
    const query = emailInput.value.split(',').pop().trim();
    if (query.length > 0) {
        await fetchEmailSuggestions(query);
    } else {
        suggestionsBox.style.display = 'none';
    }
});

emailInput.addEventListener('blur', () => {
    setTimeout(() => suggestionsBox.style.display = 'none', 150);
});

sendButton.addEventListener('click', sendEmail);

async function sendEmail() {
    const toInput = emailInput.value.trim();
    const subject = document.getElementById('emailSubject').value.trim();
    const message = document.getElementById('emailMessage').value.trim();

    const to = toInput.split(',')
        .map(email => email.trim())
        .filter(email => email !== '');

    if (to.length === 0) {
        showErrorDialog("Please enter at least one recipient email.");
        return;
    }

    try {
        await callApi('/v1/api/email/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ to, subject, message })
        });

        showSuccessDialog("Email sent successfully!");
        emailInput.value = '';
        document.getElementById('emailSubject').value = '';
        document.getElementById('emailMessage').value = '';
    } catch (e) {
        showErrorDialog("Failed to send email: " + (e.message || e));
    }
}

async function fetchEmailSuggestions(query) {
    try {
        const response = await callApi(`/v1/api/users/list?gmail=${encodeURIComponent(query)}`, {
            method: 'GET'
        });

        const users = response.data.data;
        renderEmailSuggestions(users);
    } catch (e) {
        console.error('Failed to fetch email suggestions:', e);
        suggestionsBox.style.display = 'none';
    }
}

// 🧾 Render dropdown
function renderEmailSuggestions(users) {
    suggestionsBox.innerHTML = '';

    if (!users || users.length === 0) {
        suggestionsBox.style.display = 'none';
        return;
    }

    users.forEach(user => {
        const email = user.email
        const div = document.createElement('div');
        div.textContent = email;
        div.onclick = () => {
            let emails = emailInput.value.split(',').map(e => e.trim());
            emails[emails.length - 1] = email;
            emailInput.value = emails.join(', ');
            suggestionsBox.style.display = 'none';
        };
        suggestionsBox.appendChild(div);
    });

    suggestionsBox.style.display = 'block';
}
