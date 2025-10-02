// Simulate a legacy API with callback style and odd interface
class LegacyApi {
  getUserById(id: number, cb: (err: any, data?: any)=>void) {
    setTimeout(()=> {
      if (id<=0) return cb(new Error('invalid id'));
      cb(null, { uid: id, displayName: 'User'+id });
    }, 100);
  }
}

export class LegacyApiAdapter {
  private legacy = new LegacyApi();

  async getUser(id: number) {
    if (!Number.isInteger(id) || id<=0) throw new Error('id must be positive integer');
    return new Promise((resolve, reject) => {
      this.legacy.getUserById(id, (err, data)=>{
        if (err) return reject(err);
        // adapt shape
        resolve({ id: data.uid, name: data.displayName });
      });
    });
  }
}
