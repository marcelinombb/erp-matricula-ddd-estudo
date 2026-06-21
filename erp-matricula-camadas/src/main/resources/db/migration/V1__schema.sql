-- Migrations idênticas ao módulo erp-matricula-ddd aplicadas no banco erp_matricula_camadas.
-- O schema de banco é o mesmo — o que muda é o código Java que o acessa.

-- =============================================================================
-- V1__schema.sql — Schema PostgreSQL do Bounded Context Matrícula
--
-- Princípios de design:
--   1. Uma tabela por Aggregate Root (+ tabelas de filhos internos)
--   2. aluno_id e turma_id SEM FOREIGN KEY — referência por ID entre Aggregates (ADR-003)
--   3. itens_matricula COM FK para matriculas com ON DELETE CASCADE — entidade interna,
--      ciclo de vida completamente acoplado ao Aggregate Root
--   4. status como VARCHAR(20) + CHECK — mais portável que enum PostgreSQL
--   5. Unique index CONDICIONAL WHERE status = 'ATIVA' — aluno pode ter matrículas
--      canceladas no mesmo período (regra de negócio)
--
-- Referências:
--   ADR-001: Por que MyBatis em vez de JPA (docs/adrs/ADR-001-mybatis-vs-jpa.md)
--   ADR-003: Referência por ID entre Aggregates (docs/adrs/ADR-003-referencia-por-id.md)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Alunos — entidade de referência (não é o Aggregate Root desta fase)
-- O BC Matrícula usa apenas AlunoId para referenciar o Aluno.
-- A tabela existe aqui para suportar os seeds de demonstração.
-- -----------------------------------------------------------------------------
CREATE TABLE alunos (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cpf         VARCHAR(11)  NOT NULL UNIQUE,
    nome        VARCHAR(200) NOT NULL,
    ativo       BOOLEAN      NOT NULL DEFAULT true,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  alunos       IS 'Referência ao Aggregate Aluno (BC Matrícula usa apenas AlunoId)';
COMMENT ON COLUMN alunos.cpf   IS 'CPF armazenado sem máscara (11 dígitos numéricos)';
COMMENT ON COLUMN alunos.ativo IS 'Status do aluno — inativo não pode ser matriculado (VerificadorElegibilidadeMatricula)';

-- -----------------------------------------------------------------------------
-- Turmas — entidade de referência (não é o Aggregate Root desta fase)
-- O BC Matrícula usa apenas TurmaId para referenciar a Turma.
-- -----------------------------------------------------------------------------
CREATE TABLE turmas (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            VARCHAR(200) NOT NULL,
    periodo_inicio  DATE         NOT NULL,
    periodo_fim     DATE         NOT NULL,
    vagas_maximas   SMALLINT     NOT NULL CHECK (vagas_maximas > 0),
    criada_em       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE turmas IS 'Referência ao Aggregate Turma (BC Matrícula usa TurmaId para referenciar)';

-- -----------------------------------------------------------------------------
-- Matrículas — Aggregate Root do Bounded Context Matrícula
--
-- Decisões de design:
--   - aluno_id SEM FK para alunos: referência por ID entre Aggregates (ADR-003).
--     Um Aggregate não deve conhecer os internos de outro — passa apenas o ID.
--     Se o Aluno for movido para outro serviço/banco, a Matrícula não quebra.
--   - turma_id SEM FK para turmas: mesma razão.
--   - cancelada_em e concluida_em NULLABLE: só existem nos estados correspondentes.
--     StatusMatricula.Cancelada tem canceladaEm; StatusMatricula.Concluida tem concluidaEm.
-- -----------------------------------------------------------------------------
CREATE TABLE matriculas (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id       UUID        NOT NULL,
    -- Deliberadamente sem FK para alunos — referência por ID entre Aggregates. Ver ADR-003.
    turma_id       UUID        NOT NULL,
    -- Deliberadamente sem FK para turmas — mesma razão que aluno_id.
    periodo_inicio DATE        NOT NULL,
    periodo_fim    DATE        NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'ATIVA'
                               CHECK (status IN ('ATIVA', 'CANCELADA', 'CONCLUIDA')),
    cancelada_em   TIMESTAMPTZ,
    concluida_em   TIMESTAMPTZ,
    criada_em      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  matriculas              IS 'Aggregate Root: Matricula (BC Matrícula)';
COMMENT ON COLUMN matriculas.aluno_id     IS 'Referência por ID ao Aggregate Aluno — sem FK intencional (ADR-003)';
COMMENT ON COLUMN matriculas.turma_id     IS 'Referência por ID ao Aggregate Turma — sem FK intencional (ADR-003)';
COMMENT ON COLUMN matriculas.status       IS 'Persiste StatusMatricula (sealed interface): ATIVA, CANCELADA, CONCLUIDA';
COMMENT ON COLUMN matriculas.cancelada_em IS 'Preenchido apenas quando status = CANCELADA (StatusMatricula.Cancelada.canceladaEm)';
COMMENT ON COLUMN matriculas.concluida_em IS 'Preenchido apenas quando status = CONCLUIDA (StatusMatricula.Concluida.concluidaEm)';

-- Unicidade condicional: um aluno só pode ter UMA matrícula ATIVA por período.
-- Um aluno PODE ter múltiplas matrículas CANCELADAS no mesmo período
-- (ex: matriculou, cancelou, matriculou novamente).
CREATE UNIQUE INDEX uq_matricula_aluno_periodo_ativa
    ON matriculas (aluno_id, periodo_inicio, periodo_fim)
    WHERE status = 'ATIVA';

-- Índice para consultas por aluno (buscarPorAluno no MatriculaRepositorio)
CREATE INDEX idx_matriculas_aluno_id ON matriculas (aluno_id);

-- -----------------------------------------------------------------------------
-- Itens de matrícula — entidade INTERNA do Aggregate Matricula
--
-- Decisão de design:
--   - COM FK para matriculas (ON DELETE CASCADE): itens_matricula é uma entidade interna
--     do Aggregate Root Matricula. O ciclo de vida de um item está completamente acoplado
--     ao da matrícula — se a matrícula for deletada, os itens somem automaticamente.
--     Contraste com aluno_id/turma_id: aqueles são referências a outros Aggregates
--     independentes; este é parte interna do mesmo Aggregate.
--   - UNIQUE (matricula_id, disciplina): invariante de negócio "sem duplicidade"
--     garantida também no banco (defesa em profundidade — a invariante já é protegida
--     pelo Aggregate em Matricula.adicionarDisciplina()).
-- -----------------------------------------------------------------------------
CREATE TABLE itens_matricula (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    matricula_id UUID        NOT NULL REFERENCES matriculas(id) ON DELETE CASCADE,
    disciplina   VARCHAR(100) NOT NULL,
    incluida_em  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_item_disciplina_por_matricula UNIQUE (matricula_id, disciplina)
);

COMMENT ON TABLE  itens_matricula              IS 'Entidade interna do Aggregate Matricula — não referenciar diretamente de fora do Aggregate';
COMMENT ON COLUMN itens_matricula.matricula_id IS 'FK para o Aggregate Root (matriculas.id) — ON DELETE CASCADE';
COMMENT ON COLUMN itens_matricula.disciplina   IS 'Persiste NomeDisciplina.valor — VARCHAR sem tipo customizado';

-- Índice para a estratégia replace-all (DELETE por matricula_id no salvar())
CREATE INDEX idx_itens_matricula_id ON itens_matricula (matricula_id);
