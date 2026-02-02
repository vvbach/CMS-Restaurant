<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (!document.getElementById('detail-id').value) return;

    if (this.disabled) return;   // prevent double-click
    this.disabled = true;

    let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
    let url = "/v1/api/about-category/" + document.getElementById('detail-id').value;

    try {
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, async (resp) => {
            const d = resp?.data ?? resp;

            setStatus(d, "UI");

            document.querySelector('#edit-title').value = d?.title ?? "";
            document.querySelector('#edit-subtitle').value = d?.subtitle ?? "";
            document.querySelector('#edit-description').value = d?.description ?? "";
        });

        myModal.show();
    } catch (err) {
        console.error("Error loading detail:", err);
    } finally {
        this.disabled = false;  // always re-enable, even if error
    }
});


document.getElementById('openModalNewBtn').addEventListener('click', async function () {
    if (this.disabled) return;   // already running
    this.disabled = true;

    var myModal = new bootstrap.Modal(document.getElementById('popup-new'));
    document.getElementById("input-categoryPageId-new").value = location.pathname.split("/")[2]

    this.disabled = false;
    myModal.show();
});