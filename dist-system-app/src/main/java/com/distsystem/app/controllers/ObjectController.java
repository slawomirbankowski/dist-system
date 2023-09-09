package com.distsystem.app.controllers;

import com.distsystem.api.*;
import com.distsystem.api.info.CacheSetBackInfo;
import com.distsystem.app.services.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Controller for objects in cache */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ObjectController {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(ObjectController.class);
    @Autowired
    protected CacheService cacheService;

    @GetMapping("/object/{group}/{key}")
    public String getObject(@PathVariable("group") final String group, @PathVariable("key") final String key) {
        return cacheService.getObjectAsString(group, key);
    }
    @GetMapping(value = "/object/{key}")
    public String getObjectByKey(@PathVariable("key") final String key) {
        return cacheService.getObjectAsString("default", key);
    }
    @GetMapping(value = "/object-full/{key}")
    public Optional<CacheObject> getObjectFullByKey(@PathVariable("group") final String group, @PathVariable("key") final String key) {
        // TODO: implement getting object for cache and object key
        return cacheService.getObject(group, key);
    }
    @PostMapping("/object/{group}/{key}")
    public CacheSetBackInfo setObject(@PathVariable("group") final String group,
                                      @PathVariable("key") final String key,
                                      @RequestParam Map<String, String> params,
                                      @RequestBody String fullText) {
        var modeStr = params.getOrDefault("mode", "mode=1");
        Set<String> groups = new HashSet<String>();
        log.info("Set new object to cache for key: " + key + ", mode=" + modeStr + ", len=" + fullText.length());
        return cacheService.setObject(group, key, fullText, modeStr, groups);
    }

    @DeleteMapping("/object/{group}/{key}")
    public ControllerStatus deleteObject(@PathVariable("group") final String group, @PathVariable("key") final String key) {
        var removedCount = cacheService.clearCaches(group, key);
        return new ControllerStatus(ControllerStatus.STATUS_OK, removedCount);
    }

}
