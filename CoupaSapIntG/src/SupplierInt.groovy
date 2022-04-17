import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import com.eikontx.coupaint.IntUtil
import com.eikontx.coupaint.Invoice

class SupplierInt {
	
	public static void main(String[] args) {
		String filepath = "/Users/kheterpala/Documents/SAP/EmployeesListTest.csv";
		
		try {
		File f = new File(filepath);
		if (f.isFile()) System.out.println("The file " + filepath + "exists !!");
				 
		BufferedReader reader = new BufferedReader(new FileReader(filepath));

		SupplierInt sc = new SupplierInt();

		String message = sc.processData(reader);
		
		System.out.println(message);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public String transform(String sapField, String sapData) {
		String ret = sapData;
		if (sapField.equals("EmployeeEmail")) {
			if (sapData.trim().toUpperCase().equals("X")) {
				ret = "Inactive";
			}
		}
		
		return ret;
	}
	
	public String processData(BufferedReader f) {
	//Body
	
		String[] COUPA_HDR = ["Name","Display Name","Id",
			"Content Groups","Status","Commodity",
			"Enterprise Code","Supplier Number",
			"Parent Company","Account Number","Tax ID","Tax Code",
			"DUNS","Online Store URL","Online Store Login",
			"Primary Contact Email","Primary Contact Phone Work",
			"Primary Contact Phone Mobile","Primary Contact Phone Fax",
			"Primary Contact Name Given","Primary Contact Name Family",
			"Primary Address Street1","Primary Address Street2","Primary Address City","Primary Address State",
			"Primary Address Postal Code","Primary Address Country Code","Primary Address Vat Number",
			"Primary Address Vat Country Code","Primary Address Local Tax Number",
			"Invoice Matching Level","PO Method","PO Change Method","Buyer Hold","Default Locale","PO Email",
			"Payment Method","Payment Terms","Shipping Terms","PO cXML URL","PO cXML Domain","PO cXML Identity",
			"PO cXML Supplier Domain","PO cXML Supplier Identity","PO cXML Protocol",
			"cXML SSL Version","Disable Cert Verify","PO cXML HTTP Basic Auth Username",
			"Allow cXML Invoicing","cXML Invoicing - Supplier Domain","cXML Invoicing - Supplier Identity",
			"cXML Invoicing - Buyer Domain","cXML Invoicing - Buyer Identity","cXML Invoicing Shared Key",
			"Savings (%)","On Hold","Invoice Emails","Always Route Invoices From This Supplier For Approval",
			"Allow Invoicing From CSN","Create Invoices with No Backing Document",
			"Allow Invoicing Choose Billing Account From CSN","Restricted account types",
			"Preferred Language","Preferred Currency","Country of Operation Code",
			"Allow Non-Backed Lines on PO Invoices","Website","Email domain","Default contact email",
			"Hold invoices for AP review","Allow CSP Access without Two Factor","Request change orders",
			"Send Email Added Notification","Enable for Dynamic Discounting","Dynamic Discounting Settings",
			"Supply Chain Finance Configurations","Only pay financed invoices via Coupa Pay",
			"Parent Business Entity Name","Tags","Preferred Commodities"]
		
		
		Map COUPA_SAP_MAP = ["Name" : "EmployeeName" , "Primary Contact Email" : "EmployeeEmail"]
		Map COUPA_DEF_MAP = ["Invoice Matching Level" : "2-way" , "PO Method" : "prompt", 
				"Primary Contact Email" : "test@eikontx.com", "Preferred Commodities" : "Gold"]
		
		
		
		StringBuffer buffer = new StringBuffer();
		
		for (int i=0;i<COUPA_HDR.length;i++) {
			buffer.append(IntUtil.escapeSpecialCharacters(COUPA_HDR[i]));
			if (i!=(COUPA_HDR.length-1))
				buffer.append(",");
		}
		buffer.append(System.lineSeparator());
		
		String sapField
		String sapValue
		String coupaField
		String coupaValue
		
		def csv_content = f.getText()
		int count = 0;
		Reader reader = new StringReader(csv_content);
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
				.withFirstRecordAsHeader()
				.withIgnoreHeaderCase()
				.withTrim()
				.withAllowMissingColumnNames());
			
		for (CSVRecord csvRecord : csvParser) {
			for (int i=0;i<COUPA_HDR.length;i++) {
				coupaField = COUPA_HDR[i];
				coupaValue = "";
				if (COUPA_DEF_MAP.containsKey(coupaField))
					coupaValue = COUPA_DEF_MAP.get(COUPA_HDR[i]);
				if (COUPA_SAP_MAP.containsKey(coupaField)) {
					sapField = COUPA_SAP_MAP.get(COUPA_HDR[i]);
					sapValue = csvRecord.get(sapField);
					coupaValue = transform(sapField,sapValue);
					
					//println "C -> $coupaField S -> $sapField Value=$coupaValue"
				}
				buffer.append(IntUtil.escapeSpecialCharacters(coupaValue));
				if (i!=(COUPA_HDR.length-1)) buffer.append(",");
			}
		
			buffer.append(System.lineSeparator());
			count++;
		}
		
		println ("Finished processing, processed $count records");
	   
		
		return buffer.toString();
	}

}
