import java.util.*;

public class Main {
	static boolean whiteKingMoved = false;
	static boolean blackKingMoved = false;
	static boolean whiteQRookMoved = false;
	static boolean whiteKRookMoved = false;
	static boolean blackQRookMoved = false;
	static boolean blackKRookMoved = false;
	static String playerToMoveKingPos = "04";
	static String otherKingPos = "74";
	static String enPassantSquare = "";
	static String color = "";
	static int[] alphaBetaSavedEvals = new int[5];
	static ArrayList<String[][]> allPositionsW = new ArrayList<String[][]>();
	static ArrayList<String[][]> allPositionsB = new ArrayList<String[][]>();
	static boolean isEndgame = false;
	//static Map<String[][], Integer> transpositionMap = new HashMap<String[][], Integer>();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner in = new Scanner(System.in);
		/**
		 * Rating: 1211
     * Search: 3 ply in non-endgame, 5 ply in endgame
     * Evaluation: plentiful. Divided between non-endgame and endgame evaluation.
		 */
    String[][] board = {{"r", "n", "b", "q", "k", "b", "n", "r"},
							          {"p", "p", "p", "p", "p", "p", "p", "p"},
							          {" ", " ", " ", " ", " ", " ", " ", " "},
							          {" ", " ", " ", " ", " ", " ", " ", " "},
							          {" ", " ", " ", " ", " ", " ", " ", " "},
							          {" ", " ", " ", " ", " ", " ", " ", " "},
							          {"P", "P", "P", "P", "P", "P", "P", "P"},
							          {"R", "N", "B", "Q", "K", "B", "N", "R"}}; //This board is upside-down
		System.out.println("Which color would you like to play?");
		color = in.next();
		if(color.toLowerCase().equals("white")) {
			printBoard(board);
			playerMove(board);
		} else if(color.toLowerCase().equals("black")) {
			rotateBoard(board);
			playerToMoveKingPos += (7 - (otherKingPos.charAt(0) - '0'));
			playerToMoveKingPos += (7 - (otherKingPos.charAt(1) - '0'));
			otherKingPos += (7 - (playerToMoveKingPos.charAt(0) - '0'));
			otherKingPos += (7 - (playerToMoveKingPos.charAt(1) - '0'));
			playerToMoveKingPos = playerToMoveKingPos.substring(2);
			otherKingPos = otherKingPos.substring(2);
			opponentMove(board);
		} else {
			System.out.println("Please enter white or black.");
			main(args);
		}
	}
	public static void printBoard(String[][] arr) {
		System.out.println("	Black");
		for(int i = 7; i >= 0; i--) {
			System.out.print((i + 1) + " ");
			for(int j = 0; j <= 7; j++) {
				System.out.print("[" + arr[i][j] + "]");
			}
			System.out.println();
		}
		System.out.println("   a  b  c  d  e  f  g  h");
		System.out.println("	White");
	}
	public static String[][] copyBoard(String[][] arr) {
		String[][] newArr = new String[8][8];
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				newArr[i][j] = arr[i][j];
 			}
		}
		return newArr;
	}
	public static String[][] rotateBoard(String[][] arr) {
		String[][] newArr = new String[8][8];
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(Character.isLowerCase(arr[i][j].charAt(0))) {
					newArr[7-i][7-j] = arr[i][j].toUpperCase();
				} else if(Character.isUpperCase(arr[i][j].charAt(0))) {
					newArr[7-i][7-j] = arr[i][j].toLowerCase();
				} else {
					newArr[7-i][7-j] = arr[i][j];
				}
			}
		}
		playerToMoveKingPos += (7 - (otherKingPos.charAt(0) - '0'));
		playerToMoveKingPos += (7 - (otherKingPos.charAt(1) - '0'));
		otherKingPos += (7 - (playerToMoveKingPos.charAt(0) - '0'));
		otherKingPos += (7 - (playerToMoveKingPos.charAt(1) - '0'));
		playerToMoveKingPos = playerToMoveKingPos.substring(2);
		otherKingPos = otherKingPos.substring(2);
		return newArr;
	}
	public static void printRotatedBoard(String[][] arr) {
		System.out.println("	White");
		for(int i = 7; i >= 0; i--) {
			System.out.print((8 - i) + " ");
			for(int j = 0; j <= 7; j++) {
				System.out.print("[" + arr[i][j] + "]");
			}
			System.out.println();
		}
		System.out.println("   h  g  f  e  d  c  b  a");
		System.out.println("	Black");
	}
	public static void playerMove(String[][] arr) {
		Scanner in = new Scanner(System.in);
		ArrayList<String> allLegalMoves = generateAllLegalMoves(arr);
		if(allLegalMoves.size() == 0) {
			if(inCheck(arr)) {
				System.out.println("Checkmate! You lose!");
				return;
			} else {
				System.out.println("Stalemate! It's a draw!");
				return;
			}
		}
		if(allPositionsB.size() >= 50) {
			System.out.println("50-move rule! It's a draw!");
			return;
		}
		for(int i = 0; i < allPositionsB.size(); i++) {
			for(int j = i + 1; j < allPositionsB.size(); j++) {
				if(Arrays.deepEquals(allPositionsB.get(j), allPositionsB.get(i))) {
					for(int k = j + 1; k < allPositionsB.size(); k++) {
						if(Arrays.deepEquals(allPositionsB.get(j), allPositionsB.get(k))) {
							System.out.println("Threefold repetition! It's a draw!");
							return;
						}
					}
				}
			}
		}
		String move = "";
		String oldMove = "";
		boolean moveIsLegal = false;
		while (moveIsLegal == false) {
			System.out.println("Input your starting square and ending square:");
			oldMove = in.next();
			if(oldMove.equals("0-0") || oldMove.equals("0-0-0")) {
				if(oldMove.equals("0-0") && (whiteKingMoved || whiteKRookMoved) || 
						(oldMove.equals("0-0-0") && (whiteKingMoved || whiteQRookMoved))) {
					System.out.println("Your king or rook has moved!");
					moveIsLegal = false;
				} else {
					if(color.equals("white")) {
						moveIsLegal = checkCastling(arr, oldMove, 0, 1, 2, 3, 4, 5, 6, 7);
						if(moveIsLegal) {
							if(oldMove.equals("0-0")) playerToMoveKingPos = "06";
							else playerToMoveKingPos = "02";
						}
					} else {
						moveIsLegal = checkCastling(arr, oldMove, 7, 6, 5, 4, 3, 2, 1, 0);
						if(moveIsLegal) {
							if(oldMove.equals("0-0")) playerToMoveKingPos = "01";
							else playerToMoveKingPos = "05";
						}
					}
				}
				if(oldMove.equals("0-0")) {
					if(color.equals("black")) {
						arr[0][0] = " ";
						arr[0][1] = "k";
						arr[0][2] = "r";
						arr[0][3] = " ";
						playerToMoveKingPos = "01";
					} else {
						arr[0][4] = " ";
						arr[0][5] = "r";
						arr[0][6] = "k";
						arr[0][7] = " ";
						playerToMoveKingPos = "06";
					}
				} else if(oldMove.equals("0-0-0")) {
					if(color.equals("white")) {
						arr[0][0] = " ";
						arr[0][2] = "k";
						arr[0][3] = "r";
						arr[0][4] = " ";
						playerToMoveKingPos = "02";
					} else {
						arr[0][3] = " ";
						arr[0][4] = "r";
						arr[0][5] = "k";
						arr[0][7] = " ";
						playerToMoveKingPos = "05";
					}
				} 
			} else {
				if(oldMove.length() != 4) {
					System.out.println("Invalid move.");
					continue;
				}
				if(color.equals("white")) {
					move = "" + (oldMove.charAt(1) - '1') + (oldMove.charAt(0) - 'a') + 
							(oldMove.charAt(3) - '1') + (oldMove.charAt(2) - 'a');
				} else {
					move = "" + (7 - (oldMove.charAt(1) - '1')) + (7 - (oldMove.charAt(0) - 'a')) + 
							(7 - (oldMove.charAt(3) - '1')) + (7 - (oldMove.charAt(2) - 'a'));
				}
				try {
					int test = Integer.parseInt(move);
				} catch (Exception NumberFormatException) {
					System.out.println("Invalid move.");
					playerMove(arr);
					return;
				}
				if(move.length() != 4 || move.contains("8") || move.contains("9")) {
					System.out.println("Invalid move.");
					continue;
				}
				if(allLegalMoves.contains(move)) {
					moveIsLegal = true;
					if(!arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals(" ")) {
						allPositionsW.clear();
						allPositionsB.clear();
					}
					arr[move.charAt(2) - '0'][move.charAt(3) - '0'] = arr[move.charAt(0) - '0'][move.charAt(1) - '0'];
					arr[move.charAt(0) - '0'][move.charAt(1) - '0'] = " ";
					if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals("p")) {
						allPositionsW.clear();
						allPositionsB.clear();
					}
					if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals("k")) {
						playerToMoveKingPos = move.substring(2);
						whiteKingMoved = true;
					} else if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals("r")) {
						if(move.charAt(0) == '0') {
							if(move.charAt(1) == '0') {
								whiteQRookMoved = true;
							} else if(move.charAt(1) == '7') {
								whiteKRookMoved = true;
							}
						}
					} else if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals("p") &&
							move.charAt(3) - move.charAt(1) == 0 && move.charAt(2) - move.charAt(0) == 2) {
						enPassantSquare = move.substring(2);
					} else {
						enPassantSquare = "";
					}
				} else {
					if(allLegalMoves.get(allLegalMoves.size() - 1).substring(0, 4).equals(move)) {
						moveIsLegal = true;
						arr[move.charAt(2) - '0'][move.charAt(3) - '0'] = arr[move.charAt(0) - '0'][move.charAt(1) - '0'];
						arr[move.charAt(0) - '0'][move.charAt(1) - '0'] = " ";
						arr[move.charAt(2) - '0' - 1][move.charAt(3) - '0'] = " ";
						allPositionsW.clear();
						allPositionsB.clear();
						enPassantSquare = "";
					}
				}
			}
			if(moveIsLegal) {
				arr = promote(arr, 1);
				allPositionsW.add(copyBoard(arr));
				arr = rotateBoard(arr);		
				if(color.equals("white")) {
					printRotatedBoard(arr);
				} else {
					printBoard(arr);
				}
				opponentMove(arr);
			} else {
				System.out.println("Illegal move.");
			}
		}
	}
	public static boolean checkRookMove(String[][] arr, String move) {
		try {
			boolean sameRank = false;
			boolean sameFile = false;
			if(move.charAt(0) == move.charAt(2)) {
				sameRank = true;
			}
			if(move.charAt(1) == move.charAt(3)) {
				sameFile = true;
			}
			if(sameFile && sameRank || !sameFile && !sameRank) {
				return false;
			}
			if(sameRank) {
				for(int i = Math.min(move.charAt(1) - '0', move.charAt(3) - '0') + 1;
						i < Math.max(move.charAt(1) - '0', move.charAt(3) - '0'); i++) {
					if(!arr[move.charAt(0) - '0'][i].equals(" ")) {
						return false;
					}
				}
			} else {
				for(int i = Math.min(move.charAt(0) - '0', move.charAt(2) - '0') + 1;
						i < Math.max(move.charAt(0) - '0', move.charAt(2) - '0'); i++) {
					if(!arr[i][move.charAt(1) - '0'].equals(" ")) {
						return false;
					}
				}
			}
			if(Character.isLowerCase(arr[Integer.parseInt(move.substring(2, 3))][Integer.parseInt(move.substring(3, 4))].charAt(0))) {
				return false;
			}
			String[][] newArr = copyBoard(arr);
			newArr[move.charAt(2) - '0'][move.charAt(3) - '0'] = "r";
			newArr[move.charAt(0) - '0'][move.charAt(1) - '0'] = " ";
			if(inCheck(newArr)) {
				return false;
			}
			return true;
		} catch (Exception ArrayIndexOutOfBoundsException) {
			return false;
		}
	}
	public static boolean checkBishopMove(String[][] arr, String move) {
		try {
			if(move.charAt(0) - move.charAt(2) == move.charAt(1) - move.charAt(3)) {
				if(move.charAt(2) > move.charAt(0)) {
					for(int i = 1; i < move.charAt(2) - move.charAt(0); i++) {
						if(!(arr[move.charAt(0) - '0' + i][move.charAt(1) - '0' + i].equals(" "))) {
							return false;
						}
					}
				} else {
					for(int i = 1; i < move.charAt(0) - move.charAt(2); i++) {
						if(!(arr[move.charAt(0) - '0' - i][move.charAt(1) - '0' - i].equals(" "))) {
							return false;
						}
					}
				}
				if(Character.isLowerCase(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].charAt(0))) {
					return false;
				}
			} else if(move.charAt(0) + move.charAt(1) == move.charAt(2) + move.charAt(3)) {
				if(move.charAt(2) > move.charAt(0)) {
					for(int i = 1; i < move.charAt(2) - move.charAt(0); i++) {
						if(!(arr[move.charAt(0) - '0' + i][move.charAt(1) - '0' - i].equals(" "))) {
							return false;
						}
					}
				} else {
					for(int i = 1; i < move.charAt(0) - move.charAt(2); i++) {
						if(!(arr[move.charAt(0) - '0' - i][move.charAt(1) - '0' + i].equals(" "))) {
							return false;
						}
					}
				}
				if(Character.isLowerCase(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].charAt(0))) {
					return false;
				}
			} else {
				return false;
			}
			String[][] newArr = copyBoard(arr);
			newArr[move.charAt(2) - '0'][move.charAt(3) - '0'] = "b";
			newArr[move.charAt(0) - '0'][move.charAt(1) - '0'] = " ";
			if(inCheck(newArr)) {
				return false;
			}
			return true;
		} catch (Exception ArrayIndexOutOfBoundsException) {
			return false;
		}
	}
	public static boolean checkQueenMove(String[][] arr, String move) {
		try {
			if(checkBishopMove(arr, move) || checkRookMove(arr, move)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ArrayIndexOutOfBoundsException) {
			return false;
		}
	}
	public static boolean checkKnightMove(String[][] arr, String move) {
		try {
			if((Math.abs(move.charAt(0) - move.charAt(2)) == 2 && Math.abs(move.charAt(1) - move.charAt(3)) == 1) ||
					(Math.abs(move.charAt(0) - move.charAt(2)) == 1 && Math.abs(move.charAt(1) - move.charAt(3)) == 2)) {
				if(Character.isLowerCase(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].charAt(0))) {
					return false;
				}
				String[][] newArr = copyBoard(arr);
				newArr[move.charAt(2) - '0'][move.charAt(3) - '0'] = "n";
				newArr[move.charAt(0) - '0'][move.charAt(1) - '0'] = " ";
				if(inCheck(newArr)) {
					return false;
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception ArrayListIndexOutOfBoundsException) {
			return false;
		}
	}
	public static boolean checkPawnMove(String[][] arr, String move) {
		try {
			if(move.charAt(2) - move.charAt(0) == 1 && move.charAt(1) == move.charAt(3)) {
				if(!arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals(" ")) {
					return false;
				}
			} else if(move.charAt(2) - move.charAt(0) == 1 && Math.abs(move.charAt(1) - move.charAt(3)) == 1) {
				if(!Character.isUpperCase(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].charAt(0))) {
					return false;
				}
			} else if(move.charAt(2) - move.charAt(0) == 2 && move.charAt(1) == move.charAt(3)) {
				if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals(" ") && 
						arr[move.charAt(2) - '0' - 1][move.charAt(3) - '0'].equals(" ") && move.charAt(0) == '1') {
				} else {
					return false;
				}
			} else {
				return false;
			}
			String[][] newArr = copyBoard(arr);
			newArr[move.charAt(2) - '0'][move.charAt(3) - '0'] = "p";
			newArr[move.charAt(0) - '0'][move.charAt(1) - '0'] = " ";
			if(inCheck(newArr)) {
				return false;
			}
			return true;
		} catch (Exception ArrayIndexOutOfBoundsException) {
			return false;
		}
	}
	public static boolean checkKingMove(String[][] arr, String move) {
		try {
			if((Math.abs(move.charAt(0) - move.charAt(2)) <= 1 && Math.abs(move.charAt(1) - move.charAt(3)) <= 1) && 
					!Character.isLowerCase(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].charAt(0))) {
				String[][] newArr = copyBoard(arr);
				newArr[move.charAt(2) - '0'][move.charAt(3) - '0'] = "k";
				newArr[move.charAt(0) - '0'][move.charAt(1) - '0'] = " ";
				playerToMoveKingPos = move.substring(2);
				if(inCheck(newArr)) {
					playerToMoveKingPos = move.substring(0, 2);
					return false;
				}
				playerToMoveKingPos = move.substring(0, 2);
				return true;
			} else {
				return false;
			}
		} catch (Exception ArrayIndexOutOfBoundsException) {
			return false;
		}
	}
	public static boolean checkCastling(String[][] arr, String move, int a, int b, int c, int d, int e, int f, int g, int h) {
		if(inCheck(arr)) {
			//System.out.println("Check on e file " + playerToMoveKingPos);
			return false;
		}
		if(move.equals("0-0")) {
			if(arr[0][e].equals("k") && arr[0][f].equals(" ") && arr[0][g].equals(" ") && arr[0][h].equals("r")) {
				String[][] newArr = copyBoard(arr);
				newArr[0][f] = "k";
				newArr[0][e] = " ";
				playerToMoveKingPos = "" + 0 + f;
				if(inCheck(newArr)) {
					playerToMoveKingPos = "" + 0 + e;
					//System.out.println("Check on f file " + playerToMoveKingPos);
					return false;
				}
				newArr[0][g] = "k";
				newArr[0][f] = " ";
				playerToMoveKingPos = "" + 0 + g;
				if(inCheck(newArr)) {
					playerToMoveKingPos = "" + 0 + e;					
					//System.out.println("Check on g file " + playerToMoveKingPos);
					return false;
				}
				newArr[0][f] = "r";
				newArr[0][h] = " ";
				playerToMoveKingPos = "" + 0 + e;
				return true;
			} else {
				//System.out.println("Check #4 " + playerToMoveKingPos);
				return false;
			}
		} else {
			if(arr[0][e].equals("k") && arr[0][d].equals(" ") && arr[0][c].equals(" ") && arr[0][b].equals(" ") 
					&& arr[0][a].equals("r")) {
				String[][] newArr = copyBoard(arr);
				newArr[0][d] = "k";
				newArr[0][e] = " ";
				playerToMoveKingPos = "" + 0 + d;
				if(inCheck(newArr)) {
					playerToMoveKingPos = "" + 0 + e;
					return false;
				}
				newArr[0][c] = "k";
				newArr[0][d] = " ";
				playerToMoveKingPos = "" + 0 + c;
				if(inCheck(newArr)) {
					playerToMoveKingPos = "" + 0 + e;
					return false;
				}
				newArr[0][d] = "r";
				newArr[0][a] = " ";
				playerToMoveKingPos = "" + 0 + e;
				return true;
			} else {
				return false;
			}
		}
	}
	public static String[][] promote(String[][] arr, int player) {
		Scanner in = new Scanner(System.in);
		for(int i = 0; i < 8; i++) {
			if(arr[7][i].equals("p")) {
				if(player == 1) {
					String piece = "";
					while(!piece.equals("knight") && !piece.equals("bishop") && !piece.equals("rook") && !piece.equals("queen")) {
						System.out.println("Which piece would you like to promote the pawn to?");
						piece = in.next().toLowerCase();
					}
					switch(piece) {
					case "knight":
						arr[7][i] = "n";
						break;
					case "bishop":
						arr[7][i] = "b";
						break;
					case "rook":
						arr[7][i] = "r";
						break;
					case "queen":
						arr[7][i] = "q";
						break;
					}
				} else {
					arr[7][i] = "q";
					String[][] arrKnight = copyBoard(arr);
					arrKnight[7][i] = "n";
					if(evaluate(arr) > evaluate(arrKnight)) {
						arr = copyBoard(arrKnight);
					}
				}
			}
		}
		return arr;
	}
	public static boolean rookCheck(String[][] arr, int x, int y, int xInc, int yInc) {
		String piece = " ";
		while(piece.equals(" ")) {
			try {
				x += xInc;
				y += yInc;
				piece = arr[x][y];
			} catch (Exception ArrayIndexOutOfBoundsException) {
				break;
			}
		}
		if(piece.equals("R") && (xInc == 0 || yInc == 0) || piece.equals("B") && xInc != 0 && yInc != 0 || piece.equals("Q")) {
			return true;
		}
		return false;
	}
	public static boolean queenCheck(String[][] arr, int x, int y, int xInc, int yInc) {
		String piece = " ";
		while(piece.equals(" ")) {
			try {
				x += xInc;
				y += yInc;
				piece = arr[x][y];
			} catch (Exception ArrayIndexOutOfBoundsException) {
				break;
			}
		}
		if(piece.equals("Q")) {
			return true;
		}
		return false;
	}
	public static boolean knightCheck(String[][] arr, int x, int y, int xInc, int yInc) {
		try {
			if(arr[x + xInc][y + yInc].equals("N")) {
				return true;
			}
		} catch (Exception ArrayIndexOutOfBoundsException){
			return false;
		}
		return false;
	}
	public static boolean pawnCheck(String[][] arr, int x, int y, int yInc) {
		try {
			if(arr[x + 1][y + yInc].equals("P")) {
				return true;
			}
		} catch (Exception ArrayIndexOutOfBoundsException ){
			return false;
		}
		return false;
	}
	public static boolean inCheck(String[][] arr) {
		boolean finalCheck = false;
		int x = playerToMoveKingPos.charAt(0) - '0';
		int y = playerToMoveKingPos.charAt(1) - '0';
		finalCheck |= rookCheck(arr, x, y, 1, 0);
		finalCheck |= rookCheck(arr, x, y, -1, 0);
		finalCheck |= rookCheck(arr, x, y, 0, -1);
		finalCheck |= rookCheck(arr, x, y, 0, 1); //These 4 lines check for rook
		finalCheck |= rookCheck(arr, x, y, 1, 1);
		finalCheck |= rookCheck(arr, x, y, -1, 1);
		finalCheck |= rookCheck(arr, x, y, 1, -1);
		finalCheck |= rookCheck(arr, x, y, -1, -1); //These 8 lines in total will make up a queen and bishop
		finalCheck |= knightCheck(arr, x, y, 2, 1);
		finalCheck |= knightCheck(arr, x, y, 1, 2);
		finalCheck |= knightCheck(arr, x, y, -2, 1);
		finalCheck |= knightCheck(arr, x, y, -1, 2);
		finalCheck |= knightCheck(arr, x, y, 2, -1);
		finalCheck |= knightCheck(arr, x, y, 1, -2);
		finalCheck |= knightCheck(arr, x, y, -2, -1);
		finalCheck |= knightCheck(arr, x, y, -1, -2); //These 8 lines check for knight
		finalCheck |= (Math.abs(x - (otherKingPos.charAt(0) - '0')) <= 1 && Math.abs(y - (otherKingPos.charAt(1) - '0')) <= 1); //king
		finalCheck |= pawnCheck(arr, x, y, 1);
		finalCheck |= pawnCheck(arr, x, y, -1);
		return finalCheck;
	}
	public static int squareAttacked(String[][] arr, int x, int y) {
		int num = 0;
		if(rookCheck(arr, x, y, 1, 0)) num++;
		if(rookCheck(arr, x, y, -1, 0)) num++;
		if(rookCheck(arr, x, y, 0, -1)) num++;
		if(rookCheck(arr, x, y, 0, 1)) num++;
		if(rookCheck(arr, x, y, 1, 1)) num++;
		if(rookCheck(arr, x, y, -1, 1)) num++;
		if(rookCheck(arr, x, y, 1, -1)) num++;
		if(rookCheck(arr, x, y, -1, -1)) num++;
		if(knightCheck(arr, x, y, 2, 1)) num++;
		if(knightCheck(arr, x, y, 1, 2)) num++;
		if(knightCheck(arr, x, y, -2, 1)) num++;
		if(knightCheck(arr, x, y, -1, 2)) num++;
		if(knightCheck(arr, x, y, 2, -1)) num++;
		if(knightCheck(arr, x, y, 1, -2)) num++;
		if(knightCheck(arr, x, y, -2, -1)) num++;
		if(knightCheck(arr, x, y, -1, -2)) num++; //These 8 lines check for knight
		if(pawnCheck(arr, x, y, 1)) num++;
		if(pawnCheck(arr, x, y, -1)) num++;
		return num;
	}
	public static ArrayList<Integer> staticExchange(String[][] arr, int x, int y) {
		ArrayList<Integer> pieces = new ArrayList<Integer>();
		if(rookCheck(arr, x, y, 1, 0)) {
			if(queenCheck(arr, x, y, 1, 0)) {
				pieces.add(9);
			} else {
				pieces.add(5);
			}
		}
		if(rookCheck(arr, x, y, -1, 0)) {
			if(queenCheck(arr, x, y, -1, 0)) {
				pieces.add(9);
			} else {
				pieces.add(5);
			}
		}
		if(rookCheck(arr, x, y, 0, -1)) {
			if(queenCheck(arr, x, y, 0, -1)) {
				pieces.add(9);
			} else {
				pieces.add(5);
			}
		}
		if(rookCheck(arr, x, y, 0, 1)) {
			if(queenCheck(arr, x, y, 0, 1)) {
				pieces.add(9);
			} else {
				pieces.add(5);
			}
		}
		if(rookCheck(arr, x, y, 1, 1)) {
			if(queenCheck(arr, x, y, 1, 1)) {
				pieces.add(9);
			} else {
				pieces.add(3);
			}
		}
		if(rookCheck(arr, x, y, -1, 1)) {
			if(queenCheck(arr, x, y, -1, 1)) {
				pieces.add(9);
			} else {
				pieces.add(3);
			}
		}
		if(rookCheck(arr, x, y, 1, -1)) {
			if(queenCheck(arr, x, y, 1, -1)) {
				pieces.add(9);
			} else {
				pieces.add(3);
			}
		}
		if(rookCheck(arr, x, y, -1, -1)) {
			if(queenCheck(arr, x, y, -1, -1)) {
				pieces.add(9);
			} else {
				pieces.add(3);
			}
		}
		if(knightCheck(arr, x, y, 2, 1)) pieces.add(3);
		if(knightCheck(arr, x, y, 1, 2)) pieces.add(3);
		if(knightCheck(arr, x, y, -2, 1)) pieces.add(3);
		if(knightCheck(arr, x, y, -1, 2)) pieces.add(3);
		if(knightCheck(arr, x, y, 2, -1)) pieces.add(3);
		if(knightCheck(arr, x, y, 1, -2)) pieces.add(3);
		if(knightCheck(arr, x, y, -2, -1)) pieces.add(3);
		if(knightCheck(arr, x, y, -1, -2)) pieces.add(3);
		if(pawnCheck(arr, x, y, 1)) pieces.add(1);
		if(pawnCheck(arr, x, y, -1)) pieces.add(1);
		return pieces;
	}
	public static ArrayList<String> generateAllLegalMoves(String[][] arr) {
		ArrayList<String> allLegalMoves = new ArrayList<String>();
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(Character.isLowerCase(arr[i][j].charAt(0))) {
					switch(arr[i][j]) {
					case "p":
						if(i + 1 < 8) {
							if(checkPawnMove(arr, "" + i + j + (i+1) + j)) {
								allLegalMoves.add("" + i + j + (i+1) + j);
							}
							if(j + 1 < 8) {
								if(checkPawnMove(arr, "" + i + j + (i+1) + (j+1))) allLegalMoves.add("" + i + j + (i+1) + (j+1));
							}
							if(j - 1 >= 0) {
								if(checkPawnMove(arr, "" + i + j + (i+1) + (j-1))) allLegalMoves.add("" + i + j + (i+1) + (j-1));
							}
						}
						if(i + 2 < 8) {
							if(checkPawnMove(arr, "" + i + j + (i+2) + j)) allLegalMoves.add("" + i + j + (i+2) + j);
						}
						break;
					case "n":
						for(int k = 1; k <= 2; k++) {
							if(checkKnightMove(arr, "" + i + j + (i+k) + (j+3-k))) allLegalMoves.add("" + i + j + (i+k) + (j+3-k));
							if(checkKnightMove(arr, "" + i + j + (i-k) + (j+3-k))) allLegalMoves.add("" + i + j + (i-k) + (j+3-k));
							if(checkKnightMove(arr, "" + i + j + (i+k) + (j-3+k))) allLegalMoves.add("" + i + j + (i+k) + (j-3+k));
							if(checkKnightMove(arr, "" + i + j + (i-k) + (j-3+k))) allLegalMoves.add("" + i + j + (i-k) + (j-3+k));
						}
						break;
					case "b":
						for(int k = 1; k < 8; k++) {
							if(i + k < 8 && j + k < 8) {
								if(checkBishopMove(arr, "" + i + j + (i+k) + (j+k))) allLegalMoves.add("" + i + j + (i+k) + (j+k));
							}
							if(i + k < 8 && j - k >= 0) {
								if(checkBishopMove(arr, "" + i + j + (i+k) + (j-k))) allLegalMoves.add("" + i + j + (i+k) + (j-k));
							}
							if(i - k >= 0 && j + k < 8) {
								if(checkBishopMove(arr, "" + i + j + (i-k) + (j+k))) allLegalMoves.add("" + i + j + (i-k) + (j+k));
							}
							if(i - k >= 0 && j - k >= 0) {
								if(checkBishopMove(arr, "" + i + j + (i-k) + (j-k))) allLegalMoves.add("" + i + j + (i-k) + (j-k));
							}
						}
						break;
					case "r":
						for(int k = 0; k < 8; k++) {
							if(checkRookMove(arr, "" + i + j + k + j)) {
								allLegalMoves.add("" + i + j + k + j);
							}
							if(checkRookMove(arr, "" + i + j + i + k)) {
								allLegalMoves.add("" + i + j + i + k);
							}
						}
						break;
					case "q":
						for(int k = 1; k < 8; k++) {
							if(i + k < 8 && j + k < 8) {
								if(checkBishopMove(arr, "" + i + j + (i+k) + (j+k))) allLegalMoves.add("" + i + j + (i+k) + (j+k));
							}
							if(i + k < 8 && j - k >= 0) {
								if(checkBishopMove(arr, "" + i + j + (i+k) + (j-k))) allLegalMoves.add("" + i + j + (i+k) + (j-k));
							}
							if(i - k >= 0 && j + k < 8) {
								if(checkBishopMove(arr, "" + i + j + (i-k) + (j+k))) allLegalMoves.add("" + i + j + (i-k) + (j+k));
							}
							if(i - k >= 0 && j - k >= 0) {
								if(checkBishopMove(arr, "" + i + j + (i-k) + (j-k))) allLegalMoves.add("" + i + j + (i-k) + (j-k));
							}
						}
						for(int k = 0; k < 8; k++) {
							if(checkRookMove(arr, "" + i + j + k + j)) allLegalMoves.add("" + i + j + k + j);
							if(checkRookMove(arr, "" + i + j + i + k)) allLegalMoves.add("" + i + j + i + k);;
						}
						break;
					case "k":
						for(int k = -1; k <= 1; k++) {
							for(int l = -1; l <= 1; l++) {
								if(checkKingMove(arr, "" + i + j + (i+k) + (j+l))) {
									allLegalMoves.add("" + i + j + (i+k) + (j+l));
								}
							}
						}
						break;
					}
				}
			}
		}
		if(!(enPassantSquare.equals(""))) {
			String[][] newArr = copyBoard(arr);
			try {
				if(arr[7 - (enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1) - '0') - 1].equals("p")) {
					newArr[7 - (enPassantSquare.charAt(0) - '0') + 1][7 - (enPassantSquare.charAt(1) - '0')] = 
							newArr[(enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1)  - '0') - 1];
					newArr[7 - (enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1)  - '0') - 1] = " ";
					newArr[7 - (enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1) - '0')] = " ";
					if(!inCheck(newArr)) {
						allLegalMoves.add("" + (7 - (enPassantSquare.charAt(0) - '0')) + (7 - (enPassantSquare.charAt(1) - '0') - 1) + 
							(7 - (enPassantSquare.charAt(0) - '0') + 1) + (7 - (enPassantSquare.charAt(1) - '0')) + "ep"); 
						
					}
				}
			} catch (Exception ArrayIndexOutOfBoundsException) {
				
			}
			try {
				if(arr[7 - (enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1) - '0') + 1].equals("p")) {
					newArr[7 - (enPassantSquare.charAt(0) - '0') + 1][7 - (enPassantSquare.charAt(1) - '0')] = 
							newArr[(enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1)  - '0') + 1];
					newArr[7 - (enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1)  - '0') + 1] = " ";
					newArr[7 - (enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1) - '0')] = " ";
					if(!inCheck(newArr)) {
						allLegalMoves.add("" + (7 - (enPassantSquare.charAt(0) - '0')) + (7 - (enPassantSquare.charAt(1) - '0') + 1) + 
							(7 - (enPassantSquare.charAt(0) - '0') + 1) + (7 - (enPassantSquare.charAt(1) - '0')) + "ep");
					}
				}
			} catch (Exception ArrayIndexOutOfBoundsException) {
				
			}
		}
		return allLegalMoves;
	}
	public static void opponentMove(String[][] arr) {
		Scanner in = new Scanner(System.in);
		if(!isEndgame) {
			int pieceCount = 0;
			for(int i = 0; i < arr.length; i++) {
				for(int j = 0; j < arr[i].length; j++) {
					if(!arr[i][j].equals(" ") && !arr[i][j].equals("p") && !arr[i][j].equals("P")) {
						pieceCount++;
					}
				}
			}
			if(pieceCount <= 7) isEndgame = true;
		}
		ArrayList<String> allLegalMoves = generateAllLegalMoves(arr);
		if(allLegalMoves.size() == 0) {
			if(inCheck(arr)) {
				System.out.println("Checkmate! You win!");
				return;
			} else {
				System.out.println("Stalemate! It's a draw!");
				return;
			}
		}
		if(allPositionsW.size() >= 50) {
			System.out.println("50-move rule! It's a draw!");
			return;
		}
		for(int i = 0; i < allPositionsW.size(); i++) {
			for(int j = i + 1; j < allPositionsW.size(); j++) {
				if(Arrays.deepEquals(allPositionsW.get(j), allPositionsW.get(i))) {
					for(int k = j + 1; k < allPositionsW.size(); k++) {
						if(Arrays.deepEquals(allPositionsW.get(j), allPositionsW.get(k))) {
							System.out.println("Threefold repetition! It's a draw!");
							return;
						}
					}
				}
			}
		}
		String move = "";
		String oldMove = "";
		int curBestEvaluation = Integer.MIN_VALUE;
		boolean moveIsLegal = false;
		if(!blackKingMoved) {
			if(color.equals("black")) {
				if(checkCastling(arr, "0-0", 0, 1, 2, 3, 4, 5, 6, 7) && !blackKRookMoved) allLegalMoves.add("0-0");
				if(checkCastling(arr, "0-0-0", 0, 1, 2, 3, 4, 5, 6, 7) && !blackQRookMoved) allLegalMoves.add("0-0-0");
			} else {
				if(checkCastling(arr, "0-0", 7, 6, 5, 4, 3, 2, 1, 0) && !blackKRookMoved) allLegalMoves.add("0-0");
				if(checkCastling(arr, "0-0-0", 0, 1, 2, 3, 4, 5, 6, 7) && !blackQRookMoved) allLegalMoves.add("0-0-0");
			}
		}
		while (moveIsLegal == false) {
			//transpositionMap.clear();
			for(int i = 0; i < allLegalMoves.size(); i++) {
				// Alpha-beta pruning
				alphaBetaSavedEvals[0] = Integer.MIN_VALUE;
				alphaBetaSavedEvals[1] = Integer.MAX_VALUE;
				alphaBetaSavedEvals[2] = Integer.MIN_VALUE;
				alphaBetaSavedEvals[3] = Integer.MAX_VALUE;
				alphaBetaSavedEvals[4] = Integer.MIN_VALUE;
				TreeNode x;
				if(isEndgame) {
					x = minimax(allLegalMoves.get(i), 1, 3, arr);
          System.out.println(x.move + " " + x.eval);
				} else {
					x = minimax(allLegalMoves.get(i), 1, 3, arr);
          System.out.println(x.move + " " + x.eval);
				}
				if(x.eval > curBestEvaluation) {
					curBestEvaluation = x.eval;
					oldMove = x.move;
				} else if(x.eval == curBestEvaluation) {
					if(Math.random() > 0.5) {
						oldMove = x.move;
					}
				}
			}
			move = oldMove;
			if(allLegalMoves.contains(move)) {
				moveIsLegal = true;
				if(move.equals("0-0")) {
					if(color.equals("white")) {
						arr[0][0] = " ";
						arr[0][1] = "k";
						arr[0][2] = "r";
						arr[0][3] = " ";
						playerToMoveKingPos = "01";
					} else {
						arr[0][4] = " ";
						arr[0][5] = "r";
						arr[0][6] = "k";
						arr[0][7] = " ";
						playerToMoveKingPos = "06";
					}
				} else if(move.equals("0-0-0")) {
					if(color.equals("black")) {
						arr[0][0] = " ";
						arr[0][2] = "k";
						arr[0][3] = "r";
						arr[0][4] = " ";
						playerToMoveKingPos = "02";
					} else {
						arr[0][3] = " ";
						arr[0][4] = "r";
						arr[0][5] = "k";
						arr[0][7] = " ";
						playerToMoveKingPos = "05";
					}
				} else {
					if(!arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals(" ")) {
						allPositionsW.clear();
						allPositionsB.clear();
					}
					arr[move.charAt(2) - '0'][move.charAt(3) - '0'] = arr[move.charAt(0) - '0'][move.charAt(1) - '0'];
					arr[move.charAt(0) - '0'][move.charAt(1) - '0'] = " ";
					if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals("p")) {
						allPositionsW.clear();
						allPositionsB.clear();
					}
					if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals("k")) {
						enPassantSquare = "";
						playerToMoveKingPos = move.substring(2);
						blackKingMoved = true;
					} else if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals("r")) {
						enPassantSquare = "";
						if(move.charAt(0) == '0') {
							if(move.charAt(1) == '0') {
								blackQRookMoved = true;
							} else if(move.charAt(1) == '7') {
								blackKRookMoved = true;
							}
						}
					} else if(arr[move.charAt(2) - '0'][move.charAt(3) - '0'].equals("p")) {
						if(move.charAt(3) - move.charAt(1) == 0 && move.charAt(2) - move.charAt(0) == 2) {
							enPassantSquare = move.substring(2);
						} else if(!enPassantSquare.equals("") && 7 - (enPassantSquare.charAt(0) - '0') == move.charAt(2) - '1'
								&& 7 - (enPassantSquare.charAt(1) - '0') == move.charAt(3) - '0') {
							arr[move.charAt(2) - '0' - 1][move.charAt(3) - '0'] = " ";
							allPositionsW.clear();
							allPositionsB.clear();
							enPassantSquare = "";
						}
					} else {
						enPassantSquare = "";
					}	
				}
			}
		}
		if(moveIsLegal) {
			if(move.equals("0-0") || move.equals("0-0-0")) {
				System.out.println("My move is " + move + "!");
			} else if(color.equals("white")) {
				System.out.println("My move is " + (char)(7 - (move.charAt(1) - '0') + 'a') + (8 - (move.charAt(0) - '0')) + 
						(char)(7 - (move.charAt(3) - '0') + 'a') + (8 - (move.charAt(2) - '0')) + "!");
			} else {
				System.out.println("My move is " + (char)((move.charAt(1) - '0') + 'a') + ((move.charAt(0) - '0') + 1) + 
						(char)((move.charAt(3) - '0') + 'a') + ((move.charAt(2) - '0') + 1) + "!");
			}
			arr = promote(arr, 2);
			allPositionsB.add(copyBoard(arr));
			arr = rotateBoard(arr);
			if(color.equals("white")) {
				printBoard(arr);
			} else {
				printRotatedBoard(arr);
			}
			playerMove(arr);
		} else {
			System.out.println("This line should never print.");
		}
	}
	static class TreeNode {
		public String move;
		public int eval;
		public ArrayList<TreeNode> nextReplies;
		TreeNode(String a) {
			move = a;
			nextReplies = new ArrayList<TreeNode>();
		}
	}
	static TreeNode minimax(String currentMove, int curDepth, int finalDepth, String[][] arr) {
		String[][] newArr = copyBoard(arr);
		TreeNode root = new TreeNode(currentMove);
		String tempKingPos = playerToMoveKingPos;
		String tempOtherKingPos = otherKingPos;
		ArrayList<String[][]> tempAllPositionsW = new ArrayList<String[][]>(allPositionsW);
		ArrayList<String[][]> tempAllPositionsB = new ArrayList<String[][]>(allPositionsB);
		if(currentMove.equals("0-0") || currentMove.equals("0-0-0")) {
			if(color.equals("white") && currentMove.equals("0-0")) {
				newArr[0][0] = " ";
				newArr[0][1] = "k";
				newArr[0][2] = "r";
				newArr[0][3] = " ";
				playerToMoveKingPos = "01";
			} else if(color.equals("white") && currentMove.equals("0-0-0")) {
				newArr[0][7] = " ";
				newArr[0][5] = "k";
				newArr[0][4] = "r";
				newArr[0][3] = " ";
				playerToMoveKingPos = "05";
			} else if(color.equals("black") && currentMove.equals("0-0")) {
				newArr[0][7] = " ";
				newArr[0][6] = "k";
				newArr[0][5] = "r";
				newArr[0][4] = " ";
				playerToMoveKingPos = "06";
			} else {
				newArr[0][0] = " ";
				newArr[0][2] = "k";
				newArr[0][3] = "r";
				newArr[0][4] = " ";
				playerToMoveKingPos = "02";
			}
		} else {
			if(!newArr[currentMove.charAt(2) - '0'][currentMove.charAt(3) - '0'].equals(" ")) {
				allPositionsW.clear();
				allPositionsB.clear();
			}
			newArr[currentMove.charAt(2) - '0'][currentMove.charAt(3) - '0'] = 
					newArr[currentMove.charAt(0) - '0'][currentMove.charAt(1) - '0'];
			newArr[currentMove.charAt(0) - '0'][currentMove.charAt(1) - '0'] = " ";
			if(newArr[currentMove.charAt(2) - '0'][currentMove.charAt(3) - '0'].equals("k")) {
				playerToMoveKingPos = currentMove.substring(2);
			}
			if(newArr[currentMove.charAt(2) - '0'][currentMove.charAt(3) - '0'].equals("p")) {
				allPositionsW.clear();
				allPositionsB.clear();
			}
			if(currentMove.length() >= 6) {
				newArr[7 - (enPassantSquare.charAt(0) - '0')][7 - (enPassantSquare.charAt(1) - '0')] = " ";
			}
		}
		newArr = promote(newArr, 2);
		if(curDepth % 2 == 1) {
			allPositionsB.add(newArr);
		} else {
			allPositionsW.add(newArr);
		}
		if(threefoldCheck()) {
			root.eval = 0;
			playerToMoveKingPos = tempKingPos;
			otherKingPos = tempOtherKingPos;
			allPositionsW = new ArrayList<String[][]>(tempAllPositionsW);
			allPositionsB = new ArrayList<String[][]>(tempAllPositionsB);
			return root;
		}
		newArr = rotateBoard(newArr);
		ArrayList<String> allLegalMoves = generateAllLegalMoves(newArr);
		if(allLegalMoves.size() == 0) {
			if(curDepth % 2 == 1) {
				if(inCheck(newArr)) {
					root.eval = 9999999 - curDepth;
				} else {
					root.eval = 0;
				}
			} else {
				if(inCheck(newArr)) {
					root.eval = -9999999 + curDepth;
				} else {
					root.eval = 0;
				}
			}
		} else if(curDepth == finalDepth) {
			root.eval = evaluate(newArr);
		} else {
			for(int i = 0; i < allLegalMoves.size(); i++) {
				root.nextReplies.add(minimax(allLegalMoves.get(i), curDepth + 1, finalDepth, newArr));
        
				// Alpha-beta pruning
				if(curDepth % 2 == 1) {
					if(root.eval < alphaBetaSavedEvals[curDepth - 1]) {
						break;
					}
				} else {
					if(root.eval > alphaBetaSavedEvals[curDepth - 1]) {
						break;
					}
				}
			}
			if(curDepth % 2 == 1) {
				root.eval = min(root);

				// Alpha-beta pruning
				alphaBetaSavedEvals[curDepth - 1] = root.eval;
				for(int i = curDepth; i < 5; i++) {
					if(i % 2 == 1) alphaBetaSavedEvals[i] = Integer.MAX_VALUE;
					else alphaBetaSavedEvals[i] = Integer.MIN_VALUE;
				}
			} else {
				root.eval = max(root);
        
				// Alpha-beta pruning
				alphaBetaSavedEvals[curDepth - 1] = root.eval;
				for(int i = curDepth; i < 5; i++) {
					if(i % 2 == 1) alphaBetaSavedEvals[i] = Integer.MAX_VALUE;
					else alphaBetaSavedEvals[i] = Integer.MIN_VALUE;
				}
			}
		}
		playerToMoveKingPos = tempKingPos;
		otherKingPos = tempOtherKingPos;
		allPositionsW = new ArrayList<String[][]>(tempAllPositionsW);
		allPositionsB = new ArrayList<String[][]>(tempAllPositionsB);
		return root;
	}
	public static int min(TreeNode a) {
		int min = 9999999;
		for(int i = 0; i < a.nextReplies.size(); i++) {
			if(a.nextReplies.get(i).eval < min) {
				min = a.nextReplies.get(i).eval;
			}
		}
		return min;
	}
	public static int max(TreeNode a) {
		int max = -9999999;
		for(int i = 0; i < a.nextReplies.size(); i++) {
			if(a.nextReplies.get(i).eval > max) {
				max = a.nextReplies.get(i).eval;
			}
		}
		return max;
	}
	public static int evaluate(String[][] arr) {
		arr = rotateBoard(arr);
    /*
		if(transpositionMap.containsKey(arr)) {
			return transpositionMap.get(arr);
		}
    */
		int finalEvaluation = 0;
		int whitePawns = 0, whiteKnights = 0, whiteBishops = 0, whiteRooks = 0, whiteQueens = 0;
		int blackPawns = 0, blackKnights = 0, blackBishops = 0, blackRooks = 0, blackQueens = 0;
		int pieceOnBackRank = 0, yourRookOn7th = 0, oppRookOn7th = 0, yourPawnOn2nd = 0, oppPawnOn2nd = 0;
		int minMaxSeeScore = 0;
		int[] yourPawnsPerFile = new int[10];
		int[] oppPawnsPerFile = new int[10];
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				switch(arr[i][j]) {
				case "p":
					blackPawns++;
					if(i == 3 && (j == 3 || j == 4)) finalEvaluation += 10;
					yourPawnsPerFile[j+1]++;
					if(i == 1) yourPawnOn2nd++;
					if(isEndgame) {
						if(i == 5) finalEvaluation += 20;
						if(i == 6) finalEvaluation += 50;
					}
					break;
				case "n":
					blackKnights++;
					if(i == 0) pieceOnBackRank++;
					if(i == 2) {
						if(j == 3 && arr[1][3].equals("p")) finalEvaluation -= 45;
						if(j == 4 && arr[1][3].equals("p")) finalEvaluation -= 45;
					}
					break;
				case "b":
					blackBishops++;
					if(i == 0) pieceOnBackRank++;
					if(i == 2) {
						if(j == 3 && arr[1][3].equals("p")) finalEvaluation -= 45;
						if(j == 4 && arr[1][3].equals("p")) finalEvaluation -= 45;
					}
					break;
				case "r":
					blackRooks++;
					if(i == 6) yourRookOn7th++;
					break;
				case "q":
					blackQueens++;
					if(i != 0 && pieceOnBackRank >= 2) {
						finalEvaluation -= 50;
					}
					break;
				case "k":
					if(isEndgame) {
						if(i == 0 || i == 7 || j == 0 || j == 7) {
							finalEvaluation -= 30;
						}
						if((i == 3 || i == 4) && (j == 3 || j == 4)) {
							finalEvaluation += 10;
						}
					} else {
						if(color.equals("white")) {
							if((i == 0 && j == 1 && arr[1][1].equals("p")) || (i == 0 && j == 5 && arr[1][5].equals("p"))
								|| (i == 0 && j == 6 && arr[1][6].equals("p"))) {
							finalEvaluation += 30;
							} else {
								if(blackKingMoved) finalEvaluation -= 30;
							}
						} else {
						  	if((i == 0 && j == 6 && arr[1][6].equals("p")) || (i == 0 && j == 2 && arr[1][2].equals("p"))
								|| (i == 0 && j == 1 && arr[1][1].equals("p"))) {
							  	finalEvaluation += 30;
						  	} else {
							  	if(blackKingMoved) finalEvaluation -= 30;
						  	}
					  	}
					}
					break;
				case "P":
					whitePawns++;
					if(i == 4 && (j == 3 || j == 4)) finalEvaluation -= 10;
					oppPawnsPerFile[j+1]++;
					if(i == 6) oppPawnOn2nd++;
					if(isEndgame) {
						if(i == 2) finalEvaluation -= 20;
						if(i == 1) finalEvaluation -= 50;
					}
					break;
				case "N":
					whiteKnights++;
					if(i == 7) finalEvaluation += 7;
					if(i == 5) {
						if(j == 3 && arr[6][3].equals("P")) finalEvaluation += 45;
						if(j == 4 && arr[6][3].equals("P")) finalEvaluation += 45;
					}
					break;
				case "B":
					whiteBishops++;
					if(i == 7) finalEvaluation += 7;
					if(i == 5) {
						if(j == 3 && arr[6][3].equals("P")) finalEvaluation += 45;
						if(j == 4 && arr[6][3].equals("P")) finalEvaluation += 45;
					}
					break;
				case "R":
					whiteRooks++;
					if(i == 1) oppRookOn7th++;
					break;
				case "Q":
					whiteQueens++;
					break;
				case "K":
					if(isEndgame) {
						if(i == 0 || i == 7 || j == 0 || j == 7) {
							finalEvaluation += 30;
						}
						if((i == 3 || i == 4) && (j == 3 || j == 4)) {
							finalEvaluation -= 10;
						}
					} else {
						if(color.equals("black")) {
							if((i == 7 && j == 1 && arr[6][1].equals("P")) || (i == 7 && j == 5 && arr[6][5].equals("P"))
								|| (i == 7 && j == 6 && arr[6][6].equals("P"))) {
								finalEvaluation -= 30;
						 	} else {
						 		if(whiteKingMoved) finalEvaluation += 30;
						 	}
						} else {
							if((i == 7 && j == 6 && arr[6][6].equals("P")) || (i == 7 && j == 2 && arr[6][2].equals("P"))
								|| (i == 7 && j == 1 && arr[6][1].equals("P"))) {
								finalEvaluation -= 30;
							} else {
							  if(whiteKingMoved) finalEvaluation += 30;
							}
						} 
					}
					break;
				}
				ArrayList<Integer> oppPieces = new ArrayList<Integer>();
				ArrayList<Integer> yourPieces = new ArrayList<Integer>();
				if((i == 3 || i == 4) && (j == 3 || j == 4)) {
					finalEvaluation -= squareAttacked(arr, i, j) * 4;
					if(Character.isLowerCase(arr[i][j].charAt(0))) {
						oppPieces = staticExchange(arr, i, j);
					}
					arr = rotateBoard(arr);
					finalEvaluation += squareAttacked(arr, 7 - i, 7 - j) * 4;
					if(Character.isUpperCase(arr[7 - i][7 - j].charAt(0))) {
						yourPieces = staticExchange(arr, 7 - i, 7 - j);
					}
				} else {
					finalEvaluation -= squareAttacked(arr, i, j) * 2;
					if(Character.isLowerCase(arr[i][j].charAt(0))) {
						oppPieces = staticExchange(arr, i, j);
					}
					arr = rotateBoard(arr);
					finalEvaluation += squareAttacked(arr, 7 - i, 7 - j) * 2;
					if(Character.isUpperCase(arr[7 - i][7 - j].charAt(0))) {
						yourPieces = staticExchange(arr, 7 - i, 7 - j);
					}
				}
				arr = rotateBoard(arr);
				int curSeeScore = 0;
				int maxSeeScore = 0;
				int curPieceValue = 0;
				if(arr[i][j].equals("p")) curPieceValue = 1;
				else if(arr[i][j].equals("n") || arr[i][j].equals("b")) curPieceValue = 3;
				else if(arr[i][j].equals("r")) curPieceValue = 5;
				else if(arr[i][j].equals("q")) curPieceValue = 9;
				else curPieceValue = 0;
				Collections.sort(yourPieces);
				Collections.sort(oppPieces);
				while(true) {
					if(oppPieces.size() > 0) {
						curSeeScore -= curPieceValue;
						curPieceValue = oppPieces.get(0);
						oppPieces.remove(0);
					} else {
						maxSeeScore = curSeeScore;
						if(yourPieces.isEmpty() && Math.abs(otherKingPos.charAt(0)-'0' - i) <= 1 && 
								Math.abs(otherKingPos.charAt(1)-'0' - j) <= 1 && 
								!(Math.abs(playerToMoveKingPos.charAt(0)-'0' - i) <= 1 && 
										Math.abs(playerToMoveKingPos.charAt(1)-'0' - j) <= 1)) {
							maxSeeScore -= curPieceValue;
						}
						break;
					}
					if(curSeeScore > 0) {
						if(curSeeScore >= maxSeeScore) maxSeeScore = curSeeScore;
						else break;
					}
					
					if(yourPieces.size() > 0) {
						curSeeScore += curPieceValue;
						curPieceValue = yourPieces.get(0);
						yourPieces.remove(0);
					} else {
						maxSeeScore = curSeeScore;
						if(oppPieces.isEmpty() && Math.abs(playerToMoveKingPos.charAt(0)-'0' - i) <= 1 && 
								Math.abs(playerToMoveKingPos.charAt(1)-'0' - j) <= 1 && 
								!(Math.abs(otherKingPos.charAt(0)-'0' - i) <= 1 && 
										Math.abs(otherKingPos.charAt(1)-'0' - j) <= 1)) {
							maxSeeScore += curPieceValue;
						}
						break;
					}
					if(curSeeScore < 0) {
						if(curSeeScore <= maxSeeScore) maxSeeScore = curSeeScore;
						else break;
					}
				}
				if(maxSeeScore < minMaxSeeScore) {
					minMaxSeeScore = maxSeeScore;
				}
			}
		}
		finalEvaluation -= pawnStructure(yourPawnsPerFile, oppPawnsPerFile);
		finalEvaluation += pawnStructure(oppPawnsPerFile, yourPawnsPerFile);
		finalEvaluation += minMaxSeeScore * 100;
		finalEvaluation -= whitePawns * 100;
		finalEvaluation += blackPawns * 100;
		finalEvaluation -= whiteKnights * (320 + whitePawns + blackPawns);
		finalEvaluation += blackKnights * (320 + whitePawns + blackPawns);
		finalEvaluation -= whiteBishops * 320 + Math.pow(whiteBishops, 5);
		finalEvaluation += blackBishops * 320 + Math.pow(blackBishops, 5);
		finalEvaluation -= whiteRooks * (525 - 2 * whitePawns - blackPawns);
		finalEvaluation += blackRooks * (525 - whitePawns - 2 * blackPawns);
		finalEvaluation -= (whiteQueens * 970 - 3 * whiteRooks);
		finalEvaluation += (blackQueens * 970 - 3 * blackRooks);
		finalEvaluation -= (pieceOnBackRank) * 7;
		finalEvaluation += yourRookOn7th * oppPawnOn2nd * 25;
		finalEvaluation -= oppRookOn7th * yourPawnOn2nd * 25;
		//transpositionMap.put(arr, finalEvaluation);
		return finalEvaluation;	
	}
	//Remember! For this function lower is better
	public static int pawnStructure(int[] arr, int[] oppArr) {
		int pawnValue = 0;
		for(int i = 1; i <= 8; i++) {
			pawnValue += Math.pow(arr[i], 3);
			if(arr[i-1] == 0 && arr[i+1] == 0) {
				pawnValue += arr[i] * 20;
			}
			if(isEndgame) {
				if(arr[i] >= 1 && oppArr[i-1] == 0 && oppArr[i] == 0 && oppArr[i+1] == 0) {
					pawnValue -= 75;
				}
			}
		}
		return pawnValue;
	}
	public static boolean threefoldCheck() {
		if(allPositionsB.size() >= 50 || allPositionsW.size() >= 50) {
			return true;
		}
		for(int i = 0; i < allPositionsB.size(); i++) {
			for(int j = i + 1; j < allPositionsB.size(); j++) {
				if(Arrays.deepEquals(allPositionsB.get(j), allPositionsB.get(i))) {
					for(int k = j + 1; k < allPositionsB.size(); k++) {
						if(Arrays.deepEquals(allPositionsB.get(j), allPositionsB.get(k))) {
							return true;
						}
					}
				}
			}
		}
		for(int i = 0; i < allPositionsW.size(); i++) {
			for(int j = i + 1; j < allPositionsW.size(); j++) {
				if(Arrays.deepEquals(allPositionsW.get(j), allPositionsW.get(i))) {
					for(int k = j + 1; k < allPositionsW.size(); k++) {
						if(Arrays.deepEquals(allPositionsW.get(j), allPositionsW.get(k))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}