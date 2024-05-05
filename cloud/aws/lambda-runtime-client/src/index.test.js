var index = require('./index')

describe('it should import', () => {
  it('should return 1', () => {
    expect(index.thing() === 1).toBeTrue();
  });
})
