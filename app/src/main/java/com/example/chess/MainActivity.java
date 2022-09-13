package com.example.chess;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    private Board currentBoard;

    private boolean ai = true;
    private ArrayList<Long> keys;
    private ArrayList<Integer> moves;
    private OpeningBook book = new OpeningBook();
    private String moveList = "";

    public static final int[] TILE_IDS = fillTileIds();
    public static final Map<String, Integer> PIECE_IDS = fillPieceIds();

    private static int[] fillTileIds() {
        return new int[]{R.id.a8, R.id.b8, R.id.c8, R.id.d8, R.id.e8, R.id.f8, R.id.g8, R.id.h8,
                R.id.a7, R.id.b7, R.id.c7, R.id.d7, R.id.e7, R.id.f7, R.id.g7, R.id.h7,
                R.id.a6, R.id.b6, R.id.c6, R.id.d6, R.id.e6, R.id.f6, R.id.g6, R.id.h6,
                R.id.a5, R.id.b5, R.id.c5, R.id.d5, R.id.e5, R.id.f5, R.id.g5, R.id.h5,
                R.id.a4, R.id.b4, R.id.c4, R.id.d4, R.id.e4, R.id.f4, R.id.g4, R.id.h4,
                R.id.a3, R.id.b3, R.id.c3, R.id.d3, R.id.e3, R.id.f3, R.id.g3, R.id.h3,
                R.id.a2, R.id.b2, R.id.c2, R.id.d2, R.id.e2, R.id.f2, R.id.g2, R.id.h2,
                R.id.a1, R.id.b1, R.id.c1, R.id.d1, R.id.e1, R.id.f1, R.id.g1, R.id.h1};
    }

    private static Map<String, Integer> fillPieceIds() {
        final Map<String, Integer> ids = new HashMap<>();
        ids.put("empty", R.drawable.empty);
        ids.put("white_king", R.drawable.white_king);
        ids.put("white_queen", R.drawable.white_queen);
        ids.put("white_rook", R.drawable.white_rook);
        ids.put("white_knight", R.drawable.white_knight);
        ids.put("white_bishop", R.drawable.white_bishop);
        ids.put("white_pawn", R.drawable.white_pawn);
        ids.put("black_king", R.drawable.black_king);
        ids.put("black_queen", R.drawable.black_queen);
        ids.put("black_rook", R.drawable.black_rook);
        ids.put("black_knight", R.drawable.black_knight);
        ids.put("black_bishop", R.drawable.black_bishop);
        ids.put("black_pawn", R.drawable.black_pawn);
        return ids;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.currentBoard = GameLogic.createStartBoard();
//        this.currentBoard = GameLogic.createBoardFromFEN("1k1r3r/p1q4p/1p1bppp1/PBp1pb2/8/1PBP1Q1P/2P2PP1/R3R1K1 w - - 0 10");

        keys = new ArrayList<>();
        moves = new ArrayList<>();

        keys.add(currentBoard.zobristKey);

//        currentBoard.makeMove(currentBoard.createMove(57,42));
//        currentBoard.makeMove(currentBoard.createMove(6,21));

        updateDisplay();
    }

    public void onTileClick(View view) {

        unHighlightAllTiles();

        int tileNum = Integer.parseInt(view.getTag().toString());

        handleTilePress(tileNum);
//        testSuite(5);
//        perft(5);
//        test();

        if (currentBoard.getPromotionProgress() == 8) {
            updateDisplay();
        }
    }

    public void testSuite(int depth) {
        for (int i = 1; i <= depth; i++) {
            long startTime = System.currentTimeMillis();
            long count = countPos(i);
            long endTime = System.currentTimeMillis();
            int time = (int)(endTime - startTime);
            if (time < 1) {
                System.out.println("Depth: " + i + "\tPositions: " + count + "\tTime: " + time + " ms");
            } else {
                int speed = (int)((double)count / time);
                System.out.println("Depth: " + i + "\tPositions: " + count + "\tTime: " + time + " ms\tSpeed: " + speed + " pos/ms");
            }
        }
    }

    public void perft(int depth) {
        if (depth > 1) {
            long total = 0L;
            for (int move : currentBoard.generateAllLegalMoves(false)) {
                String out = "";
                currentBoard.makeMove(move);
                out += GameLogic.getSquares(move);
                out += ": ";
                long count = countPos(depth - 1);
                total += count;
                out += String.valueOf(count);
                currentBoard.unMakeMove(move);
                System.out.println(out);
            }
            System.out.println("Total: " + total);
        }
    }

    public long countPos(int depth) {
        if (depth == 1) {
            return currentBoard.generateAllLegalMoves(false).size();
        }
        long numPos = 0L;
        for (int move : currentBoard.generateAllLegalMoves(false)) {
            currentBoard.makeMove(move);
            numPos += countPos(depth - 1);
            currentBoard.unMakeMove(move);
        }
        return numPos;
    }

    public void handleTilePress(int tileNum) {

        if (currentBoard.getPromotionProgress() < 8) {
            if (tileNum % 8 == currentBoard.getPromotionProgress()) {
                int row = GameLogic.getRow(tileNum);
                if (currentBoard.whiteToMove()) {
                    if (row > 3) {
                        highlightTile(tileNum);
                    } else {
                        currentBoard.setPromotionProgress(8);
                        move(currentBoard.createPromotionMove(currentBoard.getCurrentStartSquare(), tileNum % 8, 3 - row));
                    }
                } else {
                    if (row < 4) {
                        highlightTile(tileNum);
                    } else {
                        currentBoard.setPromotionProgress(8);
                        move(currentBoard.createPromotionMove(currentBoard.getCurrentStartSquare(), 56 + tileNum % 8, row - 4));
                    }
                }
            } else {
                highlightTile(tileNum);
            }
            currentBoard.clearTargets();
        } else if (currentBoard.piecesTurn(tileNum)) {
            currentBoard.updateTargetSquares(tileNum);
            long targetSquares = currentBoard.getTargetSquares();
            for (int i = 0; i < 64; i++) {
                if ((targetSquares >> i & 1) == 1) {
                    highlightTile(i);
                }
            }
            if (targetSquares == 0) {
                highlightTileRed(tileNum);
            } else {
                highlightTileGreen(tileNum);
            }
        } else {
            if (currentBoard.containsTargetSquare(tileNum)) {
                if ((currentBoard.getPieceOnTile(currentBoard.getCurrentStartSquare()) & 7) == 1 && (tileNum < 8 || tileNum > 55)) {
                    clearDisplay();
                    currentBoard.setPromotionProgress(tileNum % 8);
                    if (currentBoard.whiteToMove()) {
                        ((ImageButton) findViewById(TILE_IDS[tileNum])).setImageResource(R.drawable.white_queen);
                        ((ImageButton) findViewById(TILE_IDS[tileNum + 8])).setImageResource(R.drawable.white_rook);
                        ((ImageButton) findViewById(TILE_IDS[tileNum + 16])).setImageResource(R.drawable.white_knight);
                        ((ImageButton) findViewById(TILE_IDS[tileNum + 24])).setImageResource(R.drawable.white_bishop);
                    } else {
                        ((ImageButton) findViewById(TILE_IDS[tileNum])).setImageResource(R.drawable.black_queen);
                        ((ImageButton) findViewById(TILE_IDS[tileNum - 8])).setImageResource(R.drawable.black_rook);
                        ((ImageButton) findViewById(TILE_IDS[tileNum - 16])).setImageResource(R.drawable.black_knight);
                        ((ImageButton) findViewById(TILE_IDS[tileNum - 24])).setImageResource(R.drawable.black_bishop);
                    }
                } else {
                    move(currentBoard.createMove(currentBoard.getCurrentStartSquare(), tileNum));
                }
            } else {
                highlightTile(tileNum);
                currentBoard.clearTargets();
            }
        }
    }

    public void undoMove(View view) {
//        if (!currentBoard.moveHistory.isEmpty()) {
//            int move = currentBoard.moveHistory.get(currentBoard.moveHistory.size() - 1);
//            currentBoard.unMakeMove(move);
//            currentBoard.moveHistory.remove(currentBoard.moveHistory.size() - 1);
//            currentBoard.removeHistory();
//        }
//        updateDisplay();


//        String out = "book.put(0x";
//        out += Long.toHexString(currentBoard.zobristKey);
//        out += "L, new int[]{";
//        out += moveList;
//        out += "}); // ";
//        System.out.println(out);


        if (book.containsPos(currentBoard.zobristKey)) {
            int aiMove = book.getMove(currentBoard.zobristKey);
            currentBoard.makeMove(aiMove);
            currentBoard.moveHistory.add(aiMove);
        } else {
            currentBoard.startSearch(3000);
            int aiMove = currentBoard.getBestMove();
            currentBoard.makeMove(aiMove);
            currentBoard.moveHistory.add(aiMove);
        }
        currentBoard.addHistory();
        updateDisplay();
    }

    public void move(int move) {
        currentBoard.makeMove(move);
        currentBoard.addHistory();
        currentBoard.moveHistory.add(move);

//        moveList += ("0x" + Integer.toHexString(move) + ",");

        if (currentBoard.generateAllLegalMoves(false).isEmpty()) {
            displayText("Checkmate - " + (currentBoard.whiteToMove() ? "black" : "white") + " wins");
        } else if (ai) {
            if (book.containsPos(currentBoard.zobristKey)) {
                int aiMove = book.getMove(currentBoard.zobristKey);
                currentBoard.makeMove(aiMove);
                currentBoard.moveHistory.add(aiMove);
            } else {
                currentBoard.startSearch(3000);
                int aiMove = currentBoard.getBestMove();
                currentBoard.makeMove(aiMove);
                currentBoard.moveHistory.add(aiMove);
            }
            currentBoard.addHistory();
            if (currentBoard.generateAllLegalMoves(false).isEmpty()) {
                displayText("Checkmate - " + (currentBoard.whiteToMove() ? "black" : "white") + " wins");
            }
        }
        currentBoard.clearTargets();
    }

    public void highlightTile(int tileNum) {
        findViewById(TILE_IDS[tileNum]).setBackgroundColor(getResources().getColor
                (tileNum % 2 != GameLogic.getRow(tileNum) % 2 ? R.color.dark_square_highlight : R.color.light_square_highlight));
    }

    public void highlightTileGreen(int tileNum){
        findViewById(TILE_IDS[tileNum]).setBackgroundColor(getResources().getColor(R.color.green_highlight));
    }

    public void highlightTileRed(int tileNum){
        findViewById(TILE_IDS[tileNum]).setBackgroundColor(getResources().getColor(R.color.red_highlight));
    }

    public void unHighlightTile(int tileNum) {
        (findViewById(TILE_IDS[tileNum])).setBackgroundColor(getResources().getColor
                (tileNum % 2 != GameLogic.getRow(tileNum) % 2 ? R.color.dark_square : R.color.light_square));
    }

    public void unHighlightAllTiles() {
        for(int i = 0; i < 64; i ++) {
            unHighlightTile(i);
        }
    }

    public void updateDisplay() {
        for (int i = 0; i < 64; i ++) {
            ((ImageButton)findViewById(TILE_IDS[i])).setImageResource(PIECE_IDS.get(GameLogic.getPieceName(currentBoard.getPieceOnTile(i))));
        }
    }

    public void clearDisplay() {
        for (int i = 0; i < 64; i ++) {
            ((ImageButton)findViewById(TILE_IDS[i])).setImageResource(R.drawable.empty);
        }
    }

    public void displayText(String text) {
        ((TextView)findViewById(R.id.textView)).setText(text);
    }
}