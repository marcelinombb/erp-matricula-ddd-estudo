package br.com.escola.matricula.interfaces;

import br.com.escola.matricula.dominio.excecao.AlunoInativoException;
import br.com.escola.matricula.dominio.excecao.DisciplinaJaMatriculadaException;
import br.com.escola.matricula.dominio.excecao.LimiteDisciplinasExcedidoException;
import br.com.escola.matricula.dominio.excecao.MatriculaCanceladaException;
import br.com.escola.matricula.dominio.excecao.MatriculaDuplicadaException;
import br.com.escola.matricula.dominio.excecao.MatriculaNaoEncontradaException;
import br.com.escola.matricula.dominio.excecao.PeriodoFechadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Único ponto do sistema onde exceções do pacote dominio.excecao.* se transformam em respostas HTTP.
 * O domínio nunca importa HttpStatus, ResponseEntity ou qualquer tipo jakarta.ws — essa responsabilidade
 * pertence exclusivamente a esta classe.
 * Demonstra que a camada de domínio é independente do protocolo HTTP: o mesmo domínio poderia ser
 * exposto via CLI, gRPC ou mensageria sem alterar nenhuma linha das classes de exceção.
 *
 * <p><strong>Mapeamento exceção → HTTP:</strong>
 * <ul>
 *   <li>409 Conflict — conflitos de estado: disciplina duplicada, matrícula duplicada, matrícula cancelada</li>
 *   <li>422 Unprocessable Entity — violações de invariante: limite excedido, aluno inativo, período fechado</li>
 *   <li>404 Not Found — recurso inexistente: matrícula não encontrada</li>
 *   <li>400 Bad Request — dados inválidos: campos com validação Bean Validation falhada</li>
 *   <li>500 Internal Server Error — erros inesperados (fallback genérico)</li>
 * </ul>
 * </p>
 *
 * <p><strong>Por que concentrar aqui?</strong> Sem um {@code @ControllerAdvice} centralizado,
 * cada Controller precisaria de try/catch para cada exceção de domínio — duplicando o mapeamento
 * e espalhando conhecimento de HTTP pelo código de aplicação. Um único handler garante coerência
 * no formato de resposta de erro e elimina duplicação.</p>
 *
 * <p><strong>Formato das respostas de erro (em português):</strong>
 * <pre>{@code
 * // 400 / 409 / 422 / 404:
 * { "erro": "CODIGO_DO_ERRO", "mensagem": "Descrição legível" }
 *
 * // 422 com limite excedido (dados estruturados):
 * { "erro": "LIMITE_DISCIPLINAS_EXCEDIDO", "mensagem": "...", "limite": 6, "atual": 6 }
 *
 * // 400 com campos inválidos:
 * { "erro": "DADOS_INVALIDOS", "mensagem": "...", "campos": [{ "campo": "nome", "mensagem": "..." }] }
 * }</pre>
 * </p>
 */
@ControllerAdvice
public class ExcecaoHandler {

    private static final Logger log = LoggerFactory.getLogger(ExcecaoHandler.class);

    // -------------------------------------------------------------------------
    // Records de resposta de erro — Java 21 records sem Lombok (pedagógico)
    // -------------------------------------------------------------------------

    /**
     * Resposta padrão de erro — usado nos casos sem dados adicionais estruturados.
     *
     * @param erro     código do erro em UPPER_SNAKE_CASE (em português para consistência com domínio)
     * @param mensagem descrição legível do erro
     */
    public record ErroResponse(String erro, String mensagem) {}

    /**
     * Resposta de erro para limite excedido — inclui dados estruturados do limite.
     *
     * <p>Permite que o cliente exiba "você atingiu o limite de X disciplinas (atual: Y)"
     * sem parsear a mensagem de texto.</p>
     *
     * @param erro     código do erro
     * @param mensagem descrição legível
     * @param limite   número máximo configurado
     * @param atual    número atual de disciplinas na matrícula
     */
    public record ErroLimiteResponse(String erro, String mensagem, int limite, int atual) {}

    /**
     * Resposta de erro para validação de campos — lista os campos com problema.
     *
     * @param erro     código do erro
     * @param mensagem descrição geral
     * @param campos   lista de erros por campo
     */
    public record ErroCamposResponse(String erro, String mensagem, List<CampoErro> campos) {}

    /**
     * Detalhe de erro por campo individual.
     *
     * @param campo    nome do campo com validação falhada
     * @param mensagem mensagem de validação definida na anotação {@code @NotNull}/{@code @NotBlank}
     */
    public record CampoErro(String campo, String mensagem) {}

    // -------------------------------------------------------------------------
    // Handlers — 409 Conflict
    // -------------------------------------------------------------------------

