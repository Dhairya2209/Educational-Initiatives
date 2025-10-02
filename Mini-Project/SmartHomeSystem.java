import java.util.*;
import java.util.function.Predicate;

/*
 SmartHomeSystem.java
 Console-based Smart Home System simulation.
 Java 8+ compatible.

 Design highlights:
 - Observer pattern: Hub manages observers (devices) and notifies on system events.
 - Factory Method: DeviceFactory creates concrete devices.
 - Proxy pattern: DeviceProxy wraps devices to control access/logging.
 - Scheduler: stores schedules, executed by explicit command runSchedulesAt HH:MM.
 - Triggers: evaluated when device state changes (e.g., thermostat temperature).
*/

// -------------------- Device abstraction --------------------
interface Device {
    int getId();
    String getType();
    String statusReport();
}

abstract class AbstractDevice implements Device {
    protected final int id;
    protected final String type;

    protected AbstractDevice(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public int getId() { return id; }
    public String getType() { return type; }

    // Hook for device-specific commands:
    public abstract void handleCommand(String command, String... args) throws Exception;
}

// -------------------- Concrete devices --------------------
class Light extends AbstractDevice {
    private boolean isOn = false;

    public Light(int id) { super(id, "light"); }

    public boolean isOn() { return isOn; }

    @Override
    public void handleCommand(String command, String... args) {
        switch (command.toLowerCase()) {
            case "turnon": isOn = true; break;
            case "turnoff": isOn = false; break;
            default: throw new IllegalArgumentException("Unsupported command for Light: " + command);
        }
    }

    @Override
    public String statusReport() {
        return String.format("Light %d is %s", id, isOn ? "On" : "Off");
    }
}

class Thermostat extends AbstractDevice {
    private double temperature;

    public Thermostat(int id, double initialTemp) {
        super(id, "thermostat");
        this.temperature = initialTemp;
    }

    public double getTemperature() { return temperature; }

    @Override
    public void handleCommand(String command, String... args) {
        switch (command.toLowerCase()) {
            case "settemp":
                if (args.length < 1) throw new IllegalArgumentException("setTemp requires a temperature argument");
                temperature = Double.parseDouble(args[0]);
                break;
            default: throw new IllegalArgumentException("Unsupported command for Thermostat: " + command);
        }
    }

    @Override
    public String statusReport() {
        return String.format("Thermostat %d is set to %.1f degrees", id, temperature);
    }
}

class DoorLock extends AbstractDevice {
    private boolean locked = true;

    public DoorLock(int id) { super(id, "door"); }

    public boolean isLocked() { return locked; }

    @Override
    public void handleCommand(String command, String... args) {
        switch (command.toLowerCase()) {
            case "lock": locked = true; break;
            case "unlock": locked = false; break;
            default: throw new IllegalArgumentException("Unsupported command for DoorLock: " + command);
        }
    }

    @Override
    public String statusReport() {
        return String.format("Door %d is %s", id, locked ? "Locked" : "Unlocked");
    }
}

// -------------------- Factory Method --------------------
class DeviceFactory {
    public static Device createDevice(Map<String, String> props) {
        int id = Integer.parseInt(props.get("id"));
        String type = props.get("type").toLowerCase();
        switch (type) {
            case "light":
                return new Light(id);
            case "thermostat": {
                double temp = props.containsKey("temperature") ? Double.parseDouble(props.get("temperature")) : 70.0;
                return new Thermostat(id, temp);
            }
            case "door":
            case "doorlock":
                return new DoorLock(id);
            default:
                throw new IllegalArgumentException("Unknown device type: " + props.get("type"));
        }
    }
}

// -------------------- Proxy Pattern --------------------
class DeviceProxy implements Device {
    private final Device realDevice;
    private final Set<String> allowedActions; // simple access control

    public DeviceProxy(Device realDevice) {
        this.realDevice = realDevice;
        // default allow all for simplicity; could be customized
        this.allowedActions = new HashSet<>(Arrays.asList("turnon","turnoff","settemp","lock","unlock"));
    }

