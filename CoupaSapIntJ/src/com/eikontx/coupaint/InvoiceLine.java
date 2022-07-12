package com.eikontx.coupaint;

import java.util.Date;
import java.util.List;

public class InvoiceLine {
	
	Invoice inv;
	int invoiceId;
	int invoiceLineId;
	String description;
	String po;
	double price;
	double quantity;
	Date createdAt;
	double total;
	double accountingTotal;
	String category; //goods, services
	String assetId;
	String segment1;
	String segment2;
	String segment3;
	String segment4;
	String taxCode;
	double taxAmount;
	double taxRate;
	boolean accrueTax;
	double accruedTax;
	
	
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


	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}


	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}


	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}


	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}


	public double getAccountingTotal() {
		return accountingTotal;
	}
	public void setAccountingTotal(double accountingTotal) {
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

	
	public double getTaxRate() {
		return taxRate;
	}
	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}

	public double getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(double taxAmount) {
		this.taxAmount = taxAmount;
	}

	public boolean isAccrueTax() {
		return accrueTax;
	}

	public void setAccrueTax(boolean accrueTax) {
		this.accrueTax = accrueTax;
	}

	public double getAccruedTax() {
		return accruedTax;
	}

	public void setAccruedTax(double accruedTax) {
		this.accruedTax = accruedTax;
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

}
