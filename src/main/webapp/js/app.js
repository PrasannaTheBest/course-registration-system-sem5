/* Small UI niceties: count-up numbers, dismissible alerts, button loading state. */
(function () {
    'use strict';
    var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // 1. Count-up animation for numbers marked with data-count
    document.querySelectorAll('[data-count]').forEach(function (el) {
        var target = parseInt(el.getAttribute('data-count'), 10);
        if (isNaN(target) || reduce) { return; }
        var suffix = el.getAttribute('data-suffix') || '';
        var duration = 800, start = null;
        function step(ts) {
            if (start === null) { start = ts; }
            var p = Math.min((ts - start) / duration, 1);
            var eased = 1 - Math.pow(1 - p, 3);
            el.textContent = Math.round(target * eased) + suffix;
            if (p < 1) { requestAnimationFrame(step); }
        }
        el.textContent = '0' + suffix;
        requestAnimationFrame(step);
    });

    // 2. Alerts fade out by themselves after a few seconds, or on click
    document.querySelectorAll('.alert').forEach(function (alert) {
        function hide() {
            alert.classList.add('alert-hide');
            setTimeout(function () { alert.remove(); }, 400);
        }
        alert.addEventListener('click', hide);
        if (!alert.classList.contains('error')) { setTimeout(hide, 5000); }
    });

    // 3. Show a spinner on submit buttons and block double clicks
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (e.defaultPrevented) { return; }
            var btn = form.querySelector('button[type=submit]');
            if (btn && !btn.classList.contains('is-loading')) {
                setTimeout(function () { btn.classList.add('is-loading'); btn.disabled = true; }, 0);
            }
        });
    });

    // Coming back with the browser's back button: undo any loading state
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) {
            document.querySelectorAll('.btn.is-loading').forEach(function (b) {
                b.classList.remove('is-loading'); b.disabled = false;
            });
        }
    });
}());