    /**
     * DisciplinaJaMatriculadaException → 409 Conflict.
     *
     * <p>A mesma disciplina não pode aparecer duas vezes na mesma matrícula —
     * invariante do Aggregate {@code Matricula.adicionarDisciplina()}.</p>
     */
    @ExceptionHandler(DisciplinaJaMatriculadaException.class)
    public ResponseEntity<ErroResponse> handleDisciplinaJaMatriculada(DisciplinaJaMatriculadaException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponse("DISCIPLINA_JA_MATRICULADA", e.getMessage()));
    }

    /**
     * MatriculaDuplicadaException → 409 Conflict.
     *
     * <p>Um aluno pode ter apenas uma matrícula ativa por período — verificado
     * pelo {@code VerificadorElegibilidadeMatricula} antes da criação.</p>
     */
    @ExceptionHandler(MatriculaDuplicadaException.class)
    public ResponseEntity<ErroResponse> handleMatriculaDuplicada(MatriculaDuplicadaException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponse("MATRICULA_DUPLICADA", e.getMessage()));
    }

    /**
     * MatriculaCanceladaException → 409 Conflict.
     *
     * <p>Matrícula cancelada é um estado terminal — não aceita novas operações.
     * Lançada por {@code Matricula.adicionarDisciplina()} e {@code Matricula.cancelar()}.</p>
     */
    @ExceptionHandler(MatriculaCanceladaException.class)
    public ResponseEntity<ErroResponse> handleMatriculaCancelada(MatriculaCanceladaException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponse("MATRICULA_CANCELADA", e.getMessage()));
    }

    // -------------------------------------------------------------------------
    // Handlers — 422 Unprocessable Entity
    // -------------------------------------------------------------------------

    /**
     * LimiteDisciplinasExcedidoException → 422 Unprocessable Entity.
     *
     * <p>Retorna dados estruturados com {@code limite} e {@code atual} para que o
     * cliente possa exibir a mensagem sem parsear texto livre.
     * Demonstra que exceções com campos estruturados são preferíveis a strings genéricas.</p>
     */
    @ExceptionHandler(LimiteDisciplinasExcedidoException.class)
    public ResponseEntity<ErroLimiteResponse> handleLimiteDisciplinas(LimiteDisciplinasExcedidoException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroLimiteResponse(
                        "LIMITE_DISCIPLINAS_EXCEDIDO",
                        e.getMessage(),
                        e.getLimite(),
                        e.getAtual()
                ));
    }

    /**
     * AlunoInativoException → 422 Unprocessable Entity.
     *
     * <p>Aluno inativo não pode ser matriculado — verificado pelo
     * {@code VerificadorElegibilidadeMatricula} antes da criação da matrícula.</p>
     */
    @ExceptionHandler(AlunoInativoException.class)
    public ResponseEntity<ErroResponse> handleAlunoInativo(AlunoInativoException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("ALUNO_INATIVO", e.getMessage()));
    }

    /**
     * PeriodoFechadoException → 422 Unprocessable Entity.
     *
     * <p>Período letivo fechado não aceita novas matrículas —
     * verificado pelo {@code VerificadorElegibilidadeMatricula}.</p>
     */
    @ExceptionHandler(PeriodoFechadoException.class)
    public ResponseEntity<ErroResponse> handlePeriodoFechado(PeriodoFechadoException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("PERIODO_FECHADO", e.getMessage()));
    }

    // -------------------------------------------------------------------------
    // Handlers — 404 Not Found
    // -------------------------------------------------------------------------

    /**
     * MatriculaNaoEncontradaException → 404 Not Found.
     *
     * <p>Lançada pelos UseCases quando {@code repositorio.buscarPorId(id)} retorna
     * {@code Optional.empty()}. O cliente enviou um ID que não existe no sistema.</p>
     */
    @ExceptionHandler(MatriculaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleMatriculaNaoEncontrada(MatriculaNaoEncontradaException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse("MATRICULA_NAO_ENCONTRADA", e.getMessage()));
    }

    // -------------------------------------------------------------------------
    // Handlers — 400 Bad Request
    // -------------------------------------------------------------------------

    /**
     * MethodArgumentNotValidException → 400 Bad Request.
     *
     * <p>Lançada pelo Spring MVC quando {@code @Valid} falha na validação do
     * {@code @RequestBody}. Itera sobre os {@code FieldError}s para construir
     * uma lista estruturada de erros por campo.</p>
     *
     * <p>As mensagens de validação são as definidas nas anotações
     * {@code @NotNull(message = "...")} e {@code @NotBlank(message = "...")}
     * dos records de request — sempre em português.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroCamposResponse> handleValidacao(MethodArgumentNotValidException e) {
        List<CampoErro> camposComErro = e.getBindingResult().getFieldErrors().stream()
                .map((FieldError fieldError) -> new CampoErro(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroCamposResponse(
                        "DADOS_INVALIDOS",
                        "Campos inválidos na requisição",
                        camposComErro
                ));
    }

    // -------------------------------------------------------------------------
    // Handler de fallback — 500 Internal Server Error
    // -------------------------------------------------------------------------

    /**
     * Exception (fallback genérico) → 500 Internal Server Error.
     *
     * <p>Captura qualquer exceção não mapeada pelos handlers específicos.
     * A mensagem retornada é genérica para não vazar detalhes internos do sistema
     * (mitigação T-04-05: Information Disclosure via stack trace em respostas de erro).
     * O stack trace completo é logado internamente via {@code log.error()} para diagnóstico.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleErroInterno(Exception e) {
        log.error("Erro interno não tratado: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse("ERRO_INTERNO", "Erro interno do servidor"));
    }
}
