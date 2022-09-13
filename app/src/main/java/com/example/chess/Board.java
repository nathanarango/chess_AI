package com.example.chess;

import java.util.ArrayList;

public class Board {

    private long whitePieces;
    private long blackPieces;
    private long pieces;
    private long kings;
    private long queens;
    private long rooks;
    private long knights;
    private long bishops;
    private long pawns;

    private boolean whiteToMove;
    private int castleRights;
    private int enPassantColumn;
    private int halfMoveCounter;
    private int fullMoveCounter;

    private final ArrayList<Long> history;
    public ArrayList<Integer> moveHistory;
    private int whiteMaterial;
    private int blackMaterial;
    private int whitePawnsMaterial;
    private int blackPawnsMaterial;
    public long zobristKey;

    private boolean inCheck;
    private boolean inDoubleCheck;
    private long tilesToStopCheck;
    private long tilesAttacked;
    private boolean enPassantProtection;

    private long verticalPin;
    private long horizontalPin;
    private long diagonalRightPin;
    private long diagonalLeftPin;

    private long targetSquares;
    private int currentStartSquare;
    private int promotionInProgress;
    private long cutoffTime;

    private final ArrayList<Integer> bestMoves;

    public static final int PIECE_MASK = 7;
    public static final int KING_MASK = 6;
    public static final int QUEEN_MASK = 5;
    public static final int ROOK_MASK = 4;
    public static final int KNIGHT_MASK = 3;
    public static final int BISHOP_MASK = 2;
    public static final int PAWN_MASK = 1;
    public static final int EMPTY_MASK = 0;

    public final int[] KNIGHT_VECTORS = {-17, -15, -10, -6, 6, 10, 15, 17};
    public final int[] SLIDE_VECTORS = {-8, -7, 1, 9, 8, 7, -1, -9};
    public final int[][] DISTANCE_TO_EDGE = fillDistances();

    public Board(long whitePieces, long blackPieces, long kings, long queens, long rooks, long knights, long bishops, long pawns,
                 boolean whiteToMove, int castleRights, int enPassantColumn, int halfMoveCounter, int fullMoveCounter){

        this.whitePieces = whitePieces;
        this.blackPieces = blackPieces;
        this.pieces = whitePieces | blackPieces;
        this.kings = kings;
        this.queens = queens;
        this.rooks = rooks;
        this.knights = knights;
        this.bishops = bishops;
        this.pawns = pawns;

        this.whiteToMove = whiteToMove;
        this.castleRights = castleRights;
        this.enPassantColumn = enPassantColumn;
        this.halfMoveCounter = halfMoveCounter;
        this.fullMoveCounter = fullMoveCounter;
        this.promotionInProgress = 8;

        this.targetSquares = 0L;
        this.currentStartSquare = -1;

        updatePins();
        checkChecks();
        initData();

        this.bestMoves = new ArrayList<>();
        this.moveHistory = new ArrayList<>();
        this.history = new ArrayList<>();
        history.add(zobristKey);
    }

    private void initData() {
        long whitePawns = whitePieces & pawns;
        while (whitePawns != 0) {
            int pos = (whitePawns > 0 ? (int)(Math.log(whitePawns) / Math.log(2)) : 63);
            whitePawns ^= (1L << pos);
            whitePawnsMaterial += 100;
            zobristKey ^= GameLogic.WHITE_PAWN_HASH[pos];
        }
        long whiteBishops = whitePieces & bishops;
        while (whiteBishops != 0) {
            int pos = (whiteBishops > 0 ? (int)(Math.log(whiteBishops) / Math.log(2)) : 63);
            whiteBishops ^= (1L << pos);
            whiteMaterial += 300;
            zobristKey ^= GameLogic.WHITE_BISHOP_HASH[pos];
        }
        long whiteKnights = whitePieces & knights;
        while (whiteKnights != 0) {
            int pos = (whiteKnights > 0 ? (int)(Math.log(whiteKnights) / Math.log(2)) : 63);
            whiteKnights ^= (1L << pos);
            whiteMaterial += 300;
            zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[pos];
        }
        long whiteRooks = whitePieces & rooks;
        while (whiteRooks != 0) {
            int pos = (whiteRooks > 0 ? (int)(Math.log(whiteRooks) / Math.log(2)) : 63);
            whiteRooks ^= (1L << pos);
            whiteMaterial += 500;
            zobristKey ^= GameLogic.WHITE_ROOK_HASH[pos];
        }
        long whiteQueens = whitePieces & queens;
        while (whiteQueens != 0) {
            int pos = (whiteQueens > 0 ? (int)(Math.log(whiteQueens) / Math.log(2)) : 63);
            whiteQueens ^= (1L << pos);
            whiteMaterial += 900;
            zobristKey ^= GameLogic.WHITE_QUEEN_HASH[pos];
        }
        long whiteKing = whitePieces & kings;
        int whiteKingPos = (whiteKing > 0 ? (int)(Math.log(whiteKing) / Math.log(2)) : 63);
        zobristKey ^= GameLogic.WHITE_KING_HASH[whiteKingPos];

        long blackPawns = blackPieces & pawns;
        while (blackPawns != 0) {
            int pos = (blackPawns > 0 ? (int)(Math.log(blackPawns) / Math.log(2)) : 63);
            blackPawns ^= (1L << pos);
            blackPawnsMaterial += 100;
            zobristKey ^= GameLogic.BLACK_PAWN_HASH[pos];
        }
        long blackBishops = blackPieces & bishops;
        while (blackBishops != 0) {
            int pos = (blackBishops > 0 ? (int)(Math.log(blackBishops) / Math.log(2)) : 63);
            blackBishops ^= (1L << pos);
            blackMaterial += 300;
            zobristKey ^= GameLogic.BLACK_BISHOP_HASH[pos];
        }
        long blackKnights = blackPieces & knights;
        while (blackKnights != 0) {
            int pos = (blackKnights > 0 ? (int)(Math.log(blackKnights) / Math.log(2)) : 63);
            blackKnights ^= (1L << pos);
            blackMaterial += 300;
            zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[pos];
        }
        long blackRooks = blackPieces & rooks;
        while (blackRooks != 0) {
            int pos = (blackRooks > 0 ? (int)(Math.log(blackRooks) / Math.log(2)) : 63);
            blackRooks ^= (1L << pos);
            blackMaterial += 500;
            zobristKey ^= GameLogic.BLACK_ROOK_HASH[pos];
        }
        long blackQueens = blackPieces & queens;
        while (blackQueens != 0) {
            int pos = (blackQueens > 0 ? (int)(Math.log(blackQueens) / Math.log(2)) : 63);
            blackQueens ^= (1L << pos);
            blackMaterial += 900;
            zobristKey ^= GameLogic.BLACK_QUEEN_HASH[pos];
        }
        long blackKing = blackPieces & kings;
        int pos = (blackKing > 0 ? (int)(Math.log(blackKing) / Math.log(2)) : 63);
        zobristKey ^= GameLogic.BLACK_KING_HASH[pos];

        if (whiteToMove) {
            zobristKey ^= GameLogic.WHITE_TO_MOVE_HASH;
        }
        if (enPassantColumn != 8) {
            zobristKey ^= GameLogic.EN_PASSANT_COLUMN_HASH[enPassantColumn];
        }
        for (int i = 0; i < 4; i++) {
            if (((castleRights >> i) & 1) == 1) {
                zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[3 - i];
            }
        }

    }

    public void addHistory() {
        history.add(zobristKey);
    }

    public void removeHistory() {
        history.remove(history.size() - 1);
    }

