/*The integration developer needs to create the method processData 
 This method takes Message object of package com.sap.gateway.ip.core.customdev.util 
which includes helper methods useful for the content developer:
The methods available are:
    public java.lang.Object getBody()
	public void setBody(java.lang.Object exchangeBody)
    public java.util.Map<java.lang.String,java.lang.Object> getHeaders()
    public void setHeaders(java.util.Map<java.lang.String,java.lang.Object> exchangeHeaders)
    public void setHeader(java.lang.String name, java.lang.Object value)
    public java.util.Map<java.lang.String,java.lang.Object> getProperties()
    public void setProperties(java.util.Map<java.lang.String,java.lang.Object> exchangeProperties) 
    public void setProperty(java.lang.String name, java.lang.Object value)
    public java.util.List<com.sap.gateway.ip.core.customdev.util.SoapHeader> getSoapHeaders()
    public void setSoapHeaders(java.util.List<com.sap.gateway.ip.core.customdev.util.SoapHeader> soapHeaders) 
       public void clearSoapHeaders()
 */

 import com.sap.gateway.ip.core.customdev.util.Message
import java.nio.charset.StandardCharsets
import com.eikontx.coupaint.*

def Message processData(Message message) {
    //Body 

	def messageLog = messageLogFactory.getMessageLog(message);
	def Map headers = message.getHeaders();
	def fileName = headers.get("CamelFileName")

	messageLog.addAttachmentAsString("Log#1", "Starting processing for $fileName", "text/plain");
	def body = message.getBody(java.lang.String) as String;
	messageLog.addAttachmentAsString("Log#2", body, "text/plain");
	
	def reader = message.getBody(java.io.Reader);
	def csvContent = reader.getText();

	
    String[] OUT_INV_HDR = ["XREF1","BUKRS","BLART","BLDAT","BUDAT","MONAT","BKTXT",
				"WAERS","XBLNR"];

	String[] OUT_INVLINE_HDR = ["NEWBS","NEWKO","NEWBW","SGTXT","WRBTR","DMBTR","MWSKZ",
			"TXJCD","KOSTL","AUFNR","ZUONR"];
		
	String delim = IntUtil.getProperty("sap_delim");
	boolean printHdr = false;
	
	InvoiceUtil iu = new InvoiceUtil();
		
	//Invoices with all content
	List<Invoice> invoices = iu.getInvoices(csvContent);
	System.out.println("All Invoices:" + invoices.size());
	
	StringBuffer buffer = new StringBuffer();
	if (printHdr) {
		for (int i=0;i<OUT_INV_HDR.length;i++) {
			buffer.append(IntUtil.escapeSpecialCharacters(OUT_INV_HDR[i]));
			buffer.append(delim);
		}
		for (int i=0;i<OUT_INVLINE_HDR.length;i++) {
			buffer.append(IntUtil.escapeSpecialCharacters(OUT_INVLINE_HDR[i]));
			if (i!=(OUT_INVLINE_HDR.length-1))
				buffer.append(delim);
		}
		buffer.append(System.lineSeparator());
	}
	
	String fiscalPeriod = "";
	int invoiceCount = 0;
	String invoiceList = "";
	invoices.each { inv ->
		

		String invNum = inv.getInvoiceNumber();
		String invError = inv.getErrorCode();
		
		if (inv.getErrorCode() != null) {
			messageLog.addAttachmentAsString("ErrorLog#1", "Error in invoice $invNum with error code $invError ", "text/plain");
		}
		else {
			invoiceList += invNum + ",";
		}

		Date postingDate = IntUtil.getPostingDate(inv.getInvoiceDate())
			
		buffer.append("ID-XREF1-" + inv.getId() + delim +
				  inv.getFirstCompanyCode() + delim + inv.getJEType() +  delim +
				  IntUtil.getSAPDtStr(inv.getInvoiceDate()) + delim +  IntUtil.getSAPDtStr(postingDate) + delim +
				   fiscalPeriod + delim +
				   inv.getFirstPO() + delim +  inv.getCurrency() + delim +
				   inv.getInvoiceNumber()+delim);
			 
		List<InvoiceLine> invLines = inv.getLines();
		List<InvoiceCharge> invCharges = inv.getCharges();
		invoiceCount++;
		
		int entryCount = 1; //Reset entry count
		List<JEntry> jEntries = inv.getJEntries();
		jEntries.each { jEntry ->
			if (entryCount > 1) for (int i=0;i<OUT_INV_HDR.length;i++) buffer.append(delim);
			
			buffer.append(jEntry.getPostingKey() + delim +  jEntry.getAccount() + delim +
			jEntry.getTxType() + delim +  IntUtil.escapeSpecialCharacters(jEntry.getItemText()) + delim +
			Math.abs(jEntry.getTxCurAmt()) + delim +  Math.abs(jEntry.getLocalCurAmt()) + delim + jEntry.getTaxCode() + delim +
			jEntry.getTaxJurisdiction() + delim +  jEntry.getCostCenter() + delim +
			jEntry.getIntOrderNumber() + delim + jEntry.getAssignmentNumber());
				 
			//*/
			
			buffer.append(System.lineSeparator());
			entryCount++;
		};
		                                                                                                                                                                               
	};	   
	
	messageLog.addAttachmentAsString("Log#3", "Finished processing for $fileName, processed $invoiceCount invoices: $invoiceList", "text/plain");
	message.setBody(buffer.toString());
	message.setHeader("Process_Number", '17');
	
	return message;
}