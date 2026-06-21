---
phase: 05-diagnostico-codigo-com-anti-padroes
verified: 2026-06-21T23:30:00Z
status: human_needed
score: 6/7 must-haves verified
overrides_applied: 0
human_verification:
  - test: "Abrir MatriculaServiceImpl.java e 06-acoplamento-banco.md lado a lado: a documentação cita 'matriculaRepository.countDisciplinas()' na linha 51, mas o service chama 'itemMatriculaRepository.countByMatriculaId()'. Um estudante seguindo o trail de leitura da doc não encontra a chamada no service."
    expected: "Avaliar se a discrepância quebra a experiência de aprendizado: o conceito DIAG-06 ainda é demonstrável via countByMatriculaId, mas a doc aponta para o método errado."
    why_human: "Julgamento pedagógico: a discrepância é grave o suficiente para confundir um estudante que segue a documentação, ou é tolerável porque o método countDisciplinas existe e a explicação ainda funciona?"
  - test: "Verificar se DisciplinaServiceImpl.java (@Service mas nunca chamado por nenhum controller) funciona como material didático de DIAG-04. Ler 04-duplicacao-regras.md e checar se um estudante conseguiria entender o padrão sem executar a segunda rota HTTP."
    expected: "A duplicação de código é visível para leitura — o arquivo existe com comentários DIAG-04. Mas a frase 'quando DisciplinaServiceImpl foi criado para uma feature nova' soa como código live, quando na realidade é unreachable."
    why_human: "Julgamento pedagógico: duplicação via leitura de código (sem executar) é suficiente para DIAG-04, ou é necessário que o código seja ativo via HTTP para a experiência ser completa?"
gaps:
  - truth: "A documentação 06-acoplamento-banco.md e o código de serviço são consistentes: o método countDisciplinas citado na doc é o mesmo chamado no service"
    status: failed
    reason: "06-acoplamento-banco.md instrui o estudante a rastrear 'matriculaRepository.countDisciplinas()' mas MatriculaServiceImpl.adicionarDisciplina() chama 'itemMatriculaRepository.countByMatriculaId()'. countDisciplinas existe apenas em MatriculaRepository mas não é chamado por nenhum service. O trail de três arquivos descrito na doc (Matricula.java → MatriculaServiceImpl → MatriculaRepository.countDisciplinas → MatriculaMapper.xml) está quebrado no segundo passo."
    artifacts:
      - path: "erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java"
        issue: "Linha 149 chama itemMatriculaRepository.countByMatriculaId(), não matriculaRepository.countDisciplinas()"
      - path: "docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md"
        issue: "Linha 51 mostra 'int quantidadeAtual = matriculaRepository.countDisciplinas(matriculaId)' — método que nenhum service chama"
      - path: "erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java"
        issue: "countDisciplinas() declarado na linha 44 e mapeado no XML mas nunca invocado — dead code"
    missing:
      - "Alinhar código e doc: ou MatriculaServiceImpl.adicionarDisciplina() deve chamar matriculaRepository.countDisciplinas() e a doc está certa, ou remover countDisciplinas de MatriculaRepository e atualizar 06-acoplamento-banco.md para usar itemMatriculaRepository.countByMatriculaId()"
---

# Phase 05: Diagnóstico de Anti-Padrões — Verification Report

**Phase Goal:** Desenvolvedor consegue reconhecer e nomear seis anti-padrões recorrentes na arquitetura tradicional a partir de exemplos Java concretos e anotados
**Verified:** 2026-06-21T23:30:00Z
**Status:** human_needed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| #  | Truth                                                                                              | Status         | Evidence                                                                                                                 |
|----|----------------------------------------------------------------------------------------------------|----------------|--------------------------------------------------------------------------------------------------------------------------|
| 1  | DIAG-01: Código Java concreto demonstra Service Anêmico com todas as regras no Service             | VERIFIED       | MatriculaServiceImpl.java (227 linhas) com 4 marcadores ANTI-PADRAO: Service Anêmico (DIAG-01) em linhas 71,83,121,174  |
| 2  | DIAG-02: Código Java concreto demonstra Entidade Anêmica sem comportamento                         | VERIFIED       | 4 model classes (Matricula, Aluno, Turma, ItemMatricula) com marcadores ANTI-PADRAO: Entidade Anêmica (DIAG-02)          |
| 3  | DIAG-03: Código Java concreto demonstra Service Deus com 200+ linhas                               | VERIFIED       | MatriculaServiceImpl.java tem 227 linhas; marcadores DIAG-03 em linhas 19 e 200; documentação 03-service-deus.md existe  |
| 4  | DIAG-04: Código Java concreto demonstra Duplicação de Regras em dois Services                      | VERIFIED       | DisciplinaServiceImpl.java duplica if (!aluno.isAtivo()) com 5 marcadores DIAG-04; código legível mas DisciplinaServiceImpl não está exposto via HTTP (WR-01)  |
| 5  | DIAG-05: Código Java concreto demonstra Regras na Interface apenas no Controller                   | VERIFIED       | MatriculaController.java tem 6 marcadores ANTI-PADRAO: Regras na Interface (DIAG-05); 3 endpoints @PostMapping presentes |
| 6  | DIAG-06: Código Java concreto demonstra Acoplamento ao Banco com regra no repositório              | VERIFIED       | countByMatriculaId em ItemMatriculaRepository + countDisciplinas em MatriculaRepository; ANTI-PADRAO DIAG-06 marcado     |
| 7  | Documentação e código são consistentes: trail de leitura da doc leva ao código correto             | FAILED         | 06-acoplamento-banco.md cita matriculaRepository.countDisciplinas() mas service chama itemMatriculaRepository.countByMatriculaId(); trail quebrado                |

