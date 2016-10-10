import Ember from 'ember';

export default Ember.Route.extend({

  actions: {
    uploadComplete: function(versionData) {
      debugger;
      this.transitionTo('app.version', versionData.application.name, versionData.version);
    },

    uploadClosed: function () {
      this.transitionTo('apps');
    }
  }
});
