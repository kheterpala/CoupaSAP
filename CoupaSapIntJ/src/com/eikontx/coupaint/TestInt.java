package com.eikontx.coupaint;

public class TestInt {
	public static void main(String[] args) {
		System.out.println("This is a test of Util" + IntUtil.getDtFromUTC("2022-03-01T14:50:34Z"));
		System.out.println("This is a test of Util" + IntUtil.getDtFromUTC("2022-03-01T06:49:42-08:00"));
	}
}
