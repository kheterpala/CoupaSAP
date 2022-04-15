package com.eikontx.coupaint;

import java.util.Date;
import java.util.List;

public class InvoiceCharge {
	
	Invoice inv;
	int invoiceId;
	int invoiceChargeId;
	String lineType;
	Date createdAt;
	float total;
	float percent;
	float accountingTotal;
	String segment1;
	String segment2;
	String segment3;
	String segment4;
	String taxCode;
	
	List<JEntry> jEntries;
	
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

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public List<JEntry> getJEntries() {
		return jEntries;
	}

	public void setJEntries(List<JEntry> jEntries) {
		this.jEntries = jEntries;
	}
	
	
}

