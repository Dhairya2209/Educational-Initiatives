# Design Patterns Demo in TypeScript

This project demonstrates **six different design pattern use-cases** implemented in **TypeScript**, following best practices including modular code structure, logging, defensive programming, validations, and CLI-driven input.  

The project is designed to run interactively and can handle long-running inputs without hard-coded loops.

---

## 🧩 Use Cases

### 1. **Observer Pattern** – Behavioral
- **Use Case:** Notification System
- **Description:** Implements a publish-subscribe mechanism. Subscribers register to topics and get notified when events are published.
- **Class/Files:** `NotificationCenter.ts`
- **Example:** News notifications or event listeners.

### 2. **Strategy Pattern** – Behavioral
- **Use Case:** Payment Processor
- **Description:** Allows selecting different payment strategies (Stripe, PayPal) at runtime without modifying context logic.
- **Class/Files:** `Payment.ts`
- **Example:** Switching between different payment gateways dynamically.

### 3. **Factory Method Pattern** – Creational
- **Use Case:** Document Parser
- **Description:** Creates parsers (`JsonParser`, `TextParser`) based on input type, encapsulating object creation logic.
- **Class/Files:** `DocumentFactory.ts`, `parsers.ts`
- **Example:** Parsing multiple document types without coupling to concrete classes.

### 4. **Builder Pattern** – Creational
- **Use Case:** UserProfile Builder
- **Description:** Constructs complex objects (`UserProfile`) step-by-step with validations.
- **Class/Files:** `UserProfileBuilder.ts`
- **Example:** Building a user profile with optional and mandatory fields.

### 5. **Adapter Pattern** – Structural
- **Use Case:** Legacy API Adapter
- **Description:** Adapts legacy callback-style API to modern async/await usage, converting data structure as needed.
- **Class/Files:** `LegacyApiAdapter.ts`
- **Example:** Wrapping an old system to fit new interfaces.

### 6. **Decorator Pattern** – Structural
- **Use Case:** Service Logging Decorator
- **Description:** Wraps service methods with additional logging behavior dynamically without altering original service logic.
- **Class/Files:** `ServiceDecorator.ts`
- **Example:** Adding logging, metrics, or authentication checks dynamically.

---

## ⚡ Features

- **Interactive CLI:** Uses `inquirer` for selecting and running use cases.
- **Logging:** `winston` used for structured logging to console and file.
- **Error Handling:** Defensive programming, retry mechanisms, and validations at all levels.
- **No Hardcoding Loops:** CLI-driven user interactions; no `while(true)` loops.
- **Modular Structure:** Each pattern implemented in its own folder and file.

