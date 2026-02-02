document.getElementById('btn-new-image').addEventListener('click', async (e) => {
    // e.preventDefault();
    if(!document.getElementById('input-file-new').files[0]) {
        document.getElementById("invalid-feedback-input-file-new").classList.remove("invalid-feedback");
        document.getElementById("invalid-feedback-input-file-new").classList.add("text-danger");
    }

    if(!document.getElementById('input-describe-new').value.trim()) {
        document.getElementById("invalid-feedback-input-describe-new").classList.remove("invalid-feedback");
        document.getElementById("invalid-feedback-input-describe-new").classList.add("text-danger");
    }

    const form = document.getElementById('form-new');
    this.disabled = true;

    // Lấy dữ liệu form
    const formData = new FormData(form);
    try {
        // Ví dụ endpoint giả. Đổi thành API thật của bạn.
        const data = await callApi('/v1/api/image-web/add', {
                method: 'POST',
                body: formData
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
    this.disabled = false;
});