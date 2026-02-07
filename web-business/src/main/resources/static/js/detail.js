async function loadProductDetail() {
    const id = window.location.pathname.split("/").pop(); // get {id} from URL
    const url = `/v1/api/food/${id}`;

    const loading = document.getElementById("loading");
    const detail = document.getElementById("productDetail");

    await callApi(url, {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    }, (data) => {
        if (!data || !data.data) {
            document.getElementById("productDetail").innerHTML = `
                <div class="col-12 text-center"><p>Product not found.</p></div>`;
            return;
        }

        const food = data.data;

        // Update image
        document.getElementById("foodImage").src = food.imageUrl;
        document.getElementById("foodImage").alt = food.name;

        // Update text fields
        document.getElementById("foodName").textContent = food.name;
        document.getElementById("foodDescription").textContent = food.description;

        // Discount logic
        const discountBadge = document.getElementById("discountBadge");
        const discountValue = document.getElementById("discountValue");
        const discountedPrice = document.getElementById("discountedPrice");
        const originalPrice = document.getElementById("originalPrice");

        if (food.discount && food.discount > 0) {
            discountBadge.style.display = "inline-block";
            discountValue.textContent = `-${food.discount}`;

            const discounted = food.price - (food.price * food.discount / 100);
            discountedPrice.textContent = `${discounted.toLocaleString()} ₫`;
            originalPrice.textContent = `${food.price.toLocaleString()} ₫`;
        } else {
            discountBadge.style.display = "none";
            discountedPrice.textContent = `${food.price.toLocaleString()} ₫`;
            originalPrice.textContent = "";
        }

        loading.style.display = "none";
        detail.style.display = "flex";

        // Quantity logic
        let quantity = 1;
        const quantityValue = document.getElementById("quantityValue");

        document.getElementById("increaseQty").addEventListener("click", async () => {
            quantity++;
            quantityValue.textContent = quantity;
        });

        document.getElementById("decreaseQty").addEventListener("click", async () => {
            if (quantity > 1) {
                quantity--;
                quantityValue.textContent = quantity;
            }
        });

        // Add to cart button
        document.getElementById("addToCartBtn").addEventListener("click", async () => {
            const foodId = food.id;
            const res = await fetch("/cart/add", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ foodId, quantity })
            });

            if (res.ok) {
                alert("Item added to cart!");
            } else {
                alert("Failed to add to cart.");
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", loadProductDetail);

