import ApplicationAdapter from './application';

export default ApplicationAdapter.extend({
  urlForFindRecord(id, modelName, snapshot) {
    let params = JSON.parse(id);
    return `/applications/${params.appname}/versions/${params.version}`;
  }
});
