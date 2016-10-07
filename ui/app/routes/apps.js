import Ember from 'ember';

export default Ember.Route.extend({
  queryParams: {
    like: {
      refreshModel: true
    }
  },
  model(params) {
    if (Ember.isEmpty(params.like)) {
      return new Ember.A();
    }
    return this.get('store').query('application', params);
  },
  actions: {
    searchApplication(query) {
      this.transitionTo({ queryParams: { like: query }});
    }
  }
});
