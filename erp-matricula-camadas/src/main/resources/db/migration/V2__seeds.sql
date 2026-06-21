-- Migrations idênticas ao módulo erp-matricula-ddd aplicadas no banco erp_matricula_camadas.
-- O schema de banco é o mesmo — o que muda é o código Java que o acessa.

-- =============================================================================
-- V2__seeds.sql — Dados de demonstração para os 3 fluxos de negócio
--
-- ATENÇÃO: Seeds são apenas para desenvolvimento e demonstração.
-- Não devem ser aplicados em produção (Fase 4 introduzirá profiles Spring).
--
-- UUIDs são fixos para permitir referência direta no DemoRunner.java sem consulta
-- ao banco — a demonstração funciona de forma reproduzível em qualquer execução.
--
-- Fluxos habilitados por estes seeds:
--   FLUXO 1 — Matricular aluno:   Aluno Maria Silva (a0...001) matrícula na Turma 2026-1 (b0...001)
--   FLUXO 2 — Adicionar disciplina: Matrícula pré-existente (c0...001) recebe nova disciplina
--   FLUXO 3 — Cancelar matrícula:  Matrícula criada no Fluxo 1 é cancelada
-- =============================================================================

-- -----------------------------------------------------------------------------
-- FLUXO 1 — Aluno para matricular
-- Maria Silva está ativa e não tem matrícula no período 2026/1.
-- O DemoRunner cria uma nova matrícula para ela.
-- -----------------------------------------------------------------------------
INSERT INTO alunos (id, cpf, nome, ativo)
VALUES ('a0000000-0000-0000-0000-000000000001', '52998224725', 'Maria Silva', true);

-- -----------------------------------------------------------------------------
-- FLUXOS 2 e 3 — Aluno com matrícula pré-existente
-- João Santos já tem matrícula ATIVA no período 2026/1 (ver INSERT em matriculas abaixo).
-- FLUXO 2: DemoRunner adiciona "Física Quântica" à matrícula dele.
-- -----------------------------------------------------------------------------
INSERT INTO alunos (id, cpf, nome, ativo)
VALUES ('a0000000-0000-0000-0000-000000000002', '98765432100', 'João Santos', true);

-- -----------------------------------------------------------------------------
-- Turma 2026-1
-- Período: 2026-02-01 a 2026-07-31 (semestre 1 de 2026)
-- Mapeamento: PeriodoLetivo(ano=2026, semestre=1) → periodo_inicio=2026-02-01, periodo_fim=2026-07-31
-- Usada tanto no Fluxo 1 (matricular Maria) quanto no Fluxo 2 e 3 (matrícula do João).
-- -----------------------------------------------------------------------------
INSERT INTO turmas (id, nome, periodo_inicio, periodo_fim, vagas_maximas)
VALUES ('b0000000-0000-0000-0000-000000000001', 'Turma 2026-1', '2026-02-01', '2026-07-31', 30);

-- -----------------------------------------------------------------------------
-- FLUXOS 2 e 3 — Matrícula pré-existente de João Santos
-- Status ATIVA, período 2026/1 — seed para os fluxos de adição e cancelamento.
-- O DemoRunner usa este UUID diretamente: new MatriculaId(UUID.fromString("c0...001"))
-- -----------------------------------------------------------------------------
INSERT INTO matriculas (id, aluno_id, turma_id, periodo_inicio, periodo_fim, status)
VALUES (
    'c0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000001',
    '2026-02-01', '2026-07-31', 'ATIVA'
);

-- Item inicial da matrícula pré-existente
-- DemoRunner Fluxo 2 adiciona "Física Quântica" além desta disciplina.
INSERT INTO itens_matricula (matricula_id, disciplina)
VALUES ('c0000000-0000-0000-0000-000000000001', 'Matemática Básica');
