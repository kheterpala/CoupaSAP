package com.eikontx.coupaint;

import java.util.Date;

public class TestInt {
	public static void main(String[] args) {
		Date dt = IntUtil.getDtFromUTC("2022-04-06T14:50:34Z");
		System.out.println("This is a test of Util posting" + IntUtil.getPostingDate(dt));
		System.out.println("Format" + IntUtil.getTodayTS());
	}
}