**Score:** 6/7 truths verified

### Deferred Items

Nenhum.

### Required Artifacts

| Artifact                                                                                              | Expected                                  | Status      | Details                                                                           |
|-------------------------------------------------------------------------------------------------------|-------------------------------------------|-------------|-----------------------------------------------------------------------------------|
| `pom.xml` (root)                                                                                      | Parent POM com 2 módulos                  | VERIFIED    | Contém `<module>erp-matricula-ddd</module>` e `<module>erp-matricula-camadas</module>` |
| `erp-matricula-ddd/pom.xml`                                                                           | Herda de erp-matricula-parent             | VERIFIED    | `<artifactId>erp-matricula-parent</artifactId>` na linha 23                       |
| `erp-matricula-camadas/pom.xml`                                                                       | POM do módulo camadas                     | VERIFIED    | Herda de erp-matricula-parent; mybatis-spring-boot-starter 3.0.5 explícito        |
| `Dockerfile.camadas`                                                                                  | Dockerfile para módulo camadas            | VERIFIED    | Contém `-pl erp-matricula-camadas`                                                |
| `init-db.sql`                                                                                         | Cria banco erp_matricula_camadas          | VERIFIED    | `CREATE DATABASE erp_matricula_camadas;`                                          |
| `docker-compose.yml`                                                                                  | Três serviços: postgres, app-ddd, app-camadas | VERIFIED | app-ddd (8080) e app-camadas (8081) declarados                                    |
| `erp-matricula-camadas/.../model/Matricula.java`                                                      | Entidade anêmica DIAG-02 + DIAG-06        | VERIFIED    | String status, UUID alunoId, sem adicionarDisciplina/cancelar; marcadores presentes |
| `erp-matricula-camadas/.../model/Aluno.java`                                                          | Entidade anêmica DIAG-02                  | VERIFIED    | isAtivo() getter (não estaAtivo() de domínio); marcadores presentes               |
| `erp-matricula-camadas/.../model/Turma.java`                                                          | Entidade anêmica DIAG-02                  | VERIFIED    | Sem periodoEstaAberto(); marcadores presentes                                     |
| `erp-matricula-camadas/.../model/ItemMatricula.java`                                                  | Entidade anêmica DIAG-02 + DIAG-06        | VERIFIED    | String disciplina; marcadores presentes                                           |
| `erp-matricula-camadas/.../service/MatriculaServiceImpl.java`                                         | Service Deus + Anêmico + DIAG-04 + DIAG-06 | VERIFIED  | 227 linhas; 8 marcadores ANTI-PADRAO; calls isAtivo() 3x; countByMatriculaId      |
| `erp-matricula-camadas/.../service/DisciplinaServiceImpl.java`                                        | Duplicação de Regras DIAG-04              | VERIFIED    | 5 marcadores DIAG-04; duplica isAtivo(); @Service mas não exposto via HTTP         |
| `erp-matricula-camadas/.../service/MatriculaService.java`                                             | Interface de serviço                      | VERIFIED    | `public interface MatriculaService`                                               |
| `erp-matricula-camadas/.../controller/MatriculaController.java`                                       | Regras na Interface DIAG-05               | VERIFIED    | 6 marcadores DIAG-05; 3 @PostMapping; matriculaService.matricular/adicionarDisciplina/cancelar |
| `erp-matricula-camadas/.../repository/MatriculaRepository.java`                                       | @Mapper com countDisciplinas (DIAG-06)    | VERIFIED*   | countDisciplinas declarado e anotado DIAG-06; porém NUNCA CHAMADO pelo service (dead code) |
| `erp-matricula-camadas/src/main/resources/mapper/MatriculaMapper.xml`                                 | resultMap + countDisciplinas SQL          | VERIFIED    | Namespace correto; resultMap presente; countDisciplinas SQL presente               |
| `erp-matricula-camadas/src/main/resources/db/migration/V1__schema.sql`                               | Schema com CREATE TABLE matriculas        | VERIFIED    | Cópia idêntica ao módulo DDD                                                      |
| `docs/00-ddd-sem-mudar-arquitetura/00-introducao.md`                                                  | Introdução ao módulo camadas              | VERIFIED    | Referências a erp-matricula-camadas e erp-matricula-ddd; tabela de navegação       |
| `docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md`                                             | Explicação DIAG-01 com snippet            | VERIFIED    | 4 referências a MatriculaServiceImpl; snippet de código presente                  |
| `docs/00-ddd-sem-mudar-arquitetura/02-entidade-anemica.md`                                            | Explicação DIAG-02 com snippet            | VERIFIED    | Referência a setStatus; snippet de código presente                                |
| `docs/00-ddd-sem-mudar-arquitetura/03-service-deus.md`                                                | Explicação DIAG-03 com snippet            | VERIFIED*   | Lista "200+" linhas; porém inclui limparMatriculasAntigas() que não existe no código |
| `docs/00-ddd-sem-mudar-arquitetura/04-duplicacao-regras.md`                                           | Explicação DIAG-04 com snippet            | VERIFIED    | 4 referências a DisciplinaServiceImpl; snippet com isAtivo() duplicado            |
| `docs/00-ddd-sem-mudar-arquitetura/05-regras-na-interface.md`                                         | Explicação DIAG-05 com snippet            | VERIFIED*   | 5 referências a MatriculaController; mas snippet usa LocalDate.isBefore() enquanto código usa String.startsWith("199") |
| `docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md`                                           | Explicação DIAG-06 com snippet            | STUB        | countDisciplinas citado como exemplo canônico mas o service chama countByMatriculaId — trail quebrado |

