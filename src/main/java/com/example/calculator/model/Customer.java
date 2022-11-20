package com.example.calculator.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Customer data model.
 */
@Getter
@RequiredArgsConstructor
public class Customer {

    @NonNull
    private String name;

    @NonNull
    private String vatNumber;

    private Set<Document> documents = new HashSet<>();

    /**
     * Add a document for this customer
     * @param document A {@link com.example.calculator.model.Document Document} instance to add.
     */
    public void addDocument(Document document) {
        documents.add(document);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return name.equals(customer.name) && vatNumber.equals(customer.vatNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vatNumber);
    }
}
