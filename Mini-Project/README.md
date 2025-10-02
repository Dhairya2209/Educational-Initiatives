# 🏠 Smart Home System – Design Patterns Assignment  

## 📌 Overview  
This project is a **console-based Smart Home Automation System** built in **Java**.  
It demonstrates the application of **Behavioral, Creational, and Structural Design Patterns** while following **OOP and SOLID principles**.  

The system simulates devices such as **Lights, Thermostats, and Door Locks**, allowing operations like **turning devices on/off, scheduling, and automation triggers**.  

---

## 🎯 Problem Statement  
Create a simulation for a **Smart Home System** that allows the user to:  
- Control devices (on/off).  
- Schedule device operations at specific times.  
- Automate tasks based on triggers (e.g., turn off lights if temperature > 75).  
- Dynamically add/remove devices.  

---

## 🛠️ Tech Stack  
- **Language**: Java 17+  
- **IDE**: IntelliJ IDEA / VS Code / Eclipse  
- **Build Tool**: javac  

---

## 📂 Project Structure  
```bash
src/
├── devices/
│   ├── SmartDevice.java        # Abstract base class
│   ├── Light.java              # Light device
│   ├── Thermostat.java         # Thermostat device
│   └── DoorLock.java           # Door lock device
├── patterns/
│   ├── observer/               # Observer Pattern (device updates)
│   ├── factory/                # Factory Method (device creation)
│   └── proxy/                  # Proxy Pattern (access control)
└── SmartHomeMain.java          # Entry point
README.md
