import DS from 'ember-data';
import Ember from 'ember';

export default DS.Model.extend({
  version: DS.attr('string'),
  createdAt: DS.attr('date'),
  published: DS.attr('boolean'),
  applicationConfig: DS.attr('string'),
  config: Ember.computed('applicationConfig', function() {
    return JSON.parse(this.get('applicationConfig'));
  }),
  prettyJson: Ember.computed('config', function() {
    return JSON.stringify(this.get('config'), null, 4);
  }),
  application: DS.belongsTo('application', { async: false }),

  withhold: function() {
    let adapter = this.get('store').adapterFor(this.constructor.modelName);
    return adapter.publish(this, false).then(() => {
      this.set('published', false);
    });
  },
  publish: function() {
    let adapter = this.get('store').adapterFor(this.constructor.modelName);
    return adapter.publish(this, true).then(() => {
      this.set('published', true);
    });
  }

});
