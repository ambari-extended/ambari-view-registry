/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.view.registry.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.ambari.view.registry.internal.exception.ConfigFileParseException;
import org.apache.ambari.view.registry.web.config.ApplicationConfig;
import org.apache.ambari.view.registry.web.model.dto.ApplicationWrapper;
import org.apache.ambari.view.registry.web.model.dto.ApplicationsWrapper;
import org.apache.ambari.view.registry.web.model.dto.VersionPublishRequest;
import org.apache.ambari.view.registry.web.model.dto.VersionWrapper;
import org.apache.ambari.view.registry.web.model.entity.Application;
import org.apache.ambari.view.registry.web.model.entity.ApplicationVersion;
import org.apache.ambari.view.registry.web.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
@RestController
@RequestMapping("/applications")
@Slf4j
public class ApplicationController {

  private final ApplicationService service;

  @Autowired
  public ApplicationController(ApplicationService service) {
    this.service = service;
  }

  /**
   * Returns all the applications. Can be filtered with 'after' query params
   * @return List of applications
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApplicationsWrapper getAll(@RequestParam(value = "after") Optional<Long> after,
                                    @RequestParam("like") Optional<String> appNameLike,
                                    @RequestParam(value = "embed", defaultValue = "false") String embed) {
    List<Application> applications = service.findApplications(appNameLike, after);
    return new ApplicationsWrapper(applications);
  }

  @GetMapping(path = "/{appName}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApplicationWrapper getOne(@PathVariable("appName") String appName) {
    Optional<Application> application = service.findApplicationByName(appName);
    return new ApplicationWrapper(application);
  }

  @GetMapping(path = "/{appName}/versions/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
  public VersionWrapper getVersionForApplication(@PathVariable("appName") String appName,
                                                @PathVariable("version") String version) {
    Optional<ApplicationVersion> ver = service.findApplicationVersion(appName, version);
    return new VersionWrapper(ver);
  }

  @PutMapping(path = "/{appName}/versions/{version}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  public VersionWrapper publishVersion(@PathVariable("appName") String appName,
                                       @PathVariable("version") String version,
                                       @RequestBody VersionPublishRequest publishRequest
                                       ) {
    Optional<ApplicationVersion> ver = service.publishVersion(appName, version, publishRequest.isPublish());
    return new VersionWrapper(ver);
  }

  @PostMapping
  @ResponseBody
  public ResponseEntity addApplication(@RequestParam("file") MultipartFile file) {
    ApplicationConfig config = null;
    try {
      config = service.parseApplicationConfig(file.getInputStream(), file.getOriginalFilename());
    } catch (IOException e) {
      log.error("Failed to get the stream from multipart file", e);
      throw new ConfigFileParseException(file.getOriginalFilename(), e);
    }

    ApplicationVersion version = service.saveApplicationConfig(config);
    Map<String, Object> map = new LinkedHashMap<>();
    Map<String, Object> map1 = new LinkedHashMap<>();
    map1.put("id", version.getApplication().getId());
    map1.put("name", version.getApplication().getName());
    map.put("id", version.getId());
    map.put("version", version.getVersion());
    map.put("application", map1);
    return ResponseEntity.ok(map);
  }


}
