package com.ffssabcloud.file_system.controller;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

public class BaseController {
    
    protected static String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping
                .PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping
                .BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }
    
    protected static String encodeURL(String raw, String enc) {
        Stream<String> splited = Arrays.asList(raw.split("/")).stream();
        return splited.filter(str -> !StringUtils.isEmpty(str))
                .map(str -> tryEncodeURL(str, enc))
                .reduce((sum, item) -> String.format("%s/%s", sum, item)).get();
    }
    
    private static String tryEncodeURL(String raw, String enc) {
        try {
            return URLEncoder.encode(raw, enc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected class Entity<K, V> {
        
        private K key;
        private V value;
        
        public Entity(K k, V v) {
            key = k;
            value = v;
        }
        
        public void setKey(K key) {
            this.key = key;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public K getKey() {
            return key;
        }
        
        public V getValue() {
            return value;
        }
    }
    
    public static void main(String[] args) {
        System.out.println(encodeURL("abc/qwe/你好", "UTF-8"));
        System.out.println(encodeURL("abc/qwe/你好/", "UTF-8"));
        System.out.println(encodeURL("/abc/qwe/你好", "UTF-8"));
        System.out.println(encodeURL("/abc/qwe/你好/", "UTF-8"));
        System.out.println(encodeURL("/abc/qwe//你好/", "UTF-8"));
        System.out.println(encodeURL("/abc///qwe//你好/", "UTF-8"));
    }
}
