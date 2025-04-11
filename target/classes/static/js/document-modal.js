document.addEventListener('DOMContentLoaded', function() {
    // Настраиваем модальное окно для просмотра документов
    const documentModal = document.getElementById('documentModal');
    if (documentModal) {
        documentModal.addEventListener('show.bs.modal', function(event) {
            const button = event.relatedTarget;
            const imageUrl = button.getAttribute('data-document-url');
            const modalImage = document.getElementById('modalDocumentImage');
            const downloadLink = document.getElementById('downloadDocumentLink');
            const openLink = document.getElementById('openDocumentLink');

            if (modalImage) modalImage.src = imageUrl;
            if (downloadLink) downloadLink.href = imageUrl;
            if (openLink) openLink.href = imageUrl;
        });
    }

    // Добавляем функциональность зума для мобильных устройств
    const modalImage = document.getElementById('modalDocumentImage');
    const zoomContainer = document.getElementById('imageZoomContainer');

    if (modalImage && zoomContainer && window.innerWidth < 768) {
        let isZoomed = false;
        let scale = 1;

        modalImage.addEventListener('click', function() {
            if (!isZoomed) {
                // Увеличиваем изображение
                scale = 2;
                this.style.transform = `scale(${scale})`;
                this.style.transformOrigin = 'center center';
                this.style.transition = 'transform 0.3s ease';
                this.style.cursor = 'zoom-out';
                isZoomed = true;
            } else {
                // Возвращаем к нормальному размеру
                this.style.transform = 'scale(1)';
                this.style.cursor = 'zoom-in';
                isZoomed = false;
                scale = 1;
            }
        });

        // Добавляем возможность перемещения при увеличении
        let isDragging = false;
        let startX, startY, initialTranslateX = 0, initialTranslateY = 0;

        modalImage.addEventListener('touchstart', function(e) {
            if (isZoomed) {
                isDragging = true;
                startX = e.touches[0].clientX;
                startY = e.touches[0].clientY;

                // Получаем текущие значения translateX и translateY
                const transformValue = window.getComputedStyle(this).getPropertyValue('transform');
                const matrix = new DOMMatrix(transformValue);
                initialTranslateX = matrix.m41;
                initialTranslateY = matrix.m42;

                e.preventDefault();
            }
        });

        modalImage.addEventListener('touchmove', function(e) {
            if (isDragging && isZoomed) {
                const x = e.touches[0].clientX;
                const y = e.touches[0].clientY;

                const deltaX = x - startX;
                const deltaY = y - startY;

                // Ограничиваем перемещение исходя из масштаба
                const maxTranslate = (scale - 1) * 100; // Примерное ограничение

                const newTranslateX = Math.min(Math.max(initialTranslateX + deltaX, -maxTranslate), maxTranslate);
                const newTranslateY = Math.min(Math.max(initialTranslateY + deltaY, -maxTranslate), maxTranslate);

                this.style.transform = `scale(${scale}) translate(${newTranslateX / scale}px, ${newTranslateY / scale}px)`;

                e.preventDefault();
            }
        });

        modalImage.addEventListener('touchend', function() {
            isDragging = false;
        });
    } else if (modalImage) {
        // Для десктопов устанавливаем стиль курсора
        modalImage.style.cursor = 'zoom-in';

        // Простое увеличение для десктопов
        let isZoomed = false;

        modalImage.addEventListener('click', function() {
            if (!isZoomed) {
                this.style.transform = 'scale(1.5)';
                this.style.cursor = 'zoom-out';
                isZoomed = true;
            } else {
                this.style.transform = 'scale(1)';
                this.style.cursor = 'zoom-in';
                isZoomed = false;
            }
        });
    }
});