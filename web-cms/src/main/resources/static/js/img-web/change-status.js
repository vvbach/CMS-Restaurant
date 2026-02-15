document.getElementById('btnSaveChange').addEventListener('click',async (e) => {
    e.preventDefault();                // Vì sao: tránh form submit mặc định
    this.disabled = true;
    let objChange = {};
    let url = "/v1/api/image-web/" + document.getElementById('id-row-edit').value;
    console.log(document.getElementById('headerState').textContent);
    if (document.getElementById('headerState').textContent === 'Pending Approval') {
        url = url + "/pending-approval";
    } else if (document.getElementById('headerState').textContent === 'Approved') {
        url = url + "/approve";
    } else if (document.getElementById('headerState').textContent === 'Published') {
        url = url + "/publish";
    } else if (document.getElementById('headerState').textContent === 'Rejected') {
        url = url + "/reject";
        objChange['rejectionReason'] = document.getElementById('reason').textContent; ;
    } else if (document.getElementById('headerState').textContent === 'Unpublished') {
        url = url + "/unpublish";
        objChange['reason'] = document.getElementById('reason').textContent;
    } else if (document.getElementById('headerState').textContent === 'Draft') {
        url = url + "/draft";
        objChange['reason'] = document.getElementById('reason').textContent;
    } else {
        this.disabled = false;
        return;
    }
    try {
        await callApi(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(objChange)
        }, (data) => {
            closeAllModals();
            handleFilter();
        });
    } catch (err) {
        console.warn(err);
    } finally {

    }


    this.disabled = false;
});