export interface DocumentParser {
  parse(raw: string): any;
}

export class JsonParser implements DocumentParser {
  parse(raw: string) {
    // defensive: try to parse JSON, fallback to simple key:value
    try {
      return JSON.parse(raw);
    } catch {
      const out:any = {};
      raw.split('\n').forEach(line=>{
        const [k,v] = line.split(':');
        if (k) out[k.trim()] = (v||'').trim();
      });
      return out;
    }
  }
}

export class TextParser implements DocumentParser {
  parse(raw: string) {
    return { text: raw };
  }
}
