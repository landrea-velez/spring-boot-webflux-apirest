package com.landreavelez.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.landreavelez.springboot.webflux.app.models.documents.Categoria;
import com.landreavelez.springboot.webflux.app.models.documents.Producto;
import com.landreavelez.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner{

	@Autowired
	private ProductoService service;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();
		
		Categoria electronico = new Categoria("Electrónico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computación");
		Categoria muebles = new Categoria("Muebles");
		
		Flux.just(electronico, deporte, computacion, muebles)
		.flatMap(service::saveCategoria)
		.doOnNext(c ->{
			log.info("Categoria creada: " + c.getNombre() + ", Id: " + c.getId());
		}).thenMany(
				Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
						new Producto("Sony Camara HD Digital", 177.89, electronico),
						new Producto("Apple iPod", 46.89, electronico),
						new Producto("Sony Notebook", 846.89, computacion),
						new Producto("Hewlett Packard Multifuncional", 200.89, computacion),
						new Producto("Bianchi Bicicleta", 70.89, deporte),
						new Producto("HP Notebook Omen 17", 2500.89, computacion),
						new Producto("Mica Cómoda 5 Cajones", 150.89, muebles),
						new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico)
						)
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
					})
		)
		.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));		
	}

}
