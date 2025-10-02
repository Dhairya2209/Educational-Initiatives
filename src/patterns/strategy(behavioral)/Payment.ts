export interface PaymentStrategy {
  pay(amount: number): Promise<boolean>;
}

export class StripeStrategy implements PaymentStrategy {
  async pay(amount: number): Promise<boolean> {
    console.log('[Stripe] processing', amount);
    // simulate async network call
    await new Promise(r=>setTimeout(r,100));
    return true;
  }
}

export class PaypalStrategy implements PaymentStrategy {
  async pay(amount: number): Promise<boolean> {
    console.log('[Paypal] processing', amount);
    await new Promise(r=>setTimeout(r,120));
    return true;
  }
}

export class PaymentContext {
  private strategy?: PaymentStrategy;
  setStrategy(s: PaymentStrategy) { this.strategy = s; }
  async pay(amount: number) {
    if (!this.strategy) throw new Error('Strategy not set');
    return this.strategy.pay(amount);
  }
}
