package com.main.passthedoodle;

public class RoundInfo {
	String imageUrl;
	String prompt;
	String desc;
	String user_drawer;
	String user_guesser;
	
	// local play
	public RoundInfo(String a, String b, String c) {
		imageUrl = a;
		prompt = b;
		desc = c;
		user_drawer = "Player";
		user_guesser = "Player";
	}
	// server play
	public RoundInfo(String a, String b, String c, String d, String e) {
		imageUrl = a;
		prompt = b;
		desc = c;
		user_drawer = d;
		user_guesser = e;
	}
}
