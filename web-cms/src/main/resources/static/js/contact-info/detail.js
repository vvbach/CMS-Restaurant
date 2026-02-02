async function viewDetail(id) {
    viewUIDetail()
    let url = "/v1/api/contact-info/" + id
    await callApi(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
    }, (resp) => {
        const d = resp?.data ?? resp;

        document.getElementById('detail-id').value = d?.id ?? '';
        document.getElementById('detail-image').src = d?.imageUrl ?? '';
        document.getElementById('detail-text').value = d?.text ?? '';
        document.getElementById('detail-address').value = d?.address ?? '';
        document.getElementById('detail-email').value = d?.email ?? '';
        document.getElementById('detail-phone').value = d?.phoneNumber ?? '';
        document.getElementById('detail-status').value = mapStatus(d?.status);
        document.getElementById('detail-is-delete').value = mapIsDelete(d?.isDelete);
        document.getElementById('inputAuthor').value = d?.createdByName ?? d?.createdBy ?? '-';
        document.getElementById('inputCreateAt').value = viDateTime(d?.createdAt);
        document.getElementById('inputUpdater').value = d?.updatedByName ?? d?.updatedBy ?? '-';
        document.getElementById('inputUpdateAt').value = viDateTime(d?.updatedAt);

        if (d?.status === 'Draft') {
            document.getElementById('bt-update-detail').classList.remove('d-none')
        } else {
            document.getElementById('bt-update-detail').classList.add('d-none')
        }

    });
    this.disabled = false;
}