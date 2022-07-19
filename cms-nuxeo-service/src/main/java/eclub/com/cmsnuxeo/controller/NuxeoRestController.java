package eclub.com.cmsnuxeo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/nuxeo")
public class NuxeoRestController {

	private final Environment env;
	//private final BepsaHelperServices service;

	final static Logger logger = LoggerFactory.getLogger(NuxeoRestController.class);

	public NuxeoRestController(Environment env/*, BepsaHelperServices service*/) {
		this.env = env;
		//this.service = service;
	}

	@Value("${configuracion.ambiente}")
	private String config;


	@GetMapping("/get-config")
	public ResponseEntity getConfig() {
		System.out.println("get-config invocado");
		Map<String, String> map = new HashMap<>();
		map.put("config", config);
		return ResponseEntity.ok(map);
	}
}
