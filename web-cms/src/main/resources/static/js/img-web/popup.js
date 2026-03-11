<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (document.getElementById('detail-id').value) {
        let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
        let url = "/v1/api/image-web/" + document.getElementById('detail-id').value
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, (resp) => {
            // xóa hết dữ liệu
            document.getElementById('detail-list-status').innerHTML = '';

            const d = resp?.data ?? resp;
            const imgEl = document.getElementById('detail-img-view');
            imgEl.src = d?.pathImage;
            imgEl.alt = fileNameFromPath(d?.pathImage) || 'image';

            document.querySelector('#edit-description').value = d?.description ?? "";

            setStatus(d, "UI");
            setProgress(MAP_STATUS_STEP[d?.status] ?? 0);

            if (d?.status === 'DRAFT') {
                document.getElementById('btn-update-detail').classList.remove('d-none')
            } else {
                document.getElementById('btn-update-detail').classList.add('d-none')
            }
        });
        this.disabled = false;



        myModal.show();
    }
});

const MAX_SIZE_MB = 5;
const ALLOWED = ['image/jpeg','image/png'];
document.getElementById('detail-img-input').addEventListener('change', (e)=>{
    const f = e.target.files?.[0];
    applyFile(f);
    e.target.value = URL.createObjectURL(f);
});

function applyFile(file){
    if(!file) return;
    if(!ALLOWED.includes(file.type)){
        showToast('Unsupported format. Choose JPG/PNG');
        return;
    }
    if(file.size > MAX_SIZE_MB * 1024 * 1024){
        showToast(`Size is too big (>${MAX_SIZE_MB}MB).`);
        return;
    }
    document.getElementById('detail-img-view').src = URL.createObjectURL(file);
}
// Hàm để lấy nội dung textarea trong div theo ID
function getTextareaValue(divId) {
    const div = document.getElementById(divId);
    if (!div) return null;
    const textarea = div.querySelector("textarea");
    return textarea ? textarea.value : null;
}

// Hàm để thay đổi nội dung textarea trong div theo ID
document.getElementById('openModalNewBtn').addEventListener('click', function () {
    var myModal = new bootstrap.Modal(document.getElementById('popup-new'));
    myModal.show();
});

document.getElementById("form-new").addEventListener("submit", async function (e) {
    e.preventDefault(); // Ngăn reload trang

    const form = e.target;
    const formData = new FormData(form);

    // Nếu cần convert sang JSON
    const payload = Object.fromEntries(formData.entries());

});

