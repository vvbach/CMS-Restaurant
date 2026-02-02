<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (document.getElementById('detail-id').value) {
        let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
        let url = "/v1/api/social-link/" + document.getElementById('detail-id').value
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, (resp) => {
            // xóa hết dữ liệu
            const d = resp?.data ?? resp;

            setStatus(d, "UI");

            document.querySelector('#detail-img-update').src = d?.iconUrl;
            document.querySelector('#detail-platform-update').querySelector("input").value = d?.platform;
            document.querySelector('#detail-url-update').querySelector("input").value = d?.url;
        });
        this.disabled = false;

        myModal.show();
    }
});

document.getElementById('openModalNewBtn').addEventListener('click', function () {
    const myModal = new bootstrap.Modal(document.getElementById('popup-new'));
    document.getElementById('input-image-new').src = '';
    myModal.show();
});