    public void setAllowedActions(Set<String> actions) {
        allowedActions.clear();
        allowedActions.addAll(actions);
    }

    public void execute(String command, String... args) throws Exception {
        // Basic permission check:
        String cmd = command.toLowerCase();
        if (!allowedActions.contains(cmd)) {
            throw new SecurityException("Action not allowed: " + cmd);
        }
        // Logging (simple)
        System.out.printf("[Proxy] Executing %s on Device %d (%s)%n", command, realDevice.getId(), realDevice.getType());
        if (realDevice instanceof AbstractDevice) {
            ((AbstractDevice) realDevice).handleCommand(command, args);
        } else {
            throw new UnsupportedOperationException("Cannot handle command on this device");
        }
    }

    @Override
    public int getId() { return realDevice.getId(); }
    @Override
    public String getType() { return realDevice.getType(); }
    @Override
    public String statusReport() { return realDevice.statusReport(); }
}

// -------------------- Observer Pattern: Hub & Event --------------------
interface HubObserver {
    void onHubEvent(HubEvent event);
}

class HubEvent {
    public final String type; // e.g., "TRIGGER_FIRED", "SCHEDULE_EXECUTED", "STATE_CHANGE"
    public final Map<String, Object> payload;

    public HubEvent(String type) {
        this(type, new HashMap<>());
    }
    public HubEvent(String type, Map<String, Object> payload) {
        this.type = type;
        this.payload = payload;
    }
}

class SmartHub {
    private final Map<Integer, DeviceProxy> devices = new HashMap<>();
    private final List<HubObserver> observers = new ArrayList<>();

    // Scheduling and triggers:
    private final List<ScheduleEntry> schedules = new ArrayList<>();
    private final List<TriggerEntry> triggers = new ArrayList<>();

    // Register/unregister observers (devices can observe hub or other observers)
    public void addObserver(HubObserver o) { observers.add(o); }
    public void removeObserver(HubObserver o) { observers.remove(o); }

    // Device management
    public void registerDevice(DeviceProxy proxy) {
        devices.put(proxy.getId(), proxy);
        notifyAllObservers(new HubEvent("DEVICE_REGISTERED", Map.of("deviceId", proxy.getId())));
    }

    public DeviceProxy unregisterDevice(int id) {
        DeviceProxy p = devices.remove(id);
        if (p != null) notifyAllObservers(new HubEvent("DEVICE_UNREGISTERED", Map.of("deviceId", id)));
        return p;
    }

    public Optional<DeviceProxy> getDevice(int id) { return Optional.ofNullable(devices.get(id)); }

    public Collection<DeviceProxy> listDevices() { return devices.values(); }

    /* Scheduling API */
    public void addSchedule(ScheduleEntry s) {
        schedules.add(s);
        notifyAllObservers(new HubEvent("SCHEDULE_ADDED", Map.of("schedule", s)));
    }

    public List<ScheduleEntry> listSchedules() { return Collections.unmodifiableList(schedules); }

    /* Triggers API */
    public void addTrigger(TriggerEntry t) {
        triggers.add(t);
        notifyAllObservers(new HubEvent("TRIGGER_ADDED", Map.of("trigger", t)));
    }

    public List<TriggerEntry> listTriggers() { return Collections.unmodifiableList(triggers); }

    /* Execute a command via proxy with safety & trigger evaluation */
    public void executeCommand(int deviceId, String command, String... args) throws Exception {
        DeviceProxy p = devices.get(deviceId);
        if (p == null) throw new NoSuchElementException("Device not found: " + deviceId);
        p.execute(command, args);

        // After executing, notify observers of state change
        notifyAllObservers(new HubEvent("STATE_CHANGE", Map.of("deviceId", deviceId, "command", command)));

        // Evaluate triggers (simple immediate evaluation)
        evaluateTriggers();
    }

