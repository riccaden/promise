# Supabase Setup Guide for Oblivio

This guide will help you set up Supabase authentication for your Oblivio website.

## Step 1: Create a Supabase Account

1. Go to [supabase.com](https://supabase.com)
2. Click **"Start your project"**
3. Sign up with GitHub (recommended) or Email

## Step 2: Create a New Project

1. Click **"New Project"**
2. Fill in the details:
   - **Name**: Oblivio
   - **Database Password**: Create a strong password (save this!)
   - **Region**: Choose closest to you
   - **Plan**: Free (perfect for your needs)
3. Click **"Create new project"**
4. Wait ~2 minutes for setup to complete

## Step 3: Enable Email Authentication

1. In your project dashboard, go to **Authentication** (left sidebar)
2. Click **"Providers"**
3. Make sure **"Email"** is enabled (it should be by default)
4. Configure email settings:
   - **Enable email confirmations**: You can disable this for testing
   - **Enable sign ups**: Keep this enabled

## Step 4: Get Your API Credentials

1. Go to **Settings** → **API** (left sidebar)
2. You'll see two important values:
   - **Project URL** (looks like: `https://xxxxx.supabase.co`)
   - **anon public key** (long string)
3. **COPY THESE VALUES** - you'll need them in the next step!

## Step 5: Configure Your Website

You need to add your Supabase credentials to **THREE files**:

### 1. `login.html`
Open `login.html` and find these lines (around line 325):
```javascript
const SUPABASE_URL = 'YOUR_SUPABASE_URL_HERE';
const SUPABASE_ANON_KEY = 'YOUR_SUPABASE_ANON_KEY_HERE';
```

Replace with your actual values:
```javascript
const SUPABASE_URL = 'https://xxxxx.supabase.co';
const SUPABASE_ANON_KEY = 'your-long-anon-key-here';
```

### 2. `signup.html`
Open `signup.html` and find the same lines (around line 349):
```javascript
const SUPABASE_URL = 'YOUR_SUPABASE_URL_HERE';
const SUPABASE_ANON_KEY = 'YOUR_SUPABASE_ANON_KEY_HERE';
```

Replace with your actual values (same as above).

### 3. `journey.html`
Open `journey.html` and find the same lines (around line 362):
```javascript
const SUPABASE_URL = 'YOUR_SUPABASE_URL_HERE';
const SUPABASE_ANON_KEY = 'YOUR_SUPABASE_ANON_KEY_HERE';
```

Replace with your actual values (same as above).

## Step 6: Test Your Setup

1. Open your website in a browser
2. Go to the signup page (`signup.html`)
3. Try creating a test account
4. Check if you can login

### Verify in Supabase Dashboard:
1. Go to **Authentication** → **Users** in your Supabase dashboard
2. You should see your test user listed there!

## Step 7: Optional Configuration

### Disable Email Confirmation (for testing)
If you want to skip email confirmation during development:
1. Go to **Authentication** → **Providers**
2. Click on **Email**
3. Disable **"Confirm email"**
4. Save changes

### Add a Custom Email Template
1. Go to **Authentication** → **Email Templates**
2. Customize the confirmation email if needed

## Troubleshooting

### "Invalid API credentials" error
- Double-check that you copied the correct URL and anon key
- Make sure there are no extra spaces or quotes
- The URL should start with `https://`

### "Email rate limit exceeded"
- Supabase free tier has rate limits
- Wait a few minutes and try again
- Or disable email confirmation (see Step 7)

### User not appearing in dashboard
- Check the email confirmation setting
- Look in the spam folder for the confirmation email

## Next Steps

Once authentication is working:
- You can proceed to build the `biographer.html` page
- You can build the `chat.html` page
- Both pages can use `supabase.auth.getUser()` to get the logged-in user

## Security Notes

- ✅ The anon key is safe to use in frontend code
- ✅ Supabase handles all password hashing and security
- ✅ Never commit your Supabase credentials to Git (optional: use environment variables)
- ✅ For production, consider enabling Row Level Security (RLS) in Supabase

## Need Help?

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Auth Guide](https://supabase.com/docs/guides/auth)
- [Discord Community](https://discord.supabase.com)

---

**Your Oblivio authentication system is now ready! 🎉**
