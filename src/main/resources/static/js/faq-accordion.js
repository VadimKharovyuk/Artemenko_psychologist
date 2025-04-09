$(document).ready(function() {
    // Инициализируем Bootstrap collapse - это должно сработать автоматически

    // Правильная обработка иконок при открытии/закрытии
    $('.collapse').on('show.bs.collapse', function () {
        $(this).prev('.card-header').find('.btn-link').removeClass('collapsed');
    });

    $('.collapse').on('hide.bs.collapse', function () {
        $(this).prev('.card-header').find('.btn-link').addClass('collapsed');
    });

    // Дополнительно для мобильных устройств
    if (window.innerWidth < 576) {
        // Уменьшаем задержку анимации на мобильных
        $('.faq-card').attr('data-aos-delay', '50');
    }
});