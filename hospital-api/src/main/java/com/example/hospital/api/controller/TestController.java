package com.example.hospital.api.controller;

import com.example.hospital.api.common.R;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/test")
public class TestController {

  @Resource
    private AmqpTemplate amqpTemplate;


  @GetMapping
  public String top(){
      return amqpTemplate.toString();
  }


 /* @GetMapping("/dontClickIt")
  public R deleteFaceModel(){

  }*/
}
