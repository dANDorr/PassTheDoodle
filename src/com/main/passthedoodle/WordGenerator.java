package com.main.passthedoodle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;

public class WordGenerator {
	ArrayList<String> wordArrayList;
	String difficultyFile;
	AssetManager am;
	
	public WordGenerator(Context cont, String diff) {
		am = cont.getAssets();
		difficultyFile = diff;
		wordArrayList =  new ArrayList<String>();
	}
	
	private String getWord(InputStream is) throws IOException {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		String line = null;
		while ((line = br.readLine()) != null) {
	        wordArrayList.add(line);
	    }

		Random rand = new Random(System.nanoTime());
		return wordArrayList.get(rand.nextInt(wordArrayList.size() - 1));	
	}
	
	public String getWord() {
		try {
			InputStream is = am.open(difficultyFile);
			return getWord(is);
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
			return "IO Exception";
		} catch (NullPointerException npe) {
			// TODO Auto-generated catch block
			npe.printStackTrace();
			return "NPE";
		}
	}
}