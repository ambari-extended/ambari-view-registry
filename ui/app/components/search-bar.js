import Ember from 'ember';

export default Ember.Component.extend({
  searchField: Ember.computed.oneWay('search'),
  actions: {
    search(query) {
      this.sendAction('searchAction', query);
    }
  }
});