*VERIFIED com ressalva: substantivo e pedagogicamente útil mas com divergência entre doc e código real.

### Key Link Verification

| From                                         | To                                    | Via                         | Status    | Details                                                                              |
|----------------------------------------------|---------------------------------------|-----------------------------|-----------|--------------------------------------------------------------------------------------|
| `docker-compose.yml`                         | `Dockerfile`                          | app-ddd service build       | WIRED     | `dockerfile: Dockerfile` presente                                                    |
| `docker-compose.yml`                         | `Dockerfile.camadas`                  | app-camadas service build   | WIRED     | `dockerfile: Dockerfile.camadas` presente; porta 8081 mapeada                       |
| `pom.xml` (root)                             | `erp-matricula-ddd/pom.xml`           | Maven module hierarchy      | WIRED     | `<module>erp-matricula-ddd</module>`                                                 |
| `ErpMatriculaCamadasApplication.java`        | `br.com.escola.matricula.repository`  | @MapperScan                 | WIRED     | `@MapperScan("br.com.escola.matricula.repository")` linha 19                         |
| `MatriculaRepository.java`                   | `MatriculaMapper.xml`                 | namespace                   | WIRED     | namespace=`br.com.escola.matricula.repository.MatriculaRepository`                   |
| `MatriculaServiceImpl.adicionarDisciplina`   | `ItemMatriculaRepository.countByMatriculaId` | DIAG-06 count     | WIRED     | Linha 149: `itemMatriculaRepository.countByMatriculaId(matriculaId)`                 |
| `MatriculaRepository.countDisciplinas`       | `MatriculaServiceImpl`                | chamada de uso              | NOT_WIRED | countDisciplinas declarado mas nenhum service chama — dead code (CR-02)              |
| `DisciplinaServiceImpl`                      | `MatriculaController`                 | injeção via HTTP             | NOT_WIRED | DisciplinaServiceImpl é @Service mas nenhum controller o injeta (WR-01)              |
| `docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md` | `MatriculaServiceImpl` | trail de leitura    | BROKEN    | Doc instrui `matriculaRepository.countDisciplinas()` mas service usa método diferente |

### Data-Flow Trace (Level 4)

Não aplicável para fase pedagógica com código que não precisa estar wired para dados dinâmicos. O objetivo é leitura e compreensão do código, não execução de queries em produção.

