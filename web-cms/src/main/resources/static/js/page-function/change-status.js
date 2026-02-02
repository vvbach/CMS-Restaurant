function saveStatusChange(baseUrl) {
    return async function(e) {
        e.preventDefault();
        this.disabled = true;
        let objChange = {};
        let url = baseUrl + document.getElementById('id-row-edit').value;
        console.log(document.getElementById('headerState').textContent);
        if (document.getElementById('headerState').textContent === 'PendingApproval') {
            url = url + "/pending-approval";
        } else if (document.getElementById('headerState').textContent === 'Approved') {
            url = url + "/approve";
        } else if (document.getElementById('headerState').textContent === 'Publish') {
            url = url + "/publish";
        } else if (document.getElementById('headerState').textContent === 'Rejected') {
            url = url + "/reject";
            objChange['reason'] = document.getElementById('reason').querySelector('textarea').value;
        } else if (document.getElementById('headerState').textContent === 'Unpublish') {
            url = url + "/unpublish";
            objChange['reason'] = document.getElementById('reason').querySelector('textarea').value;
        } else if (document.getElementById('headerState').textContent === 'Draft') {
            url = url + "/revertToDraft";
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
            this.disabled = false;
        }
    };
}
