// Captures screenshots of every screen of the running app (used for the README and the project report).
//
//   npm install playwright && npx playwright install chromium
//   node scripts/screenshots.js [base_url] [output_dir]
//
// The app must be running (mvn jetty:run -Pdemo) with the seeded admin account.
const { chromium } = require('playwright');

const BASE = (process.argv[2] || 'http://localhost:8080').replace(/\/$/, '');
const OUT = process.argv[3] || 'docs/screenshots';
const STUDENT = { name: 'Siddharth Singh', email: '24bcs12906@example.edu', password: 'Secret@123' };
const ADMIN = { email: 'admin@college.edu', password: 'admin123' };

const fs = require('fs');
fs.mkdirSync(OUT, { recursive: true });

(async () => {
    const browser = await chromium.launch();
    const ctx = await browser.newContext({ viewport: { width: 1280, height: 800 } });
    const page = await ctx.newPage();
    const settle = (ms = 1600) => page.waitForTimeout(ms); // let CSS animations and count-ups finish
    const shot = async (name, opts = {}) => {
        await settle(opts.wait);
        await page.screenshot({ path: `${OUT}/${name}.png`, fullPage: opts.full !== false });
        console.log('captured', name);
    };
    const login = async (u) => {
        await page.goto(BASE + '/login');
        await page.fill('#email', u.email);
        await page.fill('#password', u.password);
        await page.click('button[type=submit]');
        await page.waitForLoadState('networkidle');
    };
    const logout = async () => {
        await page.click('header button:has-text("Log out")');
        await page.waitForLoadState('networkidle');
    };

    // 1. Home
    await page.goto(BASE + '/');
    await shot('01-home');

    // 2. Register
    await page.goto(BASE + '/register');
    await page.fill('#name', STUDENT.name);
    await page.fill('#email', STUDENT.email);
    await page.fill('#password', STUDENT.password);
    await page.fill('#confirm', STUDENT.password);
    await shot('02-register', { wait: 700 });
    await page.click('button[type=submit]');
    await page.waitForLoadState('networkidle');

    // 3. Login (with the "account created" message)
    await page.fill('#email', STUDENT.email);
    await page.fill('#password', STUDENT.password);
    await shot('03-login', { wait: 700 });
    await page.click('button[type=submit]');
    await page.waitForLoadState('networkidle');

    // 4. Course catalogue
    await shot('04-courses');

    // 5. Search
    await page.fill('input[name=q]', 'data');
    await page.click('.search button[type=submit]');
    await page.waitForLoadState('networkidle');
    await shot('05-search');
    await page.goto(BASE + '/student/courses');

    // 6. Enrol in three courses
    for (let i = 0; i < 3; i++) {
        await page.locator('button:has-text("Enroll")').first().click();
        await page.waitForLoadState('networkidle');
        if (i === 0) { await shot('06-enrolled-message', { wait: 700 }); }
    }
    await shot('07-courses-after-enrol');

    // 7. My courses
    await page.goto(BASE + '/student/my-courses');
    await shot('08-my-courses');

    // 8. Student blocked from admin pages
    await page.goto(BASE + '/admin/courses');
    await shot('09-access-denied', { wait: 700 });
    await page.goto(BASE + '/student/my-courses');
    await logout();

    // 9. Admin
    await login(ADMIN);
    await shot('10-admin-dashboard');
    await page.goto(BASE + '/admin/course-form');
    await page.fill('#code', 'CS450');
    await page.fill('#credits', '4');
    await page.fill('#seats', '30');
    await page.fill('#title', 'Machine Learning');
    await page.fill('#instructor', 'Dr. Meera Nair');
    await page.fill('#description', 'Supervised and unsupervised learning, model evaluation and basic neural networks.');
    await shot('11-admin-add-course', { wait: 700 });
    await page.click('button[type=submit]');
    await page.waitForLoadState('networkidle');
    await shot('12-admin-course-added');

    // 10. Students enrolled in the first course
    const link = page.locator('a:has-text("Students")').first();
    await link.click();
    await page.waitForLoadState('networkidle');
    await shot('13-admin-enrolled-students');

    // 11. Mobile layout
    await logout();
    const mobile = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2 });
    const mp = await mobile.newPage();
    await mp.goto(BASE + '/login');
    await mp.fill('#email', STUDENT.email);
    await mp.fill('#password', STUDENT.password);
    await mp.click('button[type=submit]');
    await mp.waitForLoadState('networkidle');
    await mp.waitForTimeout(1800);
    await mp.screenshot({ path: `${OUT}/14-mobile-courses.png`, fullPage: false });
    console.log('captured 14-mobile-courses');

    await browser.close();
})().catch((e) => { console.error(e); process.exit(1); });
