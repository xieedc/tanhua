package com.tanhua.server.controller;

import com.tanhua.server.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 搜附近控制层
 */
@RestController
@RequestMapping("/baidu")
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * 上报地理位置信息
     * @param map
     * @return
     */
    @RequestMapping(value = "/location",method = RequestMethod.POST)
    public ResponseEntity addLocation(@RequestBody Map<String,Object> map){
        locationService.addLocation(map);
        return ResponseEntity.ok(null);
    }
}
