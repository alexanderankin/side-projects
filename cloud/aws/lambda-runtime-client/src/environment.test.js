const { Environment } = require('./environment')

describe('Environment', () => {
  it('should create Environment with or without "new"', () => {
    expect(Environment()).toBeInstanceOf(Environment)
    expect(new Environment()).toBeInstanceOf(Environment)
  });
});
