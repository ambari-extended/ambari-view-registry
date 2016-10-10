import Ember from 'ember';
import EmberUploader from 'ember-uploader';

export default EmberUploader.Uploader.extend({
  type: 'PUT',
  ajaxSettings: function(url, params, method) {
    var defaultSettings = this._super(url, params, method);
    return Ember.merge(defaultSettings, {
      beforeSend:function (xhr) {
        xhr.setRequestHeader('X-Requested-By', 'ambari');
      }
    });
  }
});
