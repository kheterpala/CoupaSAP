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
import java.util.Optional;
import java.util.stream.Collectors;

public class InvoiceUtil {

	
	public List<Invoice> getInvoices(String csvContent) throws IOException {
		List<Invoice> invoices = new ArrayList<Invoice>();
		
		BufferedReader reader = new BufferedReader(new StringReader(csvContent));
		String invoiceContent = IntUtil.splitData(reader,"id","created-at",0,"Header");
		//System.out.println("content:" + invoiceContent);
		
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
			inv.setAccountingTotalCurrency(csvRecord.get("accounting-total-currency"));
			inv.setInvoiceNumber(csvRecord.get("invoice-number"));
			inv.setCreatedAt(IntUtil.getDtFromUTC(csvRecord.get("created-at")));
			inv.setInvoiceDate(IntUtil.getDtFromUTC(csvRecord.get("invoice-date")));
			inv.setDeliveryDate(IntUtil.getDtFromUTC(csvRecord.get("delivery-date")));
			inv.setSupplierNumber(csvRecord.get("supplier-number"));
			
			
			if (inv.getSupplierNumber().equals("")) inv.setSupplierNumber("200010");
			
			inv.setSupplierName(csvRecord.get("supplier-name"));
			inv.setInternalNote(csvRecord.get("internal-note"));
			
			if (!csvRecord.get("total").equals("")) {
				System.out.println("Excel Inv Total :" + csvRecord.get("total"));
				Double total = Double.parseDouble(csvRecord.get("total"));
				System.out.println("Excel Parsed Inv Total :" + total);
				inv.setTotal(total);
			}
			
			System.out.println("Excel Parsed Inv Total :" + inv.getTotal());
			


			
			if (!csvRecord.get("accounting-total-amount").equals("")) {
				Double acTotal = Double.parseDouble(csvRecord.get("accounting-total-amount"));
				inv.setAccountingTotal(acTotal);
			}
			inv.setTaxCode(csvRecord.get("header-tax-code"));

			if (!csvRecord.get("line-level-taxation").equals("")) inv.setTaxLineTaxation(Boolean.parseBoolean(csvRecord.get("line-level-taxation")));
			
			if (!inv.isTaxLineTaxation() && !csvRecord.get("header-tax-amount").equals("")) {
				Double hdrTax = Double.parseDouble(csvRecord.get("header-tax-amount"));
				inv.setHeaderTax(hdrTax);
			}
			
			if (!inv.isTaxLineTaxation() && !csvRecord.get("tax-due-to-supplier").equals("")) {
				Double totalTax = Double.parseDouble(csvRecord.get("tax-due-to-supplier"));
				inv.setTotalTax(totalTax);
			}
			
			if (!csvRecord.get("handling_accural").equals("")) inv.setHandlingAccural(Boolean.parseBoolean(csvRecord.get("handling_accural")));
			if (inv.isHandlingAccural()  && !csvRecord.get("accrued_handling_tax_amount").equals("")) {
				Double handlingAccrualTax = Double.parseDouble(csvRecord.get("accrued_handling_tax_amount"));
				inv.setHandlingAccrualTax(handlingAccrualTax);
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

		processInvoices(invoices,lines,charges);
		
		return invoices;
		
	}
	
	private double getRound2Dec(double amt) {
		return (double) Math.round(amt * 100) / 100;
	}
	
	private boolean allocateTaxes(Invoice inv, List<InvoiceLine> lines, List<InvoiceCharge> charges) {
		double totTaxAmt = 0;
		
		// Need to allocate to prepay lines ?
		
		for (InvoiceLine line : lines) {
			if (line.getTotal() == 0) continue;
			double taxRate = line.getTaxRate();
			double taxAmt = (double) (line.getAccountingTotal() * (taxRate/100.0));
			System.out.println(" Rate:" + taxRate + " Tax:" + taxAmt);
			taxAmt = getRound2Dec(taxAmt);
			double taxAccrual = line.getAccruedTax();
			double diff = taxAmt - taxAccrual;
			if (diff > 0) {
				line.setTaxAmount(diff);
				totTaxAmt += diff;
			}
		}
		for (InvoiceCharge charge : charges) {
			if (charge.getTotal() == 0) continue;
			totTaxAmt += charge.getTaxAmount();
		}
		totTaxAmt = getRound2Dec(totTaxAmt);
		
		System.out.println(" Total Vendor:" + inv.getTotalTax() + " Allocated Tax:" + totTaxAmt);

		double adjTaxAmt = inv.getTotalTax() - totTaxAmt;
		double taxAdjLimit = Double.parseDouble(IntUtil.getProperty("tax_adj_limit"));
		
		boolean ret = true;
		if (Math.abs(adjTaxAmt) > taxAdjLimit) {
			System.out.println(" Tot Tax:" + totTaxAmt + " Inv Tax:" + inv.getTotalTax());
			System.out.println(" Diff Limit:" + taxAdjLimit + " Diff:" + adjTaxAmt);
			ret = false;
		}
		else if (adjTaxAmt != 0) {
			Optional<InvoiceLine> result = lines
				      .stream().parallel()
				      .filter(line -> line.getTaxAmount() != 0).findFirst();
			
			if (result.isPresent()) {
				InvoiceLine lastLine = result.get();
				double taxAmt = lastLine.getTaxAmount();
				taxAmt += adjTaxAmt;
				taxAmt = getRound2Dec(taxAmt);
				lastLine.setTaxAmount(taxAmt);
				System.out.println("Line Adjusted by:" + adjTaxAmt + " to:" + taxAmt);
			}
		}
		
		return ret;
	}
	
	private void processInvoices(List<Invoice> invoices, List<InvoiceLine> lines, List<InvoiceCharge> charges) {
		
		invoices.forEach(inv -> {
			
			List<InvoiceLine> invoiceLines = lines.stream()
			.filter(invline -> ((Invoice) inv).getId() == invline.getInvoiceId())
			.collect(Collectors.toList());
			
			invoiceLines.forEach(line -> line.setInv((Invoice) inv));
			
			
			List<InvoiceCharge> invoiceCharges = charges.stream()
			.filter(invcharge -> ((Invoice) inv).getId() == invcharge.getInvoiceId())
			.collect(Collectors.toList());
			
			invoiceCharges.forEach(charge -> charge.setInv(inv));
			
			
			if (!inv.isTaxLineTaxation() && invoiceLines.size() > 0) {
				if (!allocateTaxes(inv,invoiceLines,invoiceCharges)) {
					inv.setErrorCode("TaxAlloc-Err");
				}
			}
			
			double invTaxAmt = inv.getTotalTax();
			double totalCharges = 0;
			for (InvoiceCharge charge : invoiceCharges) {
				if (charge.getTotal() == 0) continue;
				totalCharges += charge.getAccountingTotal();
			}
			
			//System.out.println("Before AC Total" + inv.getAccountingTotal() + "charged:" + totalCharges + " TTax:" + inv.getTotalTax() );
			//Add charges and taxes to Invoice a/c total
			double acTotal = inv.getAccountingTotal() + totalCharges + inv.getTotalTax();
			inv.setAccountingTotal(getRound2Dec(acTotal));
			
			//System.out.println("After AC Total" + acTotal + " Total Tax:" +inv.getTotalTax() +  " Hdr Tax:" +  inv.getHeaderTax());
			
			if (inv.getCurrency().equals(inv.getAccountingTotalCurrency()) 
					&& inv.getErrorCode() == null
					&& (inv.getTotal() != inv.getAccountingTotal()))
				inv.setErrorCode("ACTotal-Err");
			
			for (InvoiceLine line : invoiceLines) {
				if (line.getAccountingTotal() == 0) continue;
				
				//System.out.println("Line AC Tot:" +line.getAccountingTotal() + " Line Tax:" + line.getTaxAmount() );
				line.setAccountingTotal(line.getAccountingTotal() + line.getTaxAmount());
				
				if (inv.getCurrency().equals(inv.getAccountingTotalCurrency()))
					line.setTotal(line.getTotal() + line.getTaxAmount());
				
			    List<JEntry> jEntries = processEntries(line);
			    List<JEntry> invJEntries = line.getInv().getJEntries();
			    invJEntries.addAll(jEntries);
			    line.getInv().setJEntries(invJEntries);
			}
			
			((Invoice) inv).setLines(invoiceLines);
 
			String shipGlAccount = IntUtil.getProperty("ship_gl_ac");
			for (InvoiceCharge charge : invoiceCharges) {
				if (charge.getAccountingTotal() == 0) continue;
				charge.setAccountingTotal(charge.getAccountingTotal() + charge.getTaxAmount());
				
				if (inv.getCurrency().equals(inv.getAccountingTotalCurrency()))
					charge.setTotal(charge.getTotal() + charge.getTaxAmount());
				
				if (!shipGlAccount.equals("")) {
					charge.setSegment2(shipGlAccount);
					charge.setSegment3(inv.getFirstSegment3());
				}

				List<JEntry> jEntries = processEntries(charge);
			    List<JEntry> invJEntries = charge.getInv().getJEntries();
			    invJEntries.addAll(jEntries);
			    charge.getInv().setJEntries(invJEntries);
			}
			((Invoice) inv).setCharges(invoiceCharges);
			
			JEntry vendorJEntry = getVendorJEntry(inv);
			List<JEntry> invoiceJEntries = inv.getJEntries();
			invoiceJEntries.add(0,vendorJEntry);
			
			List<JEntry> jEntries = getHandlingJEntries(inv);
			if (!jEntries.isEmpty()) invoiceJEntries.addAll(jEntries);
			
		    inv.setJEntries(invoiceJEntries);
		    
		    
		    double entryACTotal = 0;
		    for (int i=1; i < invoiceJEntries.size(); i++) {
		    	JEntry entry = invoiceJEntries.get(i);
		    	if (entry.getType().equals(JEntry.ENTRY_STD)) {
		    		entryACTotal += entry.getLocalCurAmt();
		    		System.out.println("Amt:" +entry.getLocalCurAmt() + " Total:" + entryACTotal );
		    	}
		    }
		    
		    System.out.println("Inv Amt:" +inv.getTotal() + " Local Amt:" + inv.getAccountingTotal());
		    
		    //if (getRound2Dec(entryACTotal) != inv.getAccountingTotal()) inv.setErrorCode("ACTotalvsLineTotal-Err");
		    
		});
		
	}
 	
	private JEntry getAccountJEntry(InvoiceLine line) {
		String postingKey = null;
		String invoiceType = line.getInv().getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT) || line.getTotal() < 0) postingKey = "50";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "40";
		
