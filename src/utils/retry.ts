export async function resilient<T>(fn: ()=>Promise<T>, attempts=3, delayMs=300): Promise<T> {
  let lastErr: any;
  for (let i=0;i<attempts;i++) {
    try {
      return await fn();
    } catch (err) {
      lastErr = err;
      const backoff = delayMs * Math.pow(2, i);
      await new Promise(r=>setTimeout(r, backoff));
    }
  }
  throw lastErr;
}
