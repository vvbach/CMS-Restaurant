document.getElementById('bt-update-detail').addEventListener('click',async (e) => {
    e.preventDefault();
    e.disabled = true;
    const fileInput = document.getElementById('detail-img-input');
    // Lấy dữ liệu form
    const formdata = new FormData();
    if (fileInput.files || fileInput.files.length !== 0) {
        formdata.append('file', fileInput.files[0]);
    }

    formdata.append('description', getTextareaValue('detail-descs'));

    try {
        let url = '/v1/api/image-web/update/' + document.getElementById('id-row-edit').value;
        // Ví dụ endpoint giả. Đổi thành API thật của bạn.
        const data = await callApi(url, {
                method: 'POST',
                body: formdata
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
})