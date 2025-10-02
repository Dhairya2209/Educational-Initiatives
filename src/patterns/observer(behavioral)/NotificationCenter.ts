export interface Subscriber {
  update(msg: string): void;
}

export class NotificationCenter {
  private topics: Map<string, Set<Subscriber>> = new Map();

  subscribe(topic: string, sub: Subscriber) {
    if (!this.topics.has(topic)) this.topics.set(topic, new Set());
    this.topics.get(topic)!.add(sub);
  }

  unsubscribe(topic: string, sub: Subscriber) {
    this.topics.get(topic)?.delete(sub);
  }

  publish(topic: string, message: string) {
    const subs = this.topics.get(topic);
    if (!subs) return;
    for (const s of subs) {
      try {
        s.update(message);
      } catch (err) {
        // defensive: log and continue
        console.error('Subscriber failed', err);
      }
    }
  }
}
