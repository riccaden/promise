# Oblivio Frontend Template

Sanitised version of the Oblivio frontend for reuse. The actual production frontend lives separately on Hostpoint — this is the template you can fork and adapt.

## What's Included

| File / Folder | Purpose |
|---|---|
| `index.html` | Landing page |
| `biographer.html` | Biographer interview UI (Pre-Survey + 10 blocks) |
| `legacy.html` | Legacy chat UI (3 variants) |
| `journey.html` | User dashboard (own personas) |
| `signup.html`, `login.html` | Supabase Auth pages |
| `about.html`, `faq.html`, `blog.html`, `pricing.html`, `features.html`, `contact.html` | Marketing pages |
| `privacy.html`, `terms.html`, `security.html` | Legal pages |
| `404.html` | Custom error page |
| `js/translations.js` | i18n engine |
| `js/lang-*.js` (8 files) | Translations for 8 languages |
| `js/biographer-promise.js` | API client for the Biographer |
| `js/legacy-chat.js` | API client for the Legacy chat with visitor context |
| `js/config.js.template` | Config template — **rename to `config.js` and fill in your values** |
| `.htaccess`, `robots.txt`, `sitemap.xml` | Web server config |

## What's NOT Included

For privacy and storage reasons, this template does **not** contain:

- **Images** (`images/`) — including logo, persona avatars, profile photos
- **Audio** (`audio/`) — including ambient background music
- **Real Supabase credentials** — replaced with placeholders
- **Persona data** — all personas were unique to study participants

## Setup Steps

### 1. Rename config template

```bash
mv js/config.js.template js/config.js
```

### 2. Fill in your Supabase credentials

Edit `js/config.js`:
```javascript
PROMISE_API_URL: 'https://your-promise-backend.example.com',
SUPABASE_URL: 'https://your-project.supabase.co',
SUPABASE_ANON_KEY: 'your-anon-key-here',
```

### 3. Update HTML files

Several HTML files contain inline Supabase config (for redundancy). Replace `YOUR_SUPABASE_URL` and `YOUR_SUPABASE_ANON_KEY` in:
- `index.html`
- `signup.html`, `login.html`
- `biographer.html`
- `legacy.html`
- `journey.html`

A simple find-and-replace across the folder works:
```bash
find . -name "*.html" -exec sed -i '' 's|YOUR_SUPABASE_URL|https://your-project.supabase.co|g' {} +
find . -name "*.html" -exec sed -i '' "s|YOUR_SUPABASE_ANON_KEY|your-anon-key-here|g" {} +
```

### 4. Add your images

Create an `images/` folder with at least:
- `logo.png` — site logo
- `og-image.jpg` — social media preview
- `dennis-riccardo.jpg` — author photo on `about.html` (or change/remove the reference)
- `avatars/` — folder with one `.jpg` per persona

### 5. Upload to your hosting

This is a **static site** — no build step required. Just upload all files via FTP/SFTP to your web host.

## Backend Required

This frontend needs the [Oblivio backend](../) running on Railway (or similar) for the Biographer and Legacy chat to work. See the main repo README for backend setup.

---

**Author:** Dennis Riccardo · ZHAW · Bachelor's Thesis 2026
