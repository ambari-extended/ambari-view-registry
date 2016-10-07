import Ember from 'ember';
import config from './config/environment';

const Router = Ember.Router.extend({
  location: config.locationType,
  rootURL: config.rootURL
});

Router.map(function () {
  this.route('upload');
  this.route('apps', {path: '/applications'});
  this.route('app', {path: '/application/:name'});
  this.route('version', {path: '/application/:name/version/:version'});
});

export default Router;
