import com.eikontx.coupaint.*

class OK2PayInvoiceInt {
		
	public static void main(String[] args) {
		String filepath = "/Users/kheterpala/Documents/SAP/InvoiceHeader_20220415_181601Z.csv";
		
		try {
			File f = new File(filepath);
			if (f.isFile()) System.out.println("The file " + filepath + "exists !!");
			
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String csvContent = br.getText();
			
			OK2PayInvoiceInt sc = new OK2PayInvoiceInt();
			String processedMessage = sc.processData(csvContent);
			System.out.println(processedMessage);
		

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public String processData(String csvContent) {
	//Body
	
		String delim = "|";
		String[] OUT_INV_HDR = ["#","XREF1","BUKRS","BLART","BLDAT","BUDAT","MONAT","BKTXT",
				"WAERS","XBLNR"];

		String[] OUT_INVLINE_HDR = ["NEWBS","NEWKO","NEWBW","SGTXT","WRBTR","DMBTR","MWSKZ",
			"TXJCD","KOSTL","AUFNR","ZUONR"];

	
		InvoiceUtil iu = new InvoiceUtil();
		
		//Invoices with all content
		List<Invoice> invoices = iu.getInvoices(csvContent);
		System.out.println("All Invoices:" + invoices.size());
		
		StringBuffer buffer = new StringBuffer();
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
		
		int invoiceCount = 1;
		invoices.each { inv ->
			
			Date postingDate = IntUtil.getPostingDate();
			buffer.append("Count:" +invoiceCount + delim + "Invoice Id:" + inv.getId() + delim + 
				"company Code:" + inv.getFirstCompanyCode() + delim + "Doc Type:" + inv.getJEType() +  delim + 
				"CreatedAt:" + IntUtil.getSAPDtStr(inv.getCreatedAt()) + delim + "Posting Date:" + IntUtil.getSAPDtStr(postingDate) + delim +
				 "Fiscal Period:" + "" + delim + 
				 "Supplier #:" + inv.getSupplierNumber() + delim + "Note:" + inv.getInternalNote() + delim +
				 "PO:" + inv.getFirstPO() + delim + "Currency "  + inv.getCurrency() + delim + 
				 "Number:" + inv.getInvoiceNumber() + delim);
			List<InvoiceLine> invLines = inv.getLines();
			List<InvoiceCharge> invCharges = inv.getCharges();
			invoiceCount++;
			
			int entryCount = 1; //Reset entry count
			invLines.each { invLine ->
					List<JEntry> jEntries = invLine.getJEntries();
					jEntries.each { jEntry ->
						if (entryCount > 1) for (int i=0;i<OUT_INV_HDR.length;i++) buffer.append(delim);
						buffer.append("Posting Key:" + jEntry.getPostingKey() + delim + "Account:" + jEntry.getAccount() + delim + 
							jEntry.getTxType() + delim + "Line Desc:" + IntUtil.escapeSpecialCharacters(jEntry.getItemText()) + delim + 
							"Total:" + jEntry.getTxCurAmt() + delim + "AC Total:"  + jEntry.getLocalCurAmt() + delim + 
							"TAX J:" + jEntry.getTaxJurisdiction() + delim + "CC:" + jEntry.getCostCenter() + delim +
							"Ord#:" + jEntry.getIntOrderNumber() + delim + "Ass#:" + jEntry.getAssignmentNumber());
						buffer.append(System.lineSeparator());
						entryCount++;
					};
			};
			
			invCharges.each { invCharge ->
					List<JEntry> jEntries = invCharge.getJEntries();
					jEntries.each { jEntry ->
						if (entryCount > 1) for (int i=0;i<OUT_INV_HDR.length;i++) buffer.append(delim);
						buffer.append("Posting Key:" + jEntry.getPostingKey() + delim + "Account:" + jEntry.getAccount() + delim + 
							jEntry.getTxType() + delim + "Line Desc:" + IntUtil.escapeSpecialCharacters(jEntry.getItemText()) + delim + 
							"Total:" + jEntry.getTxCurAmt() + delim + "AC Total:"  + jEntry.getLocalCurAmt() + delim + 
							"TAX J:" + jEntry.getTaxJurisdiction() + delim + "CC:" + jEntry.getCostCenter() + delim +
							"Ord#:" + jEntry.getIntOrderNumber() + delim + "Ass#:" + jEntry.getAssignmentNumber());
						buffer.append(System.lineSeparator());
						entryCount++;
					};
			};
		};	   
		
		return buffer.toString();
	}
	
}


