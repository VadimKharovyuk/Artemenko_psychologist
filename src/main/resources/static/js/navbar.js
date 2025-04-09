// $(document).ready(function() {
//     // Закрытие меню при клике на ссылку
//     $('.navbar-nav .nav-link').on('click', function() {
//         $('.navbar-collapse').collapse('hide');
//     });
//
//     // Закрытие меню при клике вне навигации
//     $(document).on('click', function(event) {
//         var target = $(event.target);
//         if (!target.closest('.navbar').length && $('.navbar-collapse').hasClass('show')) {
//             $('.navbar-collapse').collapse('hide');
//         }
//     });
// });

$(document).ready(function() {
    // Закрытие меню при клике на ссылку
    $('.navbar-nav .nav-link').on('click', function() {
        $('.navbar-collapse').collapse('hide');
    });

    // Закрытие меню при клике вне навигации
    $(document).on('click', function(event) {
        var target = $(event.target);
        if (!target.closest('.navbar').length && $('.navbar-collapse').hasClass('show')) {
            $('.navbar-collapse').collapse('hide');
        }
    });

    // Закрытие меню при клике на само меню (только на мобильных)
    $('.navbar-collapse').on('click', function(event) {
        // Проверяем, что клик был непосредственно на меню, а не на ссылку или другой элемент внутри
        if ($(event.target).is('.navbar-collapse') && $('.navbar-collapse').hasClass('show')) {
            $('.navbar-collapse').collapse('hide');
        }
    });
});