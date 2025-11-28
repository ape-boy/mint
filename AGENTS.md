# Repository Guidelines

## Project Structure & Module Organization
- `src/` holds product code: `api/` Axios clients, `components/` shared UI, `composables/` `use*` utilities, `layouts/AppLayout.vue` for the shell, `views/` routed pages, `router/index.ts`, `stores/` Pinia state, `styles/`, `types/`, and assets.
- `public/` serves static files; `mock-server/db.json` feeds the json-server mock API; `tests/e2e/*.sh` contains bash E2E checks; `dist/` is generated build output.

## Build, Test, and Development Commands
- Install once: `npm install`.
- `npm run dev` starts Vite on port 5173 with `/api` proxied to `http://localhost:3001`; `npm run mock` launches json-server; `npm run dev:all` runs both together.
- `npm run build` runs `vue-tsc -b` type checks then Vite build; `npm run preview` serves the built `dist/`.
- `npm run test:e2e` executes `tests/e2e/api.test.sh` and `scenarios.test.sh`; requires bash + curl and a running API (via `npm run mock` or `API_URL` override).

## Coding Style & Naming Conventions
- Vue 3 + TypeScript + Pinia + Ant Design Vue using `<script setup>`; prefer typed stores and components.
- 2-space indentation, single quotes, trailing commas where they help diffs, and the existing no-semicolon style.
- Use the `@/` alias for imports; PascalCase `.vue` components; composables named `useX`; Pinia stores exported as `useXStore` in `src/stores`; shared models live in `src/types`.

## Testing Guidelines
- Keep E2E scripts executable, idempotent, and fail-fast; mirror existing `curl -s ... | grep` assertions when adding endpoints.
- Start the mock API (`npm run mock`) before tests or point `API_URL` at another base; keep mock IDs within the ranges documented in `tests/e2e/README.md`.
- For UI flows, prefer hitting proxied `/api` endpoints through the dev server to mirror production routing.

## Commit & Pull Request Guidelines
- Follow the Conventional Commit-style history (`feat:`, `refactor:`, `chore:`...) with concise, imperative subjects.
- PRs should describe scope, list verification (`npm run build`, `npm run test:e2e`), and attach screenshots or GIFs for UI changes.
- Note any changes to `mock-server/db.json` or API contracts and link related issues or tickets.
