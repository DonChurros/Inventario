package com.example.inventario;

import com.example.inventario.infrastructure.InventarioApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = InventarioApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:InventarioContextTestdb"
)
class InventarioApplicationTests {

	@Test
	void contextLoads() {
	}

}
