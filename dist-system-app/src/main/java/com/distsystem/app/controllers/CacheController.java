package com.distsystem.app.controllers;

import com.distsystem.api.info.CacheInfo;
import com.distsystem.api.CacheRegister;
import com.distsystem.api.ControllerStatus;
import com.distsystem.app.services.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

/** Controller for caches */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class CacheController {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(CacheController.class);
    @Autowired
    protected CacheService cacheService;

    @GetMapping("/caches")
    public Set<String> getCacheKeys() {
        return cacheService.listCaches();
    }

    @GetMapping("/cache/{id}")
    public CacheInfo getCacheByKey(@PathVariable("id") final String id) {
        return cacheService.getCacheInfoByKey(id);
    }

    @PutMapping("/cache")
    public CacheInfo addCache(@RequestBody CacheRegister register) {
        return cacheService.initializeCache(register);
    }

    @PostMapping("/cache")
    public CacheInfo changeCache(@RequestBody CacheRegister register) {
        return cacheService.initializeCache(register);
    }

    @DeleteMapping("/cache/{id}")
    public ControllerStatus deleteCache(@PathVariable("id") final String id) {
        return cacheService.deactivateCache(id);
    }

}
