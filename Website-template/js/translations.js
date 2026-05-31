// ============================================
// OBLIVIO — Translation Core (dynamic loading)
// ============================================

const supportedLanguages = ['en', 'de', 'it', 'ko', 'fr', 'ja', 'zh', 'tr'];

const langFullNames = {
    en: 'English', de: 'Deutsch', it: 'Italiano',
    ko: '한국어', fr: 'Français', ja: '日本語', zh: '中文', tr: 'Türkçe'
};

// Cache for loaded translations
const translations = {};
let _currentLang = null;

// Load a language file dynamically, returns a promise
function loadLanguage(lang) {
    if (translations[lang]) return Promise.resolve();
    return new Promise(function(resolve, reject) {
        var script = document.createElement('script');
        script.src = 'js/lang-' + lang + '.js';
        script.onload = function() {
            translations[lang] = window._oblivioLang;
            window._oblivioLang = null;
            resolve();
        };
        script.onerror = function() {
            console.warn('Could not load language file: ' + lang);
            reject();
        };
        document.head.appendChild(script);
    });
}

// Get current language from localStorage or browser
function getCurrentLanguage() {
    if (_currentLang) return _currentLang;
    var saved = localStorage.getItem('oblivio_language');
    if (saved && supportedLanguages.indexOf(saved) !== -1) {
        _currentLang = saved;
        return saved;
    }
    var browserLang = (navigator.language || navigator.userLanguage || 'en').split('-')[0].toLowerCase();
    if (supportedLanguages.indexOf(browserLang) !== -1) {
        _currentLang = browserLang;
        return browserLang;
    }
    _currentLang = 'en';
    return 'en';
}

// Set language: save, load file, apply
function setLanguage(lang) {
    localStorage.setItem('oblivio_language', lang);
    _currentLang = lang;
    loadLanguage(lang).then(function() {
        applyTranslations(lang);
    });
}

// Get a single translation key
function t(key) {
    var lang = getCurrentLanguage();
    var dict = translations[lang] || translations['en'] || {};
    return dict[key] || (translations['en'] && translations['en'][key]) || key;
}

// Apply translations to the page
function applyTranslations(lang) {
    var dict = translations[lang];
    if (!dict) return;

    document.querySelectorAll('[data-i18n]').forEach(function(el) {
        var key = el.getAttribute('data-i18n');
        var val = dict[key];
        if (val) {
            if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
                el.placeholder = val;
            } else {
                el.innerHTML = val;
            }
        }
    });

    // Update dropdown toggles
    document.querySelectorAll('.lang-dropdown-toggle').forEach(function(btn) {
        btn.textContent = langFullNames[lang] || lang.toUpperCase();
    });

    // Update active state in menus
    document.querySelectorAll('.lang-dropdown-menu li').forEach(function(li) {
        li.classList.toggle('active', li.getAttribute('data-lang') === lang);
    });

    // Update page title
    var pageId = document.querySelector('meta[name="page-id"]');
    if (pageId) {
        var titleKey = 'page_title_' + pageId.getAttribute('content');
        if (dict[titleKey]) document.title = dict[titleKey];
    }

    document.documentElement.lang = lang;

    if (localStorage.getItem('oblivio_biographer_language')) {
        localStorage.setItem('oblivio_biographer_language', lang);
    }
}

// ============================================
// Custom Language Dropdown
// ============================================

