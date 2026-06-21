-- =============================================================================
-- V3__adicionar_adicionada_em.sql — Adiciona coluna adicionada_em em itens_matricula
--
-- Motivação:
--   MatriculaDto.ItemDto (D-04) expõe o timestamp de quando cada disciplina foi
--   adicionada à matrícula. Esse dado é relevante para auditoria e para o material
--   didático que demonstra como timestamps fluem do banco até o DTO de resposta.
--
-- Retrocompatibilidade com V2__seeds.sql:
--   DEFAULT NOW() garante que os registros inseridos pelos seeds (sem informar
--   adicionada_em) recebam automaticamente o timestamp do momento da migration.
--   O INSERT inserirItens em MatriculaMapper.xml NÃO precisa ser alterado —
--   a coluna usa DEFAULT, então novos itens também recebem o timestamp do banco.
--
-- Referência: PLAN.md Tarefa 2, D-04 (04-CONTEXT.md)
-- =============================================================================

ALTER TABLE itens_matricula
    ADD COLUMN adicionada_em TIMESTAMP NOT NULL DEFAULT NOW();

COMMENT ON COLUMN itens_matricula.adicionada_em
    IS 'Timestamp de quando a disciplina foi adicionada à matrícula — exposto no MatriculaDto.ItemDto (D-04)';
