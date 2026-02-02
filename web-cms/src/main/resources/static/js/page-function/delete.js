function deleteItem(apiEndpoint) {
    return async function (e) {
        e.preventDefault();
        const btn = e.currentTarget;
        btn.disabled = true;

        const requestDelete = {
            reason: document.getElementById('deleteReasonDetail').value
        };

        try {
            const id = document.getElementById('id-confirm-delete').value;
            const url = apiEndpoint + id;

            await callApi(url, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestDelete)
            }, (data) => {
                showSuccessDialog(data.message);
                handleFilter();
            });
        } catch (err) {
            console.error(err);
        } finally {
            closeAllModals();
            btn.disabled = false;
        }
    };
}
