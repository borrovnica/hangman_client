package gui;


import java.awt.Dimension;
import java.awt.EventQueue;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Random;


import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import clients.Client;


public class GUIControler extends Thread {

	private static WelcomeWindow welcomeWindow;
	private static ConnectingWindow connectingWindow ;
	private static MainWindow mainWindow;

	static JDialog dialog = null;
	static JDialog timeoutDialog = null;
	static boolean answered = false;

	//public static boolean goodbye = false;
	public static String word="";
	public static String category="";
	public static String newW=null;
	public static String letter;
	public static int errorCount=0;
	public static int lettersCorrect=0;
	public static int end=0;
	public static boolean acceptedGame = false;

	static String usernameToValidate = "";
	static String tryOpponent = "";
	private static JDialog dialogForWord;
	private static JDialog dialogForGameStatus;
	
	public static boolean messageAdded = false;


	@Override 
	public void run() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					welcomeWindow = new WelcomeWindow();
					welcomeWindow.setVisible(true);
					welcomeWindow.setLocationRelativeTo(null);
					welcomeWindow.getTextField().requestFocusInWindow();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	//Closing WelcomeWindow
	public static void closeApp1() {
		int option = JOptionPane.showConfirmDialog(welcomeWindow.getContentPane(), "Are you sure you want to close the game?",
				"Closing app", JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.YES_OPTION) {
			Client.sendExitSignal();
			System.exit(0);
		} else if(option == JOptionPane.NO_OPTION){
			welcomeWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
	}

	//Closing ConnectingWindow
	public static void closeApp2() {
		int option = JOptionPane.showConfirmDialog(connectingWindow.getContentPane(), "Are you sure you want to close the game?",
				"Closing app", JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.YES_OPTION) {
			Client.sendExitSignal();
			System.exit(0);
		} else if(option == JOptionPane.NO_OPTION){
			connectingWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
	}

	//Closing MainWindow
	public static void closeApp3() {
		int option = JOptionPane.showConfirmDialog(mainWindow.getContentPane(), "Are you sure you want to close the game?",
				"Closing app", JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.YES_OPTION) {
			Client.sendExitSignal();
			System.exit(0);
		} else if(option == JOptionPane.NO_OPTION){
			mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}

	}
//Show Connecting window
	public static void showConnectingWindow() {
		connectingWindow = new ConnectingWindow();
		connectingWindow.setLocationRelativeTo(welcomeWindow);		
		welcomeWindow.setVisible(false);
		connectingWindow.setVisible(true);
	}

	//Username validation	
	public static boolean validateUsernameLocally(String username) {
		usernameToValidate =username;
		boolean valid=false;
		if (username.isEmpty()) {
			JOptionPane.showMessageDialog(welcomeWindow, "You have to enter a username!", "Try again :(", JOptionPane.ERROR_MESSAGE);
		} else if(!username.matches("[A-Za-z0-9]+")) {
			JOptionPane.showMessageDialog(welcomeWindow, "Incorrect username. Please use only letters a-z and/or numbers 0-9", "Try again :(", JOptionPane.ERROR_MESSAGE);

		} else if(username.length()>10) {
			JOptionPane.showMessageDialog(welcomeWindow, "Username too long. Please use up to 10 characters.", "Try again :(", JOptionPane.ERROR_MESSAGE);
		} else {
			valid=true;
		}
		return valid;
	}		
	public static void validateUsernameFromServer(String msg) {
		if (!msg.equals("OK")) {
			JOptionPane.showMessageDialog(welcomeWindow, "Username already taken. Please choose a different one.", "Try again :(", JOptionPane.ERROR_MESSAGE);			
		} else {
			Client.setUsername(usernameToValidate);
			System.out.println("******"+Client.getUsername().toUpperCase()+"******");
			showConnectingWindow();
		}

	}	

	//Personalized welcome message 
	public static JLabel welcomeUser() {
		return new JLabel("Welcome, " + Client.getUsername() + "!");
	}

	//Select user from the list to send invite
	public static void choose(String user) {
		if(Client.activeGames.contains(user)) {
			JOptionPane.showMessageDialog(connectingWindow, user+" is already playing a game. Try a different user or try again later.",
					"User unavailable", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int option = JOptionPane.showConfirmDialog(connectingWindow.getContentPane(), "Are you sure you want to play with "+user+ " ?",
				"Connecting", JOptionPane.YES_NO_OPTION);
		tryOpponent = user;

		if(option == JOptionPane.YES_OPTION){

			//loading screen:
			dialog = new JDialog();
			JLabel label = new JLabel("Sending invite to "+tryOpponent+"...", JLabel.CENTER);
			dialog.setIconImage(Toolkit.getDefaultToolkit().getImage(ConnectingWindow.class.getResource("/icons/h.png")));
			dialog.setTitle("Please Wait...");
			dialog.add(label);
			dialog.setPreferredSize(new Dimension(200, 90));
			dialog.setResizable(false);
			dialog.pack();
			dialog.setLocationRelativeTo(connectingWindow);
			dialog.setVisible(true);
			connectingWindow.setEnabled(false);

			dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			Client.inviteUserToPlay(tryOpponent);
		} else {
			SwingUtilities.updateComponentTreeUI(connectingWindow);
		}
	}

	//Receive and handle response to invite
	public static void receiveResponseToInvite(String name, String response) {
		//		if(response.equals("BUSY")) {
		//			dialog.setVisible(false);
		//			connectingWindow.setEnabled(true);
		//			JOptionPane.showMessageDialog(connectingWindow, "User "+name+" is already playing a game. Try a different user or try again later.",
		//					"Connection failed", JOptionPane.ERROR_MESSAGE);
		//		}
		if(response.equals("ACCEPTED")) {			
			Client.setOpponent(name);
			//Client.changeGameStatus("true");
			dialog.setVisible(false);
			Client.sentRequestForGame=1;
			startGame();
		}
		else if(response.equals("REJECTED")) {
			dialog.setVisible(false);
			connectingWindow.setEnabled(true);
			JOptionPane.showMessageDialog(connectingWindow, "Connection to "+name+" was unsuccessful or they rejected your invite. Try a different user.",
					"Connection failed", JOptionPane.ERROR_MESSAGE);
		} else {
			dialog.setVisible(false);
			connectingWindow.setEnabled(true);
		}
	}

	//Random button functionality 
	public static void chooseRandom() {
		if(Client.onlineLista.isEmpty() || (Client.onlineLista.size()==Client.activeGames.size())){
			JOptionPane.showMessageDialog(connectingWindow, "There are no available players at the moment!");						
		}else{
			Random randomizer = new Random();
			String random = "";

			while(true) {
				random = Client.onlineLista.get(randomizer.nextInt(Client.onlineLista.size()));
				if(Client.activeGames.contains(random)) 
					continue;
				else 
					break;
			}

			int option = JOptionPane.showConfirmDialog(connectingWindow.getContentPane(), random+" is available. Do you want to play with them? ",
					"Connecting", JOptionPane.YES_NO_OPTION);


			if(option == JOptionPane.YES_OPTION){
				tryOpponent = random;
				dialog = new JDialog();
				JLabel label = new JLabel("Sending invite to "+tryOpponent+"...", JLabel.CENTER);
				dialog.setIconImage(Toolkit.getDefaultToolkit().getImage(ConnectingWindow.class.getResource("/icons/h.png")));
				dialog.setTitle("Please Wait...");
				dialog.add(label);
				dialog.setPreferredSize(new Dimension(200, 90));
				dialog.setResizable(false);
				dialog.pack();
				dialog.setLocationRelativeTo(connectingWindow);
				dialog.setVisible(true);
				connectingWindow.setEnabled(false);
				Client.inviteUserToPlay(tryOpponent);
			}else{
				SwingUtilities.updateComponentTreeUI(connectingWindow);
			}
		}	
	} 

	public static void updateTable() {
		connectingWindow.refreshTable();
	}	

	// ******************** game logic methods ***************** 

//Set Hangman picture
	public static void setHangmanImage(String imgPath){

		ImageIcon img = new ImageIcon(MainWindow.class.getResource(imgPath));
		Image img1 = img.getImage();
		Image img2 = img1.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
		img = new ImageIcon(img2);
		mainWindow.getLblSlika().setIcon(img);


	} 	
// Place guessed and not guessed letters on Main Window
	public static void placeTheLetter() {
		letter = MainWindow.getTextField().getText().toLowerCase();
		MainWindow.getTextField().setText("");
		if(letter.matches("[a-z]")) {

			String w =word.toLowerCase();
			if (!(w.contains(letter)) || (newW!=null && numberOfLettersInAWord(newW, letter)==numberOfLettersInAWord(w, letter))){
				MainWindow.getTxtpnABC().setText(MainWindow.getTxtpnABC().getText()+letter +"\n");
				errorCount++;
				switch (errorCount) {
				case 1: 
					changeHangmnPicAndPlaceWrongLetter("/icons/state-1.png", Client.getOpponent(), letter);
					break;

				case 2: 
					changeHangmnPicAndPlaceWrongLetter("/icons/state-2.png", Client.getOpponent(), letter);
					break;
					
				case 3:
					changeHangmnPicAndPlaceWrongLetter("/icons/state-3.png", Client.getOpponent(), letter);
					break;

				case 4:
					changeHangmnPicAndPlaceWrongLetter("/icons/state-4.png", Client.getOpponent(), letter);
					break;
				case 5:
					changeHangmnPicAndPlaceWrongLetter("/icons/state-5.png", Client.getOpponent(), letter);
					break;
				case 6:
					changeHangmnPicAndPlaceWrongLetter("/icons/state-6.png", Client.getOpponent(), letter);
					Client.setNumOfLosses(Client.getNumOfLosses()+1);
					
					
					if(end<2) { //na dve pobede
						if(Client.getNumOfLosses()>end ) {
							end=Client.getNumOfLosses();
							
							if(end==2){
								sendingResultNClearingValues(Client.getOpponent(), Client.getNumOfWins()+"", Client.getNumOfLosses()+"");
								gameOver(Client.getOpponent(), "You lost :(", "YOU WON!");
							}
							else{
								switchMainWindow(Client.getOpponent(), Client.sentRequestForGame+"" , 0+"", "You haven't gueesed the word. \n It's your turn to set a word for "+
										Client.getOpponent()+".", Client.getNumOfWins()+"", Client.getNumOfLosses()+"" );
							}
							
						}
						else {
							switchMainWindow(Client.getOpponent(), Client.sentRequestForGame+"" , 0+"", "You haven't gueesed the word. \n It's your turn to set a word for "+
									Client.getOpponent()+".", Client.getNumOfWins()+"", Client.getNumOfLosses()+"" );
						}
						
					}
				
					break;
				default : break;
				}
			}
			else {
				for (int i=0; i<w.length(); i++){

					if (letter.charAt(0)==w.charAt(i)){
						MainWindow.listOfButtons.get(i).setText(letter.toUpperCase());
						Client.changeRigthLetterSignal(letter, Client.getOpponent(), i+"");
						
						newW=newW+letter;
						lettersCorrect++;

					}
				}
				
				if(lettersCorrect==w.length()){
					Client.setNumOfWins(Client.getNumOfWins()+1);
					
					if(end<2) { //na dve pobede
						if(Client.getNumOfWins()>end ) {
							end=Client.getNumOfWins();
							
							if(end==2){
								sendingResultNClearingValues(Client.getOpponent(), Client.getNumOfWins()+"", Client.getNumOfLosses()+"");
								gameOver(Client.getOpponent(), "YOU WON!", "You lost :(");
							}
							else{
								switchMainWindow(Client.getOpponent(), Client.sentRequestForGame+"" , 1+"", "You guessed the word. \n "
										+ "It's your turn to set a word for "+Client.getOpponent(), Client.getNumOfWins()+"", Client.getNumOfLosses()+"" );
							}
							
						}
						else{
							switchMainWindow(Client.getOpponent(), Client.sentRequestForGame+"" , 1+"", "You guessed the word. \n "
									+ "It's your turn to set a word for "+Client.getOpponent(), Client.getNumOfWins()+"", Client.getNumOfLosses()+"" );
						}
						
					}
				}
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "Enter a letter", "Error", JOptionPane.ERROR_MESSAGE );
		}


	}
// sending gameOver signal to opponent and launching gameOver JOptionPane
	private static void gameOver(String opponent, String message, String msgOpp) {
		Client.sendGameOverSignal(opponent, msgOpp);
		gameOverWindow(message);
	
}
	
	// gameOver JOptionPane 
	public static void gameOverWindow(String message){
		String[] options = new String[] {"Wanna play with same player?", "Exit game"};
	    int response = JOptionPane.showOptionDialog(mainWindow, message, "Game Status",
	        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
	        null, options, options[0]);
	    if(response==0){ //ponovo igraju
	    	
	    }
	    if(response==1){ //vracaju se na izbor igraca
	    	connectingWindow.setVisible(true);
			connectingWindow.setEnabled(true);
			connectingWindow.setLocationRelativeTo(mainWindow);
			mainWindow.setVisible(false);
	    }
	    else{ //pritisnuto x
	    	
	    }
	}

	// Changing Hangman picture of player and opponent and placing letter that is not guessed on opponent Main Window

	public static void changeHangmnPicAndPlaceWrongLetter(String url, String opponent, String letter1) {
		setHangmanImage(url);
		Client.changeHangmanPictureSignal(url, opponent);
		Client.changeWrongLettersSignal(letter1, opponent);
		
	}

// Calculate number of letters in word that should be guessed
	
	public static int numberOfLettersInAWord(String word, String l) {
		int count=0;
		for(int i=0; i<word.length(); i++){
			if(word.charAt(i)==l.charAt(0)){
				count++;
			}
		}
		return count;

	}
	
	// Sending signal for changing result and showing game status window on opponent's Main Window
	// Switching the users roles (one who was guessing is now setting the word)
	
	
	public static void sendingResultNClearingValues(String opponent, String r1, String r2){
		Client.sentRequestForGame=0;
		mainWindow.getlblResult().setText("Result: "+r1+":"+r2);
		Client.sendChangeResult(opponent, r2, r1);
		errorCount=0; 
		lettersCorrect=0; 
		newW=null;  
		mainWindow.listOfButtons.clear();
	}
	
	
	public static void switchMainWindow(String opponent, String gameRqNum, String result, String message, String r1, String r2) {
		sendingResultNClearingValues(opponent, r1, r2);
		Client.sendGameStatusWindow(opponent, gameRqNum , result);
		int input = JOptionPane.showOptionDialog(mainWindow, message, "Status", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		if(input==0) {
			startGame();
			
		}
		if(input==-1){ //ovo je  ako se pritisne x, treba definisati
			
		}
	}


	/******Connecting two players in a game**************/


	public static void receiveInvite(String name) {
		JOptionPane pane = null;
		answered=false;

		Timer timer = new Timer(7000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!answered) {
					timeoutDialog.setVisible(false);
					answered=false;
					Client.rejectInvite(name);
					//System.out.println("timer went off");
					return;
				}
			}
		});
		timer.start();
		timer.setRepeats(false);

		pane = new JOptionPane(name+" has invited you to play with them. Do you want to accept?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		timeoutDialog = pane.createDialog(connectingWindow.getContentPane(), "Received an invitation!");
		timeoutDialog.setVisible(true);

		try {
			Object selectedValue = pane.getValue();
			int value = 0;

			if(selectedValue == null)
				value = JOptionPane.CLOSED_OPTION;      
			else
				value = Integer.parseInt(selectedValue.toString());

			if(value == JOptionPane.YES_OPTION) {
				answered=true;
				Client.acceptInvite(name);
			} else if (value == JOptionPane.NO_OPTION) {
				answered=true;
				Client.rejectInvite(name);
				//System.out.println("clicked no");
			} else if (value == JOptionPane.CLOSED_OPTION) {
				answered=true;
				Client.rejectInvite(name);
				//System.out.println("clicked X");
			}
		} catch (NumberFormatException e1) {
			answered=false;
			//System.out.println("did nothing");
			return;
		} catch (Exception e) {
			return;
		}
	}

	public static void startGame() {
		if(mainWindow!=null){
			mainWindow.setVisible(false);
			mainWindow= new MainWindow();
			mainWindow.getLblWord().setText("");
			mainWindow.getTxtpnABC().setText("");
			mainWindow.getPanel_5().removeAll();
		}
		else {
			mainWindow = new MainWindow();
			mainWindow.setLocationRelativeTo(connectingWindow);	 
			connectingWindow.setVisible(false);
			
		}
		mainWindow.getBtnGuess().setVisible(false);
		mainWindow.getTextField().setVisible(false);
		
		mainWindow.setVisible(true);

		if(Client.sentRequestForGame==1) { //ceka na rec
			mainWindow.getlblResult().setText("Result: "+Client.getNumOfWins()+":"+Client.getNumOfLosses());
			waitingMainWindow();
		} else {
			mainWindow.getlblResult().setText("Result: "+Client.getNumOfWins()+":"+Client.getNumOfLosses());
			givingWordMainWindow();
		}

	}

	private static void waitingMainWindow() {
		//loading screen:
		dialogForWord = new JDialog();
		JLabel label = new JLabel("Waiting on opponent to set the word to guess...", JLabel.CENTER);
		dialogForWord.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/icons/h.png")));
		dialogForWord.setTitle("Please Wait...");
		dialogForWord.add(label);
		dialogForWord.setPreferredSize(new Dimension(400, 90));
		dialogForWord.setResizable(false);
		dialogForWord.pack();
		dialogForWord.setLocationRelativeTo(mainWindow);
		dialogForWord.setVisible(true);
		mainWindow.setEnabled(false);
	}

		
	private static void givingWordMainWindow() {
		String w="";
		String[] options = {"OK"};
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(80, 50));
		JLabel lbl = new JLabel("Enter a word: ");
		JTextField txt = new JTextField(15);
		panel.add(lbl);
		panel.add(txt);


		do {
			int selectedOption = JOptionPane.showOptionDialog(mainWindow, panel, "It's your turn to give a word", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, options , null);

			if(selectedOption==JOptionPane.CLOSED_OPTION){
				int option = JOptionPane.showConfirmDialog(mainWindow, "Are you sure you want to quit the game?",
						"Leaving the game", JOptionPane.YES_NO_OPTION);

				if (option == JOptionPane.YES_OPTION) {
					Client.sendQuitTheGameSignal(Client.getOpponent());
					System.out.println("quit signal sent");
					connectingWindow.setVisible(true);
					connectingWindow.setLocationRelativeTo(mainWindow);
					connectingWindow.setEnabled(true);
					mainWindow.setVisible(false);
					return;
				}
			}
			if(selectedOption==0){   
				w = txt.getText();
				if(w.equals("")){
					JOptionPane.showMessageDialog(mainWindow, "You have to type something!", "Not a word", JOptionPane.ERROR_MESSAGE);
				}else if(!w.matches("[A-Za-z]+")){
					JOptionPane.showMessageDialog(mainWindow, "Use only a-z caracters!", "Not a word", JOptionPane.ERROR_MESSAGE);
					txt.setText("");
				}else{
					word=w;
					break;
				}

			}

		} while (true);
							
		
		
		//KATEGORIJU BI TREBALO ODVOJITI KAO POSEBNU METODU
		
		String c="";
		lbl.setText("Enter word category");
		txt.setText("");

		do {
			int selectedOption1 = JOptionPane.showOptionDialog(mainWindow, panel, "Now give us word category!", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options , null);
			if(selectedOption1==JOptionPane.CLOSED_OPTION){
				int option = JOptionPane.showConfirmDialog(mainWindow, "Are you sure you want to quit the game?",
						"Leaving the game", JOptionPane.YES_NO_OPTION);

				if (option == JOptionPane.YES_OPTION) {
					Client.sendQuitTheGameSignal(Client.getOpponent());
					connectingWindow.setVisible(true);
					connectingWindow.setLocationRelativeTo(mainWindow);
					connectingWindow.setEnabled(true);
					mainWindow.setVisible(false);
					return;
				}
			}
			if(selectedOption1==0){   
				c = txt.getText();
				if(c.equals("") || c.equals(" ")){
					JOptionPane.showMessageDialog(mainWindow, "You have to type something!", "Not a word", JOptionPane.ERROR_MESSAGE);
				}else if(!c.matches("[A-Za-z ]+")){
					JOptionPane.showMessageDialog(mainWindow, "Use only a-z caracters!", "Not a word", JOptionPane.ERROR_MESSAGE);
				}else{
					break;
				}
			}
		} while (true);


		if(c!=null){
			category = c;
			Client.sendWordSetSignal(Client.getOpponent(), word, category);
			setOpponentMainWindow();
			
		}
		
	}


	public static void receiveSignalWordSet(String w, String c) {
		dialogForWord.setVisible(false);
		setPlayerMainWindow(w, c);
	}

	
	//Main Window of player guessing the word
	public static void setPlayerMainWindow(String w, String c){ 

		dialogForWord.setVisible(false);
		mainWindow.setEnabled(true);
		word=w;
		category=c;
		mainWindow.getBtnGuess().setVisible(true);
		mainWindow.getTextField().setVisible(true);
		for (int i=0; i<word.length(); i++) {
			mainWindow.getPanel_5().add(mainWindow.getBtnLetter());
			mainWindow.getPanel_5().revalidate();
			mainWindow.getPanel_5().repaint();
		} 
		mainWindow.getPanel_1().revalidate();
		mainWindow.getLblCategory().setVisible(true);
		
		mainWindow.getLblCategory().setText(mainWindow.getLblCategory().getText()+" "+category);
		mainWindow.getlblResult().setVisible(true);
	}
	
// Main Window of player who is setting the word
	public static void setOpponentMainWindow() { 

		mainWindow.remove(mainWindow.getBtnGuess());
		mainWindow.remove(mainWindow.getTextField());
		mainWindow.getLblWord().setText(word.toLowerCase()); 
		mainWindow.getLblWord().setHorizontalAlignment(SwingConstants.CENTER);
		mainWindow.getLblTip().setText("*Capital letters in word represent ones opponent has guessed.");
		mainWindow.getPanel_5().add(mainWindow.getLblWord());
		mainWindow.getPanel_5().revalidate();
		mainWindow.getPanel_5().repaint();
		mainWindow.getPanel_1().add(mainWindow.getLblTip());
		mainWindow.getPanel_1().revalidate();
		mainWindow.getPanel_1().repaint();
		mainWindow.getLblCategory().setVisible(true);
		mainWindow.getLblCategory().setText(mainWindow.getLblCategory().getText()+" "+category);
		mainWindow.getlblResult().setVisible(true);

	}


	public static void receiveSignalHnagmanPicChanged(String url) {
		setHangmanImage(url);;

	}

	public static void receiveSignalWrongLetter(String letter) {
		mainWindow.getTxtpnABC().setText(MainWindow.getTxtpnABC().getText()+letter +"\n");

	}

	public static void receiveSignalRightLetter(String letter, String index) {
		String w= mainWindow.getLblWord().getText();
	
		w =  w.replace(w.charAt(Integer.parseInt(index))+"", letter.toUpperCase()) ;
		mainWindow.getLblWord().setText(w);

	}



	/**************CHATBOX********************/

	public static void addMessage(String username, String message) {
		String newMsg = username + ":\n" + message;
		Client.chatHistory.addElement(newMsg);	
		messageAdded=true;
	}




	public static void recieveQuitTheGameSignal(String name) {
		dialogForWord.setVisible(false);
		JOptionPane.showMessageDialog(mainWindow, name+" has quit the game; Please choose a new one to play with.");
		connectingWindow.setVisible(true);
		connectingWindow.setEnabled(true);
		connectingWindow.setLocationRelativeTo(mainWindow);
		mainWindow.setVisible(false);

	}

	public static void receiveSignalStatusWindow(String gameRqNum, String result) {
		String message="";
		int r=Integer.parseInt(result);
		
		Client.sentRequestForGame=Integer.parseInt(gameRqNum);
	
		if(r==1){
			 message="Your opponent guessed the word";
			
		}
		else {
			message="Your opponent didn't guess the word. ";
		}
		
		int option=JOptionPane.showOptionDialog(mainWindow, message+"\n It's your turn to guess", "Status", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		if(option==0)
			startGame();
		if(option==-1) {
			//pritisne se x definisati
		}
		
	}

	
// Change result on opponent's Main Window
	public static void receiveSignalResultChanged(String r1, String r2) {
		mainWindow.getlblResult().setText("Result: "+r1+":"+r2);
		Client.setNumOfLosses(Integer.parseInt(r2));
		Client.setNumOfWins(Integer.parseInt(r1));
		
	}

	public static void receiveGameOverSignal(String msg) {
		gameOverWindow(msg);
		
	}


}







