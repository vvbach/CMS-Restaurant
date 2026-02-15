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
            document.getElementById('detail-lst-status').innerHTML = '';

            const d = resp?.data ?? resp;
            const imgEl = document.getElementById('detail-image');
            imgEl.src = d?.pathImage;
            imgEl.alt = fileNameFromPath(d?.pathImage) || 'image';

            document.getElementById('headerState').textContent = mapStatus(d?.status);
            document.getElementById('statusField').value = mapStatus(d?.status);
            setTextareaValue('detail-descs', d?.description);
            if (d?.status === 'DRAFT') {
                hideDiv('detail-reason-reject');
                hideDiv('detail-reason-unpublish');
                document.querySelector('#detail-descs textarea').removeAttribute("disabled")
                document.getElementById('detail-lst-status').insertAdjacentHTML('beforeend',
                    `<li><button class="dropdown-item" data-action="PendingApproval">Pending Approval</button></li>`) ;

            }
            else if (d?.status === 'REJECTED') {
                showDiv('detail-reason-reject');
                hideDiv('detail-reason-unpublish');
                updateRejectReason( d?.rejectionReason,'#detail-reason-reject')
                document.getElementById('detail-lst-status').insertAdjacentHTML('beforeend',
                    `<li><button class="dropdown-item" data-action="PendingApproval">Pending Approval</button></li>`) ;
            }
            else if (d?.status === 'PENDING_APPROVAL') {
                hideDiv('detail-reason-reject');
                hideDiv('detail-reason-unpublish');
                document.getElementById('detail-lst-status').insertAdjacentHTML('beforeend',
                    `<li><button class="dropdown-item" data-action="Rejected"><b>Reject</b></button></li>
                            <li><button class="dropdown-item" data-action="Approve">Approve</button></li>`) ;
            }
            else if (d?.status === 'APPROVED') {
                hideDiv('detail-reason-reject');
                hideDiv('detail-reason-unpublish');
                document.getElementById('detail-lst-status').insertAdjacentHTML('beforeend',
                    `<li><button class="dropdown-item" data-action="Rejected"><b>Reject</b></button></li>
                            <li><button class="dropdown-item" data-action="Publish">Publish</button></li>`) ;
            }
            else if (d?.status === 'PUBLISHED') {
                hideDiv('detail-reason-reject');
                hideDiv('detail-reason-unpublish');
                document.getElementById('detail-lst-status').insertAdjacentHTML('beforeend',
                    `<li><button class="dropdown-item" data-action="Unpublish">Unpublish</button></li>`) ;
            }
            else if (d?.status === 'UNPUBLISHED') {
                hideDiv('detail-reason-reject');
                showDiv('detail-reason-unpublish');
                updateRejectReason( d?.reasonUnPublish,'#detail-reason-unpublish')
                document.getElementById('detail-lst-status').insertAdjacentHTML('beforeend',
                    `<li><button class="dropdown-item" data-action="Publish">Publish</button></li>
                            <li><button class="dropdown-item" data-action="Draft">Draft</button></li>`) ;
            }

            if (d?.isDelete === 'YES') {
                showDiv('detail-reason-delete')
                updateRejectReason( d?.deletionReason,'#detail-reason-delete')
            } else {
                hideDiv('detail-reason-delete')
            }

            document.getElementById('detail-img-view').src = d?.pathImage;
            document.getElementById('detail-txt-status').textContent = mapStatus(d?.status);
            document.getElementById('detail-txt-delete').textContent = mapIsDelete(d?.isDelete);
            document.getElementById('created-user').value = d?.createdByName;
            document.getElementById('created-at').value =formatDate(d?.createdAt);
            document.getElementById('updated-user').value = d?.updatedByName
            document.getElementById('updated-at').value = formatDate(d?.updatedAt);
            document.getElementById('id-row-edit').value = d?.id;

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
        showToast('❗ Định dạng không hỗ trợ. Chọn JPG/PNG');
        return;
    }
    if(file.size > MAX_SIZE_MB * 1024 * 1024){
        showToast(`❗ Ảnh quá lớn (>${MAX_SIZE_MB}MB).`);
        return;
    }
    document.getElementById('detail-img-view').src = URL.createObjectURL(file);
}

function updateRejectReason(newReason, divId) {
    if (newReason) {
        const textarea = document.querySelector(divId +' textarea');
        textarea.value = newReason;
    }
}

// Hàm để lấy nội dung textarea trong div theo ID
function getTextareaValue(divId) {
    const div = document.getElementById(divId);
    if (!div) return null;
    const textarea = div.querySelector("textarea");
    return textarea ? textarea.value : null;
}

// Hàm để thay đổi nội dung textarea trong div theo ID
function setTextareaValue(divId, newValue) {
    const div = document.getElementById(divId);
    if (!div) return;
    const textarea = div.querySelector("textarea");
    if (textarea) {
        textarea.value = newValue;
        textarea.readOnly = false; // nếu bạn muốn cho phép chỉnh sửa
    }
}

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

document.getElementById("openModalDeleteBtn").addEventListener("click", async function (e) {
    e.preventDefault(); // Ngăn reload trang
    this.disabled = true;
    let id = document.getElementById('detail-id').value || null;
    if (id !== null) {
        let myModal = new bootstrap.Modal(document.getElementById('confirmDeleteModal'));
        document.getElementById('id-confirm-delete').value = id;
        myModal.show();
    }

    this.disabled = false

});

