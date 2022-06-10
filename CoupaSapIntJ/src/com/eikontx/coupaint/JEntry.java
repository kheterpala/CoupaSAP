package com.eikontx.coupaint;

public class JEntry {
	
	static String ENTRY_STD = "Standard";
	static String ENTRY_ACC = "Accrual";
	
	String postingKey;
	String account;
	String txType = "";
	String itemText;
	float txCurAmt;
	float localCurAmt;
	String taxCode;
	String taxJurisdiction;
	String costCenter;
	String intOrderNumber;
	String assignmentNumber;
	String type;
	
	public JEntry() {
	}
	
	
	public String getPostingKey() {
		return postingKey;
	}
	public void setPostingKey(String postingKey) {
		this.postingKey = postingKey;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getTxType() {
		return txType;
	}
	public void setTxType(String txType) {
		this.txType = txType;
	}
	public String getItemText() {
		return itemText;
	}
	public void setItemText(String itemText) {
		this.itemText = itemText;
	}
	public float getTxCurAmt() {
		return txCurAmt;
	}
	public void setTxCurAmt(float txCurAmt) {
		this.txCurAmt = txCurAmt;
	}
	public float getLocalCurAmt() {
		return localCurAmt;
	}
	public void setLocalCurAmt(float localCurAmt) {
		this.localCurAmt = localCurAmt;
	}
	public String getTaxCode() {
		return taxCode;
	}
	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}
	public String getTaxJurisdiction() {
		return taxJurisdiction;
	}
	public void setTaxJurisdiction(String taxJurisdiction) {
		this.taxJurisdiction = taxJurisdiction;
	}
	public String getCostCenter() {
		return costCenter;
	}
	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}
	public String getIntOrderNumber() {
		return intOrderNumber;
	}
	public void setIntOrderNumber(String intOrderNumber) {
		this.intOrderNumber = intOrderNumber;
	}
	public String getAssignmentNumber() {
		return assignmentNumber;
	}
	public void setAssignmentNumber(String assignmentNumber) {
		this.assignmentNumber = assignmentNumber;
	}


	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	
	
}
