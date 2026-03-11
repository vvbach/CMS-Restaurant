async function openImageLibrary() {
    const modalEl = document.getElementById('imageLibraryModal');
    const imageGrid = document.getElementById('imageLibraryGrid');
    const selectBtn = document.getElementById('selectImageBtn');
    const inputImageNew = document.getElementById('input-image-new');
    const detailImgUpdate = document.getElementById('detail-img-update')

    // Clear previous images and selection
    imageGrid.innerHTML = '';
    selectBtn.disabled = true;
    let selectedImageUrl = null;

    // Fetch image list from API
    try {
        const data = await callApi('/v1/api/image-web');
        data.data.forEach((img) => {
            if (img.isDelete === 'YES' || img.status !== 'PUBLISHED') return;
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

    // Handle image selection
    selectBtn.onclick = () => {
        if (selectedImageUrl) {
            inputImageNew.src = selectedImageUrl;
            inputImageNew.width = 100;
            inputImageNew.height = 100;
            detailImgUpdate.src = selectedImageUrl;
        }

        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.hide();
    };

    // Show modal
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
}
