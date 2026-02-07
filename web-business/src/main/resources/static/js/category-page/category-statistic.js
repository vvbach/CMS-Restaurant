async function categoryStatistic() {
    await callApi(`/v1/api/category-statistic/${location.pathname.split("/")[2]}`, {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (res) => {
        if (!res) return;

        const data = res?.data;
        console.log(data)

        const container = document.getElementById("statistic-container")
        container.innerHTML = data.map(el => `
            <div class="col-12 col-sm-6 col-lg-3">
                <div class="single-cool-fact">
                    <img src="${el.imageUrl}" alt="" style="height: 82px; width: 82px">
                    <h3><span class="counter">${el.count}</span></h3>
                    <h6>${el.name}</h6>
                </div>
            </div>
        `).join("")
    });
}

document.addEventListener("DOMContentLoaded", categoryStatistic);
