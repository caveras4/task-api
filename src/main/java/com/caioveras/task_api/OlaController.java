package com.caioveras.task_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OlaController {

    @GetMapping("/ola")
    public String hello(){
        return "Olá, Mundo!";
    }

    @GetMapping("/ola/{name}")
    public String helloWithName(@PathVariable String name){
        return "Olá, " + name;
    }

}
