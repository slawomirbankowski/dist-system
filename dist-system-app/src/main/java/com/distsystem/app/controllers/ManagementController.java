package com.distsystem.app.controllers;

import com.distsystem.api.info.AppGlobalInfo;
import com.distsystem.utils.DistUtils;
import com.distsystem.api.CacheVersion;
import com.distsystem.app.services.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/** Controller exposing management endpoints like /ping /version /info /beans */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ManagementController {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(ManagementController.class);

    @Autowired
    protected CacheService cacheService;

    @Autowired
    protected ApplicationContext ctx;

    @GetMapping("/")
    public String index() {
        return "Greetings from DistCache!";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/version")
    public CacheVersion version() {
        return CacheVersion.current;
    }

    @GetMapping("/info")
    public AppGlobalInfo info() {
        return DistUtils.getInfo();
    }

    @GetMapping("/beans")
    public String[] beans() {
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        return beanNames;
    }

}
