package br.com.escola.matricula;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do ERP Matrícula — Projeto Didático DDD.
 *
 * <p><strong>@MapperScan</strong>: Registra automaticamente todas as interfaces anotadas
 * com {@code @Mapper} no pacote {@code infraestrutura.persistencia} como beans Spring.
 * Sem esta anotação, o contexto Spring não encontra os Mappers MyBatis e falha com
 * "Field mapper required a bean of type '...' that could not be found" (Pitfall 4 do RESEARCH.md).
 *
 * <p><strong>Por que não há spring-boot-starter-web aqui?</strong>
 * Esta fase demonstra que o domínio de negócio (DDD) é independente do protocolo HTTP.
 * A lógica de Matrícula compila, sobe no contexto Spring e executa completamente
 * sem um servidor web. Controllers REST são adicionados na Fase 4.
 * Configuração: {@code spring.main.web-application-type: none} no application.yml.
 *
 * @see org.mybatis.spring.annotation.MapperScan — documentação oficial do registro automático
 */
@SpringBootApplication
@MapperScan("br.com.escola.matricula.infraestrutura.persistencia")
public class ErpMatriculaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpMatriculaApplication.class, args);
    }

}
