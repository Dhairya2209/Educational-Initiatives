import { DocumentParser, JsonParser, TextParser } from './parsers';

export class DocumentFactory {
  createParser(type: string): DocumentParser {
    const t = (type||'').toLowerCase();
    if (t === 'json') return new JsonParser();
    if (t === 'txt') return new TextParser();
    throw new Error('Unsupported document type');
  }
}
