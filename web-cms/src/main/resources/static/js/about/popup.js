<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (!document.getElementById('detail-id').value) return;

    let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
    let url = "/v1/api/about/" + document.getElementById('detail-id').value;

    let context = '';
    await callApi(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
    }, (resp) => {
        const d = resp?.data ?? resp;

        setStatus(d, "UI");
        document.querySelector('#detail-img-update').src = d?.imageUrl;
        document.querySelector('#detail-title-update').querySelector("input").value = d?.title;
        context = d?.text;
    });
    this.disabled = false;

    myModal.show();


    document.getElementById('popup-update').addEventListener('shown.bs.modal', function () {
        initTinyMCE('#edit-text', context || "");
    }, { once: true }); // only run once per open
    myModal.show();
});

document.getElementById('openModalNewBtn').addEventListener('click', function () {
    initTinyMCE('#input-text-new', "");
    const myModal = new bootstrap.Modal(document.getElementById('popup-new'));
    myModal.show();
});

function initTinyMCE(selector, initialContent = "") {
    // remove old instance if exists
    if (tinymce.get(selector.replace('#', ''))) {
        tinymce.remove(selector);
    }

    tinymce.init({
        selector: selector,
        license_key: 'gpl',
        height: 400,
        menubar: false,
        plugins: 'lists link image table code',
        toolbar: `
            undo redo | formatselect | fontsizeselect fontselect |
            bold italic underline forecolor backcolor |
            alignleft aligncenter alignright alignjustify |
            bullist numlist outdent indent | removeformat | code | openImageLibrary
        `,
        fontsize_formats: "8pt 10pt 12pt 14pt 18pt 24pt 36pt",
        font_formats: "Arial=arial,helvetica,sans-serif; Times New Roman=times new roman,times; Courier New=courier; Verdana=verdana,geneva;",
        setup: function (editor) {
            editor.ui.registry.addButton('openImageLibrary', {
                text: '📁 Insert from Library',
                onAction: function () {
                    openImageLibraryForEditor(editor);
                }
            });
        },
        init_instance_callback: function (editor) {
            if (initialContent) {
                editor.setContent(initialContent);
            }
        }
    });
}


async function openImageLibraryForEditor(editorInstance) {
    const modalEl = document.getElementById('imageLibraryModal');
    const imageGrid = document.getElementById('imageLibraryGrid');
    const selectBtn = document.getElementById('selectImageBtn');

    // Clear previous images and selection
    imageGrid.innerHTML = '';
    selectBtn.disabled = true;
    let selectedImageUrl = null;

    // Fetch image list from API
    try {
        const data = await callApi('/v1/api/image-web');
        data.data.forEach((img) => {
            const col = document.createElement('div');
            col.className = 'col-md-3 mb-3';

            col.innerHTML = `
                <img src="${img.pathImage}" alt="library image" class="img-thumbnail selectable-image" style="cursor:pointer;" />
            `;

            col.querySelector('img').addEventListener('click', () => {
                // Highlight selected image
                document.querySelectorAll('.selectable-image').forEach(el => el.classList.remove('border-primary'));
                col.querySelector('img').classList.add('border-primary');

                selectedImageUrl = img.pathImage;
                selectBtn.disabled = false;
            });

            imageGrid.appendChild(col);
        });

    } catch (err) {
        console.error('Error loading image library:', err);
    }

    // Show modal
    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    selectBtn.onclick = () => {
        const selected = document.querySelector('.selectable-image.border-primary');
        if (selected) {
            const url = selected.getAttribute('src');
            // Insert image into TinyMCE at cursor position
            editorInstance.insertContent(`<img src="${url}" alt="Image" />`);
        }
        // Close modal
        bootstrap.Modal.getOrCreateInstance(modalEl).hide();
    };

}