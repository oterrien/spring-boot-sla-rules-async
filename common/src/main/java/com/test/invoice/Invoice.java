package com.test.invoice;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "INVOICE")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SELLER")
    private String seller;

    @Column(name = "DEBTOR")
    private String debtor;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "INVOICE_DATE")
    private String invoiceDate;

    @Column(name = "NUMBER_OF_INVOICES")
    private Integer numberOfInvoices;

}