function injectLangDropdownCSS() {
    var style = document.createElement('style');
    style.textContent =
        '.lang-dropdown{position:relative;display:inline-block}' +
        '.lang-dropdown-toggle{background-color:var(--cream);border:2px solid rgba(26,26,26,.1);padding:8px 28px 8px 12px;font-family:"Crimson Pro",serif;font-size:13px;font-weight:600;cursor:pointer;transition:all .3s;color:var(--moss);border-radius:4px;position:relative;white-space:nowrap}' +
        '.lang-dropdown-toggle::after{content:"\\25BE";position:absolute;right:9px;top:50%;transform:translateY(-50%);font-size:11px}' +
        '.lang-dropdown-toggle:hover{border-color:var(--rust);color:var(--rust)}' +
        '.lang-dropdown-menu{display:none;position:absolute;top:calc(100% + 4px);right:0;background:var(--cream);border:2px solid rgba(26,26,26,.1);border-radius:4px;list-style:none;padding:4px 0;margin:0;min-width:130px;z-index:10001;box-shadow:0 4px 16px rgba(0,0,0,.12)}' +
        '.lang-dropdown-menu.open{display:block}' +
        '.lang-dropdown-menu li{padding:8px 16px;font-family:"Crimson Pro",serif;font-size:14px;cursor:pointer;color:var(--charcoal);transition:background .2s,color .2s;white-space:nowrap}' +
        '.lang-dropdown-menu li:hover{background:rgba(168,72,50,.08);color:var(--rust)}' +
        '.lang-dropdown-menu li.active{color:var(--rust);font-weight:600}' +
        '.mobile-overlay .lang-dropdown{margin-top:16px}' +
        '.mobile-overlay .lang-dropdown-menu{right:auto;left:50%;transform:translateX(-50%);bottom:calc(100% + 4px);top:auto;max-height:60vh;overflow-y:auto}' +
        '.mobile-overlay .lang-dropdown-toggle{font-size:16px;padding:10px 32px 10px 16px}' +
        '.mobile-overlay .lang-dropdown-menu li{font-size:18px;padding:12px 24px}';
    document.head.appendChild(style);
}

function replaceLangSelectors() {
    var currentLang = getCurrentLanguage();

    document.querySelectorAll('.lang-select').forEach(function(sel) {
        var wrapper = document.createElement('div');
        wrapper.className = 'lang-dropdown';

        var toggle = document.createElement('button');
        toggle.className = 'lang-dropdown-toggle';
        toggle.type = 'button';
        toggle.setAttribute('aria-haspopup', 'listbox');
        toggle.setAttribute('aria-expanded', 'false');
        toggle.textContent = langFullNames[currentLang] || currentLang.toUpperCase();

        var menu = document.createElement('ul');
        menu.className = 'lang-dropdown-menu';
        menu.setAttribute('role', 'listbox');

        ['en', 'de', 'fr', 'it', 'tr', 'ko', 'ja', 'zh'].forEach(function(code) {
            var li = document.createElement('li');
            li.setAttribute('role', 'option');
            li.setAttribute('data-lang', code);
            li.textContent = langFullNames[code] || code.toUpperCase();
            if (code === currentLang) li.classList.add('active');
            li.addEventListener('click', function() {
                setLanguage(code);
                document.querySelectorAll('.lang-dropdown-menu').forEach(function(m) { m.classList.remove('open'); });
                document.querySelectorAll('.lang-dropdown-toggle').forEach(function(b) {
                    b.textContent = langFullNames[code];
                    b.setAttribute('aria-expanded', 'false');
                });
            });
            menu.appendChild(li);
        });

        toggle.addEventListener('click', function(e) {
            e.stopPropagation();
            var isOpen = menu.classList.contains('open');
            document.querySelectorAll('.lang-dropdown-menu').forEach(function(m) { m.classList.remove('open'); });
            document.querySelectorAll('.lang-dropdown-toggle').forEach(function(b) { b.setAttribute('aria-expanded', 'false'); });
            if (!isOpen) {
                menu.classList.add('open');
                toggle.setAttribute('aria-expanded', 'true');
            }
        });

        wrapper.appendChild(toggle);
        wrapper.appendChild(menu);
        sel.parentNode.replaceChild(wrapper, sel);
    });

    document.addEventListener('click', function() {
        document.querySelectorAll('.lang-dropdown-menu').forEach(function(m) { m.classList.remove('open'); });
        document.querySelectorAll('.lang-dropdown-toggle').forEach(function(b) { b.setAttribute('aria-expanded', 'false'); });
    });
}

// ============================================
// Init
// ============================================

function initLanguage() {
    injectLangDropdownCSS();
    replaceLangSelectors();
    var lang = getCurrentLanguage();
    loadLanguage(lang).then(function() {
        applyTranslations(lang);
        // Preload all other languages in background
        supportedLanguages.forEach(function(code) {
            if (code !== lang) loadLanguage(code);
        });
    });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initLanguage);
} else {
    initLanguage();
}
