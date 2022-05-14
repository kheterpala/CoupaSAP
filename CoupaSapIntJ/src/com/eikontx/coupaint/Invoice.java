package com.eikontx.coupaint;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Date;

public class Invoice {
	static String INV_DEBIT = "Invoice";
	static String INV_CREDIT = "Credit Note";
	
	int id;
	Date createdAt;
	String status;
	String documentType;
	Date invoiceDate;
	Date deliveryDate;	
	String invoiceNumber;
	String supplierNumber;
	String supplierName;
	String internalNote;
	float total;
	float accountingTotal;
	String accountingTotalCurrency;
	String currency;
	boolean taxLineTaxation;
	float totalTax;
	String taxCode;
	List<JEntry> jEntries = new ArrayList<JEntry>();;
	
	List<InvoiceLine> lines;
	List<InvoiceCharge> charges;
	
	public Invoice(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public Date getDeliveryDate() {
		return deliveryDate;
	}
	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public List<InvoiceLine> getLines() {
		return lines;
	}
	public void setLines(List<InvoiceLine> lines) {
		this.lines = lines;
	}

	public List<InvoiceCharge> getCharges() {
		return charges;
	}
	public void setCharges(List<InvoiceCharge> charges) {
		this.charges = charges;
	}

	public String getSupplierNumber() {
		return supplierNumber;
	}
	public void setSupplierNumber(String supplierNumber) {
		this.supplierNumber = supplierNumber;
	}	
	
	public String getDocumentType() {
		return documentType;
	}
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
	
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	
	public String getAccountingTotalCurrency() {
		return accountingTotalCurrency;
	}

	public void setAccountingTotalCurrency(String accountingTotalCurrency) {
		this.accountingTotalCurrency = accountingTotalCurrency;
	}

	public float getTotal() {
		return total;
	}
	public void setTotal(float total) {
		this.total = total;
	}

	public float getAccountingTotal() {
		return accountingTotal;
	}
	public void setAccountingTotal(float accountingTotal) {
		this.accountingTotal = accountingTotal;
	}

	public String getSupplierName() {
		return supplierName;
	}
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getInternalNote() {
		return internalNote;
	}
	public void setInternalNote(String internalNote) {
		this.internalNote = internalNote;
	}
	
	public boolean isTaxLineTaxation() {
		return taxLineTaxation;
	}
	public void setTaxLineTaxation(boolean taxLineTaxation) {
		this.taxLineTaxation = taxLineTaxation;
	}
	
	public float getTotalTax() {
		return totalTax;
	}
	public void setTotalTax(float totalTax) {
		this.totalTax = totalTax;
	}
	
	public String getTaxCode() {
		return taxCode;
	}
	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public String getJEType() {
		String type = "";
		
		if (this.getDocumentType().equals(Invoice.INV_DEBIT)) type = "KR";
		else if (this.getDocumentType().equals(Invoice.INV_CREDIT)) type = "KG";
		
		return type;
	}
	
	
	public List<JEntry> getJEntries() {
		return jEntries;
	}

	public void setJEntries(List<JEntry> jEntries) {
		this.jEntries = jEntries;
	}
	
	public String getFirstPO() {
		String po = "";
		
		Optional<InvoiceLine> result = this.getLines()
			      .stream().parallel()
			      .filter(line -> !line.getPo().equals("")).findFirst();

		if (result.isPresent()) po = result.get().getPo();
		
		return po;
	}
	
	public String getFirstCompanyCode() {
		String ccode = "";
		
		Optional<InvoiceLine> result = this.getLines()
			      .stream().parallel()
			      .filter(line -> !line.getSegment1().equals("")).findFirst();

		if (result.isPresent()) ccode = result.get().getSegment1();
		
		return ccode;
	}
	
	public String getFirstSegment3() {
		String segment = "";
		
		Optional<InvoiceLine> result = this.getLines()
			      .stream().parallel()
			      .filter(line -> !line.getSegment3().equals("")).findFirst();

		if (result.isPresent()) segment = result.get().getSegment3();
		
		return segment;
	}
	
	public String getFirstSegment4() {
		String segment = "";
		
		Optional<InvoiceLine> result = this.getLines()
			      .stream().parallel()
			      .filter(line -> !line.getSegment4().equals("")).findFirst();

		if (result.isPresent()) segment = result.get().getSegment4();
		
		return segment;
	}

}
