package com.eikontx.coupaint;

import java.util.Date;

public class InvoiceCharge {
	
	Invoice inv;
	int invoiceId;
	int invoiceChargeId;
	String lineType;
	Date createdAt;
	float total;
	float percent;
	float accountingTotal;
	String accountingTotalCurrency;
	String companyCode;
	String glAccount;
	
	
	public InvoiceCharge(int invoiceId, int invoiceChargeId) {
		this.invoiceId = invoiceId;
		this.invoiceChargeId = invoiceChargeId;
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

	public int getInvoiceChargeId() {
		return invoiceChargeId;
	}

	public void setInvoiceChargeId(int invoiceChargeId) {
		this.invoiceChargeId = invoiceChargeId;
	}

	public String getLineType() {
		return lineType;
	}

	public void setLineType(String lineType) {
		this.lineType = lineType;
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

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}

	public float getAccountingTotal() {
		return accountingTotal;
	}
	public void setAccountingTotal(float accountingTotal) {
		this.accountingTotal = accountingTotal;
	}

	public String getAccountingTotalCurrency() {
		return accountingTotalCurrency;
	}
	public void setAccountingTotalCurrency(String accountingTotalCurrency) {
		this.accountingTotalCurrency = accountingTotalCurrency;
	}
	
	public String getCompanyCode() {
		return companyCode;
	}
	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getGlAccount() {
		return glAccount;
	}
	public void setGlAccount(String glAccount) {
		this.glAccount = glAccount;
	}

	
	public String getPostingKey() {
		String postingKey = "";
				
		if ((!this.getGlAccount().equals("")) && this.getInv().getDocumentType().equals(Invoice.INV_DEBIT)) postingKey = "40";
		else if ((!this.getGlAccount().equals("")) && this.getInv().getDocumentType().equals(Invoice.INV_CREDIT)) postingKey = "50";
		else if (this.getInv().getDocumentType().equals(Invoice.INV_DEBIT)) postingKey = "31";
		else if (this.getInv().getDocumentType().equals(Invoice.INV_CREDIT)) postingKey = "21";
		
		
		return postingKey;		
	}
	 
	public String getAccount() {
		String account = "";
				
		if (! this.getGlAccount().equals("")) account = this.getGlAccount();
		else if (! this.getInv().getDocumentType().equals("")) account = this.getInv().getSupplierNumber();
		
		
		return account;		
	}
	
}

