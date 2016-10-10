import ApplicationAdapter from './application';

export default ApplicationAdapter.extend({
  urlForFindRecord(id) {
    let params = JSON.parse(id);
    return `/applications/${params.appname}/versions/${params.version}`;
  },

  publish(model, publish = true) {
    let id = `{"appname": "${model.get('application.name')}", "version": "${model.get('version')}" }`;
    let url = this.urlForFindRecord(id) + "/publish";
    return this.ajax(url, 'PUT', {data: {publish: publish}});
  }
});
