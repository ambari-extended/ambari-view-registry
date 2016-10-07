import DS from 'ember-data';
import Ember from 'ember';

export default DS.Model.extend({
  version: DS.attr('string'),
  createdAt: DS.attr('date'),
  applicationConfig: DS.attr('string'),
  config: Ember.computed('applicationConfig', function() {
    return JSON.parse(this.get('applicationConfig'));
  }),
  application: DS.belongsTo('application', { async: false })

});
