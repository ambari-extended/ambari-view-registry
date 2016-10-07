import DS from 'ember-data';

export default DS.RESTAdapter.extend({
  /*findRecord: function(store, type, id, snapshot) {
    var url = [type.modelName, id].join('/');

    return new Ember.RSVP.Promise(function(resolve, reject) {
      Ember.$.getJSON(url).then(function(data) {
        Ember.run(null, resolve, data);
      }, function(jqXHR) {
        jqXHR.then = null; // tame jQuery's ill mannered promises
        Ember.run(null, reject, jqXHR);
      });
    });
  }*/
});
