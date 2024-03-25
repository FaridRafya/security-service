package com.example.corurseservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CourseController {


    @GetMapping("/course")
    public Map<String,Object> testing (){
        return  Map.of("messge","testing success");
    }
}
