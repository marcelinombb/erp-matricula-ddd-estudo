# Project Retrospective

*Documento vivo — atualizado após cada milestone. Lições alimentam o planejamento futuro.*

---

## Milestone: v1.0 — Projeto Didático DDD

**Shipped:** 2026-06-21
**Phases:** 4 | **Plans:** 15 | **Timeline:** 2 dias (2026-06-20 → 2026-06-21)
**Stats:** 117 commits, 138 arquivos, 23.869 linhas adicionadas · 42 arquivos Java (3.514 LOC) · 4.492 linhas Markdown

### What Was Built

- Documentação estratégica DDD completa: problema de negócio, linguagem ubíqua com 6 termos e Conceitos Ambíguos, Context Map Mermaid com 4 BCs e 3 eventos cross-context, 4 ADRs no template Nygard com comparativo de código antes/depois
- Documentação tática DDD completa: 7 Markdowns com padrões ERRADO/CERTO em Java 21 para todos os building blocks (VO, Entidade, Agregado, Domain Service, Domain Event, Repositório) + 4 diagramas Mermaid
- Implementação Java 21 completa: Aggregate Root Matricula com 3 invariantes encapsuladas, 3 casos de uso, schema Flyway, seeds, MyBatis com TypeHandlers e ResultMap JOIN+collection (sem N+1), 42 arquivos BUILD SUCCESS
- API REST + Docker: MatriculaController, ExcecaoHandler com mapeamento semântico de exceções, Dockerfile multi-stage, docker-compose.yml com healthcheck-chain
- Material didático comparativo: ddd-vs-camadas.md, guia-consulta.md conceito→arquivo, licoes-aprendidas.md, estrutura-pastas.md + ADRs enriquecidos com exemplos de código real

### What Worked

- **Wave-based parallelization nas fases:** Planos independentes (ex: 04-02 e 04-03) rodando em paralelo acelerou a execução sem conflitos de arquivos
- **ADRs na Fase 1 (antecipado):** Documentar decisões antes de implementar permitiu que todo o código referenciasse as justificativas já existentes — sem reescritas
- **GSD workflow completo:** discuss → plan → execute → code-review → verification funcionou como pipeline de qualidade; os code reviews capturaram 4 bugs reais antes do commit final
- **Padrão ERRADO/CERTO nos docs táticos:** Mostrar o antipadrão antes do padrão correto tornou a documentação pedagógica muito mais eficaz do que apenas mostrar o jeito certo
- **MyBatis TypeHandlers para VOs:** Decisão de separar CpfTypeHandler e PeriodoLetivoTypeHandler do resto validou-se como padrão pedagógico claro — cada TypeHandler é uma aula sobre a fronteira domínio/persistência

### What Was Inefficient

- **REQUIREMENTS.md com checkboxes não marcados:** Todos os 51 requirements foram entregues mas os checkboxes `[ ]` nunca foram atualizados durante a execução — traceability table ficou com "Pending" até o fechamento do milestone
- **One-liners ausentes nos SUMMARY.md:** Vários planos deixaram o campo `one_liner` em branco, o que dificultou a extração automática de accomplishments pelo gsd-sdk
- **Verificação manual deferida:** Os 3 cenários de teste em 04-VERIFICATION.md requerem Docker rodando e ficaram como human_needed — ideal teria sido documentar como executar os testes antes de fechar

### Patterns Established

- **Documentação progressiva por fase:** problema → linguagem → bounded contexts → tática → código → interface — cada fase referencia a anterior, criando uma narrativa coerente
- **Seção Conceitos Ambíguos no glossário:** Demonstrar que "Aluno" tem modelos distintos no BC Matrícula vs BC Acadêmico é um dos insights centrais do DDD e deve aparecer cedo
- **Código em português end-to-end:** Nomes de pacote, classe, método e variável em português funcionaram sem fricção e reforçaram a Linguagem Ubíqua de forma tangível
- **MatriculaRow como anti-corruption layer:** Separação explícita entre modelo relacional (MatriculaRow) e modelo de domínio (Matricula) via MatriculaRowMapper é o padrão central de infraestrutura DDD com MyBatis
- **Replace-all para coleções:** Deletar+reinserir os itens da Matrícula a cada save simplifica o Mapper sem perder a integridade — documentado com comentário no repositório

### Key Lessons

1. **Marcar requirements durante a execução:** Atualizar `[x]` no REQUIREMENTS.md conforme cada plano completa evita a inconsistência de "tudo entregue mas tudo Pending" no fechamento do milestone
2. **One-liner é obrigatório no SUMMARY.md:** O campo `one_liner` alimenta automatismos do GSD (progress bar, milestone stats, retrospective) — não preenchê-lo gera ruído na automação
3. **Verificação manual tem setup:** Para cenários que requerem infrastructure (Docker, banco real), documentar o passo a passo no VERIFICATION.md e executar antes de fechar — ou aceitar explicitamente o defer
4. **ADRs early pay forward:** Quanto mais cedo as decisões arquiteturais estão documentadas, mais consistente o código — vale a "antecipação" mesmo quebrando a sequência de fases planejada

### Cost Observations

- Modelo: claude-sonnet-4-6 (balanced profile)
- Sessions: múltiplas em 2 dias
- Notable: execução em waves paralelas maximizou throughput; code review por agente separado capturou bugs que passariam despercebidos em revisão manual

---

## Cross-Milestone Trends

### Process Evolution

| Milestone | Fases | Planos | Key Pattern |
|-----------|-------|--------|-------------|
| v1.0 | 4 | 15 | GSD discuss→plan→execute→review→verify pipeline completo |

### Cumulative Quality

| Milestone | Arquivos Java | LOC Java | Docs (linhas) | Build |
|-----------|--------------|----------|---------------|-------|
| v1.0 | 42 | 3.514 | 4.492 | ✓ SUCCESS |

### Top Lessons (Verified Across Milestones)

1. Documentar antes de implementar (ADRs antecipados) resulta em código mais consistente com menos retrabalho
2. Code review automatizado por agente especializado captura bugs reais — não é só cerimônia
