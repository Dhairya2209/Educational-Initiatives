# Design Patterns - TypeScript Demo

This project demonstrates six design pattern use-cases:
- Behavioral: Observer (Notification System), Strategy (Payment Processor)
- Creational: Factory Method (Document Parser), Builder (UserProfile Builder)
- Structural: Adapter (Legacy API Adapter), Decorator (Service Decorator for logging)

Features:
- CLI-driven (uses `inquirer`) — event-driven input (no hard-coded while(true) flags).
- Logging using `winston`.
- Defensive programming and validations.
- Simple retry wrapper for transient errors.

How to run:
1. `npm install`
2. `npm run build`
3. `npm start`

Or for development:
- `npm run dev` (requires ts-node)

The final deliverable should be uploaded to GitHub. To push this repo:

```
git init
git add .
git commit -m "Initial commit - design patterns demo"
gh repo create your-username/design-patterns-ts --public --source=. --remote=origin
git push -u origin main
```

Walkthrough:
- `src/index.ts` contains CLI and wiring where you can invoke each use-case interactively.
