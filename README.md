# Design Patterns - TypeScript 

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

