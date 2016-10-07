import Ember from 'ember';

export default Ember.Component.extend({
  classNames: ['row'],
  application: null,
  name: Ember.computed.alias('application.name'),
  maxVersion: Ember.computed('application.sortedVersions', function() {
    return this.get('application.sortedVersions').get('firstObject');
  })
});
