package br.com.escola.matricula.dominio.modelo;

import java.time.LocalDateTime;

/**
 * Estados possíveis de uma Matrícula, modelados como {@code sealed interface}.
 *
 * <p><strong>Por que {@code sealed interface} e não {@code enum}?</strong></p>
 *
 * <p>Um {@code enum} não carrega dados adicionais por estado. O estado {@code Cancelada}
 * precisa registrar {@code canceladaEm: LocalDateTime} — quando o cancelamento ocorreu —
 * para fins de histórico e auditoria. {@code Concluida} precisa de {@code concluidaEm}.
 * Com {@code enum}, esses dados ficariam como campos opcionais na classe {@code Matricula},
 * nulos na maioria dos estados — o compilador não saberia que {@code canceladaEm} só existe
 * quando o status é {@code Cancelada}.</p>
 *
 * <p>{@code sealed interface} com {@code record} interno resolve isso: cada estado carrega
 * exatamente seus próprios dados, sem campos desnecessários.</p>
 *
 * <p><strong>Como o pattern matching exaustivo elimina {@code if (status == "ATIVA")}:</strong></p>
 *
 * <pre>{@code
 * // Com sealed interface — switch exaustivo sem default:
 * String descricao = switch (matricula.getStatus()) {
 *     case StatusMatricula.Ativa a      -> "Em andamento";
 *     case StatusMatricula.Cancelada c  -> "Cancelada em " + c.canceladaEm();
 *     case StatusMatricula.Concluida cc -> "Concluída em " + cc.concluidaEm();
 *     // sem default: compilador garante que todos os estados são tratados
 *     // se um novo estado for adicionado, o compilador aponta TODOS os switches
 *     // que precisam ser atualizados — sem silent bug
 * };
 * }</pre>
 *
 * <p>Com {@code enum}: o {@code switch} precisa de {@code default} para compilar, o que
 * significa que um novo estado pode ser ignorado silenciosamente. Com {@code sealed interface},
 * o {@code switch} de pattern matching é exaustivo por definição.</p>
 */
public sealed interface StatusMatricula
        permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida {

    /**
     * Estado inicial da matrícula. A matrícula pode receber novas disciplinas e ser cancelada.
     */
    record Ativa() implements StatusMatricula {}

    /**
     * Estado terminal por cancelamento. Registra quando o cancelamento ocorreu.
     * Nenhuma operação de modificação é permitida após o cancelamento.
     *
     * @param canceladaEm momento em que o cancelamento foi registrado
     */
    record Cancelada(LocalDateTime canceladaEm) implements StatusMatricula {}

    /**
     * Estado terminal por conclusão do período letivo. Registra quando a conclusão ocorreu.
     *
     * @param concluidaEm momento em que a conclusão foi registrada
     */
    record Concluida(LocalDateTime concluidaEm) implements StatusMatricula {}
}
