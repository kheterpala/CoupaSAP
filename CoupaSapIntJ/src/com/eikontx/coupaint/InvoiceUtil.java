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
		//System.out.println(invoiceContent);
		
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
			inv.setDeliveryDate(IntUtil.getDtFromUTC(csvRecord.get("delivery-date")));
			inv.setSupplierNumber(csvRecord.get("supplier-number"));
			inv.setSupplierName(csvRecord.get("supplier-name"));
			inv.setInternalNote(csvRecord.get("internal-note"));
			
			if (!csvRecord.get("total").equals("")) {
				Float total = Float.parseFloat(csvRecord.get("total"));
				inv.setTotal(total);
			}
			
			if (!csvRecord.get("accounting-total-amount").equals("")) {
				Float acTotal = Float.parseFloat(csvRecord.get("accounting-total-amount"));
				inv.setAccountingTotal(acTotal);
			}
			if (!csvRecord.get("line-level-taxation").equals("")) inv.setTaxLineTaxation(Boolean.parseBoolean(csvRecord.get("line-level-taxation")));
			
			if (!inv.isTaxLineTaxation() && !csvRecord.get("header-tax-amount").equals("")) {
				Float totalTax = Float.parseFloat(csvRecord.get("header-tax-amount"));
				inv.setTotalTax(totalTax);
			}
			
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
			float invTaxAmt = inv.getTotalTax();
			for (InvoiceLine line : invoiceLines) {
				if (line.getTotal() == 0) continue;
				//Set Line Level Tax
				if (!inv.isTaxLineTaxation()) {
					float taxRate = line.getTaxRate();
					float taxAmt = (float) (line.getAccountingTotal() * (taxRate/100.0));
					//System.out.println("Total:" + line.getAccountingTotal() + " Rate:" + taxRate + " Tax:" + taxAmt);
					taxAmt = (float) Math.round(taxAmt * 100) / 100;
					line.setTaxAmount(taxAmt);
					
					//System.out.println("Rounded Tax:" + line.getTaxAmount());
				}
				
				System.out.println("AC Total:" + line.getAccountingTotal() + " Tax:" + line.getTaxAmount());
				line.setAccountingTotal(line.getAccountingTotal() + line.getTaxAmount());
			
				
			    List<JEntry> jEntries = processEntries(line);
			    List<JEntry> invJEntries = line.getInv().getJEntries();
			    invJEntries.addAll(jEntries);
			    line.getInv().setJEntries(invJEntries);
			}
			
			((Invoice) inv).setLines(invoiceLines);
			
			List<InvoiceCharge> invoiceCharges = charges.stream()
			.filter(invcharge -> ((Invoice) inv).getId() == invcharge.getInvoiceId())
			.collect(Collectors.toList());
			
			invoiceCharges.forEach(charge -> charge.setInv(inv));
			for (InvoiceCharge charge : invoiceCharges) {
				if (charge.getAccountingTotal() == 0) continue;
			    List<JEntry> jEntries = processEntries(charge);
			    List<JEntry> invJEntries = charge.getInv().getJEntries();
			    invJEntries.addAll(jEntries);
			    charge.getInv().setJEntries(invJEntries);
			}
			((Invoice) inv).setCharges(invoiceCharges);
			
			JEntry vendorJEntry = getVendorJEntry(inv);
			List<JEntry> invoiceJEntries = inv.getJEntries();
			invoiceJEntries.add(0,vendorJEntry);
		    inv.setJEntries(invoiceJEntries);
		});
		
		return invoices;
		
	}
	
	
	private JEntry getAccountJEntry(InvoiceLine line) {
		String postingKey = null;
		String account = null;
		String invoiceType = line.getInv().getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT)) postingKey = "50";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "40";
		
		String supplierNum = line.getInv().getSupplierNumber();
		account = line.getSegment2();
		
		JEntry jEntry = getJEntry(postingKey, account,line.getDescription(),
					line.getTotal(), line.getAccountingTotal(),line.getTaxCode(),
					line.getSegment3(), line.getSegment4(), supplierNum);
		return jEntry;
	}
	
	private JEntry getVendorJEntry(Invoice inv) {
		String postingKey = null;
		String account = null;
		String invoiceType = inv.getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT)) postingKey = "21";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "31";
		
		String supplierNum = inv.getSupplierNumber();
		account = supplierNum;
		
		JEntry jEntry = getJEntry(postingKey, account,inv.getSupplierName(),
					inv.getTotal(), inv.getAccountingTotal(),inv.getTaxCode(),
					inv.getFirstSegment3(), inv.getFirstSegment4(), supplierNum);
		return jEntry;
	}
	
	
	private JEntry getAssetJEntry(InvoiceLine line) {
		String postingKey = "70";
		String account = null;
		
		String supplierNum = line.getInv().getSupplierNumber();
		account = line.getAssetId();
		
		JEntry jEntry = getJEntry(postingKey, account,line.getDescription(),
					line.getTotal(), line.getAccountingTotal(),line.getTaxCode(),
					line.getSegment3(), line.getSegment4(), supplierNum);
		jEntry.setTxType("100");
		return jEntry;
	}
	
	private JEntry getJEntry(String postingKey, String account, String itemText,
				float txCurAmt, float localCurAmt, String taxCode, String costCenter,
				String orderNum, String adssignmentNum) {
		JEntry jEntry = new JEntry();
		jEntry.setPostingKey(postingKey);
		jEntry.setAccount(account);
		jEntry.setItemText(itemText);
		jEntry.setTxCurAmt(txCurAmt);
		jEntry.setLocalCurAmt(localCurAmt);
		jEntry.setTaxCode(taxCode);
		jEntry.setCostCenter(costCenter);
		jEntry.setIntOrderNumber(orderNum);
		jEntry.setAssignmentNumber(adssignmentNum);

		return jEntry;
	}
	
	
	private JEntry getAccountJEntry(InvoiceCharge charge) {
		String postingKey = null;
		String account = null;
		String invoiceType = charge.getInv().getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT)) postingKey = "50";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "40";
		
		String supplierNum = charge.getInv().getSupplierNumber();
		account = charge.getSegment2();
		
		JEntry jEntry = getJEntry(postingKey, account,charge.getLineType(),
				charge.getTotal(), charge.getAccountingTotal(),charge.getTaxCode(),
				charge.getSegment3(), charge.getSegment4(), supplierNum);
		return jEntry;
	}
	
	
	
	private List<JEntry> processEntries(InvoiceLine line) {
		List<JEntry> jEntries = new ArrayList<JEntry>();
		
		jEntries.add(getAccountJEntry(line));
		if (!line.getAssetId().equals(""))
			jEntries.add(getAssetJEntry(line));
		
		return jEntries;
	}
	
	
	
	private List<JEntry> processEntries(InvoiceCharge charge) {
		List<JEntry> jEntries = new ArrayList<JEntry>();
		
		jEntries.add(getAccountJEntry(charge));
		
		return jEntries;
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
				Float quantity = Float.parseFloat(csvRecord.get("quantity"));
				invLine.setQuantity(quantity);
			}
			invLine.setDescription(csvRecord.get("description"));
			invLine.setPo(csvRecord.get("po-number"));
			
			invLine.setCategory(csvRecord.get("category"));
			invLine.setTaxCode(csvRecord.get("tax-code"));
			if (!csvRecord.get("tax-amount").equals("")) {
				Float tax = Float.parseFloat(csvRecord.get("tax-amount"));
				invLine.setTaxAmount(tax);
			}
			
			if (!csvRecord.get("rnd_tax_type").equals("") && csvRecord.get("rnd_tax_type").contains("-")) {
				String[] rndTaxArr = csvRecord.get("rnd_tax_type").split("-");
				Float taxRate = Float.parseFloat(rndTaxArr[1].trim());
				invLine.setTaxRate(taxRate);
			}
			
			String assetId = csvRecord.get("asset_id external_ref_num").equals("00000") ? "" : csvRecord.get("asset_id external_ref_num");
			assetId = csvRecord.get("asset_id external_ref_num").equals("0") ? "" : csvRecord.get("asset_id external_ref_num");
			invLine.setAssetId(assetId);
			
			invLine.setSegment1(csvRecord.get("segment-1"));
			invLine.setSegment2(csvRecord.get("segment-2"));
			invLine.setSegment3(csvRecord.get("segment-3"));
			invLine.setSegment4(csvRecord.get("segment-4"));
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
			
			invCharge.setSegment1(csvRecord.get("segment-1"));
			invCharge.setSegment2(csvRecord.get("segment-2"));
			invCharge.setSegment3(csvRecord.get("segment-3"));
			invCharge.setSegment4(csvRecord.get("segment-4"));
			
			invCharge.setTaxCode(csvRecord.get("tax-code"));
			if (!csvRecord.get("tax-amount").equals("")) {
				Float tax = Float.parseFloat(csvRecord.get("tax-amount"));
				invCharge.setTaxAmount(tax);
			}
			
			invCharges.add(invCharge);
		}
		
		return invCharges;
		
	}
	
}
