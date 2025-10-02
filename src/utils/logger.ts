import winston from 'winston';
const { combine, timestamp, printf } = winston.format;

const myFormat = printf(({ level, message, timestamp, ...meta }) => {
  return `${timestamp} [${level}] ${message} ${Object.keys(meta).length?JSON.stringify(meta):''}`;
});

export class Logger {
  private static logger = winston.createLogger({
    level: 'info',
    format: combine(timestamp(), myFormat),
    transports: [
      new winston.transports.Console(),
      new winston.transports.File({ filename: 'app.log' })
    ]
  });

  static getLogger() {
    return Logger.logger;
  }
}
