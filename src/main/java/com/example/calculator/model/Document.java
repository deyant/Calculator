package com.example.calculator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Document data model.
 */
@Getter
@Builder(builderMethodName = "internalBuilder")
public class Document {

    @NonNull
    private String documentNumber;
    @NonNull
    private DocumentType documentType;
    private String parentDocumentNumber;
    private BigDecimal total;
    private Currency currency;

    public static DocumentBuilder builder(final String documentNumber, final DocumentType documentType) {
        return internalBuilder().documentNumber(documentNumber).documentType(documentType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return documentNumber.equals(document.documentNumber)
                && documentType == document.documentType
                && Objects.equals(parentDocumentNumber, document.parentDocumentNumber)
                && Objects.equals(total, document.total)
                && Objects.equals(currency, document.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentNumber);
    }
}
