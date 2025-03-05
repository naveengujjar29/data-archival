package com.archival.archivalservice.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;


@RestController
@RequestMapping("/swagger")
public class SwaggerController {

    @GetMapping(value = "/yaml", produces = "application/yaml")
    public ResponseEntity<String> getSwaggerYaml() throws IOException {
        Resource resource = new ClassPathResource("static/swagger.yml");
        String yaml = Files.readString(resource.getFile().toPath());
        return ResponseEntity.ok().body(yaml);
    }
}


