package com.redhat.sample.rest;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloService {

    @Value("${hello.message}")
	private String message;


	@GetMapping(path = { "/hello", "/hello/{name}" }, produces = TEXT_PLAIN_VALUE)
	public String hello(@PathVariable(required = false) Optional<String> name) {
		return this.message + " " + name.orElse("World 10");
	}
}
