import Ember from 'ember';

export default Ember.Route.extend({
  model(params) {
    return new Ember.RSVP.Promise((resolve, reject) => {
      this.modelFor('app').get('versions').then(function(versions) {
        let v = versions.filter(x => params.version === x.get('version')).get('firstObject');
        return v !== undefined ? resolve(v) : reject({errors: [{msg: `Version ${params.version} not found`}]});
      });
    });
  }
});
