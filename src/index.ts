import inquirer from 'inquirer';
import { Logger } from './utils/logger';
import { NotificationCenter } from './patterns/observer/NotificationCenter';
import { PaymentContext, StripeStrategy, PaypalStrategy } from './patterns/strategy/Payment';
import { DocumentFactory } from './patterns/factory/DocumentFactory';
import { UserProfileBuilder } from './patterns/builder/UserProfileBuilder';
import { LegacyApiAdapter } from './patterns/adapter/LegacyApiAdapter';
import { ServiceDecorator } from './patterns/decorator/ServiceDecorator';
import { resilient } from './utils/retry';

const logger = Logger.getLogger();

async function main() {
  logger.info('Design Patterns Demo starting...');
  const notificationCenter = new NotificationCenter();
  const paymentContext = new PaymentContext();
  const docFactory = new DocumentFactory();
  const builder = new UserProfileBuilder();
  const adapter = new LegacyApiAdapter();
  const baseService = {
    execute: async (input: string) => {
      return `Base service processed: ${input}`;
    }
  };
  const decorated = ServiceDecorator(baseService);

  const actions = [
    { name: 'Observer: Subscribe & Publish Notification', value: 'observer' },
    { name: 'Strategy: Process Payment (Stripe/Paypal)', value: 'strategy' },
    { name: 'Factory Method: Parse Document', value: 'factory' },
    { name: 'Builder: Build User Profile', value: 'builder' },
    { name: 'Adapter: Call Legacy API via Adapter', value: 'adapter' },
    { name: 'Decorator: Call Service with Logging Decorator', value: 'decorator' },
    { name: 'Exit', value: 'exit' }
  ];

  while (true) {
    const res = await inquirer.prompt({
      type: 'list',
      name: 'action',
      message: 'Choose a use-case to run (press ctrl+c to quit):',
      choices: actions
    });
    const choice = res.action;
    try {
      if (choice === 'observer') {
        const sub = {
          update: (msg: string) => console.log('[Subscriber] got ->', msg)
        };
        notificationCenter.subscribe('news', sub);
        notificationCenter.publish('news', 'Observer pattern: New event arrived!');
        notificationCenter.unsubscribe('news', sub);
      } else if (choice === 'strategy') {
        const payer = await inquirer.prompt({
          type: 'list', name: 'method', message: 'Choose payment method', choices: ['stripe','paypal']
        });
        if (payer.method === 'stripe') paymentContext.setStrategy(new StripeStrategy());
        else paymentContext.setStrategy(new PaypalStrategy());
        await paymentContext.pay(49.99);
      } else if (choice === 'factory') {
        const answer = await inquirer.prompt({name:'type', message:'Document type (json/txt)', default:'json'});
        const parser = docFactory.createParser(answer.type);
        const output = parser.parse('id:1\nname:demo');
        console.log('Parsed output ->', output);
      } else if (choice === 'builder') {
        const name = await inquirer.prompt({name:'name', message:'Name'});
        const email = await inquirer.prompt({name:'email', message:'Email'});
        const profile = builder.setName(name.name).setEmail(email.email).setRole('Developer').build();
        console.log('Built profile ->', profile);
      } else if (choice === 'adapter') {
        const resp = await adapter.getUser(42);
        console.log('Adapter returned ->', resp);
      } else if (choice === 'decorator') {
        const result = await resilient(() => decorated.execute('payload'), 3);
        console.log('Decorator result ->', result);
      } else if (choice === 'exit') {
        logger.info('Exiting demo.');
        process.exit(0);
      }
    } catch (err) {
      logger.error('Error running action', {error: err});
      console.error('Operation failed:', err);
    }
  }
}

main().catch(err => {
  Logger.getLogger().error('Fatal error', {error: err});
  process.exit(1);
});
