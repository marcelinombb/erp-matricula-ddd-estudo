# Phase 8: Testes Unitários do Domínio DDD - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-23
**Phase:** 8-Testes Unitários do Domínio DDD
**Areas discussed:** Stub vs. Mockito para repositório, Organização dos arquivos de teste, Padrão de asserção de eventos, Nomenclatura e estrutura de pacotes

---

## Stub vs. Mockito para o Repositório

| Option | Description | Selected |
|--------|-------------|----------|
| Stub in-memory à mão | Classe MatriculaRepositorioEmMemoria implement MatriculaRepositorio — zero framework nos testes do domínio | ✓ |
| Mockito @Mock | Uma linha de setup, familiar para o dev — Mockito é framework externo nos testes do domínio | |
| Os dois, com comparativo | Mostrar ambas as abordagens com comentário didático | |

**User's choice:** Stub in-memory à mão
**Notes:** Implementar apenas `existeMatriculaAtiva()` — demais métodos lançam `UnsupportedOperationException`.

---

## Abrangência do Stub

| Option | Description | Selected |
|--------|-------------|----------|
| Apenas os métodos usados pelo Verificador | existeMatriculaAtiva() — demais UnsupportedOperationException | ✓ |
| Interface completa funcional | Todos os métodos com lista em memória — reutilizável nas fases seguintes | |

**User's choice:** Apenas os métodos usados pelo Verificador

---

## Organização dos Arquivos de Teste (Aggregate)

| Option | Description | Selected |
|--------|-------------|----------|
| Um arquivo por classe de produção | MatriculaTest.java, NomeDisciplinaTest.java — convencional, fácil de encontrar no IDE | ✓ |
| Um arquivo por comportamento | InvariantesMatriculaTest.java, EventosMatriculaTest.java — narrativa didática mas menos convencional | |

**User's choice:** Um arquivo por classe de produção

---

## Organização dos VOs

| Option | Description | Selected |
|--------|-------------|----------|
| Um arquivo por VO | NomeDisciplinaTest.java, PeriodoLetivoTest.java — cada VO com suas regras explícitas | ✓ |
| ValueObjectsTest.java único | Todos os VOs num só arquivo — mais compacto | |

**User's choice:** Um arquivo por VO

---

## Padrão de Asserção de Domain Events

| Option | Description | Selected |
|--------|-------------|----------|
| Coletar + verificar tipo e dados | var evento = (AlunoMatriculado) eventos.get(0); assertThat(evento.alunoId()).isEqualTo(...) | ✓ |
| Só verificar o tipo | assertThat(eventos.get(0)).isInstanceOf(AlunoMatriculado.class) | |

**User's choice:** Coletar + verificar tipo e dados

---

## Localização dos Testes de Eventos

| Option | Description | Selected |
|--------|-------------|----------|
| Dentro de MatriculaTest.java | Eventos são comportamento do Aggregate — junto com demais testes | ✓ |
| MatriculaEventosTest.java separado | Separa preocupações | |

**User's choice:** Dentro de MatriculaTest.java

---

## Estrutura de Pacotes dos Testes

| Option | Description | Selected |
|--------|-------------|----------|
| Espelhar src/main | dominio/modelo/MatriculaTest.java, dominio/vo/NomeDisciplinaTest.java — convenção Java | ✓ |
| Pacote único dominio/ | Todos os testes do domínio num só pacote | |

**User's choice:** Espelhar src/main

---

## Localização do Stub

| Option | Description | Selected |
|--------|-------------|----------|
| dominio/servico/ (junto ao VerificadorTest) | Stub como arquivo auxiliar no mesmo pacote do teste que o usa | ✓ |
| suporte/ ou fixtures/ (pacote auxiliar de testes) | Indica infraestrutura de teste, reutilizável | |

**User's choice:** dominio/servico/ junto ao VerificadorTest

---

## Claude's Discretion

- Nomes dos métodos de teste em português (padrão já estabelecido no projeto)
- Usar AssertJ para asserções fluentes (já disponível via spring-boot-starter-test)
- Padrão Given-When-Then com comentários explícitos nos testes

## Deferred Ideas

None — discussion stayed within phase scope.
