export class UserProfile {
  name?: string;
  email?: string;
  role?: string;
  metadata?: Record<string,string>;
}

export class UserProfileBuilder {
  private profile: UserProfile = new UserProfile();
  setName(name: string) {
    if (!name) throw new Error('Name required');
    this.profile.name = name;
    return this;
  }
  setEmail(email: string) {
    if (!email || !email.includes('@')) throw new Error('Valid email required');
    this.profile.email = email;
    return this;
  }
  setRole(role: string) {
    this.profile.role = role;
    return this;
  }
  setMeta(key: string, val: string) {
    if (!this.profile.metadata) this.profile.metadata = {};
    this.profile.metadata[key] = val;
    return this;
  }
  build() {
    // defensive final validation
    if (!this.profile.name || !this.profile.email) throw new Error('Missing required fields');
    return this.profile;
  }
}
