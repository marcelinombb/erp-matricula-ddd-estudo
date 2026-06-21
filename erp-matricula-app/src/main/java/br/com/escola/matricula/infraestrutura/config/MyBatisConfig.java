package br.com.escola.matricula.infraestrutura.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuração MyBatis da aplicação.
 *
 * <p><strong>TypeHandlers registrados via application.yml</strong> — não via {@code @Bean} aqui.
 * A propriedade {@code mybatis.type-handlers-package} no {@code application.yml} instrui o
 * MyBatis a escanear o pacote e registrar automaticamente todos os TypeHandlers encontrados:
 * <pre>
 * mybatis:
 *   type-handlers-package: br.com.escola.matricula.infraestrutura.persistencia.typehandler
 * </pre>
 * Esta é a abordagem "Don't Hand-Roll" — mais simples, menos propensa a erros de configuração
 * (ex: esquecer de registrar um novo TypeHandler quando o projeto crescer).
 * Ver RESEARCH.md "Don't Hand-Roll — Registro de TypeHandlers".</p>
 *
 * <p><strong>Esta classe existe para futuras configurações de infraestrutura</strong> — como
 * um {@code DataSource} customizado (pool diferente na Fase 4), interceptors de auditoria,
 * ou configuração de plugins. Pedagogicamente, documenta que configuração de infraestrutura
 * fica em {@code infraestrutura.config}, nunca em pacotes de domínio ou aplicação.</p>
 */
@Configuration
public class MyBatisConfig {
    // TypeHandlers registrados automaticamente via mybatis.type-handlers-package no application.yml
    // Não adicionar @Bean de TypeHandler aqui — usar o package scanning é mais manutenível.
    //
    // Futuras adições candidatas (Fase 4+):
    //   - Interceptor de auditoria (log de queries lentas)
    //   - DataSource com connection pool customizado
    //   - Plugin de paginação para listas grandes
}
