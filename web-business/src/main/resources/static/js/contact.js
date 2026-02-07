async function loadContactInfo() {
    await callApi("/v1/api/contact-info", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (response) => {
        if (response && response.data) {
            document.querySelector('.contact-text p').textContent = response.data.text;
            document.querySelector('.contact-address p').textContent = response.data.address;
            document.querySelector('.contact-phone p').textContent = response.data.phoneNumber;
            document.querySelector('.contact-email p').textContent = response.data.email;
            document.querySelector('.newsletter-form').style.backgroundImage = `url('${response.data.imageUrl}')`;
        }
    });
}

document.addEventListener("DOMContentLoaded", loadContactInfo);
