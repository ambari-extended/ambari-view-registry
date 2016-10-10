/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Ember from 'ember';
import FileUploader from '../utils/file-uploader';

export default Ember.Component.extend({
  fileOperationService: Ember.inject.service('file-operation'),
  tagName: "div",
  classNames: ['col-md-10', 'col-md-offset-1'],
  isUploading: false,
  uploadFileName: '',
  uploadPercent: '0%',
  uploadPercentStyle: Ember.computed('uploadPercent', function() {
    var style = 'width: ' + this.get('uploadPercent') + ';';
    return style.htmlSafe();
  }),

  setUploadPercent: function(percent) {
    var intValue = Math.round(percent);
    this.set('uploadPercent', `${intValue}%`);
  },

  setUploading: function(fileName) {
    this.set('uploadFileName', fileName);
    this.set('isUploading', true);
    this.set('closeOnEscape', false);
  },

  // Returns a promise which resolves if the entry is a file else it rejects if it is a directory.
  // This tries to read entry and FileReader fails if the entry points to a directory. The file is
  // only opened and the reader is aborted when the loading starts.
  _checkIfFileIsNotDirectory: function(file) {
    return new Ember.RSVP.Promise((resolve, reject) => {

      if (!Ember.isNone(file.size) && file.size <= 4096) { // Directories generally have less equal to 4096 bytes as size
        var reader = new FileReader();
        reader.onerror = function() {
          return reject();
        };

        reader.onloadstart = function() {
          reader.abort();
          return resolve();
        };

        reader.readAsArrayBuffer(file);

      } else {
        return resolve();
      }
    })
  },

  actions: {

    fileLoaded: function(file) {

      this._checkIfFileIsNotDirectory(file).then(() => {
        //var url = this.get('fileOperationService').getUploadUrl();
        let url = "applications";
        var uploader = FileUploader.create({
          url: url
        });
        this.set('uploader', uploader);
        if(!Ember.isEmpty(file)) {
          uploader.upload(file, {upload: this.get('path')});
          this.setUploading(file.name);
          uploader.on('progress', (e) => {
            this.setUploadPercent(e.percent);
          });
          uploader.on('didUpload', (e) => {
            this.set('uploader');
            this.sendAction('uploadComplete', e);
          });
          uploader.on('didError', (jqXHR, textStatus, errorThrown) => {
            var error = Ember.$.parseJSON(jqXHR.responseText);
            this.set('uploader');
            console.log(error);
            this.sendAction('close');
            return false;
          });
        }
      }, () => {
        console.error("Cannot add a directory.");
      });

    },

    close: function() {
      if (!Ember.isNone(this.get('uploader'))) {
        console.log('cancelling the upload');
        this.get('uploader').abort();
      }
      this.sendAction('close');
    }

  }
});

