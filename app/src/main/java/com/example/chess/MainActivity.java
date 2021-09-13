package com.example.chess;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int[] TILE_IDS = fillTileIds();
    public static final Map<String, Integer> PIECE_IDS = fillPieceIds();
    public static final boolean[] IS_DARK_SQUARE = fillDarkSquare();
    public static final String[] TILE_NAMES = fillTileNames();

    private static boolean[] fillDarkSquare() {

        return new boolean[]{false, true, false, true, false, true, false, true,
                true, false, true, false, true, false, true, false,
                false, true, false, true, false, true, false, true,
                true, false, true, false, true, false, true, false,
                false, true, false, true, false, true, false, true,
                true, false, true, false, true, false, true, false,
                false, true, false, true, false, true, false, true,
                true, false, true, false, true, false, true, false,};
    }

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

    private static String[] fillTileNames() {

        return new String[]{"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",};
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

        updateDisplay();
    }

    public void onTileClick(View view) {

        unHighlightAllTiles();

        int tileNum = Integer.parseInt(view.getTag().toString());

        highlightTile(tileNum);
    }

    public void highlightTile(int tileNum){

        ImageButton tile = findViewById(TILE_IDS[tileNum]);
        if(IS_DARK_SQUARE[tileNum]){
            tile.setBackgroundColor(getResources().getColor(R.color.dark_square_highlight));
        }
        else {
            tile.setBackgroundColor(getResources().getColor(R.color.light_square_highlight));
        }
    }

    public void unHighlightTile(int tileNum){

        ImageButton tile = findViewById(TILE_IDS[tileNum]);
        if(IS_DARK_SQUARE[tileNum]){
            tile.setBackgroundColor(getResources().getColor(R.color.dark_square));
        }
        else {
            tile.setBackgroundColor(getResources().getColor(R.color.light_square));
        }
    }

    public void unHighlightAllTiles(){

        for(int i = 0; i < 64; i ++){
            unHighlightTile(i);
        }
    }

    public void updateDisplay(){

        unHighlightAllTiles();
    }
}