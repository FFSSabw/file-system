package com.ffssabcloud.file_system.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

public class BaseController {
    
    protected String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping
                .PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping
                .BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }
    
    protected String encodeURL(String raw, String enc) throws UnsupportedEncodingException {
        List<String> temps = new ArrayList<>();
        for(String s : raw.split("/"))
            temps.add(URLEncoder.encode(s, enc));
        return String.join("/", temps);
    }
    
    protected class Entity<K, V> {
        
        private K key;
        private V value;
        
        public Entity(K k, V v) {
            key = k;
            value = v;
        }
        
        public K getKey() {
            return key;
        }
        
        public V getValue() {
            return value;
        }
    }
}
