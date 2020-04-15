//import Vue from 'vue';
import Custom from '../../src/custom.vue';



describe('custom', () => {

  it('has a created hook', () => {
    expect(typeof Custom.created).toBe('function')
  });
});




