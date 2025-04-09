$(document).ready(function() {
    AOS.init({
        duration: 600,
        easing: 'ease-in-out'
    });

    // Добавляем обработку клика на кнопку аккордеона
    $('.accordion-button').on('click', function(e) {
        const target = $(this).data('bs-target');
        const $targetEl = $(target);

        if ($targetEl.hasClass('show')) {
            // Если уже открыто — закрываем вручную
            $targetEl.collapse('hide');
            e.preventDefault(); // чтобы Bootstrap не пытался открыть снова
        }

        // Обновляем иконки
        $('.accordion-button i').removeClass('fa-chevron-up').addClass('fa-chevron-down');

        // Если элемент закрывается — не добавляем стрелку вверх
        if (!$targetEl.hasClass('show')) {
            $(this).find('i').removeClass('fa-chevron-down').addClass('fa-chevron-up');
        }
    });

    // Доп. — когда аккордеон открывается или закрывается, меняем иконку
    $('.collapse').on('show.bs.collapse', function () {
        const button = $(`[data-bs-target="#${this.id}"]`);
        button.find('i').removeClass('fa-chevron-down').addClass('fa-chevron-up');
    });

    $('.collapse').on('hide.bs.collapse', function () {
        const button = $(`[data-bs-target="#${this.id}"]`);
        button.find('i').removeClass('fa-chevron-up').addClass('fa-chevron-down');
    });

    if (window.innerWidth < 576) {
        $('[data-aos]').attr('data-aos-delay', '50');
    }
});