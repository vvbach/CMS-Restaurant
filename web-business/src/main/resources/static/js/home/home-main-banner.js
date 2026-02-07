async function loadHomeMainBanner() {
    await callApi("/v1/api/home-main-banner", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (data) => {
        if (!data) return;
        const heroSlides = document.querySelector(".hero-slides");

        if (!heroSlides) return;
        heroSlides.innerHTML = data.data.map(food => `
            <div class="single-hero-slide bg-img" style="background-image: url(${food.imageUrl});">
                <div class="container h-100">
                    <div class="row h-100 align-items-center">
                        <div class="col-12 col-md-9 col-lg-7 col-xl-6">
                            <div class="hero-slides-content" data-animation="fadeInUp" data-delay="100ms">
                                <h2 data-animation="fadeInUp" data-delay="300ms">${food.title}</h2>
                                <p data-animation="fadeInUp" data-delay="700ms">${food.description}</p>
                                <a href="/product/detail/${food.foodId}" class="btn delicious-btn" data-animation="fadeInUp" data-delay="1000ms">Xem ngay</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    });

    initHeroCarousel();
}

function initHeroCarousel() {
    if ($.fn.owlCarousel) {
        var welcomeSlide = $('.hero-slides');
        var receipeSlide = $('.receipe-slider');

        welcomeSlide.owlCarousel({
            items: 1,
            margin: 0,
            loop: true,
            nav: true,
            navText: ['Prev', 'Next'],
            dots: true,
            autoplay: true,
            autoplayTimeout: 5000,
            smartSpeed: 1000
        });

        welcomeSlide.on('translate.owl.carousel', function () {
            var slideLayer = $("[data-animation]");
            slideLayer.each(function () {
                var anim_name = $(this).data('animation');
                $(this).removeClass('animated ' + anim_name).css('opacity', '0');
            });
        });

        welcomeSlide.on('translated.owl.carousel', function () {
            var slideLayer = welcomeSlide.find('.owl-item.active').find("[data-animation]");
            slideLayer.each(function () {
                var anim_name = $(this).data('animation');
                $(this).addClass('animated ' + anim_name).css('opacity', '1');
            });
        });

        $("[data-delay]").each(function () {
            var anim_del = $(this).data('delay');
            $(this).css('animation-delay', anim_del);
        });

        $("[data-duration]").each(function () {
            var anim_dur = $(this).data('duration');
            $(this).css('animation-duration', anim_dur);
        });

        var dot = $('.hero-slides .owl-dot');
        dot.each(function () {
            var index = $(this).index() + 1 + '.';
            if (index < 10) {
                $(this).html('0').append(index);
            } else {
                $(this).html(index);
            }
        });

        receipeSlide.owlCarousel({
            items: 1,
            margin: 0,
            loop: true,
            nav: true,
            navText: ['Prev', 'Next'],
            dots: true,
            autoplay: true,
            autoplayTimeout: 5000,
            smartSpeed: 1000
        });
    }
}


document.addEventListener("DOMContentLoaded", loadHomeMainBanner);
