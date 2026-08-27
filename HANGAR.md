# Herald

**Animated tab header/footer plus scheduled title, action bar and chat announcements. For Paper 1.21+ and 26.x.**

---

## Why this plugin exists

[TitleManager](https://www.spigotmc.org/resources/titlemanager.1049/) was the standard for animated tab lists
and scheduled announcements — 1.25 million downloads — and has had no update since 2021.

Herald is a successor in spirit: the same job, a much smaller jar, and built for current Paper.

## Features

- **Animated tab header and footer** — a list of frames that rotates at a configurable interval
- **Scheduled announcements** as chat message, title/subtitle or action bar
- **Built-in placeholders**: `{player}` `{online}` `{max}` `{world}`
- **PlaceholderAPI** supported as a soft dependency — installed, its placeholders work; not installed, nothing breaks
- **MiniMessage and legacy `&` colours**, including gradients
- No hard dependencies, one small jar

## Commands

| Command | What it does |
|---|---|
| `/herald` | Show status (which animations and schedules are active) |
| `/herald reload` | Reload the config |

Permission: `herald.admin` (default: op).

## Compatibility

Built for the Paper API 1.21 and up. Every release is started on a **live Paper 1.21.11 server and a live
Paper 26.2 server** and the actual behaviour is checked — not just "the plugin loads".

## Updates

Fast updates on new Minecraft versions are the reason this plugin exists rather than another abandoned
tab-list plugin.

## Source & licence

MIT licensed, source on [GitHub](https://github.com/arvidhqx-commits/herald).

## Development note

This project is **AI-assisted**: the code is written with Claude under the direction, testing and release
approval of the maintainer. Every release is run against a live Paper server before it ships.