    public final int[][] fillDistances() {
        int[][] distances = new int[64][8];
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                distances[8 * row + column] = new int[]{row, Math.min(row, 7 - column), 7 - column,
                        Math.min(7 - row, 7 - column), 7 - row, Math.min(7 - row, column), column, Math.min(row, column)};
            }
        }
        return distances;
    }

    public int getFullMoveCounter() {
        return  fullMoveCounter;
    }

    public int getBestMove() {
        return bestMoves.get(0);
    }

    public boolean whiteToMove() {
        return whiteToMove;
    }

    public int getPromotionProgress() {
        return promotionInProgress;
    }

    public void setPromotionProgress(int num) {
        promotionInProgress = num;
    }

    public int getPieceOnTile(int tile) {
        int piece = 0;

        if (((blackPieces >> tile) & 1) == 1) {
            piece |= 0b1000;
        } else if (((whitePieces >> tile) & 1) != 1) {
            return piece;
        }

        if (((pawns >> tile) & 1) == 1) {
            piece |= PAWN_MASK;
        } else if (((bishops >> tile) & 1) == 1) {
            piece |= BISHOP_MASK;
        } else if (((knights >> tile) & 1) == 1) {
            piece |= KNIGHT_MASK;
        } else if (((rooks >> tile) & 1) == 1) {
            piece |= ROOK_MASK;
        } else if (((queens >> tile) & 1) == 1) {
            piece |= QUEEN_MASK;
        } else {
            piece |= KING_MASK;
        }

        return piece;
    }

    public boolean piecesTurn(int pos) {
        if (whiteToMove) {
            return ((whitePieces >> pos) & 1) == 1;
        }
        return ((blackPieces >> pos) & 1) == 1;
    }

    public boolean containsTargetSquare(int pos) {
        return (targetSquares >> pos & 1) == 1;
    }

    public long getTargetSquares() {
        return targetSquares;
    }

    public int getCurrentStartSquare() {
        return currentStartSquare;
    }

    public void clearTargets() {
        currentStartSquare = -1;
        targetSquares = 0L;
    }

    public void updateTargetSquares(int startPos) {
        targetSquares = 0L;
        targetSquares = generateEndPos(startPos, false);
        currentStartSquare = startPos;
    }

    public int createMove(int startPos, int endPos) {

        // bits 18-23: start pos
        int move = (startPos << 9);

        // bits 16-17: 16 = check, 17 = double check
        if (inCheck) {
            move |= (1 << 16);
            if (inDoubleCheck) {
                move |= (1 << 15);
            }
        }

        // previous info bits 1-15; 1-4: castling rights, 5-8: en passant column, 9-15: half moves
        int prevInfo = (castleRights << 28) | (enPassantColumn << 24) | (halfMoveCounter << 17);
        move |= prevInfo;

        // bits 24-26: piece on end pos
        if (((pawns >> endPos) & 1) == 1) {
            move |= (1 << 6);
        } else if (((bishops >> endPos) & 1) == 1) {
            move |= (2 << 6);
        } else if (((knights >> endPos) & 1) == 1) {
            move |= (3 << 6);
        } else if (((rooks >> endPos) & 1) == 1) {
            move |= (4 << 6);
        } else if (((queens >> endPos) & 1) == 1) {
            move |= (5 << 6);
        }

        // bits 27-32: end pos
        move |= endPos;

        return move;
    }

    public int createPromotionMove(int startPos, int endPos, int promotion) {

        // bits 18-23: start pos
        int move = (startPos << 9);

        // previous info bits 1-15; 1-4: castling rights, 5-8: en passant column, 9-15: half moves
        int prevInfo = (castleRights << 28) | (enPassantColumn << 24) | (halfMoveCounter << 17);
        move |= prevInfo;

        // bits 24-26: piece on end pos
        if (((pawns >> endPos) & 1) == 1) {
            move |= (1 << 6);
        } else if (((bishops >> endPos) & 1) == 1) {
            move |= (2 << 6);
        } else if (((knights >> endPos) & 1) == 1) {
            move |= (3 << 6);
        } else if (((rooks >> endPos) & 1) == 1) {
            move |= (4 << 6);
        } else if (((queens >> endPos) & 1) == 1) {
            move |= (5 << 6);
        }

        // promotion identifier
        move |= (1L << 15);

        // bits 31-32: 31 = check, 32 = double check
        if (inCheck) {
            move |= 2;
            if (inDoubleCheck) {
                move |= 1;
            }
        }

        // bits 27-28: L=00 M=01 R=10
        if (startPos % 8 == endPos % 8) {
            move |= (1 << 4);
        } else if (startPos % 8 < endPos % 8) {
            move |= (1 << 5);
        }

        // bits 29-30: 0=bishop 1=knight 2=rook 3=queen
        move |= (promotion << 2);

        return move;
    }

    public ArrayList<Integer> generateAllLegalMoves(boolean attackOnly) {
        ArrayList<Integer> allMoves = new ArrayList<>();

        for (int i = 0; i < 64; i ++) {
            if ((((whiteToMove ? whitePieces : blackPieces) >> i) & 1) == 1) {

                long endPositions = generateEndPos(i, attackOnly);
                while (endPositions != 0) {
                    int endPos = (endPositions > 0 ? (int)(Math.log(endPositions) / Math.log(2)) : 63);
                    endPositions ^= (1L << endPos);

                    if (((pawns >> i) & 1) == 1 && (endPos < 8 || endPos > 55)) {
                        for (int j = 0; j < 4; j++) {
                            allMoves.add(createPromotionMove(i, endPos, j));
                        }
                    } else {
                        allMoves.add(createMove(i, endPos));
                    }
                }
            }
        }
        return allMoves;
    }

    private boolean notEnPassantPinned(int position) {

        long myKing = kings & (whiteToMove ? whitePieces : blackPieces);
        int kingPos = (myKing > 0 ? (int)(Math.log(myKing) / Math.log(2)) : 63);

        if (GameLogic.getRow(kingPos) == GameLogic.getRow(position)) {
            int pawnPos = position - (position % 8) + enPassantColumn;
            int vector = (position - kingPos) / Math.abs(kingPos - position);
            boolean pastPawns = false;
            int pos = kingPos + vector;
            for (int i = 0; i < DISTANCE_TO_EDGE[kingPos][(vector > 0 ? 2 : 6)]; i++) {
                if (pos == pawnPos || pos == position) {
                    pastPawns = true;
                } else if (((pieces >> pos) & 1) == 1) {
                    if (!pastPawns) {
                        return true;
                    } else {
                        return ((((rooks | queens) & (whiteToMove ? blackPieces : whitePieces)) >> pos) & 1) != 1;
                    }
                }
                pos += vector;
            }
        }
        return true;
    }

    private long generateEndPos(final int position, boolean attackOnly) {

        if (inDoubleCheck) {
            if (((kings >> position) & 1) == 1) {
                return tilesAttackedKing(position) & (whiteToMove ? ~whitePieces : ~blackPieces) & ~tilesAttacked;
            } else {
                return 0;
            }
        }

        if (((pawns >> position) & 1) == 1) {
            long out = tilesToMovePawn(position) | (tilesAttackedPawn(position, true, false) & (whiteToMove ? blackPieces : whitePieces));
            if (position % 8 - 1 == enPassantColumn && (position > (whiteToMove ? 23 : 31) && position < (whiteToMove ? 32 : 40))) {
                if (notEnPassantPinned(position) && ((((verticalPin | diagonalRightPin | diagonalLeftPin) >> position) & 1) == 0)) {
                    out |= (1L << (position + (whiteToMove ? -9 : 7)));
                }
            } else if (position % 8 < 7 && position % 8 + 1 == enPassantColumn &&
                    (position > (whiteToMove ? 23 : 31) && position < (whiteToMove ? 32 : 40))) {
                if (notEnPassantPinned(position) && ((((verticalPin | diagonalRightPin | diagonalLeftPin) >> position) & 1) == 0)) {
                    out |= (1L << (position + (whiteToMove ? -7 : 9)));
                }
            }
            if (inCheck) {
                if (enPassantProtection && GameLogic.getRow(position) == (whiteToMove ? 3 : 4)) {
                    out &= (tilesToStopCheck | (1L << (position - (position % 8) + enPassantColumn + (whiteToMove ? -8 : 8))));
                } else {
                    out &= tilesToStopCheck;
                }
                return out;
            }
            if (attackOnly) {
                out ^= tilesToMovePawn(position);
            }
            return out;
        } else if (((queens >> position) & 1) == 1) {
            long out = tilesAttackedQueen(position, true) & (whiteToMove ? ~whitePieces : ~blackPieces);
            if (inCheck) {
                return out & tilesToStopCheck;
            }
            if (attackOnly) {
                return out & (whiteToMove ? blackPieces : whitePieces);
            }
            return out;
        } else if (((rooks >> position) & 1) == 1) {
            long out = tilesAttackedRook(position, true) & (whiteToMove ? ~whitePieces : ~blackPieces);
            if (inCheck) {
                return out & tilesToStopCheck;
            }
            if (attackOnly) {
                return out & (whiteToMove ? blackPieces : whitePieces);
            }
            return out;
        } else if (((knights >> position) & 1) == 1) {
            long out = tilesAttackedKnight(position, true) & (whiteToMove ? ~whitePieces : ~blackPieces);
            if (inCheck) {
                return out & tilesToStopCheck;
            }
            if (attackOnly) {
                return out & (whiteToMove ? blackPieces : whitePieces);
            }
            return out;
        } else if (((bishops >> position) & 1) == 1) {
            long out = tilesAttackedBishop(position, true) & (whiteToMove ? ~whitePieces : ~blackPieces);
            if (inCheck) {
                return out & tilesToStopCheck;
            }
            if (attackOnly) {
                return out & (whiteToMove ? blackPieces : whitePieces);
            }
            return out;
        } else if (((kings >> position) & 1) == 1) {
            long out = tilesAttackedKing(position) & ~tilesAttacked & (whiteToMove ? ~whitePieces : ~blackPieces);
            if (inCheck) {
                return out & ~tilesAttacked;
            }
            if (attackOnly) {
                return out & (whiteToMove ? blackPieces : whitePieces);
            }
            if (whiteToMove) {
                if ((castleRights >> 3 & 1) == 1 && ((pieces >> 61) & 0b11) == 0 && ((tilesAttacked >> 61) & 0b11) == 0) {
                    out |= (1L << 62);
                }
                if ((castleRights >> 2 & 1) == 1 && ((pieces >> 57) & 0b111) == 0 && ((tilesAttacked >> 58) & 0b11) == 0) {
                    out |= (1L << 58);
                }
            } else {
                if ((castleRights >> 1 & 1) == 1 && ((pieces >> 5) & 0b11) == 0 && ((tilesAttacked >> 5) & 0b11) == 0) {
                    out |= (1L << 6);
                }
                if ((castleRights & 1) == 1 && (pieces & 0b1110) == 0 && (tilesAttacked & 0b1100) == 0) {
                    out |= 4L;
                }
            }
            return out;
        }
        return 0;
    }

    private long tilesAttackedKing(final int position) {
        long tiles = 0L;

        for (int vector : SLIDE_VECTORS) {
            if (position + vector >= 0 && position + vector < 64 &&
                    (!(((vector == -1 || vector == -9 || vector == 7) && position % 8 == 0) ||
                            ((vector == 1 || vector == 9 || vector == -7) && position % 8 == 7)))) {

                tiles |= (1L << (position + vector));
            }
        }
        return tiles;
    }

    private long tilesAttackedQueen(final int position, boolean checkPins) {
        long tiles = 0L;

        for (int i = 0; i < 8; i ++) {
            if (!checkPins || (i % 4 == 0 && (((diagonalLeftPin | diagonalRightPin | horizontalPin) >> position) & 1) == 0) ||
                    (i % 4 == 1 && (((diagonalLeftPin | verticalPin | horizontalPin) >> position) & 1) == 0) ||
                    (i % 4 == 2 && (((diagonalLeftPin | diagonalRightPin | verticalPin) >> position) & 1) == 0) ||
                    (i % 4 == 3 && (((verticalPin | diagonalRightPin | horizontalPin) >> position) & 1) == 0)) {
                int vector = SLIDE_VECTORS[i];
                int pos = position + vector;
                for (int j = 0; j < DISTANCE_TO_EDGE[position][i]; j++) {
                    tiles |= (1L << pos);
                    if (((pieces >> pos) & 1) == 1) {
                        if (!checkPins && (((kings & (whiteToMove ? whitePieces : blackPieces)) >> pos) & 1) == 1) {
                            tiles |= (1L << (pos + vector));
                        }
                        break;
                    }
                    pos += vector;
                }
            }
        }
        return tiles;
    }

    private long tilesAttackedRook(final int position, boolean checkPins) {
        long tiles = 0L;

        for (int i = 0; i < 8; i += 2) {
            if (!checkPins || (i % 4 == 0 && (((diagonalLeftPin | diagonalRightPin | horizontalPin) >> position) & 1) == 0) ||
                    (i % 4 == 2 && (((diagonalLeftPin | diagonalRightPin | verticalPin) >> position) & 1) == 0)) {
                int vector = SLIDE_VECTORS[i];
                int pos = position + vector;
                for (int j = 0; j < DISTANCE_TO_EDGE[position][i]; j++) {
                    tiles |= (1L << pos);
                    if (((pieces >> pos) & 1) == 1) {
                        if (!checkPins && (((kings & (whiteToMove ? whitePieces : blackPieces)) >> pos) & 1) == 1) {
                            tiles |= (1L << (pos + vector));
                        }
                        break;
                    }
                    pos += vector;
                }
            }
        }
        return tiles;
    }

    private long tilesAttackedKnight(final int position, boolean checkPins) {
        long tiles = 0L;

        for (int vector : KNIGHT_VECTORS) {
            if (!(((vector == -6 || vector == 10) && position % 8 == 6) ||
                    ((vector == -6 || vector == 10 || vector == 17 || vector == -15) && position % 8 == 7) ||
                    ((vector == 6 || vector == -10 || vector == -17 || vector == 15) && position % 8 == 0) ||
                    ((vector == 6 || vector == -10) && position % 8 == 1)) && (position + vector >= 0 && position + vector < 64) &&
                    (!checkPins || (((diagonalLeftPin | diagonalRightPin | verticalPin | horizontalPin) >> position) & 1) == 0)) {

                tiles |= (1L << (position + vector));
            }
        }
        return tiles;
    }

    private long tilesAttackedBishop(final int position, boolean checkPins) {
        long tiles = 0L;

        for (int i = 1; i < 8; i += 2) {
            if (!checkPins || (i % 4 == 1 && (((diagonalLeftPin | verticalPin | horizontalPin) >> position) & 1) == 0) ||
                    (i % 4 == 3 && (((verticalPin | diagonalRightPin | horizontalPin) >> position) & 1) == 0)) {
                int vector = SLIDE_VECTORS[i];
                int pos = position + vector;
                for (int j = 0; j < DISTANCE_TO_EDGE[position][i]; j++) {
                    tiles |= (1L << pos);
                    if (((pieces >> pos) & 1) == 1) {
                        if (!checkPins && (((kings & (whiteToMove ? whitePieces : blackPieces)) >> pos) & 1) == 1) {
                            tiles |= (1L << (pos + vector));
                        }
                        break;
                    }
                    pos += vector;
                }
            }
        }
        return tiles;
    }

    private long tilesAttackedPawn(final int position, boolean checkPins, boolean opposite) {
        long tiles = 0L;

        if (position % 8 != 0 && (!checkPins || (((verticalPin |
                (whiteToMove ? diagonalRightPin : diagonalLeftPin) | horizontalPin) >> position) & 1) == 0)) {
            tiles |= (1L << ((whiteToMove ^ opposite) ? (position - 9) : (position + 7)));
        }
        if (position % 8 != 7 && (!checkPins || ((((whiteToMove ? diagonalLeftPin : diagonalRightPin)
                | verticalPin | horizontalPin) >> position) & 1) == 0)) {
            tiles |= (1L << ((whiteToMove ^ opposite) ? (position - 7) : (position + 9)));
        }

        return tiles;
    }

    private long tilesToMovePawn(final int position) {
        long tiles = 0L;

        if (whiteToMove) {
            if (((pieces >> (position - 8)) & 1) == 0 && (((diagonalLeftPin | diagonalRightPin | horizontalPin) >> position) & 1) == 0) {
                tiles |= (1L << (position - 8));
                if (position < 56 && position > 47 && (((pieces >> (position - 16)) & 1) == 0)) {
                    tiles |= (1L << (position - 16));
                }
            }
        } else {
            if (((pieces >> (position + 8)) & 1) == 0 && (((diagonalLeftPin | diagonalRightPin | horizontalPin) >> position) & 1) == 0) {
                tiles |= (1L << (position + 8));
                if (position < 16 && position > 7 && (((pieces >> (position + 16)) & 1) == 0)) {
                    tiles |= (1L << (position + 16));
                }
            }
        }

        return tiles;
    }

    public void makeMove(int move) {
        if (whiteToMove) {
            makeWhiteMove(move);
        } else {
            makeBlackMove(move);
            fullMoveCounter ++;
        }

        pieces = whitePieces | blackPieces;
        whiteToMove = !whiteToMove;
        zobristKey ^= GameLogic.WHITE_TO_MOVE_HASH;
        updatePins();
        checkChecks();
    }

    private void makeWhiteMove(int move) {
        int startPos = (move >> 9) & 0b111111;
        int endPos = move & 0b111111;
        int takenPiece = (move >> 6) & PIECE_MASK;
        int movedPiece = PAWN_MASK;
        long endMask = 1L << endPos;
        long startMask = 1L << startPos;

        if (((bishops >> startPos) & 1) == 1) {
            movedPiece = BISHOP_MASK;
        } else if (((knights >> startPos) & 1) == 1) {
            movedPiece = KNIGHT_MASK;
        } else if (((rooks >> startPos) & 1) == 1) {
            movedPiece = ROOK_MASK;
        } else if (((kings >> startPos) & 1) == 1) {
            movedPiece = KING_MASK;
        } else if (((queens >> startPos) & 1) == 1) {
            movedPiece = QUEEN_MASK;
        }

        if (enPassantColumn != 8) {
            zobristKey ^= GameLogic.EN_PASSANT_COLUMN_HASH[enPassantColumn];
        }

        if (movedPiece == PAWN_MASK) {

            if ((move >> 15 & 0b11) == 1) {
                endPos = ((move >> 4) & 0b11) + startPos - 9;
                endMask = 1L << endPos;

                pawns ^= endMask;
                zobristKey ^= GameLogic.WHITE_PAWN_HASH[endPos];
                switch ((move >> 2) & 0b11) {
                    case 3:
                        queens ^= endMask;
                        whitePawnsMaterial -= 100;
                        whiteMaterial += 900;
                        zobristKey ^= GameLogic.WHITE_QUEEN_HASH[endPos];
                        break;
                    case 2:
                        rooks ^= endMask;
                        whitePawnsMaterial -= 100;
                        whiteMaterial += 500;
                        zobristKey ^= GameLogic.WHITE_ROOK_HASH[endPos];
                        break;
                    case 1:
                        knights ^= endMask;
                        whitePawnsMaterial -= 100;
                        whiteMaterial += 300;
                        zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[endPos];
                        break;
                    default:
                        bishops ^= endMask;
                        whitePawnsMaterial -= 100;
                        whiteMaterial += 300;
                        zobristKey ^= GameLogic.WHITE_BISHOP_HASH[endPos];
                        break;
                }
            } else if (takenPiece == 0 && startPos % 8 != endPos % 8) {
                int takenPos = endPos + 8;
                long takenMask = 1L << takenPos;
                pawns ^= takenMask;
                blackPieces ^= takenMask;
                blackPawnsMaterial -= 100;
                zobristKey ^= GameLogic.BLACK_PAWN_HASH[takenPos];
            }
            pawns ^= (endMask | startMask);
            whitePieces ^= (endMask | startMask);
            zobristKey ^= GameLogic.WHITE_PAWN_HASH[startPos];
            zobristKey ^= GameLogic.WHITE_PAWN_HASH[endPos];

            if (Math.abs(startPos - endPos) == 16) {
                enPassantColumn = startPos % 8;
                zobristKey ^= GameLogic.EN_PASSANT_COLUMN_HASH[enPassantColumn];
            } else {
                enPassantColumn = 8;
            }
        } else {
            enPassantColumn = 8;
            whitePieces ^= (endMask | startMask);

            switch (movedPiece) {
                case BISHOP_MASK:
                    bishops ^= (endMask | startMask);
                    zobristKey ^= GameLogic.WHITE_BISHOP_HASH[startPos];
                    zobristKey ^= GameLogic.WHITE_BISHOP_HASH[endPos];
                    break;
                case KNIGHT_MASK:
                    knights ^= (endMask | startMask);
                    zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[startPos];
                    zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[endPos];
                    break;
                case ROOK_MASK:
                    rooks ^= (endMask | startMask);
                    zobristKey ^= GameLogic.WHITE_ROOK_HASH[startPos];
                    zobristKey ^= GameLogic.WHITE_ROOK_HASH[endPos];
                    break;
                case QUEEN_MASK:
                    queens ^= (endMask | startMask);
                    zobristKey ^= GameLogic.WHITE_QUEEN_HASH[startPos];
                    zobristKey ^= GameLogic.WHITE_QUEEN_HASH[endPos];
                    break;
                default:
                    kings ^= (endMask | startMask);
                    zobristKey ^= GameLogic.WHITE_KING_HASH[startPos];
                    zobristKey ^= GameLogic.WHITE_KING_HASH[endPos];

                    if ((castleRights & 8) == 8) {
                        zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[0];
                    }
                    if ((castleRights & 4) == 4) {
                        zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[1];
                    }
                    castleRights &= 0b0011;

                    if (Math.abs(startPos - endPos) == 2) {
                        if (endPos == 58) {
                            rooks ^= (9L << 56);
                            whitePieces ^= (9L << 56);
                            zobristKey ^= GameLogic.WHITE_ROOK_HASH[56];
                            zobristKey ^= GameLogic.WHITE_ROOK_HASH[59];
                        } else {
                            rooks ^= (5L << 61);
                            whitePieces ^= (5L << 61);
                            zobristKey ^= GameLogic.WHITE_ROOK_HASH[61];
                            zobristKey ^= GameLogic.WHITE_ROOK_HASH[63];
                        }
                    }
                    break;
            }
        }

        if ((startPos == 0 || endPos == 0) && (castleRights & 1) == 1) {
            zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[3];
            castleRights &= 0b1110;
        }
        if ((startPos == 7 || endPos == 7) && (castleRights & 2) == 2) {
            zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[2];
            castleRights &= 0b1101;
        }
        if ((startPos == 56 || endPos == 56) && (castleRights & 4) == 4) {
            zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[1];
            castleRights &= 0b1011;
        }
        if ((startPos == 63 || endPos == 63) && (castleRights & 8) == 8) {
            zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[0];
            castleRights &= 0b0111;
        }

        switch (takenPiece) {
            case EMPTY_MASK:
                halfMoveCounter = ((movedPiece == PAWN_MASK) ? 0 : halfMoveCounter + 1);
                break;
            case PAWN_MASK:
                pawns ^= endMask;
                halfMoveCounter = 0;
                blackPawnsMaterial -= 100;
                blackPieces ^= endMask;
                zobristKey ^= GameLogic.BLACK_PAWN_HASH[endPos];
                break;
            case BISHOP_MASK:
                bishops ^= endMask;
                halfMoveCounter = 0;
                blackMaterial -= 300;
                blackPieces ^= endMask;
                zobristKey ^= GameLogic.BLACK_BISHOP_HASH[endPos];
                break;
            case KNIGHT_MASK:
                knights ^= endMask;
                halfMoveCounter = 0;
                blackMaterial -= 300;
                blackPieces ^= endMask;
                zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[endPos];
                break;
            case ROOK_MASK:
                rooks ^= endMask;
                halfMoveCounter = 0;
                blackMaterial -= 500;
                blackPieces ^= endMask;
                zobristKey ^= GameLogic.BLACK_ROOK_HASH[endPos];
                break;
            default:
                queens ^= endMask;
                halfMoveCounter = 0;
                blackMaterial -= 900;
                blackPieces ^= endMask;
                zobristKey ^= GameLogic.BLACK_QUEEN_HASH[endPos];
                break;
        }
    }

    private void makeBlackMove(int move) {
        int startPos = (move >> 9) & 0b111111;
        int endPos = move & 0b111111;
        int takenPiece = (move >> 6) & PIECE_MASK;
        int movedPiece = PAWN_MASK;
        long endMask = 1L << endPos;
        long startMask = 1L << startPos;

        if (((bishops >> startPos) & 1) == 1) {
            movedPiece = BISHOP_MASK;
        } else if (((knights >> startPos) & 1) == 1) {
            movedPiece = KNIGHT_MASK;
        } else if (((rooks >> startPos) & 1) == 1) {
            movedPiece = ROOK_MASK;
        } else if (((kings >> startPos) & 1) == 1) {
            movedPiece = KING_MASK;
        } else if (((queens >> startPos) & 1) == 1) {
            movedPiece = QUEEN_MASK;
        }

        if (enPassantColumn != 8) {
            zobristKey ^= GameLogic.EN_PASSANT_COLUMN_HASH[enPassantColumn];
        }

        if (movedPiece == PAWN_MASK) {

            if ((move >> 15 & 0b11) == 1) {
                endPos = ((move >> 4) & 0b11) + startPos + 7;
                endMask = 1L << endPos;

                pawns ^= endMask;
                zobristKey ^= GameLogic.BLACK_PAWN_HASH[endPos];
                switch ((move >> 2) & 0b11) {
                    case 3:
                        queens ^= endMask;
                        blackPawnsMaterial -= 100;
                        blackMaterial += 900;
                        zobristKey ^= GameLogic.BLACK_QUEEN_HASH[endPos];
                        break;
                    case 2:
                        rooks ^= endMask;
                        blackPawnsMaterial -= 100;
                        blackMaterial += 500;
                        zobristKey ^= GameLogic.BLACK_ROOK_HASH[endPos];
                        break;
                    case 1:
                        knights ^= endMask;
                        blackPawnsMaterial -= 100;
                        blackMaterial += 300;
                        zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[endPos];
                        break;
                    default:
                        bishops ^= endMask;
                        blackPawnsMaterial -= 100;
                        blackMaterial += 300;
                        zobristKey ^= GameLogic.BLACK_BISHOP_HASH[endPos];
                        break;
                }
            } else if (takenPiece == 0 && startPos % 8 != endPos % 8) {
                int takenPos = endPos - 8;
                long takenMask = 1L << takenPos;
                pawns ^= takenMask;
                whitePieces ^= takenMask;
                whitePawnsMaterial -= 100;
                zobristKey ^= GameLogic.WHITE_PAWN_HASH[takenPos];
            }
            pawns ^= (endMask | startMask);
            blackPieces ^= (endMask | startMask);
            zobristKey ^= GameLogic.BLACK_PAWN_HASH[startPos];
            zobristKey ^= GameLogic.BLACK_PAWN_HASH[endPos];

            if (Math.abs(startPos - endPos) == 16) {
                enPassantColumn = startPos % 8;
                zobristKey ^= GameLogic.EN_PASSANT_COLUMN_HASH[enPassantColumn];
            } else {
                enPassantColumn = 8;
            }
        } else {
            enPassantColumn = 8;
            blackPieces ^= (endMask | startMask);

            switch (movedPiece) {
                case BISHOP_MASK:
                    bishops ^= (endMask | startMask);
                    zobristKey ^= GameLogic.BLACK_BISHOP_HASH[startPos];
                    zobristKey ^= GameLogic.BLACK_BISHOP_HASH[endPos];
                    break;
                case KNIGHT_MASK:
                    knights ^= (endMask | startMask);
                    zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[startPos];
                    zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[endPos];
                    break;
                case ROOK_MASK:
                    rooks ^= (endMask | startMask);
                    zobristKey ^= GameLogic.BLACK_ROOK_HASH[startPos];
                    zobristKey ^= GameLogic.BLACK_ROOK_HASH[endPos];
                    break;
                case QUEEN_MASK:
                    queens ^= (endMask | startMask);
                    zobristKey ^= GameLogic.BLACK_QUEEN_HASH[startPos];
                    zobristKey ^= GameLogic.BLACK_QUEEN_HASH[endPos];
                    break;
                default:
                    kings ^= (endMask | startMask);
                    zobristKey ^= GameLogic.BLACK_KING_HASH[startPos];
                    zobristKey ^= GameLogic.BLACK_KING_HASH[endPos];

                    if ((castleRights & 2) == 2) {
                        zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[2];
                    }
                    if ((castleRights & 1) == 1) {
                        zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[3];
                    }
                    castleRights &= 0b1100;

                    if (Math.abs(startPos - endPos) == 2) {
                        if (endPos == 2) {
                            rooks ^= 0b1001;
                            blackPieces ^= 0b1001;
                            zobristKey ^= GameLogic.BLACK_ROOK_HASH[0];
                            zobristKey ^= GameLogic.BLACK_ROOK_HASH[3];
                        } else  {
                            rooks ^= 0b10100000;
                            blackPieces ^= 0b10100000;
                            zobristKey ^= GameLogic.BLACK_ROOK_HASH[5];
                            zobristKey ^= GameLogic.BLACK_ROOK_HASH[7];
                        }
                    }
                    break;
            }
        }

        if ((startPos == 0 || endPos == 0) && (castleRights & 1) == 1) {
            zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[3];
            castleRights &= 0b1110;
        }
        if ((startPos == 7 || endPos == 7) && (castleRights & 2) == 2) {
            zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[2];
            castleRights &= 0b1101;
        }
        if ((startPos == 56 || endPos == 56) && (castleRights & 4) == 4) {
            zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[1];
            castleRights &= 0b1011;
        }
        if ((startPos == 63 || endPos == 63) && (castleRights & 8) == 8) {
            zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[0];
            castleRights &= 0b0111;
        }

        switch (takenPiece) {
            case EMPTY_MASK:
                halfMoveCounter = ((movedPiece == PAWN_MASK) ? 0 : halfMoveCounter + 1);
                break;
            case PAWN_MASK:
                pawns ^= endMask;
                halfMoveCounter = 0;
                whitePawnsMaterial -= 100;
                whitePieces ^= endMask;
                zobristKey ^= GameLogic.WHITE_PAWN_HASH[endPos];
                break;
            case BISHOP_MASK:
                bishops ^= endMask;
                halfMoveCounter = 0;
                whiteMaterial -= 300;
                whitePieces ^= endMask;
                zobristKey ^= GameLogic.WHITE_BISHOP_HASH[endPos];
                break;
            case KNIGHT_MASK:
                knights ^= endMask;
                halfMoveCounter = 0;
                whiteMaterial -= 300;
                whitePieces ^= endMask;
                zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[endPos];
                break;
            case ROOK_MASK:
                rooks ^= endMask;
                halfMoveCounter = 0;
                whiteMaterial -= 500;
                whitePieces ^= endMask;
                zobristKey ^= GameLogic.WHITE_ROOK_HASH[endPos];
                break;
            default:
                queens ^= endMask;
                halfMoveCounter = 0;
                whiteMaterial -= 900;
                whitePieces ^= endMask;
                zobristKey ^= GameLogic.WHITE_QUEEN_HASH[endPos];
                break;
        }
    }

    public void unMakeMove(int move) {
        if (whiteToMove) {
            fullMoveCounter --;
            unMakeBlackMove(move);
        } else {
            unMakeWhiteMove(move);
        }

        int newEnPassant = (move >> 24) & 0b1111;
        if (enPassantColumn != newEnPassant) {
            if (enPassantColumn != 8) {
                zobristKey ^= GameLogic.EN_PASSANT_COLUMN_HASH[enPassantColumn];
            }
            if (newEnPassant != 8) {
                zobristKey ^= GameLogic.EN_PASSANT_COLUMN_HASH[newEnPassant];
            }
        }
        enPassantColumn = newEnPassant;

        int newCastleRights = (move >> 28) & 0b1111;
        if (castleRights != newCastleRights) {
            for (int i = 0; i < 4; i++) {
                if (((newCastleRights >> i) & 1) != ((castleRights >> i) & 1)) {
                    zobristKey ^= GameLogic.CASTLE_RIGHTS_HASH[3 - i];
                }
            }
        }
        castleRights = newCastleRights;

        halfMoveCounter = (move >> 17) & 0b1111111;

        pieces = whitePieces | blackPieces;
        whiteToMove = !whiteToMove;
        zobristKey ^= GameLogic.WHITE_TO_MOVE_HASH;
        updatePins();
        checkChecks();
    }

    private void unMakeWhiteMove(int move) {

        int startPos = (move >> 9) & 0b111111;
        int takenPiece = (move >> 6) & PIECE_MASK;
        int endPos = move & 0b111111;
        long endMask = 1L << endPos;
        long startMask = 1L << startPos;

        if ((move >> 15 & 0b11) == 1) {
            endPos = ((move >> 4) & 0b11) + startPos - 9;
            endMask = 1L << endPos;

            if (((queens >> endPos) & 1) == 1) {
                queens ^= endMask;
                zobristKey ^= GameLogic.WHITE_QUEEN_HASH[endPos];
                whitePawnsMaterial += 100;
                whiteMaterial -= 900;
            } else if (((knights >> endPos) & 1) == 1) {
                knights ^= endMask;
                zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[endPos];
                whitePawnsMaterial += 100;
                whiteMaterial -= 300;
            } else if (((rooks >> endPos) & 1) == 1) {
                rooks ^= endMask;
                zobristKey ^= GameLogic.WHITE_ROOK_HASH[endPos];
                whitePawnsMaterial += 100;
                whiteMaterial -= 500;
            } else {
                bishops ^= endMask;
                zobristKey ^= GameLogic.WHITE_BISHOP_HASH[endPos];
                whitePawnsMaterial += 100;
                whiteMaterial -= 300;
            }

            pawns ^= startMask;
            zobristKey ^= GameLogic.WHITE_PAWN_HASH[startPos];

            inCheck = ((move & 2) == 2);
            inDoubleCheck = ((move & 1) == 1);

        } else {
            if (((queens >> endPos) & 1) == 1) {
                queens ^= (startMask | endMask);
                zobristKey ^= GameLogic.WHITE_QUEEN_HASH[startPos];
                zobristKey ^= GameLogic.WHITE_QUEEN_HASH[endPos];
            } else if (((knights >> endPos) & 1) == 1) {
                knights ^= (startMask | endMask);
                zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[startPos];
                zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[endPos];
            } else if (((rooks >> endPos) & 1) == 1) {
                rooks ^= (startMask | endMask);
                zobristKey ^= GameLogic.WHITE_ROOK_HASH[startPos];
                zobristKey ^= GameLogic.WHITE_ROOK_HASH[endPos];
            } else if (((bishops >> endPos) & 1) == 1){
                bishops ^= (startMask | endMask);
                zobristKey ^= GameLogic.WHITE_BISHOP_HASH[startPos];
                zobristKey ^= GameLogic.WHITE_BISHOP_HASH[endPos];
            } else if (((kings >> endPos) & 1) == 1){
                kings ^= (startMask | endMask);
                zobristKey ^= GameLogic.WHITE_KING_HASH[startPos];
                zobristKey ^= GameLogic.WHITE_KING_HASH[endPos];
                if (Math.abs(startPos - endPos) == 2) {
                    if (endPos == 58) {
                        rooks ^= (9L << 56);
                        whitePieces ^= (9L << 56);
                        zobristKey ^= GameLogic.WHITE_ROOK_HASH[56];
                        zobristKey ^= GameLogic.WHITE_ROOK_HASH[59];
                    } else {
                        rooks ^= (5L << 61);
                        whitePieces ^= (5L << 61);
                        zobristKey ^= GameLogic.WHITE_ROOK_HASH[61];
                        zobristKey ^= GameLogic.WHITE_ROOK_HASH[63];
                    }
                }
            } else {
                pawns ^= (startMask | endMask);
                zobristKey ^= GameLogic.WHITE_PAWN_HASH[startPos];
                zobristKey ^= GameLogic.WHITE_PAWN_HASH[endPos];
                if (takenPiece == 0 && startPos % 8 != endPos % 8) {
                    long takenMask = 1L << (endPos + 8);
                    pawns ^= takenMask;
                    zobristKey ^= GameLogic.BLACK_PAWN_HASH[endPos + 8];
                    blackPieces ^= takenMask;
                    blackPawnsMaterial += 100;
                }
            }
        }

        whitePieces ^= (endMask | startMask);

        if (takenPiece != 0) {
            switch (takenPiece) {
                case PAWN_MASK:
                    pawns ^= endMask;
                    zobristKey ^= GameLogic.BLACK_PAWN_HASH[endPos];
                    blackPawnsMaterial += 100;
                    blackPieces ^= endMask;
                    break;
                case BISHOP_MASK:
                    bishops ^= endMask;
                    zobristKey ^= GameLogic.BLACK_BISHOP_HASH[endPos];
                    blackMaterial += 300;
                    blackPieces ^= endMask;
                    break;
                case KNIGHT_MASK:
                    knights ^= endMask;
                    zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[endPos];
                    blackMaterial += 300;
                    blackPieces ^= endMask;
                    break;
                case ROOK_MASK:
                    rooks ^= endMask;
                    zobristKey ^= GameLogic.BLACK_ROOK_HASH[endPos];
                    blackMaterial += 500;
                    blackPieces ^= endMask;
                    break;
                default:
                    queens ^= endMask;
                    zobristKey ^= GameLogic.BLACK_QUEEN_HASH[endPos];
                    blackMaterial += 900;
                    blackPieces ^= endMask;
                    break;
            }
        }
    }

    private void unMakeBlackMove(int move) {

        int startPos = (move >> 9) & 0b111111;
        int takenPiece = (move >> 6) & PIECE_MASK;
        int endPos = move & 0b111111;
        long endMask = 1L << endPos;
        long startMask = 1L << startPos;

        if ((move >> 15 & 0b11) == 1) {
            endPos = ((move >> 4) & 0b11) + startPos + 7;
            endMask = 1L << endPos;

            if (((queens >> endPos) & 1) == 1) {
                queens ^= endMask;
                zobristKey ^= GameLogic.BLACK_QUEEN_HASH[endPos];
                blackPawnsMaterial += 100;
                blackMaterial -= 900;
            } else if (((knights >> endPos) & 1) == 1) {
                knights ^= endMask;
                zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[endPos];
                blackPawnsMaterial += 100;
                blackMaterial -= 300;
            } else if (((rooks >> endPos) & 1) == 1) {
                rooks ^= endMask;
                zobristKey ^= GameLogic.BLACK_ROOK_HASH[endPos];
                blackPawnsMaterial += 100;
                blackMaterial -= 500;
            } else {
                bishops ^= endMask;
                zobristKey ^= GameLogic.BLACK_BISHOP_HASH[endPos];
                blackPawnsMaterial += 100;
                blackMaterial -= 300;
            }

            pawns ^= startMask;
            zobristKey ^= GameLogic.BLACK_PAWN_HASH[startPos];

            inCheck = ((move & 2) == 2);
            inDoubleCheck = ((move & 1) == 1);

        } else {
            if (((queens >> endPos) & 1) == 1) {
                queens ^= (startMask | endMask);
                zobristKey ^= GameLogic.BLACK_QUEEN_HASH[startPos];
                zobristKey ^= GameLogic.BLACK_QUEEN_HASH[endPos];
            } else if (((knights >> endPos) & 1) == 1) {
                knights ^= (startMask | endMask);
                zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[startPos];
                zobristKey ^= GameLogic.BLACK_KNIGHT_HASH[endPos];
            } else if (((rooks >> endPos) & 1) == 1) {
                rooks ^= (startMask | endMask);
                zobristKey ^= GameLogic.BLACK_ROOK_HASH[startPos];
                zobristKey ^= GameLogic.BLACK_ROOK_HASH[endPos];
            } else if (((bishops >> endPos) & 1) == 1){
                bishops ^= (startMask | endMask);
                zobristKey ^= GameLogic.BLACK_BISHOP_HASH[startPos];
                zobristKey ^= GameLogic.BLACK_BISHOP_HASH[endPos];
            } else if (((kings >> endPos) & 1) == 1){
                kings ^= (startMask | endMask);
                zobristKey ^= GameLogic.BLACK_KING_HASH[startPos];
                zobristKey ^= GameLogic.BLACK_KING_HASH[endPos];
                if (Math.abs(startPos - endPos) == 2) {
                    if (endPos == 2) {
                        rooks ^= 0b1001;
                        blackPieces ^= 0b1001;
                        zobristKey ^= GameLogic.BLACK_ROOK_HASH[0];
                        zobristKey ^= GameLogic.BLACK_ROOK_HASH[3];
                    } else {
                        rooks ^= 0b10100000;
                        blackPieces ^= 0b10100000;
                        zobristKey ^= GameLogic.BLACK_ROOK_HASH[5];
                        zobristKey ^= GameLogic.BLACK_ROOK_HASH[7];
                    }
                }
            } else {
                pawns ^= (startMask | endMask);
                zobristKey ^= GameLogic.BLACK_PAWN_HASH[startPos];
                zobristKey ^= GameLogic.BLACK_PAWN_HASH[endPos];
                if (takenPiece == 0 && startPos % 8 != endPos % 8) {
                    long takenMask = 1L << (endPos - 8);
                    pawns ^= takenMask;
                    whitePieces ^= takenMask;
                    zobristKey ^= GameLogic.WHITE_PAWN_HASH[endPos - 8];
                    whitePawnsMaterial += 100;
                }
            }
        }

        blackPieces ^= (endMask | startMask);

        if (takenPiece != 0) {
            switch (takenPiece) {
                case PAWN_MASK:
                    pawns ^= endMask;
                    whitePawnsMaterial += 100;
                    whitePieces ^= endMask;
                    zobristKey ^= GameLogic.WHITE_PAWN_HASH[endPos];
                    break;
                case BISHOP_MASK:
                    bishops ^= endMask;
                    whiteMaterial += 300;
                    whitePieces ^= endMask;
                    zobristKey ^= GameLogic.WHITE_BISHOP_HASH[endPos];
                    break;
                case KNIGHT_MASK:
                    knights ^= endMask;
                    whiteMaterial += 300;
                    whitePieces ^= endMask;
                    zobristKey ^= GameLogic.WHITE_KNIGHT_HASH[endPos];
                    break;
                case ROOK_MASK:
                    rooks ^= endMask;
                    whiteMaterial += 500;
                    whitePieces ^= endMask;
                    zobristKey ^= GameLogic.WHITE_ROOK_HASH[endPos];
                    break;
                default:
                    queens ^= endMask;
                    whiteMaterial += 900;
                    whitePieces ^= endMask;
                    zobristKey ^= GameLogic.WHITE_QUEEN_HASH[endPos];
                    break;
            }
        }
    }

    private void updatePins() {
        verticalPin = 0;
        horizontalPin = 0;
        diagonalRightPin = 0;
        diagonalLeftPin = 0;
        long myKing = kings & (whiteToMove ? whitePieces : blackPieces);
        int kingPos = (myKing > 0 ? (int)(Math.log(myKing) / Math.log(2)) : 63);

        for (int i = 0; i < 8; i++) {
            int pin = 0;
            int vector = SLIDE_VECTORS[i];
            int pos = kingPos + vector;
            for (int j = 0; j < DISTANCE_TO_EDGE[kingPos][i]; j++) {
                if ((((whiteToMove ? whitePieces : blackPieces) >> pos) & 1) == 1) {
                    if (pin > 0) {
                        break;
                    } else {
                        pin = pos;
                    }
                } else if ((((whiteToMove ? blackPieces : whitePieces) >> pos) & 1) == 1) {
                    if (pin > 0 && (((queens | (i % 2 == 0 ? rooks : bishops)) >> pos) & 1) == 1) {
                        if (i % 4 == 0) {
                            verticalPin |= (1L << pin);
                        } else if (i % 4 == 1) {
                            diagonalRightPin |= (1L << pin);
                        } else if (i % 4 == 2) {
                            horizontalPin |= (1L << pin);
                        } else {
                            diagonalLeftPin |= (1L << pin);
                        }
                    }
                    break;
                }
                pos += vector;
            }
        }
    }

    private void checkChecks() {
        inCheck = false;
        inDoubleCheck = false;
        tilesToStopCheck = 0;
        tilesAttacked = 0;
        enPassantProtection = false;
        long myKing = kings & (whiteToMove ? whitePieces : blackPieces);
        int kingPos = (myKing > 0 ? (int)(Math.log(myKing) / Math.log(2)) : 63);
        long enemyPieces = (whiteToMove ? blackPieces : whitePieces);
        long enemyQueens = enemyPieces & queens;
        long enemyRooks = enemyPieces & rooks;
        long enemyKnights = enemyPieces & knights;
        long enemyBishops = enemyPieces & bishops;
        long enemyPawns = enemyPieces & pawns;

        while (enemyQueens != 0) {
            int pos = (enemyQueens > 0 ? (int)(Math.log(enemyQueens) / Math.log(2)) : 63);
            enemyQueens ^= (1L << pos);
            long temp = tilesAttackedQueen(pos, false);
            tilesAttacked |= temp;
            if ((temp & myKing) != 0) {
                if (inCheck) {
                    inDoubleCheck = true;
                    tilesToStopCheck = 0;
                } else {
                    inCheck = true;
                    tilesToStopCheck = GameLogic.getTilesToStopCheck(kingPos, pos);
                }
            }
        }
        while (enemyRooks != 0) {
            int pos = (enemyRooks > 0 ? (int)(Math.log(enemyRooks) / Math.log(2)) : 63);
            enemyRooks ^= (1L << pos);
            long temp = tilesAttackedRook(pos, false);
            tilesAttacked |= temp;
            if ((temp & myKing) != 0) {
                if (inCheck) {
                    inDoubleCheck = true;
                    tilesToStopCheck = 0;
                } else {
                    inCheck = true;
                    tilesToStopCheck = GameLogic.getTilesToStopCheck(kingPos, pos);
                }
            }
        }
        while (enemyBishops != 0) {
            int pos = (enemyBishops > 0 ? (int)(Math.log(enemyBishops) / Math.log(2)) : 63);
            enemyBishops ^= (1L << pos);
            long temp = tilesAttackedBishop(pos, false);
            tilesAttacked |= temp;
            if ((temp & myKing) != 0) {
                if (inCheck) {
                    inDoubleCheck = true;
                    tilesToStopCheck = 0;
                } else {
                    inCheck = true;
                    tilesToStopCheck = GameLogic.getTilesToStopCheck(kingPos, pos);
                }
            }
        }
        while (enemyKnights != 0) {
            int pos = (enemyKnights > 0 ? (int)(Math.log(enemyKnights) / Math.log(2)) : 63);
            enemyKnights ^= (1L << pos);
            long temp = tilesAttackedKnight(pos, false);
            tilesAttacked |= temp;
            if ((temp & myKing) != 0) {
                if (inCheck) {
                    inDoubleCheck = true;
                    tilesToStopCheck = 0;
                } else {
                    inCheck = true;
                    tilesToStopCheck |= (1L << pos);
                }
            }
        }
        while (enemyPawns != 0) {
            int pos = (enemyPawns > 0 ? (int)(Math.log(enemyPawns) / Math.log(2)) : 63);
            enemyPawns ^= (1L << pos);
            long temp = tilesAttackedPawn(pos, false, true);
            tilesAttacked |= temp;
            if ((temp & myKing) != 0) {
                if (inCheck) {
                    inDoubleCheck = true;
                    tilesToStopCheck = 0;
                } else {
                    inCheck = true;
                    tilesToStopCheck |= (1L << pos);
                    if (pos % 8 == enPassantColumn && GameLogic.getRow(pos) == (whiteToMove ? 3 : 4)) {
                        enPassantProtection = true;
                    }
                }
            }
        }
        long enemyKing = kings ^ myKing;
        int altKingPos = (enemyKing > 0 ? (int)(Math.log(enemyKing) / Math.log(2)) : 63);
        tilesAttacked |= tilesAttackedKing(altKingPos);
    }

