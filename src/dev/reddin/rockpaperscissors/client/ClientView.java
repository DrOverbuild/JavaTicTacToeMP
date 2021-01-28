// Jasper Reddin
// 001221899
// OS Project 2

package dev.reddin.rockpaperscissors.client;

import dev.reddin.rockpaperscissors.gamelogic.Move;
import dev.reddin.rockpaperscissors.gamelogic.RockPaperScissors;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.Collection;

// View for client. Builds the view, updates the view, and
// notifies the ClientMain class of user interaction
public class ClientView extends JFrame {
	private ClientMain main; // main class for talking back to clientmain

	private JTextArea chatLog; // chat log for chat, info, and error messages
	private JTextArea playersArea; // list of players logged on
	private JTextField chatField; // text field for sending chat messages
	private JLabel[][] gameLabels = new JLabel[3][4]; // label grid for game
	private JLabel statusLabel; // status label for game

	private JButton logoutButton; // button that logs player out and closes program
	private JButton rockButton; // button that sends a rock move
	private JButton paperButton; // button that sends a paper move
	private JButton scissorsButton; // button that sends a scissors move

	// Builds the GUI for the client
	public ClientView(ClientMain main) {
		super("Rock Paper Scissors Client"); // set title
		this.main = main; // there should only be one instance of these

		// split window in half
		this.setLayout(new GridLayout(1, 2, 5,0));

		// left half will be players on top half and chat log on bottom half
		JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 5));
		leftPanel.add(buildPlayersPanel());
		leftPanel.add(buildChatLog());
		this.add(leftPanel);

		// right half shows game view. On top will show "Current Game"
		// in the center is a 4x4 grid view containing labels showing the
		// game and buttons for playing the game. On the bottom is a
		// status label showing the status of the game, and score.
		JPanel rightPanel = new JPanel(new BorderLayout(0,15));

		JLabel currentGameLabel = new JLabel("Current Game");
		currentGameLabel.setHorizontalAlignment(JLabel.CENTER);
		rightPanel.add(currentGameLabel, BorderLayout.NORTH);
		rightPanel.add(buildGamePanel(), BorderLayout.CENTER);
		this.add(rightPanel);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close window
		this.pack(); // get minimum size possible
		this.setMinimumSize(this.getSize()); // set that to the minimum size
		this.setVisible(true); // show the window
	}

	// builds and returns the top left panel, shows players logged in
	private JPanel buildPlayersPanel() {
		JPanel playerspanel = new JPanel(new BorderLayout()); // container panel
		JLabel playerslabel = new JLabel("Players"); // title label
		playerslabel.setHorizontalAlignment(JLabel.CENTER); // align title
		playerspanel.add(playerslabel, BorderLayout.NORTH); // add to container
		playersArea = new JTextArea(10, 30); // text area with logged in players
		playersArea.setLineWrap(true); // set word wrap
		playersArea.setWrapStyleWord(true);
		playersArea.setEditable(false); // turn off editing
		JScrollPane playersareascroll = new JScrollPane(playersArea); // place in scroll view
		playersareascroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // turn off horizontal scrollbar
		playerspanel.add(playersareascroll, BorderLayout.CENTER); // add to container
		return playerspanel;
	}

	// builds and returns the bottom left panel, shows chat log and ability to send message
	private JPanel buildChatLog() {
		// bottom left panel
		JPanel chatlogpanel = new JPanel(new BorderLayout());
		JLabel chatloglabel = new JLabel("Log"); // title label
		chatloglabel.setHorizontalAlignment(JLabel.CENTER); // center title label
		chatlogpanel.add(chatloglabel, BorderLayout.NORTH); // add to container

		// create log
		chatLog = new JTextArea(10, 30);
		chatLog.setLineWrap(true); // turn on word wrap
		chatLog.setWrapStyleWord(true);
		chatLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // make monospaced font
		chatLog.setEditable(false); // disable editing
		DefaultCaret caret = (DefaultCaret) chatLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // set to scroll to bottom every time

		// build scroll bar with chat log and add to panel
		JScrollPane chatlogscroll = new JScrollPane(chatLog);
		chatlogscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chatlogpanel.add(chatlogscroll, BorderLayout.CENTER);

		// build text area and button for sending chat messages
		JPanel chatFieldPanel = new JPanel(new BorderLayout());
		chatField = new JTextField(); // create text field
		chatField.addActionListener(e -> chat()); // set to send message on Return
		JButton sendBtn = new JButton("Send"); // create button
		sendBtn.addActionListener(e -> chat()); // set to send message on click
		chatFieldPanel.add(chatField, BorderLayout.CENTER); // add components
		chatFieldPanel.add(sendBtn, BorderLayout.EAST);
		chatlogpanel.add(chatFieldPanel, BorderLayout.SOUTH); // add panel to panel

		return chatlogpanel;
	}


	// builds game panel, contains labels, buttons for playing game, and status
	private JPanel buildGamePanel() {
		JPanel gamePanel = new JPanel(new BorderLayout()); // container

		// create 4 x 4 grid of labels and buttons
		// labels are the first 3 rows, contains players names and their moves,
		// as well as a round number.
		// last row contains buttons for playing the game.
		JPanel movesPanel = new JPanel(new GridLayout(4, 4, 10, 10));

		// fill first three rows with empty labels
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 4; j++) {
				gameLabels[i][j] = new JLabel(""); // create empty label
				gameLabels[i][j].setHorizontalAlignment(JLabel.CENTER); // center it
				movesPanel.add(gameLabels[i][j]); // add to grid
			}
		}

		// build buttons before adding to grid
		buildButtons();
		// add buttons to last row of grid
		movesPanel.add(logoutButton);
		movesPanel.add(rockButton);
		movesPanel.add(paperButton);
		movesPanel.add(scissorsButton);

		// custom properties for the labels that are different from the rest
		gameLabels[0][0].setText("Player 1:"); // label for showing player 1 name
		gameLabels[0][0].setHorizontalAlignment(JLabel.RIGHT); // align to right
		gameLabels[1][0].setText("Player 2:"); // label for showing player 2 name
		gameLabels[1][0].setHorizontalAlignment(JLabel.RIGHT); // align to right

		// name round labels, align to top
		gameLabels[2][1].setText("Round 1");
		gameLabels[2][1].setVerticalAlignment(JLabel.TOP);
		gameLabels[2][2].setText("Round 2");
		gameLabels[2][2].setVerticalAlignment(JLabel.TOP);
		gameLabels[2][3].setText("Round 3");
		gameLabels[2][3].setVerticalAlignment(JLabel.TOP);

		// add grid to center of container
		gamePanel.add(movesPanel, BorderLayout.CENTER);

		statusLabel = new JLabel("Game Status"); // create status label
		statusLabel.setHorizontalAlignment(JLabel.CENTER); // align center
		gamePanel.add(statusLabel, BorderLayout.SOUTH); // add to container
		return gamePanel;
	}

	private void buildButtons() {
		logoutButton = new JButton("Logout");
		logoutButton.addActionListener(e -> logout());
		rockButton = new JButton("ROCK");
		rockButton.addActionListener(e -> move(Move.ROCK));
		paperButton = new JButton("PAPER");
		paperButton.addActionListener(e -> move(Move.PAPER));
		scissorsButton = new JButton("SCISSORS");
		scissorsButton.addActionListener(e -> move(Move.SCISSORS));
	}

	// called when return or send button is clicked
	private void chat() {
		main.chat(chatField.getText());
		chatField.setText("");
	}

	// called when a move is made
	private void move(Move move) {
		main.move(move);
	}

	// called when logout is clicked
	private void logout() {
		main.stop();
		System.exit(0);
	}

	// called to print a log message. Only keeps the last 500 characters of the log
	public void log(String msg) {
		StringBuilder builder;

		if (chatLog.getText().length() > 500) {
			// only get last 500 characters of the log
			builder = new StringBuilder(
					chatLog.getText().substring(chatLog.getText().length() - 500));
		} else {
			builder = new StringBuilder(chatLog.getText());
		}

		builder.append(msg).append("\n");
		chatLog.setText(builder.toString());
	}

	// displays the logged in players in the top left view
	public void updatePlayers(Collection<String> players) {
		StringBuilder builder = new StringBuilder();
		for (String player : players) {
			builder.append(player).append("\n");
		}

		playersArea.setText(builder.toString());
	}

	// converts the object representation of the game into the labels, and status.
	// if the opponent has made a move without us, we don't get to see that move.
	public void updateGame(RockPaperScissors game) {
		String status = ""; // set up status with most important status message is final result

		// if given no game, clear labels and show no game
		if (game == null) {
			gameLabels[0][0].setText("Player 1:"); // reset player name labels
			gameLabels[1][0].setText("Player 2:");

			for (int i = 0; i < 3; i++) {
				gameLabels[0][i + 1].setText(""); // clear moves
				gameLabels[1][i + 1].setText("");
			}

			statusLabel.setText("No current game"); // show no game
			return;
		}

		// this is used in the loop to hide the opponents move if it is made
		boolean thisPlayerP1 = main.getUsername().equals(game.getPlayer1());

		// interate from the last round to the first roudn
		for (int i = 2; i >= 0; i--) {
			// get ith round
			Move p1 = game.getPlayer1Rounds()[i];
			Move p2 = game.getPlayer2Rounds()[i];

			if (p1 != null && p2 != null) {
				// if both have a vilue, this round was completed. Just update label.
				gameLabels[0][i + 1].setText(p1.name());
				gameLabels[1][i + 1].setText(p2.name());
			}else if (p1 != null) {
				// if p1 has a value (and therefore p2 is null), set p2 to empty label for this round,
				gameLabels[1][i + 1].setText("");

				// show move unless that move is made by other player
				if (thisPlayerP1) {
					gameLabels[0][i + 1].setText(p1.name());
				} else {
					gameLabels[0][i + 1].setText("");
				}

				// set status saying we're waiting for the other player to move.
				String p2name = game.getPlayer2();

				if (p2name != null) {
					status = "Waiting for " + p2name + " to move...";
				}
			} else if (p2 != null) {
				// if p2 has a value (and therefore p1 is null), set p1 to empty label for this round,
				gameLabels[0][i + 1].setText("");

				// show move unless that move is made by other player
				if (thisPlayerP1) {
					gameLabels[1][i + 1].setText("");
				} else {
					gameLabels[1][i + 1].setText(p2.name());
				}

				// set status saying we're waiting for the other player to move.
				String p1name = game.getPlayer1();

				if (p1name != null) {
					status = "Waiting for " + p1name + " to move...";
				}
			} else {
				// if both moves in this round are null, show that were starting this round
				// this status will be reset by the next round we look at (which is the
				// previous round).
				status = "Starting Round " + (game.getRoundNumber() + 1) + ".";
				gameLabels[0][i + 1].setText("");
				gameLabels[1][i + 1].setText("");
			}
		}

		// If we don't have all the players in the game yet, notify status
		if (game.getPlayer2() != null) {
			gameLabels[1][0].setText(game.getPlayer2() + ":");
		} else {
			status = "Waiting for Player 2...";
		}

		if (game.getPlayer1() != null) {
			gameLabels[0][0].setText(game.getPlayer1() + ":");
		} else {
			status = "Waiting for Player 1...";
		}

		// check for winner, show winner or tie
		String winner = game.getWinner();
		if (winner != null) {
			if (winner.equals("tie")) {
				status = "Tie.";
			} else {
				status = winner + " wins.";
			}
		}

		// add score to whatever status is if the game has both players
		if (game.getPlayer1() != null && game.getPlayer2() != null) {
			status += "   " + game.getPlayer1() + ": " + game.scorePlayer1() + ", " +
					game.getPlayer2() + ": " + game.scorePlayer2();
		}

		// finally update status label
		statusLabel.setText(status);
	}
}
