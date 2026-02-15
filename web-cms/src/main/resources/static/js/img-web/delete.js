document.getElementById('confirmDeleteBtn').addEventListener('click',async (e) => {
    e.disabled = true;
    let requestDelete = {}
    requestDelete['deletionReason'] = document.getElementById('deleteReasonDetail').value;
    try {
        let url = '/v1/api/image-web' + document.getElementById('id-confirm-delete').value;
        // Ví dụ endpoint giả. Đổi thành API thật của bạn.
        const data = await callApi(url, {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(requestDelete)
            }
            , (data) => {
                showSuccessDialog(data.message)
                handleFilter();
            });
    } catch (err) {
        // đã show toast lỗi trong callApi; vẫn có thể xử lý thêm ở đây nếu cần
        console.error(err);
    } finally {
        closeAllModals();
    }
    e.disabled = false;
});