    private void evaluateTriggers() {
        for (TriggerEntry t : triggers) {
            try {
                if (t.evaluate(this)) {
                    // trigger action(s):
                    for (String action : t.getActions()) {
                        // Action format: like turnOff(1) or setTemp(2, 68)
                        parseAndExecuteAction(action);
                    }
                    notifyAllObservers(new HubEvent("TRIGGER_FIRED", Map.of("trigger", t)));
                }
            } catch (Exception ex) {
                System.err.println("[Hub] Trigger evaluation error: " + ex.getMessage());
            }
        }
    }

    private void parseAndExecuteAction(String action) {
        // Very lightweight parser: name(args)
        action = action.trim();
        if (!action.contains("(") || !action.endsWith(")")) {
            System.err.println("[Hub] Invalid action format: " + action);
            return;
        }
        String cmdName = action.substring(0, action.indexOf('(')).trim();
        String argPart = action.substring(action.indexOf('(') + 1, action.length() - 1).trim();
        String[] parts = argPart.isEmpty() ? new String[0] : argPart.split("\\s*,\\s*");
        try {
            if (parts.length >= 1) {
                int deviceId = Integer.parseInt(parts[0]);
                String[] extraArgs = Arrays.copyOfRange(parts, 1, parts.length);
                executeCommand(deviceId, cmdName.toLowerCase(), extraArgs);
            } else {
                System.err.println("[Hub] Action missing device id: " + action);
            }
        } catch (Exception e) {
            System.err.println("[Hub] Action execution failed: " + e.getMessage());
        }
    }

    /* Scheduler execution at a given HH:MM (simulate) */
    public void runSchedulesAt(String timeHHMM) {
        List<ScheduleEntry> toRun = new ArrayList<>();
        for (ScheduleEntry s : schedules) {
            if (s.time.equals(timeHHMM)) toRun.add(s);
        }
        if (toRun.isEmpty()) {
            System.out.println("[Hub] No scheduled tasks at " + timeHHMM);
            return;
        }
        for (ScheduleEntry s : toRun) {
            System.out.printf("[Hub] Running schedule: device=%d time=%s action=%s%n", s.deviceId, s.time, s.action);
            parseAndExecuteAction(s.action);
        }
        notifyAllObservers(new HubEvent("SCHEDULES_EXECUTED", Map.of("time", timeHHMM, "count", toRun.size())));
    }

    private void notifyAllObservers(HubEvent event) {
        for (HubObserver o : observers) {
            try { o.onHubEvent(event); }
            catch (Exception e) { System.err.println("[Hub] observer error: " + e.getMessage()); }
        }
    }
}

// -------------------- Schedule and Trigger data structures --------------------
class ScheduleEntry {
    public final int deviceId;
    public final String time; // "HH:MM"
    public final String action; // e.g., "turnOn(1)"

    public ScheduleEntry(int deviceId, String time, String action) {
        this.deviceId = deviceId;
        this.time = time;
        this.action = action;
    }

    @Override
    public String toString() {
        return String.format("{device:%d, time:%s, action:%s}", deviceId, time, action);
    }
}

class TriggerEntry {
    // For simplicity, support triggers of thermostat temperature condition or general predicate
    private final String conditionDesc;
    private final Predicate<SmartHub> predicate;
    private final List<String> actions;

    public TriggerEntry(String conditionDesc, Predicate<SmartHub> predicate, List<String> actions) {
        this.conditionDesc = conditionDesc;
        this.predicate = predicate;
        this.actions = actions;
    }

    public boolean evaluate(SmartHub hub) {
        return predicate.test(hub);
    }

    public List<String> getActions() { return actions; }

    @Override
    public String toString() {
        return String.format("{condition:%s, actions:%s}", conditionDesc, actions);
    }
}

// -------------------- Simple console UI & main --------------------
public class SmartHomeSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private final SmartHub hub = new SmartHub();

    public SmartHomeSystem() {
        // hub observes itself? not necessary. but devices can act as observers if needed.
        // For logging, add a simple observer:
        hub.addObserver(event -> {
            // Basic logging; keep short:
            System.out.printf("[Observer] Event: %s %s%n", event.type, event.payload.isEmpty() ? "" : event.payload);
        });
    }