### Behavioral Spot-Checks

| Behavior                                         | Command                                                                                        | Result                                               | Status |
|--------------------------------------------------|------------------------------------------------------------------------------------------------|------------------------------------------------------|--------|
| erp-matricula-camadas module compiles             | `mvn -pl erp-matricula-camadas compile` (verificação estrutural por análise)                   | SUMMARY.md Plan 04 confirma exit 0; Plan 03 confirma estrutural OK | PASS  |
| countDisciplinas in MatriculaRepository usado no service | `grep -rn "countDisciplinas" erp-matricula-camadas/src/main/java/` | Apenas em MatriculaRepository.java — NUNCA no service | FAIL   |
| 7 doc files em docs/00-ddd-sem-mudar-arquitetura | `ls docs/00-ddd-sem-mudar-arquitetura/ \| wc -l`                                               | 7                                                    | PASS   |
| MatriculaServiceImpl >= 200 linhas               | `wc -l MatriculaServiceImpl.java`                                                              | 227                                                  | PASS   |
| DisciplinaServiceImpl exposta via controller     | `grep -rn "DisciplinaServiceImpl" erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/` | 0 hits | FAIL  |

### Requirements Coverage

| Requirement | Source Plan | Description                                                          | Status          | Evidence                                                                                   |
|-------------|------------|----------------------------------------------------------------------|-----------------|--------------------------------------------------------------------------------------------|
| DIAG-01     | 05-04, 05-06 | Identifica Service Anêmico em código Java exemplo                   | SATISFIED       | MatriculaServiceImpl.java linhas 71,83,121,174 + 01-service-anemico.md                    |
| DIAG-02     | 05-02, 05-06 | Identifica Entidade Anêmica em código Java exemplo                  | SATISFIED       | Matricula.java, Aluno.java, Turma.java, ItemMatricula.java com marcadores + 02-entidade-anemica.md |
| DIAG-03     | 05-04, 05-06 | Identifica Service Deus em código Java exemplo                      | SATISFIED       | MatriculaServiceImpl.java 227 linhas com marcadores DIAG-03 + 03-service-deus.md          |
| DIAG-04     | 05-04, 05-06 | Identifica Duplicação de Regras em código Java exemplo              | PARTIALLY SATISFIED | DisciplinaServiceImpl.java duplica isAtivo() com marcadores DIAG-04; porém @Service nunca chamado — código existe para leitura mas não para execução |
| DIAG-05     | 05-05, 05-06 | Identifica Regras na Interface em código Java exemplo               | SATISFIED       | MatriculaController.java 6 marcadores DIAG-05 + 05-regras-na-interface.md                 |
| DIAG-06     | 05-03, 05-04, 05-06 | Identifica Acoplamento ao Banco em código Java exemplo       | PARTIALLY SATISFIED | countByMatriculaId wired no service; countDisciplinas dead code; doc aponta para método inexistente no service |
| DID-01      | 05-01 a 05-06 | Módulo "antes" com código completo e comentários explicativos       | SATISFIED       | erp-matricula-camadas completo com controller+service+repository+model compilando          |

### Anti-Patterns Found

| File                                                                                       | Line | Pattern                                              | Severity    | Impact                                                                  |
|--------------------------------------------------------------------------------------------|------|------------------------------------------------------|-------------|-------------------------------------------------------------------------|
| `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java` | 44 | `countDisciplinas()` declarado mas nunca chamado     | Blocker     | Documentação 06-acoplamento-banco.md aponta para este método — trail de leitura quebrado |
| `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java` | 80 | `turma` fetched via turmaRepository mas nunca usado após o fetch  | Warning | SELECT desnecessário a cada chamada de matricular(); incoerência com comentário que diz turma.periodoEstaAberto() deveria ser chamado |
| `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java` | 34 | `@Service` mas nunca injetado por nenhum controller  | Warning     | DIAG-04 demonstrável via leitura mas não via execução HTTP              |
| `docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md`                                  | 36   | Snippet usa `existsByAlunoIdAndPeriodo()` que não existe em MatriculaRepository | Info | Estudante que vai ao código não encontra o método; confusão potencial |
| `docs/00-ddd-sem-mudar-arquitetura/03-service-deus.md`                                     | 27   | Lista `limparMatriculasAntigas()` que não existe em MatriculaServiceImpl | Info | Método listado na doc não existe no código real                         |
| `docs/00-ddd-sem-mudar-arquitetura/05-regras-na-interface.md`                              | 29   | Snippet usa `LocalDate.isBefore()` mas código real usa `String.startsWith("199")` | Info | Divergência de implementação; periodoInicio é String, não LocalDate |
| `docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md`                                | 51   | `matriculaRepository.countDisciplinas()` — método dead code no service | Blocker | Trail de leitura pedagógico quebrado: estudante não encontra a chamada |
| `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java` | 156 | `item.setId(UUID.randomUUID())` — UUID descartado pois INSERT omite coluna id | Warning | Objeto em memória tem UUID incorreto após insert; latent data bug      |

