# Addo

**A task manager built for ADHD brains.** Add → do. That's it.

No ads. No paywall. No account. No cloud. Free forever, offline forever.

## Why

Every task app is either a corporate project-management suite in a trench coat, or a
to-do list wrapped in ads and a subscription. Neither works for an ADHD brain, which
needs exactly three things:

1. **Capture must be instant** — if adding a task takes more than 2 seconds, the thought is gone.
2. **The list must never overwhelm** — a wall of overdue red is a reason to close the app forever.
3. **Starting must be easy** — deciding *what* to do is often harder than doing it.

Addo is designed around those three truths.

## Features

### ⚡ Type it like you'd say it
One input box, always visible. It understands you as you type — live preview chips show
exactly what it parsed, so there are never surprises:

| You type | Addo hears |
|---|---|
| `call mom tomorrow 3pm` | due tomorrow, 3:00 PM, reminder set automatically |
| `gym tonight !!` | due today 8 PM, medium priority |
| `meds at 9am` | 9 AM today — or tomorrow if 9 AM already passed |
| `dentist friday #health` | next Friday, tagged #health |
| `stretch in 30 min` | 30 minutes from now, with reminder |
| `taxes jan 5 !!!` | Jan 5, high priority |
| `buy 2 apples` | just a task — numbers aren't mangled into times |

No date? It lands on **Today**. Capture first, plan never.

### ☀️ Today is the only list that matters
Yesterday's unfinished tasks quietly carry over — labeled *"Carried over — today's a
fresh start"*, not painted alarm-red. The app never guilts you.

### 🎲 Can't decide? Pick for me
One tap picks your next task (weighted toward urgent, but never predictable) and drops
you straight into Focus mode. Decision paralysis, solved by a dice roll.

### 🎯 Focus mode
One task, full screen, nothing else. A visible 10/25/50-minute timer makes time
concrete, and the task's steps are right there so the next action is always tiny.

### 🪜 Steps — make it smaller
Any task can be broken into checkable steps. "Clean the kitchen" is unstartable;
"put 5 dishes in the dishwasher" is not.

### 🎉 Dopamine, engineered
Satisfying check-off animation with haptics, confetti when you clear your day, and a
gentle 🔥 streak for consecutive active days. The streak never shames you for breaking it —
it just starts counting again.

### 🔔 Reminders that respect time blindness
Tasks with a time remind you automatically (that's what the time was for). Notifications
have **Done** and **Snooze 10 min** right in them. Reminders survive reboots.

## Tech

- 100% Kotlin, Jetpack Compose, Material 3 with dynamic color
- Room + DataStore, fully offline, single small APK
- MVVM, no DI framework, no analytics, no network permission at all

## Build

```bash
./gradlew assembleDebug
```

Or grab the APK from the latest [Actions run](../../actions).

## Roadmap

- Repeating tasks (meds, chores, routines)
- Home-screen widget for zero-friction capture
- Backup/export to a file (still no cloud)
- Optional gentle "end of day" review nudge
