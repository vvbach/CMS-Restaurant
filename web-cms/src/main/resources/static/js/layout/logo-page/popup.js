<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (document.getElementById('detail-id').value) {
        let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
        let url = "/v1/api/logo-page/" + document.getElementById('detail-id').value
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, (resp) => {
            const d = resp?.data ?? resp;

            setStatus(d, "UI");

            document.querySelector('#detail-img-update').src = d?.url;
            document.querySelector('#detail-name-update').querySelector("input").value = d?.name;
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
