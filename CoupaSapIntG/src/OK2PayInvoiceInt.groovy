import com.eikontx.coupaint.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class OK2PayInvoiceInt {
		
	public static void main(String[] args) {
		//String filepath = "/Users/kheterpala/Documents/SAP/InvoiceHeader_20220702_011329Z.csv";
		String filepath = "/Users/kheterpala/Documents/SAP/InvoiceHeader_20220708_202849Z.csv";
		
		
		try {
			File f = new File(filepath);
			if (f.isFile()) System.out.println("The file " + filepath + "exists !!");
			
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String csvContent = br.getText();
			
			OK2PayInvoiceInt sc = new OK2PayInvoiceInt();
			String processedMessage = sc.processData(csvContent);
			System.out.println(processedMessage);
			
			String fileName = f.getName();
			Pattern fileNamePattern = Pattern.compile("InvoiceHeader_(\\d+)_(\\w+).csv");
			Matcher fileNameMatcher = fileNamePattern.matcher(fileName);
			
			fileNameMatcher.find();
			System.out.println("File:" + fileName + " Pattern:" + fileNameMatcher.group(2) + "_" + IntUtil.getTodayTS());
			
			
		

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public String processData(String csvContent) {
	//Body
	
		
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
		int invoiceCount = 1;
		invoices.each { inv ->
			
			if (inv.getErrorCode() != null) {
				System.out.println("Error Invoice: " + inv.getId() + " with error code: " + inv.getErrorCode());
			}
			
			Date postingDate = IntUtil.getPostingDate(inv.getInvoiceDate());
			
			/*buffer.append("Invoice Id:" + inv.getId() + delim + 
				"company Code:" + inv.getFirstCompanyCode() + delim + "Doc Type:" + inv.getJEType() +  delim + 
				"CreatedAt:" + IntUtil.getSAPDtStr(inv.getCreatedAt()) + delim + "Posting Date:" + IntUtil.getSAPDtStr(postingDate) + delim +
				 "Fiscal Period:" + "" + delim + 
				 "Supplier #:" + inv.getSupplierNumber() + delim + "Note:" + inv.getInternalNote() + delim +
				 "PO:" + inv.getFirstPO() + delim + "Currency "  + inv.getCurrency() + delim + 
				 "Inv Number:" + inv.getInvoiceNumber() + delim);*/
			 
			
			buffer.append("ID-XREF1-" + inv.getId() + delim +
				  inv.getFirstCompanyCode() + delim + inv.getJEType() +  delim +
				  IntUtil.getSAPDtStr(inv.getInvoiceDate()) + delim +  IntUtil.getSAPDtStr(postingDate) + delim +
				   fiscalPeriod + delim +
				   inv.getFirstPO() + delim +  inv.getCurrency() + delim +
				   inv.getInvoiceNumber()+delim);

			invoiceCount++;
			
			int entryCount = 1; //Reset entry count
			List<JEntry> jEntries = inv.getJEntries();
			jEntries.each { jEntry ->
				if (entryCount > 1) for (int i=0;i<OUT_INV_HDR.length;i++) buffer.append(delim);
				/*
				buffer.append("Posting Key:" + jEntry.getPostingKey() + delim + "Account:" + jEntry.getAccount() + delim + 
					jEntry.getTxType() + delim + "Line Desc:" + IntUtil.escapeSpecialCharacters(jEntry.getItemText()) + delim + 
					"Total:" + jEntry.getTxCurAmt() + delim + "AC Total:"  + jEntry.getLocalCurAmt() + delim + "Tax Code:"  + jEntry.getTaxCode() + delim
					"TAX J:" + jEntry.getTaxJurisdiction() + delim + "Tax:" + invLine.getTaxAmount() + delim + "CC:" + jEntry.getCostCenter() + delim +
					"Ord#:" + jEntry.getIntOrderNumber() + delim + "Ass#:" + jEntry.getAssignmentNumber());
				*/
				
				///*
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
		
		return buffer.toString();
	}
	
}


