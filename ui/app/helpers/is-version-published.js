import Ember from 'ember';

export function isVersionPublished([published]/*, hash*/) {
  return published === 'true';
}

export default Ember.Helper.helper(isVersionPublished);
