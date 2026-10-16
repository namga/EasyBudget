# EasyBudget v1.0.0

Privacy-first personal finance app with manual income/expense tracking, monthly budgets, savings goals, category breakdowns, weekly/monthly analytics, daily motivational quotes, friendly spending nudges, theme customization, and password-encrypted backup/import.

## Included
- Responsive Vietnamese/English PWA with 5 color themes and 5 decorative backgrounds
- Income/expense entry and category chart
- Weekly and monthly totals, income-vs-expense bar chart, category spending breakdown
- Savings goals with contributions
- Daily quote and context-aware budget reminder text
- AES-256-GCM encrypted export/import using PBKDF2-SHA-256
- Android wrapper with daily system notification reminder (user enables it in settings and grants notification permission)
- Android launcher icon and GitHub Actions debug APK workflow

## Version constraint
This build intentionally remains **versionName 1.0.0 / versionCode 1**. No version bump was made.

## Web/PWA
Serve `web/` through HTTPS or localhost for PWA installation and browser crypto APIs. Deploy the `web/` directory to Netlify or GitHub Pages.

## Android APK
1. Upload all repository files to GitHub.
2. Open Actions → **Build Android debug APK** → Run workflow.
3. Download artifact `easybudget-debug-apk`.
4. Install the debug APK on a test device.

Android reminders use an inexact daily system alarm, so Android may deliver them a little later to conserve battery. On Android 13+, allow notification permission. The reminder is enabled in the app settings and scheduled at the selected time. If the device manufacturer aggressively restricts background alarms, configure battery settings for reliable delivery.

## Privacy
Data is stored locally. No bank connection or server sync is included. Encrypted backup cannot be recovered if its password is lost. Test export/import on your target device before relying on a backup.
