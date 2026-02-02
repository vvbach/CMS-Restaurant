<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (document.getElementById('detail-id').value) {
        let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
        let url = "/v1/api/category/" + document.getElementById('detail-id').value;
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, (resp) => {
            const d = resp?.data ?? resp;

            setStatus(d, "PRODUCT");
            document.querySelector('#detail-name-update').querySelector("input").value = d?.name;
            document.querySelector('#detail-description-update').querySelector("textarea").value = d?.description;
        });
        this.disabled = false;

        myModal.show();
    }
});

document.getElementById('openModalNewBtn').addEventListener('click', function () {
    const myModal = new bootstrap.Modal(document.getElementById('popup-new'));
    myModal.show();
});
