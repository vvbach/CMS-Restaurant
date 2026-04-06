/**
 * Gọi API với xử lý lỗi tập trung, lấy message từ API và show toast
 */
async function callApi(url, options = {}, onSuccess) {
    const fullUrl = domain + url;

    // make shallow copies
    options = {...options};
    options.headers = {...(options.headers || {})};

    try {
        const response = await fetch(fullUrl, options);

        let data;
        try {
            data = await response.json();
        } catch {
            data = null;
        }

        if (response.ok) {
            if (typeof onSuccess === 'function') onSuccess(data);
            return data;
        }

        const apiMessage = (data && (data.message || (data.data && data.data.message) || data.error))
            || 'Lỗi không xác định từ server.';

        showErrorDialog(`${response.status} - ${apiMessage}`);
        console.error("API error body:", data);
    } catch (error) {
        // network errors, unexpected exceptions
        showErrorDialog(error.message || 'Network or unexpected error');
        throw error;
    }
}

function showErrorDialog(message) {
    const dialog = document.getElementById('errorDialog');
    dialog.innerText = message;
    dialog.style.display = 'block';

    // Ẩn sau 5 giây
    setTimeout(() => {
        dialog.style.display = 'none';
    }, 5000);
}

function showSuccessDialog(message) {
    const dialog = document.getElementById('successDialog');
    dialog.innerText = message;
    dialog.style.display = 'block';

    setTimeout(() => {
        dialog.style.display = 'none';
    }, 4000); // auto ẩn sau 4 giây
}

function createListPage() {
    document.getElementById('pagination').innerHTML = `
              <li class="page-item disabled" id="btn-pag-prev"><a class="page-link" onclick="setPage('-')">Previous</a></li>
            `;

    for (let i = 1; i <= maxPage; i++) {
        const li = document.createElement('li');
        li.className = 'page-item';
        if (i === currentPage) {
            li.innerHTML = `<a class="page-link selected" data-page="${i}" onclick="setPage(${i})">${i}</a>`;
        } else {
            li.innerHTML = `<a class="page-link" data-page="${i}" onclick="setPage(${i})">${i}</a>`;
        }
        document.getElementById('pagination').appendChild(li);
    }

    if (maxPage > 1) {
        document.getElementById('pagination').innerHTML += `
                  <li class="page-item" id="btn-pag-next"><a class="page-link" onclick="setPage('+')">Next</a></li>
                `;
    }
}