### Human Verification Required

#### 1. Trail DIAG-06 Quebrado: Tolerância Pedagógica

**Test:** Abrir `06-acoplamento-banco.md` e seguir o trail de leitura descrito na seção "Como descobrir a regra": a doc instrui buscar `matriculaRepository.countDisciplinas()` em MatriculaServiceImpl. Grepar para confirmação de que o service não chama esse método (chama `itemMatriculaRepository.countByMatriculaId()` em vez disso).

**Expected:** Determinar se a discrepância é aceitável para os objetivos do material ou se quebra a experiência de forma inaceitável.

**Why human:** O concept DIAG-06 ainda está demonstrado via `countByMatriculaId` no service — o estudante ainda pode ver "regra no banco, não no modelo". Mas o trail específico de leitura descrito na doc está errado, o que pode criar confusão ao seguir o guia passo-a-passo. Julgamento pedagógico é necessário.

#### 2. DisciplinaServiceImpl @Service sem Controller — DIAG-04 vivo ou morto?

**Test:** Verificar se a duplicação de DIAG-04 (DisciplinaServiceImpl) precisa estar disponível via HTTP endpoint para ser pedagogicamente eficaz, ou se a leitura side-by-side dos dois arquivos Java é suficiente.

**Expected:** Confirmar se a ausência de endpoint HTTP para DisciplinaServiceImpl é aceitável ou se um endpoint deve ser adicionado para tornar o anti-padrão "vivo" durante demonstração.

**Why human:** O código de duplicação existe e é legível. Mas como está, um estudante não consegue demonstrar DIAG-04 via curl/Postman — só consegue lendo o código. Para um módulo de treinamento onde curl é parte do workflow de demonstração (conforme `00-introducao.md` menciona "responde às mesmas 3 requisições HTTP"), a ausência de endpoint pode ser uma lacuna prática.

### Gaps Summary

**Gap Principal — DIAG-06 Doc/Código Inconsistente:**

O anti-padrão DIAG-06 (Acoplamento ao Banco) está corretamente implementado no código com `ItemMatriculaRepository.countByMatriculaId()` anotado e wired ao service. Porém, a documentação pedagógica (`06-acoplamento-banco.md`) construiu um trail de leitura de 3 passos que cita `MatriculaRepository.countDisciplinas()` como o exemplo canônico — um método que existe na interface e no XML mas que NUNCA É CHAMADO pelo service. O service usa um método diferente (`countByMatriculaId`) em um repositório diferente (`ItemMatriculaRepository`).

Isso cria dois problemas pedagógicos tangíveis:
1. Um estudante que segue o guia "leia Matricula.java → leia MatriculaServiceImpl → trace até countDisciplinas" chega ao passo 2 e não encontra a chamada descrita.
2. Existem agora dois métodos de contagem em dois repositórios diferentes, criando ambiguidade sobre qual é o canônico.

**Correção mínima necessária:** Escolher uma das duas alternativas:
- (A) Atualizar `MatriculaServiceImpl.adicionarDisciplina()` linha 149 para chamar `matriculaRepository.countDisciplinas()` em vez de `itemMatriculaRepository.countByMatriculaId()`, tornando o código consistente com a doc; ou
- (B) Atualizar `06-acoplamento-banco.md` para referenciar `itemMatriculaRepository.countByMatriculaId()` e ajustar o trail de leitura — então `MatriculaRepository.countDisciplinas()` pode ser removido como dead code limpo.

**Issues secundários (WARNINGs — não bloqueiam o goal mas degradam qualidade):**
- CR-01: `turma` fetched but unused — useless DB query a cada matrícula
- WR-01: `DisciplinaServiceImpl` @Service nunca chamado — DIAG-04 demonstrável só por leitura, não por execução
- CR-03: `item.setId()` UUID descartado pela INSERT — latent data integrity bug
- IN-01..IN-03: Snippets nas docs divergem da implementação real (existsByAlunoIdAndPeriodo, LocalDate vs String, limparMatriculasAntigas)

---

_Verified: 2026-06-21T23:30:00Z_
_Verifier: Claude (gsd-verifier)_
