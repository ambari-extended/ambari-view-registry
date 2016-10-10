import Ember from 'ember';

export default Ember.Component.extend({

  actions: {
    withhold: function() {
      this.get('version').withhold();
    },

    publish: function() {
      this.get('version').publish();
    }
  }
});
