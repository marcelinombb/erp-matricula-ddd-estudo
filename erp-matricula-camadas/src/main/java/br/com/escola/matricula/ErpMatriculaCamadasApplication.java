package br.com.escola.matricula;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Módulo erp-matricula-camadas — arquitetura em camadas (Controller->Service->Repository).
 * Porta: 8081 (via docker-compose). Banco: erp_matricula_camadas.
 *
 * <p><strong>@MapperScan</strong>: Registra automaticamente todas as interfaces anotadas
 * com {@code @Mapper} no pacote {@code repository} como beans Spring.
 *
 * <p><strong>Contraste com erp-matricula-ddd</strong>: No módulo DDD, @MapperScan aponta
 * para {@code infraestrutura.persistencia} — separando o mapeamento de persistência do
 * domínio. Aqui, o repositório é diretamente o Mapper MyBatis — sem separação de camadas.
 */
@SpringBootApplication
@MapperScan("br.com.escola.matricula.repository")
public class ErpMatriculaCamadasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpMatriculaCamadasApplication.class, args);
    }

}
