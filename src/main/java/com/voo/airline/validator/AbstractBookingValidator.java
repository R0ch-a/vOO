package com.voo.airline.validator;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.exception.BusinessException;

/**
 * Classe abstrata que define o <b>Template Method</b> para validação de reservas.
 *
 * <p>O método {@link #validate} é {@code final} — define a sequência obrigatória
 * de validação. Subclasses concretas implementam {@link #doValidate}, que é o
 * único ponto de variação.
 *
 * <p><b>Chain of Responsibility embutido:</b> cada validador pode encadear
 * o próximo via {@link #setNext}, formando uma cadeia limpa sem ifs aninhados
 * no Service.
 *
 * <p><b>Abstração + Herança:</b> subclasses não precisam se preocupar com
 * o encadeamento; apenas implementam sua regra específica.
 */
public abstract class AbstractBookingValidator {

    private AbstractBookingValidator next;

    /** Encadeia o próximo validador (fluent API). */
    public AbstractBookingValidator setNext(AbstractBookingValidator next) {
        this.next = next;
        return next;
    }

    /**
     * Template Method — executa esta validação e passa para o próximo
     * validador da cadeia, se houver.
     *
     * @throws BusinessException se a regra for violada
     */
    public final void validate(CreateBookingRequest request) {
        doValidate(request);
        if (next != null) {
            next.validate(request);
        }
    }

    /**
     * Ponto de extensão — cada subclasse implementa sua regra de negócio.
     *
     * @throws BusinessException se a validação falhar
     */
    protected abstract void doValidate(CreateBookingRequest request);

    /** Retorna o nome descritivo desta regra para fins de log. */
    public abstract String getRuleName();
}
