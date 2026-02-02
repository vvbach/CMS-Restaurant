<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (document.getElementById('detail-id').value) {
        let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
        let url = "/v1/api/contact-info/" + document.getElementById('detail-id').value;
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, (resp) => {
            const d = resp?.data ?? resp;

            setStatus(d, "UI");

            document.querySelector('#detail-img-update').src = d?.imageUrl;
            document.querySelector('#detail-text-update').querySelector("input").value = d?.text;
            document.querySelector('#detail-address-update').querySelector("textarea").value = d?.address;
            document.querySelector('#detail-email-update').querySelector("input").value = d?.email;
            document.querySelector('#detail-phone-update').querySelector("input").value = d?.phoneNumber;
        });
        this.disabled = false;

        myModal.show();
    }
});

document.getElementById('openModalNewBtn').addEventListener('click', function () {
    const myModal = new bootstrap.Modal(document.getElementById('popup-new'));
    document.getElementById('input-image-new').src = null;
    myModal.show();
});

