document.addEventListener('DOMContentLoaded', function() {
    const ratingInput = document.getElementById('rating');
    const stars = document.querySelectorAll('.star-clickable');
    const clearButton = document.getElementById('clear-rating');
    // 初期表示の設定
    function updateStarDisplay(rating) {
        stars.forEach((star, index) => {
            if (index < rating) {
                star.style.color = '#ffc107';
            } else {
                star.style.color = '#ccc';
            }
        });
    }
    // 現在の値で初期化
    const currentRating = ratingInput.value ? parseInt(ratingInput.value) : 0;
    updateStarDisplay(currentRating);
    // 星のクリックイベント
    stars.forEach(star => {
        star.addEventListener('click', function() {
            const rating = parseInt(this.getAttribute('data-rating'));
            ratingInput.value = rating;
            updateStarDisplay(rating);
        });
        // ホバー効果
        star.addEventListener('mouseenter', function() {
            const rating = parseInt(this.getAttribute('data-rating'));
            stars.forEach((s, index) => {
                if (index < rating) {
                    s.style.color = '#ffc107';
                } else {
                    s.style.color = '#ccc';
                }
            });
        });
    });
    // マウスが星の領域から離れた時の処理
    document.querySelector('.star-rating-input').addEventListener('mouseleave', function() {
        const currentRating = ratingInput.value ? parseInt(ratingInput.value) : 0;
        updateStarDisplay(currentRating);
    });
    // クリアボタンのクリックイベント
    clearButton.addEventListener('click', function() {
        ratingInput.value = '';
        updateStarDisplay(0);
    });
});
