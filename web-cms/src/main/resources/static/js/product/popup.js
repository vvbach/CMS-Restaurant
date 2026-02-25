<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (!document.getElementById('detail-id').value) return;
    if(this.disabled) return;
    this.disabled = true;
    let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
    let url = "/v1/api/food/" + document.getElementById('detail-id').value;
    try {
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, async (resp) => {
            const d = resp?.data ?? resp;

            setStatus(d, "PRODUCT");
            document.querySelector('#detail-img-update').src = d?.imageUrl;
            document.querySelector('#detail-name-update').querySelector("input").value = d?.name;
            document.querySelector('#detail-description-update').querySelector("textarea").value = d?.description;
            document.querySelector('#detail-price-update').querySelector("input").value = d?.price;
            document.querySelector('#detail-discount-update').querySelector("input").value = d?.discount;
            document.querySelector('#detail-stock-quantity-update').querySelector("input").value = d?.stockQuantity;

            const categorySelect = document.getElementById('edit-category');
            categorySelect.innerHTML = '';
            try {
                const cats = await callApi('/v1/api/category');
                const selectedIds = d?.categories?.map(c => c.id) || [];

                cats.data.forEach(cat => {
                    if (cat.isDelete === "YES" || cat.status !== 'PUBLISHED') return;
                    const option = document.createElement('option');
                    option.value = cat.id;
                    option.textContent = cat.name;
                    if (selectedIds.includes(cat.id)) {
                        option.selected = true;
                    }
                    categorySelect.appendChild(option);
                });
            } catch (err) {
                console.error("Error loading categories:", err);
            }

            if (d?.status === 'DRAFT') {
                document.getElementById('btn-update-detail').classList.remove('d-none')
            } else {
                document.getElementById('btn-update-detail').classList.add('d-none')
            }
        });
    } catch (err) {
        console.error("Error loading product:", err);
    } finally {
        this.disabled = false;
    }
    myModal.show();

});

document.getElementById('openModalNewBtn').addEventListener('click', async function () {
    if (this.disabled) return; // prevent double-click
    this.disabled = true;

    const myModal = new bootstrap.Modal(document.getElementById('popup-new'));
    document.getElementById('input-image-new').src = '';
    const categorySelect = document.getElementById('input-category-new');
    categorySelect.innerHTML = '';
    try {
        const cats = await callApi('/v1/api/category');

        cats.data.forEach(cat => {
            if (cat.isDelete === "YES" || cat.status !== 'PUBLISHED') return;
            const option = document.createElement('option');
            option.value = cat.id;
            option.textContent = cat.name;
            categorySelect.appendChild(option);
        });
    } catch (err) {
        console.error("Error loading categories:", err);
    } finally {
        this.disabled = false;
    }
    myModal.show();
});

