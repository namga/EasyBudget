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
3. Download the versioned artifact, for example `EasyBudget-v1.0.0-code1-debug`. The APK file inside is named `EasyBudget-v1.0.0-code1-debug.apk`.
4. Install the debug APK on a test device.

Android reminders use an inexact daily system alarm, so Android may deliver them a little later to conserve battery. On Android 13+, allow notification permission. The reminder is enabled in the app settings and scheduled at the selected time. If the device manufacturer aggressively restricts background alarms, configure battery settings for reliable delivery.

Android encrypted backup uses the system Save/Open document picker. The Android WebView loads app assets through AndroidX WebViewAssetLoader on a secure appassets origin so Web Crypto APIs can be used. Web/PWA uses browser download/upload.

## Privacy
Data is stored locally. No bank connection or server sync is included. Encrypted backup cannot be recovered if its password is lost. Test export/import on your target device before relying on a backup.


## Calendar cash flow and app password
- Bottom navigation now uses **Lịch** in place of the old customization tab. Theme and wallpaper options remain under Settings (gear icon).
- Calendar shows each date's recorded income, expenses, net amount, and the selected day's transaction list, with month navigation.
- Optional app login password is configured in Settings. The password itself is never stored; only a PBKDF2-SHA-256 hash and random salt are saved locally. Lock screen is shown when the app launches if password protection is enabled.
- This is a local app lock, not an online account. Keep an encrypted backup. Test password setup/unlock and calendar interactions in the generated APK before relying on them.
- Version remains **versionCode 1 / versionName 1.0.0**.


## Swipe-back navigation
- Swiping right closes the open modal first, otherwise returns to the previous in-app tab; from any non-home tab it returns to Home. A right swipe on Home does not exit the app.
- Android system Back is routed through the same in-app navigation handler before the app can close.

## Smart budget allocation
- After saving a monthly allocation, reopen it at any time from Home → Monthly budget → **Xem phân bổ đã lưu**. The button appears when the selected month has a saved plan; use the month picker to view another month.
- Enter a monthly spending cap, then get category limits based on up to three prior months of expense transactions.
- Recommendation blends 75% of the user's category spending mix with 25% starter weights to reduce the effect of unusual one-off categories; when history is absent it uses starter weights.
- Amounts are editable and can be saved as a monthly category plan. The UI links to CFPB and NerdWallet budgeting guidance. The web recommendations are reference-based and calculated locally; the app does not upload transaction history to a recommendation server.
- The 50/30/20 guideline is identified as a take-home-income rule of thumb, not a mandatory category split for every spending budget.

Version stays at **versionCode 1 / versionName 1.0.0**.


### Cải tiến nhập số tiền (bản sửa UI)
- Các ô nhập tiền hiển thị dấu phân cách hàng nghìn theo định dạng Việt Nam, ví dụ `10.000.000`.
- Thay kiểm tra `step` mặc định của trình duyệt bằng kiểm tra trong ứng dụng với thông báo tiếng Việt.
- Áp dụng cho ngân sách tháng, phân bổ theo danh mục, giao dịch và mục tiêu tiết kiệm.
- Giữ nguyên `versionCode=1`, `versionName=1.0.0`; chỉ tăng mã cache PWA để thiết bị tải giao diện đã cập nhật.
