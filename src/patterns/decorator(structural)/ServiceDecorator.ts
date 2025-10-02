export type Service = { execute: (input: string)=>Promise<string> };

export function ServiceDecorator(inner: Service): Service {
  return {
    async execute(input: string) {
      const start = Date.now();
      console.log('[Decorator] before execute');
      try {
        const res = await inner.execute(input);
        console.log('[Decorator] after execute success', Date.now()-start, 'ms');
        return res;
      } catch (err) {
        console.log('[Decorator] after execute failed', err);
        throw err;
      }
    }
  }
}
