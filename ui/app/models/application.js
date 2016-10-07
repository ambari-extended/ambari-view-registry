import DS from 'ember-data';
import Ember from 'ember';

export default DS.Model.extend({
  name: DS.attr('string'),
  label: DS.attr('string'),
  description: DS.attr('string'),
  updatedAt: DS.attr('date'),
  versions: DS.hasMany('version'),
  sortedVersions: Ember.computed.sort('versions', 'sortDefinition'),
  sortDefinition: ['createdAt:desc']
});