		String supplierNum = line.getInv().getSupplierNumber();
		String account = line.getSegment2();
		String costCenter = line.getSegment3();
		String orderNum = line.getSegment4();
		
		JEntry jEntry = getJEntry(postingKey, account,line.getDescription(),
					line.getTotal(), line.getAccountingTotal(),line.getTaxCode(),
					costCenter, orderNum, supplierNum, line.getInv().getTaxCode(), JEntry.ENTRY_STD);
		return jEntry;
	}
	
	private JEntry getVendorJEntry(Invoice inv) {
		String postingKey = null;
		String invoiceType = inv.getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT)) postingKey = "21";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "31";
		
		String supplierNum = inv.getSupplierNumber();
		String account = supplierNum;
		
		String costCenter = "";
		String orderNum = "";
		
		JEntry jEntry = getJEntry(postingKey, account,inv.getSupplierName(),
					inv.getTotal(), inv.getAccountingTotal(),inv.getTaxCode(),
					costCenter, orderNum, supplierNum, inv.getTaxCode(), JEntry.ENTRY_STD);
		return jEntry;
	}
	
	
	private JEntry getAssetJEntry(InvoiceLine line) {
		String postingKey = null;
		String txType = null;
		
		String invoiceType = line.getInv().getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT) || line.getTotal() < 0) {
			postingKey = "75";
			txType = "105";
		}
		else if (invoiceType.equals(Invoice.INV_DEBIT)) {
			postingKey = "70";
			txType = "100";
		}
		
		String supplierNum = line.getInv().getSupplierNumber();
		String account = line.getAssetId();
		String costCenter = "";
		String orderNum = line.getSegment4();
		
		JEntry jEntry = getJEntry(postingKey, account,line.getDescription(),
					line.getTotal(), line.getAccountingTotal(),line.getTaxCode(),
					costCenter, orderNum, supplierNum, line.getInv().getTaxCode(), JEntry.ENTRY_STD);
		
		jEntry.setTxType(txType);
		return jEntry;
	}
	
	
	private JEntry getJEntry(String postingKey, String account, String itemText,
			double txCurAmt, double localCurAmt, String taxCode, String costCenter,
			String orderNum, String assignmentNum,  String invTaxCode, String type) {
		
		JEntry jEntry = new JEntry();
		jEntry.setPostingKey(postingKey);
		jEntry.setAccount(account);
		jEntry.setItemText(itemText);
		jEntry.setTxCurAmt(txCurAmt);
		jEntry.setLocalCurAmt(localCurAmt);
		jEntry.setCostCenter(costCenter);
		jEntry.setIntOrderNumber(orderNum);
		jEntry.setAssignmentNumber(assignmentNum);
		
		String [] taxCodeArr;
		String sapTaxCode = "I0";
		String sapTaxJurisdiction = IntUtil.getProperty("CA_tax_jurisdiction");
		
		
		if (invTaxCode.contains("-")) {
			taxCodeArr = invTaxCode.split("-");
			
			String taxJurisdictionState = taxCodeArr[1].trim();
			if (taxJurisdictionState.matches("[A-Za-z]{2}"))
				sapTaxJurisdiction = IntUtil.getProperty(taxJurisdictionState + "_tax_jurisdiction");
		}
		
		if (!taxCode.equals("")) {
			taxCodeArr = taxCode.split("-");
			sapTaxCode = taxCodeArr[0].trim();			
		}
		
		jEntry.setTaxCode(sapTaxCode);
		jEntry.setTaxJurisdiction(sapTaxJurisdiction);
		
		jEntry.setType(type);
		
		return jEntry;
	}
	
	
	private List<JEntry> getHandlingJEntries(Invoice inv) {
		List<JEntry> jEntries = new ArrayList<JEntry>();
		
		if (inv.isHandlingAccural()) {
			jEntries.add(getInvoiceTaxAccrualJEntry(inv));
			jEntries.add(getInvoiceTaxAccrualRevJEntry(inv));
		}
		return jEntries;
	}
	
	private JEntry getAccountJEntry(InvoiceCharge charge) {
		String postingKey = null;
		String invoiceType = charge.getInv().getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT)) postingKey = "50"; //credit
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "40";
		
		String supplierNum = charge.getInv().getSupplierNumber();
		String account = charge.getSegment2();
		String costCenter = charge.getSegment3();
		String orderNum = charge.getSegment4();
		
		JEntry jEntry = getJEntry(postingKey, account,charge.getDescription(),
				charge.getTotal(), charge.getAccountingTotal(),charge.getTaxCode(),
				costCenter, orderNum, supplierNum, charge.getInv().getTaxCode(), JEntry.ENTRY_STD);
		return jEntry;
	}
	
	
	private JEntry getLineTaxAccrualJEntry(InvoiceLine line) {
		String postingKey = null;
		String invoiceType = line.getInv().getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT) || line.getTotal() < 0) postingKey = "50";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "40";
		
		String supplierNum = line.getInv().getSupplierNumber();
		String account = line.getSegment2();
		String costCenter = line.getSegment3();
		String orderNum = line.getSegment4();
		
		JEntry jEntry = getJEntry(postingKey, account,line.getDescription(),
				line.getAccruedTax(), line.getAccruedTax(),line.getTaxCode(),
					costCenter, orderNum, supplierNum, line.getInv().getTaxCode(), JEntry.ENTRY_ACC);
		return jEntry;
	}
	
	private JEntry getLineTaxAccrualRevJEntry(InvoiceLine line) {
		String postingKey = null;
		String invoiceType = line.getInv().getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT) || line.getTotal() < 0) postingKey = "40";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "50"; //credit
		
		String supplierNum = line.getInv().getSupplierNumber();
		String account = IntUtil.getProperty("tax_gl_ac");
		String costCenter = "";
		String orderNum = "";
		
		JEntry jEntry = getJEntry(postingKey, account,line.getDescription(),
					line.getAccruedTax(), line.getAccruedTax(),line.getTaxCode(),
					costCenter, orderNum, supplierNum, line.getInv().getTaxCode(), JEntry.ENTRY_ACC);
		return jEntry;
	}
	
	private JEntry getInvoiceTaxAccrualJEntry(Invoice inv) {
		String postingKey = null;
		String invoiceType = inv.getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT) || inv.getTotal() < 0) postingKey = "50";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "40";
		
		String supplierNum = inv.getSupplierNumber();
		String account = IntUtil.getProperty("ship_gl_ac");
		String costCenter = inv.getFirstSegment3();
		String orderNum = "";
		
		JEntry jEntry = getJEntry(postingKey, account,inv.getInvoiceNumber(),
				inv.getHandlingAccrualTax(), inv.getHandlingAccrualTax(),inv.getTaxCode(),
					costCenter, orderNum, supplierNum, inv.getTaxCode(), JEntry.ENTRY_ACC);
		return jEntry;
	}
	
	private JEntry getInvoiceTaxAccrualRevJEntry(Invoice inv) {
		String postingKey = null;
		String invoiceType = inv.getDocumentType();
		if (invoiceType.equals(Invoice.INV_CREDIT) || inv.getTotal() < 0) postingKey = "40";
		else if (invoiceType.equals(Invoice.INV_DEBIT)) postingKey = "50"; //credit
		 
		String supplierNum = inv.getSupplierNumber();
		String account = IntUtil.getProperty("tax_gl_ac");
		String costCenter = "";
		String orderNum = "";
		
		JEntry jEntry = getJEntry(postingKey, account,inv.getInvoiceNumber(),
				inv.getHandlingAccrualTax(), inv.getHandlingAccrualTax(),inv.getTaxCode(),
					costCenter, orderNum, supplierNum, inv.getTaxCode(),JEntry.ENTRY_ACC);
		return jEntry;
	}
	
	private List<JEntry> processEntries(InvoiceLine line) {
		List<JEntry> jEntries = new ArrayList<JEntry>();
		
		
		if (!line.getAssetId().equals(""))
			jEntries.add(getAssetJEntry(line));
		else 
			jEntries.add(getAccountJEntry(line));
		
		if (line.isAccrueTax()) {
			jEntries.add(getLineTaxAccrualJEntry(line));
			jEntries.add(getLineTaxAccrualRevJEntry(line));
		}
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
				Double total = Double.parseDouble(csvRecord.get("total"));
				invLine.setTotal(total);
			}
			
			if (!csvRecord.get("accounting-total").equals("")) {
				Double acTotal = Double.parseDouble(csvRecord.get("accounting-total"));
				invLine.setAccountingTotal(acTotal);
			}
			
			if (!csvRecord.get("price").equals("")) {
				Double price = Double.parseDouble(csvRecord.get("price"));
				invLine.setPrice(price);
			}
			if (!csvRecord.get("quantity").equals("")) {
				Double quantity = Double.parseDouble(csvRecord.get("quantity"));
				invLine.setQuantity(quantity);
			}
			invLine.setDescription(csvRecord.get("description"));
			invLine.setPo(csvRecord.get("po-number"));
			
			invLine.setCategory(csvRecord.get("category"));
			invLine.setTaxCode(csvRecord.get("tax-code"));
			
			if (!csvRecord.get("tax-amount").equals("")) {
				Double tax = Double.parseDouble(csvRecord.get("tax-amount"));
				invLine.setTaxAmount(tax);
			}
			
			if (!csvRecord.get("rnd_tax_type").equals("") && csvRecord.get("rnd_tax_type").contains("-")) {
				String[] rndTaxArr = csvRecord.get("rnd_tax_type").split("-");
				Double taxRate = Double.parseDouble(rndTaxArr[1].trim());
				invLine.setTaxRate(taxRate);
				invLine.setTaxCode(rndTaxArr[0].trim());
			}
			
			if (!csvRecord.get("accrue_tax").equals("")) invLine.setAccrueTax(Boolean.parseBoolean(csvRecord.get("accrue_tax")));
			if (invLine.isAccrueTax()  && !csvRecord.get("accrued_tax_amount").equals("")) {
				Double accruedTax = Double.parseDouble(csvRecord.get("accrued_tax_amount"));
				invLine.setAccruedTax(accruedTax);
			}	
			
			String assetId = csvRecord.get("asset_id external_ref_num");
			assetId = (assetId.equals("00000") || assetId.equals("0")) ? "" : assetId;
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
			invCharge.setDescription(csvRecord.get("line-type"));
			invCharge.setCreatedAt(IntUtil.getDtFromUTC(csvRecord.get("created-at")));
			
			if (!csvRecord.get("total").equals("")) {
				Double total = Double.parseDouble(csvRecord.get("total"));
				invCharge.setTotal(total);
			}
			
			if (!csvRecord.get("accounting-total").equals("")) {
				Double acTotal = Double.parseDouble(csvRecord.get("accounting-total"));
				invCharge.setAccountingTotal(acTotal);
			}
			
			invCharge.setSegment1(csvRecord.get("segment-1"));
			invCharge.setSegment2(csvRecord.get("segment-2"));
			invCharge.setSegment3(csvRecord.get("segment-3"));
			invCharge.setSegment4(csvRecord.get("segment-4"));
			
			invCharge.setTaxCode(csvRecord.get("tax-code"));
			if (!csvRecord.get("tax-amount").equals("")) {
				Double tax = Double.parseDouble(csvRecord.get("tax-amount"));
				invCharge.setTaxAmount(tax);
			}
			
			
			invCharges.add(invCharge);
		}
		
		return invCharges;
		
	}
	
}