    private void seedDefaults() {
        // create devices with factory and register proxies
        Map<String,String> d1 = Map.of("id","1","type","light");
        Map<String,String> d2 = Map.of("id","2","type","thermostat","temperature","70");
        Map<String,String> d3 = Map.of("id","3","type","door");
        Device dev1 = DeviceFactory.createDevice(d1);
        Device dev2 = DeviceFactory.createDevice(Map.of("id","2","type","thermostat","temperature","70"));
        Device dev3 = DeviceFactory.createDevice(d3);

        DeviceProxy p1 = new DeviceProxy(dev1);
        DeviceProxy p2 = new DeviceProxy(dev2);
        DeviceProxy p3 = new DeviceProxy(dev3);

        hub.registerDevice(p1);
        hub.registerDevice(p2);
        hub.registerDevice(p3);
    }

    private void printHelp() {
        System.out.println("Commands (examples):");
        System.out.println("  listDevices");
        System.out.println("  statusReport");
        System.out.println("  turnOn <id>");
        System.out.println("  turnOff <id>");
        System.out.println("  lock <id>");
        System.out.println("  unlock <id>");
        System.out.println("  setTemp <id> <temp>");
        System.out.println("  setSchedule <deviceId> <HH:MM> <action>   (action e.g. turnOn(1))");
        System.out.println("  runSchedulesAt <HH:MM>");
        System.out.println("  addTrigger temperature > 75 action turnOff(1)   (supported simple format)");
        System.out.println("  addDevice id type [temperature]");
        System.out.println("  removeDevice <id>");
        System.out.println("  showSchedules");
        System.out.println("  showTriggers");
        System.out.println("  help");
        System.out.println("  exit");
    }

