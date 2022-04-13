package com.eikontx.coupaint;

import java.util.Date;

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
	String companyCode;
	String category; //goods, services
	String assetId;
	String glAccount;
	
	
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
	
	public String getCompanyCode() {
		return companyCode;
	}
	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
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
	

	public String getGlAccount() {
		return glAccount;
	}
	public void setGlAccount(String glAccount) {
		this.glAccount = glAccount;
	}


	public String getPostingKey() {
		String postingKey = "";
				
		if (this.getAssetId().equals("00000")) postingKey = "";
		else if (! this.getAssetId().equals("")) postingKey = this.getAssetId();
		else if ((!this.getGlAccount().equals("")) && this.getInv().getDocumentType().equals(Invoice.INV_DEBIT)) postingKey = "40";
		else if ((!this.getGlAccount().equals("")) && this.getInv().getDocumentType().equals(Invoice.INV_CREDIT)) postingKey = "50";
		else if (this.getInv().getDocumentType().equals(Invoice.INV_DEBIT)) postingKey = "31";
		else if (this.getInv().getDocumentType().equals(Invoice.INV_CREDIT)) postingKey = "21";
		
		
		return postingKey;		
	}
	 
	public String getAccount() {
		String account = "";
				
		if (this.getAssetId().equals("00000")) account = "";
		else if (! this.getAssetId().equals("")) account = this.getAssetId();
		else if (! this.getGlAccount().equals("")) account = this.getGlAccount();
		else if (! this.getInv().getDocumentType().equals("")) account = this.getInv().getSupplierNumber();
		
		
		return account;		
	}
	
}
