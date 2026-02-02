<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (!document.getElementById('detail-id').value) return;

    if (this.disabled) return;   // prevent double-click
    this.disabled = true;

    let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
    let url = "/v1/api/motto/" + document.getElementById('detail-id').value;

    try {
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, async (resp) => {
            const d = resp?.data ?? resp;

            setStatus(d, "UI");

            document.querySelector('#detail-title-update input').value = d?.title ?? "";

            document.querySelector('#detail-description-update input').value = d?.description ?? "";
        });

        myModal.show(this);
    } catch (err) {
        console.error("Error loading detail:", err);
    } finally {
        this.disabled = false;  // always re-enable, even if error
    }
});


document.getElementById('openModalNewBtn').addEventListener('click', async function () {
    if (this.disabled) return;   // already running
    this.disabled = true;

    const myModal = new bootstrap.Modal(document.getElementById('popup-new'));

    this.disabled = false;
    myModal.show(this);
});