    private void repl() {
        seedDefaults();
        System.out.println("Smart Home System started. Type 'help' for commands.");
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            try {
                if (line.equalsIgnoreCase("exit")) { System.out.println("Bye."); break; }
                if (line.equalsIgnoreCase("help")) { printHelp(); continue; }
                if (line.equalsIgnoreCase("listDevices")) {
                    hub.listDevices().forEach(d -> System.out.println(d.statusReport()));
                    continue;
                }
                if (line.equalsIgnoreCase("statusReport")) {
                    hub.listDevices().forEach(d -> System.out.println(d.statusReport()));
                    continue;
                }
                if (line.startsWith("turnOn ")) {
                    int id = Integer.parseInt(line.split("\\s+")[1]);
                    hub.executeCommand(id, "turnOn");
                    continue;
                }
                if (line.startsWith("turnOff ")) {
                    int id = Integer.parseInt(line.split("\\s+")[1]);
                    hub.executeCommand(id, "turnOff");
                    continue;
                }
                if (line.startsWith("lock ")) {
                    int id = Integer.parseInt(line.split("\\s+")[1]);
                    hub.executeCommand(id, "lock");
                    continue;
                }
                if (line.startsWith("unlock ")) {
                    int id = Integer.parseInt(line.split("\\s+")[1]);
                    hub.executeCommand(id, "unlock");
                    continue;
                }
                if (line.startsWith("setTemp ")) {
                    String[] tok = line.split("\\s+");
                    int id = Integer.parseInt(tok[1]);
                    double t = Double.parseDouble(tok[2]);
                    hub.executeCommand(id, "setTemp", String.valueOf(t));
                    continue;
                }
                if (line.startsWith("setSchedule ")) {
                    // setSchedule <deviceId> <HH:MM> <action>
                    String[] parts = splitPreserveQuoted(line, 4);
                    int deviceId = Integer.parseInt(parts[1]);
                    String time = parts[2];
                    String action = parts[3];
                    hub.addSchedule(new ScheduleEntry(deviceId, time, action));
                    System.out.println("[UI] Schedule added: " + action + " at " + time);
                    continue;
                }
                if (line.startsWith("runSchedulesAt ")) {
                    String t = line.split("\\s+")[1];
                    hub.runSchedulesAt(t);
                    continue;
                }
                if (line.startsWith("addTrigger ")) {
                    // very simple parser for: addTrigger temperature > 75 action turnOff(1)
                    // supports only 'temperature' condition for now
                    String[] tok = line.split("\\s+");
                    if (tok.length < 6) { System.err.println("Invalid addTrigger format"); continue; }
                    if (!tok[1].equalsIgnoreCase("temperature")) { System.err.println("Only 'temperature' trigger supported in this simple parser."); continue; }
                    String op = tok[2];
                    double value = Double.parseDouble(tok[3]);
                    if (!tok[4].equalsIgnoreCase("action")) { System.err.println("Expected keyword 'action'"); continue; }
                    String action = line.substring(line.indexOf("action") + 7).trim();
                    Predicate<SmartHub> pred = (SmartHub h) -> {
                        // find thermostats and evaluate if any match condition
                        for (DeviceProxy dp : h.listDevices()) {
                            if (dp.getType().equals("thermostat") && dp instanceof DeviceProxy) {
                                Device dev = dpReal(dp);
                                if (dev instanceof Thermostat) {
                                    double tempNow = ((Thermostat) dev).getTemperature();
                                    switch (op) {
                                        case ">": return tempNow > value;
                                        case "<": return tempNow < value;
                                        case ">=": return tempNow >= value;
                                        case "<=": return tempNow <= value;
                                        case "==": return tempNow == value;
                                    }
                                }
                            }
                        }
                        return false;
                    };
                    TriggerEntry t = new TriggerEntry(String.format("temperature %s %s", op, value), pred, List.of(action));
                    hub.addTrigger(t);
                    System.out.println("[UI] Trigger added: " + t);
                    continue;
                }
                if (line.startsWith("addDevice ")) {
                    // addDevice id type [temperature]
                    String[] tok = line.split("\\s+");
                    int id = Integer.parseInt(tok[1]);
                    String type = tok[2];
                    Map<String,String> props = new HashMap<>();
                    props.put("id", String.valueOf(id));
                    props.put("type", type);
                    if (tok.length >= 4) props.put("temperature", tok[3]);
                    Device d = DeviceFactory.createDevice(props);
                    DeviceProxy dp = new DeviceProxy(d);
                    hub.registerDevice(dp);
                    System.out.println("[UI] Device added: " + d.statusReport());
                    continue;
                }
                if (line.startsWith("removeDevice ")) {
                    int id = Integer.parseInt(line.split("\\s+")[1]);
                    DeviceProxy removed = hub.unregisterDevice(id);
                    System.out.println(removed == null ? "[UI] No such device." : "[UI] Removed device " + id);
                    continue;
                }
                if (line.equals("showSchedules")) {
                    hub.listSchedules().forEach(s -> System.out.println(s));
                    continue;
                }
                if (line.equals("showTriggers")) {
                    hub.listTriggers().forEach(t -> System.out.println(t));
                    continue;
                }
                System.err.println("Unknown command. Type 'help' for list.");
            } catch (Exception ex) {
                System.err.println("[Error] " + ex.getMessage());
            }
        }
    }

    // helper to access underlying Device from DeviceProxy (trusted internal)
    private static Device dpReal(DeviceProxy dp) {
        try {
            java.lang.reflect.Field f = DeviceProxy.class.getDeclaredField("realDevice");
            f.setAccessible(true);
            return (Device) f.get(dp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // splits into N parts preserving the remainder for the last part (for actions possibly with parentheses)
    private static String[] splitPreserveQuoted(String line, int maxParts) {
        String[] tok = new String[maxParts];
        String[] split = line.split("\\s+");
        tok[0] = split[0];
        int i = 1;
        int idx = 1;
        for (; i < split.length && idx < maxParts - 1; i++, idx++) {
            tok[idx] = split[i];
        }
        // remainder into last
        if (i <= split.length) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < split.length; j++) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(split[j]);
            }
            tok[maxParts - 1] = sb.toString();
        } else {
            tok[maxParts - 1] = "";
        }
        return tok;
    }

    public static void main(String[] args) {
        new SmartHomeSystem().repl();
    }
}