//    TODO: history
//    private void updateHistory(boolean addPosition) {
//        String board = "";
//
//        int count = 0;
//
//        for (int i = 0; i < 64; i++) {
//
//            if ((pieces >> i & 1) == 0) {
//                count++;
//            } else {
//                if (count > 0) {
//                    board += Integer.toString(count);
//                }
//                count = 0;
//
//                char piece = 'k';
//                if ((pawns >> i & 1) == 1) {
//                    piece = 'p';
//                } else if ((queens >> i & 1) == 1) {
//                    piece = 'q';
//                } else if ((rooks >> i & 1) == 1) {
//                    piece = 'r';
//                } else if ((knights >> i & 1) == 1) {
//                    piece = 'n';
//                } else if ((bishops >> i & 1) == 1) {
//                    piece = 'b';
//                }
//
//                if ((whitePieces >> i & 1) == 1) {
//                    piece = Character.toUpperCase(piece);
//                }
//                board += piece;
//            }
//
//            if (i % 8 == 7) {
//                if (count > 0) {
//                    board += Integer.toString(count);
//                }
//                if (i != 63) {
//                    board += "/";
//                }
//                count = 0;
//            }
//        }
//        history.add(board);
//    }

    private int evaluate() {

        if (generateAllLegalMoves(false).isEmpty()) {
            return -999999999;
        }

        int eval = 0;

        eval += (900 * (Long.bitCount(queens) - (2 * Long.bitCount(blackPieces & queens))));
        eval += (500 * (Long.bitCount(rooks) - (2 * Long.bitCount(blackPieces & rooks))));
        eval += (300 * (Long.bitCount(knights) - (2 * Long.bitCount(blackPieces & knights))));
        eval += (300 * (Long.bitCount(bishops) - (2 * Long.bitCount(blackPieces & bishops))));
        eval += (100 * (Long.bitCount(pawns) - (2 * Long.bitCount(blackPieces & pawns))));

        if (Long.bitCount(whitePieces & bishops) == 2) {
            eval += 50;
        }
        if (Long.bitCount(blackPieces & bishops) == 2) {
            eval -= 50;
        }

        eval += GameLogic.pawnTableScore(whitePieces & pawns, blackPieces & pawns);
        eval += GameLogic.rookTableScore(whitePieces & rooks, blackPieces & rooks);
        eval += GameLogic.knightTableScore(whitePieces & knights, blackPieces & knights);
        eval += GameLogic.bishopTableScore(whitePieces & bishops, blackPieces & bishops);
        eval += GameLogic.queenTableScore(whitePieces & queens, blackPieces & queens);
        eval += GameLogic.kingTableScore(whitePieces & kings, blackPieces & kings, (whiteMaterial + blackMaterial < 2000));
        return (eval * (whiteToMove ? 1 : -1));
    }

    private int moveScore(int move, int plyFromRoot) {

        if (bestMoves.size() > plyFromRoot && bestMoves.get(plyFromRoot) == move) {
            return 999999;
        }

        int score = 100;

        if (((move >> 15) & 1) == 1) {
            if (((move >> 2) & 0b11) == 3) {
                score += 20;
            } else if (((move >> 2) & 0b11) == 1) {
                score += 10;
            }
            score += 200;
        } else if (((tilesAttacked >> (move & 0b111111)) & 1) == 1) {
            switch (getPieceOnTile((move >> 9) & 0b111111) & 0b111) {
                case PAWN_MASK:
                    score -= 10;
                case BISHOP_MASK:
                case KNIGHT_MASK:
                    score -= 30;
                    break;
                case ROOK_MASK:
                    score -= 50;
                    break;
                case QUEEN_MASK:
                    score -= 90;
                    break;
            }
        }

        switch ((move >> 6) & 0b111) {
            case 0:
                break;
            case PAWN_MASK:
                score += 10;
            case BISHOP_MASK:
            case KNIGHT_MASK:
                score += 30;
                break;
            case ROOK_MASK:
                score += 50;
                break;
            case QUEEN_MASK:
                score += 90;
                break;
        }

        return score;
    }

    public void orderMoves(ArrayList<Integer> moves, int plyFromRoot) {
        ArrayList<Integer> scores = new ArrayList<>();
        for (int move : moves) {
            scores.add(moveScore(move, plyFromRoot));
        }

        for (int i = 0; i < moves.size() - 1; i++) {
            for (int j = i + 1; j > 0; j--) {
                int swapIndex = j - 1;
                if (scores.get(swapIndex) < scores.get(j)) {

                    int temp = moves.get(swapIndex);
                    moves.set(swapIndex, moves.get(j));
                    moves.set(j, temp);

                    temp = scores.get(swapIndex);
                    scores.set(swapIndex, scores.get(j));
                    scores.set(j, temp);
                }
            }
        }
    }

    public void startSearch(int milliseconds) {
        bestMoves.clear();
        if (generateAllLegalMoves(false).size() == 1) {
            bestMoves.add(generateAllLegalMoves(false).get(0));
            return;
        }

        cutoffTime = System.currentTimeMillis() + milliseconds;
        int depth = 1;
        while (System.currentTimeMillis() < cutoffTime) {
            search(depth, 0, 0x80000001, 0x7ffffffe);
            System.out.println(depth);
            depth++;
        }
    }

    private int search(int depth, int plyFromRoot, int alpha, int beta) {

        if (System.currentTimeMillis() > cutoffTime) {
            return 0;
        }

        if (plyFromRoot > 0) {
            if (history.contains(zobristKey)) {
                return 0;
            }
        }

        if (depth == 0) {
            return quiescenceSearch(alpha, beta);
        }

        ArrayList<Integer> moves = generateAllLegalMoves(false);
        if (moves.isEmpty()) {
            return (inCheck ? -999999999 : 0);
        }

        orderMoves(moves, plyFromRoot);

        for (int move : moves) {
            makeMove(move);
            int evaluation = -search(depth - 1, plyFromRoot + 1, -beta, -alpha);
            unMakeMove(move);
            if (System.currentTimeMillis() > cutoffTime) {
                return 0;
            }
            if (evaluation >= beta) {
                return beta;
            }
            if (evaluation > alpha) {
                alpha = evaluation;
                if (bestMoves.size() > plyFromRoot) {
                    bestMoves.set(plyFromRoot, move);
                } else {
                    bestMoves.add(move);
                }
            }
        }
        return alpha;
    }

    public int quiescenceSearch(int alpha, int beta) {
        if (System.currentTimeMillis() > cutoffTime) {
            return 0;
        }
        int eval = evaluate();
        if (eval >= beta) {
            return beta;
        }
        alpha = Math.max(alpha, eval);

        ArrayList<Integer> moves = generateAllLegalMoves(!inCheck);
        orderMoves(moves, 100);
        for (int move : moves) {
            makeMove(move);
            eval = -quiescenceSearch(-beta, -alpha);
            unMakeMove(move);
            if (System.currentTimeMillis() > cutoffTime) {
                return 0;
            }

            if (eval >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, eval);
        }

        return alpha;
    }
}
