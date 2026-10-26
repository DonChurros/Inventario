package com.example.inventario.infrastructure.config;


import com.example.inventario.application.port.in.RegistrarProductoUseCase;
import com.example.inventario.application.port.out.ProductoRepositoryPort;
import com.example.inventario.application.service.ProductoService;
import com.example.inventario.infrastructure.adapter.out.persistence.ProductoPersistenceAdapter;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataProductoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RegistrarProductoUseCase registrarProductoUseCase(ProductoRepositoryPort repositoryPort) {
        return new ProductoService(repositoryPort);
    }

//    @Bean
//    public ProductoRepositoryPort productoRepositoryPort(SpringDataProductoRepository repository) {
//        return new ProductoPersistenceAdapter(repository);
//    }
}
