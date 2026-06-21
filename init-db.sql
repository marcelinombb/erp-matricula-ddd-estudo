-- init-db.sql — Script de inicialização do PostgreSQL
-- Executado pelo entrypoint do container postgres na primeira inicialização.
-- O banco erp_matricula já é criado pela variável POSTGRES_DB no docker-compose.yml.
-- Este script cria o banco separado para o módulo erp-matricula-camadas.
--
-- Por que banco separado (e não schema separado)?
-- Dois módulos Spring Boot com Flyway apontando para o mesmo banco/schema causariam
-- conflito na tabela flyway_schema_history: o segundo módulo que sobe tentaria aplicar
-- as migrations V1-V3 que o primeiro já registrou. Banco separado evita o conflito.
-- Ver: Pitfall 1 em .planning/phases/05-diagnostico-codigo-com-anti-padroes/05-RESEARCH.md

CREATE DATABASE erp_matricula_camadas;
GRANT ALL PRIVILEGES ON DATABASE erp_matricula_camadas TO matricula;
