# Setup guide

Step-by-step to get RepoPulse onto GitHub and keep your activity green with a meaningful daily commit.

## 1. Create the repository on GitHub

1. Go to <https://github.com/new>.
2. Name it `repopulse` (or anything you like).
3. Leave it **empty** — no README/.gitignore/license (this project already has them).
4. Click **Create repository**.

## 2. Push this project

From the project folder on your machine:

```bash
git init
git add .
git commit -m "feat: initial RepoPulse - Java repo health scanner"
git branch -M main
git remote add origin https://github.com/<your-username>/repopulse.git
git push -u origin main
```

Replace `<your-username>` with your GitHub username.

## 3. Make the daily commit count toward YOUR contribution graph

This is the part most people get wrong. GitHub only colours a day green if the commit's **author email is a verified email on your account**. A commit authored by the generic `github-actions[bot]` will **not** show on your graph.

So tell the workflow which identity to commit as:

1. In your repo, go to **Settings -> Secrets and variables -> Actions -> Variables** tab.
2. Click **New repository variable** and add:
   - `PULSE_GIT_EMAIL` = an email verified on your GitHub account
     (e.g. `redeemgrimm@gmail.com`, or your GitHub `noreply` address found at
     <https://github.com/settings/emails>).
   - `PULSE_GIT_NAME` = your name or GitHub username (optional; defaults to the repo owner).
3. Confirm that email is listed and verified under **Settings -> Emails**.

> Tip: the no-reply form `<ID>+<username>@users.noreply.github.com` always counts and avoids exposing your real email.

## 4. Allow Actions to push commits

1. Go to **Settings -> Actions -> General**.
2. Under **Workflow permissions**, select **Read and write permissions**.
3. Save.

(The workflow also declares `permissions: contents: write`, but this repo setting must allow it.)

## 5. Enable and test the workflow

1. Open the **Actions** tab. If prompted, click **I understand my workflows, enable them**.
2. Select **Daily Pulse** in the left sidebar.
3. Click **Run workflow** (the `workflow_dispatch` trigger) to test it immediately.
4. After it finishes, check that a new commit `chore: daily repo health report (...)` appears, authored by you, and that `REPORT.md` updated.

From then on it runs automatically at **06:14 UTC every day**.

## 6. Change the schedule (optional)

Edit `.github/workflows/daily-pulse.yml`, the `cron` line:

```yaml
- cron: '14 6 * * *'   # min hour day month weekday (UTC)
```

Examples: `30 13 * * *` = 13:30 UTC daily; `0 */6 * * *` = every 6 hours.
Cron in GitHub Actions is always UTC, and scheduled runs can be delayed several minutes under load — that is normal.

## Important honest caveats

- **Scheduled workflows pause after 60 days of repo inactivity.** If you never push manually, GitHub disables the cron. A daily self-commit usually keeps it alive, but if it stops, push any commit or re-enable it in the Actions tab.
- **A green graph from automated commits is shallow signal.** Anyone who clicks into the commits sees they are bot-generated, identical-shaped daily reports. Treat this as a fun automation and a real, working tool — not as a substitute for actual contributions. The strongest use is to keep building features on RepoPulse (see the Roadmap in `README.md`); those commits are real and also keep you green.
- **Private repos** count toward your graph only if you enable *Settings -> Profile -> Include private contributions*.

## Build locally (optional)

Requires JDK 17+ and Maven:

```bash
mvn -B verify          # compile + run tests
mvn -B package         # build target/repopulse.jar
java -jar target/repopulse.jar .
```
