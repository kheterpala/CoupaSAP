package com.eikontx.coupaint;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceUtil {

	
	public List<Invoice> getInvoices(String csvContent) throws IOException {
		List<Invoice> invoices = new ArrayList<Invoice>();
		
		BufferedReader reader = new BufferedReader(new StringReader(csvContent));
		String invoiceContent = IntUtil.splitData(reader,"id","created-at",0,"Header");
		System.out.println(invoiceContent);
		
		Reader invReader = new StringReader(invoiceContent);
		CSVParser csvParser = new CSVParser(invReader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .withAllowMissingColumnNames());
		for (CSVRecord csvRecord : csvParser) {
			Integer id = Integer.parseInt(csvRecord.get("id"));
			Invoice inv = new Invoice(id);
			inv.setStatus(csvRecord.get("status"));
			inv.setDocumentType(csvRecord.get("document-type"));
			inv.setCurrency(csvRecord.get("currency"));
			inv.setInvoiceNumber(csvRecord.get("invoice-number"));
			inv.setCreatedAt(IntUtil.getDtFromUTC(csvRecord.get("created-at")));
			inv.setInvoiceDate(IntUtil.getDtFromUTC(csvRecord.get("invoice-date")));
			System.out.println("Zulut w Offset" +csvRecord.get("delivery-date"));
			inv.setDeliveryDate(IntUtil.getDtFromUTC(csvRecord.get("delivery-date")));
			System.out.println("Zulut w Offset" +IntUtil.getDtFromUTC(csvRecord.get("delivery-date")));
			inv.setSupplierNumber(csvRecord.get("supplier-number"));
			inv.setInternalNote(csvRecord.get("internal-note"));
			invoices.add(inv);
		}
		reader.close();
		
		
		//Lines
		reader = new BufferedReader(new StringReader(csvContent));
		String lineContent = IntUtil.splitData(reader,"invoice-line-id","created-at",0,"Line");
		List<InvoiceLine> lines = getInvoiceLines(lineContent);
		System.out.println("All Lines:" +lines.size());
		reader.close();

		
		//Charges
		reader = new BufferedReader(new StringReader(csvContent));
		String chargeContent = IntUtil.splitData(reader,"invoice-charge-id","created-at",0,"Charge");
		List<InvoiceCharge> charges = getInvoiceCharges(chargeContent);
		System.out.println("All Charges:" + charges.size());
		reader.close();

		invoices.forEach(inv -> {
			
			List<InvoiceLine> invoiceLines = lines.stream()
			.filter(invline -> ((Invoice) inv).getId() == invline.getInvoiceId())
			.collect(Collectors.toList());
			
			invoiceLines.forEach(line -> line.setInv((Invoice) inv));
			((Invoice) inv).setLines(invoiceLines);
			
			List<InvoiceCharge> invoiceCharges = charges.stream()
			.filter(invcharge -> ((Invoice) inv).getId() == invcharge.getInvoiceId())
			.collect(Collectors.toList());
			
			invoiceCharges.forEach(charge -> charge.setInv(inv));
			((Invoice) inv).setCharges(invoiceCharges);
		});
		
		
		
		return invoices;
		
	}
	
	public List<InvoiceLine> getInvoiceLines(String content) throws IOException {
		List invLines = new ArrayList<InvoiceLine>();
		
		Reader reader = new StringReader(content);
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .withAllowMissingColumnNames());
		
		for (CSVRecord csvRecord : csvParser) {
			
			if (csvRecord.get("invoice-id").equals("") || csvRecord.get("invoice-line-id").equals("")) continue;
			
			Integer invoiceId = Integer.parseInt(csvRecord.get("invoice-id"));
			Integer invoiceLineId = Integer.parseInt(csvRecord.get("invoice-line-id"));
			
			InvoiceLine invLine = new InvoiceLine(invoiceId,invoiceLineId);
			invLine.setCreatedAt(IntUtil.getDtFromUTC(csvRecord.get("created-at")));
			
			if (!csvRecord.get("total").equals("")) {
				Float total = Float.parseFloat(csvRecord.get("total"));
				invLine.setTotal(total);
			}
			
			if (!csvRecord.get("accounting-total").equals("")) {
				Float acTotal = Float.parseFloat(csvRecord.get("accounting-total"));
				invLine.setAccountingTotal(acTotal);
			}
			
			if (!csvRecord.get("price").equals("")) {
				Float price = Float.parseFloat(csvRecord.get("price"));
				invLine.setPrice(price);
			}
			if (!csvRecord.get("quantity").equals("")) {
				Integer quantity = Integer.parseInt(csvRecord.get("quantity"));
				invLine.setQuantity(quantity);
			}
			invLine.setDescription(csvRecord.get("description"));
			
			invLine.setPo(csvRecord.get("po-number"));
			
			invLine.setCompanyCode(csvRecord.get("segment-1"));
			invLine.setAssetId(csvRecord.get("segment-2"));
			invLine.setGlAccount(csvRecord.get("segment-2"));
			invLine.setCategory(csvRecord.get("category"));
			invLines.add(invLine);
		}
		
		return invLines;
		
	}
	
	
	public List<InvoiceCharge> getInvoiceCharges(String content) throws IOException {
		List invCharges = new ArrayList<InvoiceCharge>();
		
		Reader reader = new StringReader(content);
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .withAllowMissingColumnNames());
		
		for (CSVRecord csvRecord : csvParser) {
		
			if (csvRecord.get("invoice-id").equals("") || csvRecord.get("invoice-charge-id").equals("")) continue;
				
			Integer invoiceId = Integer.parseInt(csvRecord.get("invoice-id"));
			Integer invoiceChargeId = Integer.parseInt(csvRecord.get("invoice-charge-id"));
			
			InvoiceCharge invCharge = new InvoiceCharge(invoiceId,invoiceChargeId);
			invCharge.setLineType(csvRecord.get("line-type"));
			invCharge.setCreatedAt(IntUtil.getDtFromUTC(csvRecord.get("created-at")));
			
			if (!csvRecord.get("total").equals("")) {
				Float total = Float.parseFloat(csvRecord.get("total"));
				invCharge.setTotal(total);
			}
			
			if (!csvRecord.get("accounting-total").equals("")) {
				Float acTotal = Float.parseFloat(csvRecord.get("accounting-total"));
				invCharge.setAccountingTotal(acTotal);
			}
			
			
			if (!csvRecord.get("accounting-total").equals("")) {
				Float acTotal = Float.parseFloat(csvRecord.get("accounting-total"));
				invCharge.setAccountingTotal(acTotal);
			}
			invCharge.setAccountingTotalCurrency(csvRecord.get("accounting-total-currency"));
			
			
			invCharge.setCompanyCode(csvRecord.get("segment-1"));
			invCharge.setGlAccount(csvRecord.get("segment-2"));
			
			invCharges.add(invCharge);
		}
		
		return invCharges;
		
	}


	
}
