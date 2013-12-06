package com.main.passthedoodle;

import java.util.ArrayList;

import android.content.Context;

/** Singleton class that holds the information about the state of a local game
 *  Accessed by DrawingActivity, TextActivity, and ViewCompletedActivity **/

public class LocalPlayHandler {
	private final static LocalPlayHandler INSTANCE = new LocalPlayHandler();
	int sequence;
	int totalRounds;
	String currentImage;
	String currentText;
	ArrayList<RoundInfo> gameRecord; // used by ViewCompleted

    private LocalPlayHandler() {
    	// Private constructor prevents instantiation from other classes
    	reset();
	}

    public static LocalPlayHandler getInstance() {
		return INSTANCE;
	}

    public void reset() {
    	sequence = 0;
    	totalRounds = 0;
    	currentImage = "";
    	currentText = "";
    	gameRecord = null;
    }
    
    public void startGame(Context cont, int total) {
    	reset();
    	// Generate word
    	// Pass in .txt filename for the appropriate difficulty level.
    	currentText = new WordGenerator(cont, "hard.txt").getWord();
    	totalRounds = total;
    	
    	// initialize ArrayList
    	gameRecord = new ArrayList<RoundInfo>();
    	// size of list should be half of the total rounds if total rounds is even
    	// or rounded up if odd
    	for (int i = 0; i < (int) Math.ceil(totalRounds / 2.0); i++)
    		// 4th parameter username always remains empty for local
    		gameRecord.add(new RoundInfo("", "", ""));
    }
    public void endDrawing(String imagePath) {
    	// update game state when a DrawingActivity is finished
    	currentImage = imagePath;
    	sequence++;
    	
    	int i = (sequence - 1) / 2;
    	gameRecord.get(i).prompt = currentText;
    	gameRecord.get(i).imageUrl = imagePath;
    }
    
    public void endText(String submission) {
    	// update game state when a TextActivity is finished
    	currentText = submission;
    	sequence++;    	
    	
    	int i = (sequence - 1) / 2;
    	gameRecord.get(i).desc = currentText;
    }
    
    public boolean isInitialRound() {
    	return sequence == 0;
    }

    public boolean gameHasEnded() {
    	return sequence == totalRounds;
    }
}