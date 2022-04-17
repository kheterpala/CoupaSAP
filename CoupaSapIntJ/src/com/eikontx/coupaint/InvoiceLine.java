package com.eikontx.coupaint;

import java.util.Date;
import java.util.List;

public class InvoiceLine {
	
	Invoice inv;
	int invoiceId;
	int invoiceLineId;
	String description;
	String po;
	float price;
	int quantity;
	Date createdAt;
	float total;
	float accountingTotal;
	String category; //goods, services
	String assetId;
	String segment1;
	String segment2;
	String segment3;
	String segment4;
	String taxCode;
	float taxAmount;
	
	List<JEntry> jEntries;
	
	public InvoiceLine(int invoiceId, int invoiceLineId) {
		this.invoiceId = invoiceId;
		this.invoiceLineId = invoiceLineId;
	}

	public Invoice getInv() {
		return inv;
	}
	public void setInv(Invoice inv) {
		this.inv = inv;
	}


	public int getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(int invoiceId) {
		this.invoiceId = invoiceId;
	}


	public int getInvoiceLineId() {
		return invoiceLineId;
	}
	public void setInvoiceLineId(int invoiceLineId) {
		this.invoiceLineId = invoiceLineId;
	}


	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}


	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}


	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}


	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
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

	public String getPo() {
		return po;
	}
	public void setPo(String po) {
		this.po = po;
	}
	
	public String getAssetId() {
		return assetId;
	}
	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}


	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	

	public String getTaxCode() {
		return taxCode;
	}
	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	
	public float getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(float taxAmount) {
		this.taxAmount = taxAmount;
	}

	
	public String getSegment1() {
		return segment1;
	}
	public void setSegment1(String segment1) {
		this.segment1 = segment1;
	}

	public String getSegment2() {
		return segment2;
	}
	public void setSegment2(String segment2) {
		this.segment2 = segment2;
	}

	public String getSegment3() {
		return segment3;
	}
	public void setSegment3(String segment3) {
		this.segment3 = segment3;
	}

	public String getSegment4() {
		return segment4;
	}
	public void setSegment4(String segment4) {
		this.segment4 = segment4;
	}


	public List<JEntry> getJEntries() {
		return jEntries;
	}
	public void setJEntries(List<JEntry> jEntries) {
		this.jEntries = jEntries;
	}

}
