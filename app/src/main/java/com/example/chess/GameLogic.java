package com.example.chess;

public class GameLogic {

    public static Board createStartBoard() {
        return createBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public static Board createBoardFromFEN(String fen) {
        long whitePieces = 0L;
        long blackPieces = 0L;
        long kings = 0L;
        long queens = 0L;
        long rooks = 0L;
        long knights = 0L;
        long bishops = 0L;
        long pawns = 0L;

        int tileCounter = 0;
        int slashCounter = 0;
        int indexNum = 0;
        for (int i = 0; i < fen.length(); i ++) {

            char symbol = fen.charAt(i);

            if (slashCounter < 8) {

                if (symbol == '/' || symbol == ' ') {
                    slashCounter ++;
                    continue;
                }

                if (Character.isDigit(symbol)) {
                    tileCounter += Character.getNumericValue(symbol);
                }
                else {

                    if (Character.isUpperCase(symbol)) {

                        whitePieces |= (1L << tileCounter);

                    } else {
                        blackPieces |= (1L << tileCounter);

                    }
                    if(Character.toLowerCase(symbol) == 'r') {
                        rooks |= (1L << tileCounter);
                    } else if(Character.toLowerCase(symbol) == 'n') {
                        knights |= (1L << tileCounter);
                    } else if(Character.toLowerCase(symbol) == 'b') {
                        bishops |= (1L << tileCounter);
                    } else if(Character.toLowerCase(symbol) == 'k') {
                        kings |= (1L << tileCounter);
                    } else if (Character.toLowerCase(symbol) == 'q') {
                        queens |= (1L << tileCounter);
                    } else {
                        pawns |= (1L << tileCounter);
                    }
                    tileCounter++;
                }
            } else {
                indexNum = i;
                break;
            }
        }

        boolean whiteToMove = fen.charAt(indexNum) == 'w';
        indexNum += 2;

        int castleRights = 0;

        if (fen.charAt(indexNum) == '-') {
            indexNum ++;
        } else {
            while (fen.charAt(indexNum) != ' ') {
                switch (fen.charAt(indexNum)) {
                    case 'K':
                        castleRights ^= 0b1000;
                        break;
                    case 'Q':
                        castleRights ^= 0b0100;
                        break;
                    case 'k':
                        castleRights ^= 0b0010;
                        break;
                    default:
                        castleRights ^= 0b0001;
                        break;
                }
                indexNum ++;
            }
        }

        indexNum ++;

        int enPassantColumn = 8;
        if (fen.charAt(indexNum) == '-') {
            indexNum += 2;
        } else {
            switch (fen.charAt(indexNum)) {
                case 'a':
                    enPassantColumn = 0;
                    break;
                case 'b':
                    enPassantColumn = 1;
                    break;
                case 'c':
                    enPassantColumn = 2;
                    break;
                case 'd':
                    enPassantColumn = 3;
                    break;
                case 'e':
                    enPassantColumn = 4;
                    break;
                case 'f':
                    enPassantColumn = 5;
                    break;
                case 'g':
                    enPassantColumn = 6;
                    break;
                case 'h':
                    enPassantColumn = 7;
                    break;
                default:
                    enPassantColumn = 8;
                    break;
            }
            indexNum += 3;
        }

        int halfMoveCounter = 0;
        int fullMoveCounter = 1;

        if (fen.length() > indexNum) {

            String halfMoves = "";
            while (fen.charAt(indexNum) != ' ') {
                halfMoves = halfMoves.concat(Character.toString(fen.charAt(indexNum)));
                indexNum ++;
            }
            halfMoveCounter = Integer.parseInt(halfMoves);
            indexNum ++;

            String fullMoves = "";
            while (indexNum < fen.length()) {
                fullMoves = fullMoves.concat(Character.toString(fen.charAt(indexNum)));
                indexNum ++;
            }
            fullMoveCounter = Integer.parseInt(fullMoves);
        }

        return new Board(whitePieces, blackPieces, kings, queens, rooks, knights, bishops, pawns, whiteToMove, castleRights, enPassantColumn, halfMoveCounter, fullMoveCounter);
    }

    public static int getRow(int tileNum) {
        if (tileNum < 8) {
            return 0;
        } else if (tileNum < 16) {
            return 1;
        } else if (tileNum < 24) {
            return 2;
        } else if (tileNum < 32) {
            return 3;
        } else if (tileNum < 40) {
            return 4;
        } else if (tileNum < 48) {
            return 5;
        } else if (tileNum < 56) {
            return 6;
        } else {
            return 7;
        }
    }

    public static String getPieceName(int piece) {
        switch (piece) {
            case 6:
                return "white_king";
            case 5:
                return "white_queen";
            case 4:
                return "white_rook";
            case 3:
                return "white_knight";
            case 2:
                return "white_bishop";
            case 1:
                return "white_pawn";
            case 14:
                return "black_king";
            case 13:
                return "black_queen";
            case 12:
                return "black_rook";
            case 11:
                return "black_knight";
            case 10:
                return "black_bishop";
            case 9:
                return "black_pawn";
            default:
                return "empty";
        }
    }

    public static long getTilesToStopCheck(int kingPosition, int enemyPosition) {
        long tiles = 0L;
        int difference = enemyPosition - kingPosition;
        if (kingPosition % 8 == enemyPosition % 8) {
            int vector = 8 * difference / Math.abs(difference);
            for (int i = kingPosition; i != enemyPosition; i += vector) {
                tiles |= (1L << (i + vector));
            }
        } else if (getRow(kingPosition) == getRow(enemyPosition)) {
            int vector = difference / Math.abs(difference);
            for (int i = kingPosition; i != enemyPosition; i += vector) {
                tiles |= (1L << (i + vector));
            }
        } else if (difference % 9 == 0) {
            int vector = 9 * difference / Math.abs(difference);
            for (int i = kingPosition; i != enemyPosition; i += vector) {
                tiles |= (1L << (i + vector));
            }
        } else {
            int vector = 7 * difference / Math.abs(difference);
            for (int i = kingPosition; i != enemyPosition; i += vector) {
                tiles |= (1L << (i + vector));
            }
        }
        return tiles;
    }

    public static String getSquares(int move) {
        int pos = (move >> 9) & 0b111111;
        String out = "";
        switch (pos % 8) {
            case 0:
                out += "a";
                break;
            case 1:
                out += "b";
                break;
            case 2:
                out += "c";
                break;
            case 3:
                out += "d";
                break;
            case 4:
                out += "e";
                break;
            case 5:
                out += "f";
                break;
            case 6:
                out += "g";
                break;
            case 7:
                out += "h";
                break;
        }
        out += String.valueOf(8 - getRow(pos));

        int endPos = move & 0b111111;
        if (((move >> 15) & 0b11) == 1) {
            endPos = pos + (pos < 30 ? -9 : 7) + ((move >> 4) & 0b11);
        }
        switch (endPos % 8) {
            case 0:
                out += "a";
                break;
            case 1:
                out += "b";
                break;
            case 2:
                out += "c";
                break;
            case 3:
                out += "d";
                break;
            case 4:
                out += "e";
                break;
            case 5:
                out += "f";
                break;
            case 6:
                out += "g";
                break;
            case 7:
                out += "h";
                break;
        }
        out += String.valueOf(8 - getRow(endPos));

        if (((move >> 15) & 0b11) == 1) {
            switch ((move >> 2) & 0b11) {
                case 0:
                    out += "b";
                    break;
                case 1:
                    out += "n";
                    break;
                case 2:
                    out += "r";
                    break;
                case 3:
                    out += "q";
                    break;
            }
        }

        return out;
    }

    public static int inversePos(final int pos) {
        return 56 - pos + (2 * (pos % 8));
    }

    public static int queenTableScore(long whiteQueens, long blackQueens) {
        int score = 0;
        while (whiteQueens != 0) {
            int pos = (whiteQueens > 0 ? (int)(Math.log(whiteQueens) / Math.log(2)) : 63);
            whiteQueens ^= (1L << pos);
            score += QUEEN_TABLE[pos];
        }
        while (blackQueens != 0) {
            int pos = (blackQueens > 0 ? (int)(Math.log(blackQueens) / Math.log(2)) : 63);
            blackQueens ^= (1L << pos);
            score -= QUEEN_TABLE[inversePos(pos)];
        }
        return score;
    }

    public static int rookTableScore(long whiteRooks, long blackRooks) {
        int score = 0;
        while (whiteRooks != 0) {
            int pos = (whiteRooks > 0 ? (int)(Math.log(whiteRooks) / Math.log(2)) : 63);
            whiteRooks ^= (1L << pos);
            score += ROOK_TABLE[pos];
        }
        while (blackRooks != 0) {
            int pos = (blackRooks > 0 ? (int)(Math.log(blackRooks) / Math.log(2)) : 63);
            blackRooks ^= (1L << pos);
            score -= ROOK_TABLE[inversePos(pos)];
        }
        return score;
    }

    public static int knightTableScore(long whiteKnights, long blackKnights) {
        int score = 0;
        while (whiteKnights != 0) {
            int pos = (whiteKnights > 0 ? (int)(Math.log(whiteKnights) / Math.log(2)) : 63);
            whiteKnights ^= (1L << pos);
            score += KNIGHT_TABLE[pos];
        }
        while (blackKnights != 0) {
            int pos = (blackKnights > 0 ? (int)(Math.log(blackKnights) / Math.log(2)) : 63);
            blackKnights ^= (1L << pos);
            score -= KNIGHT_TABLE[inversePos(pos)];
        }
        return score;
    }

    public static int bishopTableScore(long whiteBishops, long blackBishops) {
        int score = 0;
        while (whiteBishops != 0) {
            int pos = (whiteBishops > 0 ? (int)(Math.log(whiteBishops) / Math.log(2)) : 63);
            whiteBishops ^= (1L << pos);
            score += BISHOP_TABLE[pos];
        }
        while (blackBishops != 0) {
            int pos = (blackBishops > 0 ? (int)(Math.log(blackBishops) / Math.log(2)) : 63);
            blackBishops ^= (1L << pos);
            score -= BISHOP_TABLE[inversePos(pos)];
        }
        return score;
    }

    public static int pawnTableScore(long whitePawns, long blackPawns) {
        int score = 0;

        int[] whitePawnCol = {Long.bitCount(whitePawns & A_FILE), Long.bitCount(whitePawns & B_FILE),
                Long.bitCount(whitePawns & C_FILE), Long.bitCount(whitePawns & D_FILE), Long.bitCount(whitePawns & E_FILE),
                Long.bitCount(whitePawns & F_FILE), Long.bitCount(whitePawns & G_FILE), Long.bitCount(whitePawns & H_FILE)};
        int[] blackPawnCol = {Long.bitCount(blackPawns & A_FILE), Long.bitCount(blackPawns & B_FILE),
                Long.bitCount(blackPawns & C_FILE), Long.bitCount(blackPawns & D_FILE), Long.bitCount(blackPawns & E_FILE),
                Long.bitCount(blackPawns & F_FILE), Long.bitCount(blackPawns & G_FILE), Long.bitCount(blackPawns & H_FILE)};

        for (int file = 0; file < 8; file++) {
            int numWhitePawns = whitePawnCol[file];
            if (numWhitePawns != 0) {
                if (file < 7 && whitePawnCol[file + 1] > 0) {
                    score += 20;
                }
                if (numWhitePawns != 1) {
                    score -= (10 * numWhitePawns);
                }
            }
            int numBlackPawns = blackPawnCol[file];
            if (numBlackPawns != 0) {
                if (file < 7 && blackPawnCol[file + 1] > 0) {
                    score -= 20;
                }
                if (numBlackPawns != 1) {
                    score += (10 * numBlackPawns);
                }
            }
        }

        while (whitePawns != 0) {
            int pos = (whitePawns > 0 ? (int)(Math.log(whitePawns) / Math.log(2)) : 63);
            whitePawns ^= (1L << pos);
            score += BISHOP_TABLE[pos];
        }
        while (blackPawns != 0) {
            int pos = (blackPawns > 0 ? (int)(Math.log(blackPawns) / Math.log(2)) : 63);
            blackPawns ^= (1L << pos);
            score -= BISHOP_TABLE[inversePos(pos)];
        }
        return score;
    }

    public static int kingTableScore(long whiteKing, long blackKing, boolean endGame) {
        int score = 0;
        int whitePos = (whiteKing > 0 ? (int)(Math.log(whiteKing) / Math.log(2)) : 63);
        int blackPos = (blackKing > 0 ? (int)(Math.log(blackKing) / Math.log(2)) : 63);

        score += (endGame ? KING_END_TABLE[whitePos] : KING_MIDDLE_TABLE[whitePos]);
        score -= (endGame ? KING_END_TABLE[inversePos(blackPos)] : KING_MIDDLE_TABLE[inversePos(blackPos)]);

        return score;
    }


    public static final long[] WHITE_KING_HASH = {
            0x1a13b79a80a4527eL,0xc9adfdf81ba6594aL,0x2eb01b3627747783L,0xb4ca9b0ef29f2e1eL,0x7d766b311a195a15L,0x68081ea68d35bc11L,0xc3cec4c5058a72e5L,0x19e19a6bc51c11bfL,
            0x9c9d704699e10ddaL,0xf835d4098b50c643L,0x40e69508ba959084L,0xabc5ecf93d629069L,0xe32da69f71d091faL,0xdb6433f9f497e2bfL,0xb3885bdf8b04d653L,0x2602591d927f8903L,
            0xf5b86f8ca648d270L,0xe91e3b019c547567L,0x80b4d6571e23b0d6L,0x2b0aa753f88cccf4L,0x49f05a51ad861ad8L,0x3e120d1693445e73L,0xd7f6afa7c7866703L,0xd6c88e6f4f5c397dL,
            0x6b1473e6e6136786L,0x22a19a9d7257cc58L,0xb445c295209a1ec7L,0x4760241552dc1e7cL,0x07b8bbc2649829a7L,0x1f796321ddb02c2dL,0x31797b9529170b2fL,0x14c81a80b0963f9dL,
            0x9f41dabfebd08949L,0x9144ee6a46fc692dL,0x3a62841feae3cf1fL,0x43099722e7e1d560L,0x9df9f3615924db03L,0xa41fd53019de32f9L,0x1b5184c8391c89ecL,0x07eea880594626ddL,
            0xfb36b274e8e7be57L,0x0acae53e6591e7d2L,0xee5e44d0f9015fceL,0x266cafaf0e175cfcL,0x281430e165679e2fL,0xd8f548cd0c3c510fL,0x8bd6fbbbadddc285L,0xc28859f12d376d56L,
            0x67e8a53ab9fe137bL,0xb5c3a89e55abd002L,0x331c8acb4a755de8L,0x4eaad618a128d6a7L,0xd127fa0ebdc71510L,0x1a6154e419454c4cL,0x28364dc6b9f1854fL,0xfe16aca80fd96380L,
            0xbea2ad45ef8ae7a2L,0x39b085a644180d2aL,0x905aac44b867b2bfL,0x4d21470e3e755275L,0x31858569329e2246L,0x4134919c74eb90acL,0x471010681649d24fL,0x06f6c112a58a8b8fL};
    public static final long[] WHITE_QUEEN_HASH = {
            0xe944f01f75aa3e28L,0xf492b51432615463L,0x6b57f700cd387eacL,0x0cafb009db86471eL,0x3047e4a78db83c21L,0xa27f96c598d99258L,0xe75d1fe384f81b1cL,0x5dc04a35d0860751L,
            0x8372e8cdeefb3734L,0x7786b82629fd206dL,0x8ac60cdfe79fc2f3L,0xe569e44ec28a4877L,0xaa2e758da1998a3dL,0xd610e35f4c7e77d7L,0xd6db2fd12913cc4cL,0xa97023d8be567903L,
            0x94c57f0139aff986L,0xecce3cb3d8ae2eb6L,0x9eac82cae5be3d2eL,0xc89aef5302364c7dL,0x5ebcc5abcb335f2cL,0x0dc3b8f4c5337774L,0xa467c2dbafe64911L,0xb270ae051bfa39f8L,
            0x2a1945191c47de09L,0xf4fef457ec7aa065L,0x69d9df9b83ad00d4L,0x09e89d3c9f5988b7L,0x22663c1a9e3257edL,0x9e1593fc07129e6aL,0xa14fbbe21984fd88L,0x1059e9228cfc0fdeL,
            0xea8682da2196d803L,0xa3eddf211bf7cdcfL,0x60ef5eb75f7350a3L,0xec91e25efbf26645L,0x8ee5ab5c285a0cb9L,0x25bfebd3c37a098dL,0x7d2962ba35da8abaL,0x01a9a647baf22f60L,
            0x0ac4b36de9919426L,0xb702e231cf118e33L,0x0346a3738c5661adL,0x169c27950dd52fc4L,0x84e712a799f2b216L,0x9e207f35ebd13e54L,0x501d460a2fbc809aL,0xd3e532951c835ff3L,
            0xe8d6e794a0ca1db8L,0x0fb166c976e779d2L,0x94c6ad4fff153879L,0x96c51b483674bedfL,0xba2fbff653a6cef9L,0xcce796d328c6ab93L,0x783c16e9580de385L,0xd700c698917639feL,
            0x0982c9ef3e5bcc6dL,0x4e5e43ed6c22bdcfL,0x55ddd902ab3aa596L,0x465241fb7b046e4cL,0x86b7c652badf88baL,0x233df847c271146cL,0x4b4b623ef5945a90L,0xabff21955337c965L};
    public static final long[] WHITE_ROOK_HASH = {
            0x17e6e17b073c8a94L,0xd14aa915713e3f9aL,0xf8cb89685cea1eaeL,0x7b9b2c7a7c6a5abbL,0xda193cf08c83443aL,0x6c3feff1512b17d2L,0x0fe882fa7501e9acL,0x5683371793a89a16L,
            0xc6da28c97d13eb7cL,0xf6e3e9f1258230ddL,0x7f3b0892bacf4adaL,0xd00f483b677c70e5L,0xb7cbe68fed4dc30eL,0x94822f87e1f5f841L,0x3a057b1eddfc3b18L,0xad4388f997283833L,
            0x721f922409c666a4L,0x231051a4947ee6dbL,0x99ff23c148587964L,0x13649fe98c919498L,0x82deb4dc3445c67bL,0x1b321fadf923888fL,0xb3ddb25979e1a596L,0xe04e4661868684ceL,
            0xfa6ea0370a3ea1f3L,0x3b785da77f7f76b8L,0xc59c452ae7b926c4L,0xba03225aa3d4272eL,0x12b0b934de245f54L,0xc6c3384e24c2bc38L,0xd4a5662b698b89ecL,0x4efe8bf34ccb805fL,
            0x79045eef2e4324deL,0x69b6499b80fb1557L,0x6f1aa6a5f3cdb9dcL,0x9264f20c3396fd9cL,0x6566729e9ad57342L,0x5e49e88c57a54077L,0x77c70c2520552d57L,0x5a85771272e4f473L,
            0xd45783f9c1586b24L,0x135f18e2117f6644L,0xb6a05c52fc982f4aL,0xdd413b459ab81545L,0x04b4168b51ba491dL,0xc6b40fbc4116a76cL,0x77b2a83dea42d9f5L,0xa7411992d99ef27fL,
            0xdbd43a9a975fe712L,0xc7309a55dc39687bL,0x6a827b0a2cf81b88L,0x6a1c685a993b8bb9L,0x00318b4cfff5783bL,0x45888acdb178daa0L,0xd66a534f0253eeffL,0x13e90c0249c91984L,
            0xf68c720c3cf63f63L,0x777ae1e03581e458L,0x2f0eb92a0afddf42L,0x3751dc6aee12b41aL,0x9c8e9fe64cf378bfL,0x5ec06f166506bc23L,0xae9687428f63ddedL,0x4a9588e192808a9eL};
    public static final long[] WHITE_KNIGHT_HASH = {
            0xce269dc3195f143fL,0x71b84facbc3a9355L,0x2ddc8c7d1ad617a5L,0xe9024e79f9eccb69L,0x93d209d6ca8cb57dL,0x84e6fd008557cb7cL,0xf3c18926c178824bL,0x0abb1f112152bd7dL,
            0xeaed1fb520f0605dL,0x8c8c50eae60b0af3L,0x8066e248e1fb6060L,0xf1149e8c16f54860L,0xf69871601ff114dbL,0x398e76d56231ae5fL,0xd5693ab6ec8dc6c5L,0x1ffd15114b60a0e7L,
            0x09a76c584dad66b3L,0x0c6afd486055f404L,0xf88a790242283063L,0xe295d3c212bc5333L,0x797dad444df5268fL,0xa6e0ada539995e5bL,0xe2c1e741929ea20dL,0x45fd45de9e8ed97cL,
            0xd11f725ef67b6694L,0x826387736eaa03daL,0x237e495ffecbcae2L,0x502f2ad32286752dL,0x8e601fb6bfb0ef65L,0xcb797fbdd63411d1L,0x09173e7c913c54d3L,0x917de53d296459c9L,
            0xeb02f3bffde13cb6L,0x6ff9ad95d2ffc98bL,0x322bd842a336f44cL,0xd9bfcf701f92fa1fL,0x338363ecf290730fL,0xf5b92a1643ba20edL,0xc944a4a2b5231dfcL,0x729b617948e449aeL,
            0x0762b294313e9f68L,0x0672c89596442b9eL,0xc3dd576ceaec9782L,0xf438e904b71b7e10L,0xb687ecbacf5f686bL,0x1794d2e417012addL,0x41e44b9ea4f858feL,0xe09e0a96cde2b41eL,
            0xbacef461d073d79bL,0x882a76344582e532L,0x1060b3bda6313f9aL,0xdce480e39c97bfccL,0x23436b9ff8bceff0L,0x8f34265a6c4baf29L,0xa50b976a30d216a5L,0x029fb807565bdbc4L,
            0x8261a618a18ca76aL,0x03bd21c14330a6d3L,0x85128d3bdef6a72cL,0x23d9898757fe6abdL,0xccb173caf8e73928L,0x613697377148bb79L,0x089c6c63ea7a238bL,0xb18915eff941463bL};
    public static final long[] WHITE_BISHOP_HASH = {
            0x249d407c1de65d5aL,0x81bf46ca39bf73bdL,0xfce4cb40f85c4325L,0x93ff759734dcdb3bL,0x7fade9fe5fdbf0d1L,0xd0866d4b42c88bfbL,0x4d382d50bc711325L,0x7943319dccb99df7L,
            0x78e69bf9017007ddL,0x0bf6f4a11f980cc6L,0x7ce424c2f58b4675L,0x92071dd37f6079cdL,0x621470d816cf0ecaL,0x0ecfca97988a469dL,0xb9e2d488285e6e91L,0x57f9e1f1b33d43d7L,
            0xdcadb358038263c5L,0x50d5c534c9c08bffL,0x00c3cfe8227425f0L,0x93aa88b1a6829575L,0x43f7b6bf4e0235a9L,0x637ca1ffb75613acL,0x0e73e561f560f24bL,0x6241733aa56b15ddL,
            0x91bb952cc99b6524L,0xae84304550f1284aL,0xae5b67fbbf7d8a45L,0x8395f52c09e7855aL,0xbe9ccbd536874d32L,0x18351ee1f933313bL,0xf3706a17837d6759L,0xb42e774a58794a98L,
            0x8561270d1e0d054bL,0x215f1fe5b0537e55L,0x3370d7dc0a69bacbL,0x82282da7d1be5b34L,0x18c4eb85376fb755L,0xc7bdcee73b41f24dL,0xb2f2c8781a9484e1L,0xe4f60d05410ff1f4L,
            0xada779d46feffb12L,0x35f426be73ad8183L,0x20b1788c40626e0bL,0xdfa5ae0708c5c7a1L,0xf6a7ab41d86ff35bL,0x388e16da8e9bc526L,0x70fdf9b9ebe437d7L,0x6e57da3ba95857deL,
            0x98f124f5f96d6734L,0x76589c2d8cbb4d0eL,0x66ccaa78fd19fc83L,0xb9fbc3759c50b5b5L,0x8a770408e366f18eL,0x62f84677dabf97d5L,0xa5530472b5fb8442L,0xc052b1921477ce2aL,
            0x9fc9590d9e507270L,0x79e16a5e60d6fa31L,0xdfc50f08cd555bf5L,0x87295934250bed6fL,0x4435c3600359ae65L,0x7c316cb7de78d35fL,0x12280c571f0822aaL,0xa065b6cfca0e77d6L};
    public static final long[] WHITE_PAWN_HASH = {
            0xa3705b9baa147ad8L,0x7c6e19a9a49a3baeL,0x0e8699f026f07d92L,0xce586a0cfa68b2c2L,0xdad54468e14198d8L,0x6ed7728a44d35b5bL,0x55fb42756555a001L,0x0b5ecb5a9079fbeeL,
            0xeb1fa72f446281ebL,0x98dbd885c519d411L,0xff031a04a95ecf54L,0xd71e539770ef7c66L,0xfb376301c4d95d12L,0xfbd336b85b4adcedL,0x421fd40cd0002f7eL,0xbc2c56203121cae2L,
            0xcad84539a32a0ed0L,0x1e84a6b105688b52L,0x44f8ca11e48797d9L,0xc004f5f75bd2b06bL,0x3634dbca3438059cL,0xd0c4dbfa42648779L,0xb6d5f6a13d51de6cL,0x3ece5916c0ec3eabL,
            0x3682b3594ccd508bL,0x42dfad786b6ab9fcL,0x552b2386e5604565L,0x8081beef1e73a2b9L,0x9573706876ed9459L,0x927ef2d4f10b9dfdL,0xf38d2e3624dd517aL,0x1b18e7d9d3e8372bL,
            0x439e32efd21c82fbL,0x482932605cfbad9bL,0x7f3b9f1a1bb1948fL,0x481701a0e2bd26baL,0x8127640e19515082L,0x2caf85320815705bL,0x131d90b477b6c19cL,0x24e98f3bc40c1fb0L,
            0x914e2ecadebd94a5L,0x8220c71ae9e91414L,0x1f55e9115759a6daL,0xc947441d64c3a5e1L,0x310bf54e4830a706L,0xae6d8a6290d192daL,0xfaf234cce0b74cd7L,0x49a882cf0c4a2d39L,
            0x7226299760036b89L,0x189bededaadc94f2L,0x97072a4c4af1aee1L,0xc50374b71fb40c47L,0x7bd83c1b94723968L,0x11aa29a0df7d7fd7L,0x03bba6f73d82d2d8L,0xef4047648354b541L,
            0x3f2881d7a81d2b11L,0x335fb6ab5a8748edL,0xa0a31f4a0863f5d6L,0x8a175715ed15db3fL,0x6f823952b8ed6aa2L,0x3e0d06aeb6117d92L,0xea35811fee02aa7fL,0x67160de5059d45baL};
    public static final long[] BLACK_KING_HASH = {
            0x78faf6d59d1673c8L,0xcfd7fa4df2a1922dL,0x42be6ed3fde16db3L,0xb5fd6a856308061aL,0xd6624f8f95e4cb0bL,0x7afda7972df89738L,0xe442581025d87d00L,0x7184b00cd7bf1de0L,
            0x3aa7f1bfa4baad1fL,0x23ea07ccdd49e87bL,0xa3055c43978a37baL,0xe8d0a4b82201872fL,0xc9a197486b13acbdL,0xfefaa014cb05c524L,0x5f21f30e889d6525L,0x417ac44763c8b1ffL,
            0xa70782c3384f933dL,0xe2cf7c590666116bL,0x8c7f6aad906669d5L,0x142d0b430022350fL,0x1c3714d36da9945bL,0x586d329404af7dd7L,0x867e3da83d1f9ae8L,0xd386842f66f7d9ebL,
            0x8d348e2103eeb8efL,0x6a35939fbacd10c2L,0xbdb9a5a6ac1d1dddL,0xa119417c7be4583fL,0x803bfe84858d36a1L,0x434fa0d13e1d1b56L,0xc42b9d8453deadb7L,0xd37db2aa109aaf86L,
            0xae38421db6387fb7L,0xc745dba3c4f73e29L,0xfbd3707d63fbc33cL,0x22ca61b7c2613f9bL,0xcc1f3e7b3a39e8b0L,0x0953ac29f713aaf7L,0xc0f3338d263d3729L,0xa643d9a3a31a5695L,
            0xe4c6c8d1d5eaa546L,0xb7b452e48f7bad5fL,0xab4287f56139a8a3L,0x61d39660e0c69b2dL,0x7213a454a9a05601L,0xf7286a1125e4bdd7L,0xf052b44a944a45cbL,0x689dc7fbaf842a3bL,
            0x7afbb1e0a73fe208L,0x1a0f4544614c5371L,0xed1fe36b900e2c33L,0x3266449d03b65788L,0xae0f716b00b7fc0eL,0x1bd0aded116386f8L,0x1607364f81d7a20bL,0x0e6eb51a0b5c8fccL,
            0xc55ccc96d22c42b7L,0xca2d062b30a173b3L,0x7e59965b2f84b46eL,0xdfc84bc68f539425L,0x5716e174ba0634d7L,0xe3495c04f068c413L,0x9acbf1e4dc54c683L,0xed73a473b512810fL};
    public static final long[] BLACK_QUEEN_HASH = {
            0xa1308c1016b11de8L,0xb0f699e57dc13eedL,0x44f9e44656672054L,0x12f8e602dc6eb135L,0xc61e76c727472f1fL,0xc2c49c4c7b702296L,0x55de25eb75e436c7L,0xd2a5bb684bd50e42L,
            0x0ad179320668cf4dL,0xdac48102b729ed6cL,0xd5a5766aa095e167L,0xefb405f645694093L,0x9b9967d91daa4d47L,0xc6df842b9caece6cL,0x3c107fa4ae1b0f80L,0x87e032b8412afcc2L,
            0xa320efc2f386bd1aL,0xbb6837e158bffd75L,0x2c0877d8740e1eddL,0xcbd69f548e206580L,0x12668c8c3f693652L,0xc8b169c4d96663a6L,0x007def5f19b164c6L,0x2598dc49f411e613L,
            0x767d6da1f0ce6b51L,0x2d6724225144c80eL,0xa1bc8c66e927715aL,0xd51f9eb5a3d379e9L,0x69b0351b0bc74386L,0x515e721d1eb4e456L,0x5f9389c4f02ddebfL,0xfe156a91fe0aefd8L,
            0x352ed1ab0efc82f4L,0x67e536d19f87c184L,0x09f92be50087ada2L,0x1cffc97e8c332b1bL,0x9b3188330dc69cdbL,0xb739214ae4c1537bL,0x3215783d68e3625eL,0x71b2dd4b14a08e40L,
            0x40467b9e3c04c3bcL,0x5be988d24ebf3b8eL,0x2163934823d9ceeeL,0x3c4a8bd934b200eaL,0x667626365a181744L,0x6b6ed562fa9c83e0L,0xcc401b138b3f99b9L,0x7ea9e51c0423b07dL,
            0xb4a4292d41355404L,0xb00780b15a14c4cdL,0x1958ebf8ec799fc2L,0x0f1108fb1b24cb6fL,0xff3196b7f22b4393L,0xc476444cec2088bcL,0xb57b302c5c96011aL,0xff894237db20cd72L,
            0x61fbe2c5e18a591aL,0x148304e8ab5109e5L,0x4c6dffbc2d5be314L,0x6bb2dc5694dfa0a4L,0x6f6816f94142624fL,0x5cad8f9abadf3a48L,0x4ba563706d8c36a8L,0x4e2a4e03dbae4b94L};
    public static final long[] BLACK_ROOK_HASH = {
            0x4d910db77ed7b96eL,0x47b645242c5b5b0aL,0xed5be4eb54c4724bL,0x2b3a6e4d59e7808fL,0xc47510b6c96e2723L,0x743ab88107fc70c9L,0x96210d4a92158c60L,0x23e2b90311b63546L,
            0x2e024faac9081eedL,0x29b9cb7c507766b9L,0xd4ed86e759f91721L,0xd91f43980a3a0edbL,0xe1f233995e260814L,0x6f1e6f8a51f50c21L,0xc9df2d26fa1cd5c6L,0x3c7929094b08b6f4L,
            0x2e1aebe1b4d9023dL,0x2b2700e1ad5919f3L,0xf4f0d313f3207c3dL,0x1d41a374526e97d5L,0x64a6112ea11a0523L,0x865685564538e062L,0x271936ec2314cebbL,0x29e13e849301f96eL,
            0x0b0616a8135c9d2fL,0x02ab2e776b61e4d9L,0xc92b70894c9e8542L,0x88f94b9caa061b20L,0xab3cff33b5d5ad9cL,0xe26390eaf118c1c5L,0x4370f0e522bfcd74L,0xa30a34483e17a5d9L,
            0xaba395efba894164L,0x609e61236fb5d95bL,0xa58f128ab8b4904eL,0xb2dea33d5345766aL,0xdd6e37e2d817fe30L,0x95b922035e4300a1L,0xa3e59775ce67c91eL,0x39caeb604c866d9cL,
            0xb8f60b89482536a4L,0xb4f0558347baef58L,0x56a7fdf7f34f1affL,0x53e83c15238251b7L,0x928435ab38791896L,0x9704c1267132c820L,0xa6a0d33e488a22f0L,0x99d5f4422b3ec1a6L,
            0xcc423cfbf9aff183L,0xa9d99fc058a1a1c9L,0x996f9acfdc533128L,0x9af4d98505b1962bL,0xc3b517c1564e1146L,0xf6ca40fbc4401bfaL,0x861ae64024c421ecL,0x79b7069e643d3d84L,
            0xa59b38f91d36adebL,0x6b585ab21a731096L,0xd722cf550855f9d1L,0x3d2d8aa9fc07151bL,0x4479b32f6386713eL,0x2ba0d703df67cd28L,0x3c8b0ddef079f648L,0x93effdc6d68f0786L};
    public static final long[] BLACK_KNIGHT_HASH = {
            0x73f23d5f3f926ba9L,0x79f171860b27a9bcL,0x7707ca02479df5c1L,0x56fbfeac273ef0c4L,0xee2b358b2dc0506fL,0x7f1af04296d74a44L,0x5f32f9b10b351b37L,0x78ef72eb223bd7d8L,
            0x8758b73f524a8278L,0xdc6e57290ac3c42dL,0x3539b8f8eb2318f7L,0x9e025b863433403dL,0x9cd034a66ef6268dL,0x224cf18db41b6760L,0xcd56e301622f0612L,0x8c0d082a999aa423L,
            0xa55c5719252fba4bL,0xa5f9b8b0300f68c4L,0x78e2086e03a7c9aaL,0x503ea2c868a4dac2L,0x9939a07cc207b729L,0xbe0ff25f2a363744L,0x931e1bee4ac7fbf9L,0x092d5d0125690328L,
            0x0c386800b08233e3L,0x7d229607b452762bL,0xf763b129ef31b57fL,0x4f1e51d27978fea4L,0x85cc864e9ff87a3bL,0xc9c794350e0c0326L,0x9260ae4898d343e2L,0xadc455bbfece4bc9L,
            0x670644122c539efbL,0xcae9563c144d5fc2L,0xcb405c83954d41daL,0xcab6546b2c62be24L,0xd9654df8a08b3f7eL,0xc13dd4c8fd9fdb67L,0x6af8d66745b163a2L,0x3d123a7dbfa11873L,
            0xfb220a862fe29743L,0x1edb5f977c965b43L,0x39a2dbfa42e5fc17L,0x9d272e8587a00e7bL,0xff770f78ffccd2fdL,0xa96f0be20ac5316fL,0xb0a256252d575c07L,0xc0fe17fce7c8e86bL,
            0xbe0eedf079c3f24fL,0x1c1fbef0273b6c9dL,0x5c6ca0b8c9b46eacL,0xc02aabaa9e627a9cL,0xea7de12f0e9c9803L,0xa521aba10ea33e22L,0x48701071f5d795e2L,0xf2395d445abd32e9L,
            0x0f533e87105147d5L,0x0e6a2e58348768caL,0xeaefd853555e76c9L,0x7660538ae545c2b4L,0xa0b85bca78ad41ccL,0xf4f2a3220a65cdfcL,0x3fe17df72a0fc80bL,0x92a0fca346b8fa60L};
    public static final long[] BLACK_BISHOP_HASH = {
            0x0833f6ce2d6253c2L,0x859a308dd7c6f93bL,0x804d8c336c1c64fdL,0x818fd2e3a34f835dL,0x6d9c6e3f2a0d3467L,0x556eca7f7e46ef14L,0x31865cbffc1c6364L,0x9207833308e37633L,
            0x1d13637dbc110f8dL,0x3a2bb59595a67b71L,0xb434bc1333d036c6L,0xfc58bfe10bf71210L,0x6e88795f208d0c3eL,0x0e09cca3729d0070L,0x432155fa4c0e4e27L,0x1627cd02dd7f0ee1L,
            0x486ccf6c4b7ddfe9L,0x157243745a95a844L,0x3b455125f1c7850bL,0x2abd205213d89b79L,0xbea59f9c0b5ef032L,0xe2c4a1baa808cb40L,0x945a625a9260a56bL,0x6a6b534a55b5b9d4L,
            0x43e3ddd728e4e09eL,0xa2dd4a715d221f82L,0x3d166a0075a3e0a6L,0xab2fce91c0ca2bd1L,0x9d5b59770c08222fL,0xd6d08185df92fa12L,0x795257020ba13663L,0x3376c81262fa2127L,
            0x8e8cc993fd7cabecL,0x50433f9675befdb1L,0x4d41a7c2dbcb5c31L,0x23e356a6591a0365L,0x17bffa0b9521be99L,0xd53c28e21a73c2bbL,0xf381645968c4da64L,0x55e7c421ef61948aL,
            0x07219775defb1326L,0x0f576eda6f7f8b57L,0x3302fc781a9ae0adL,0x4eb24d2cf9fe0ca8L,0x207a3032a155ca71L,0x610aca308b8ed5e0L,0xb5ac6e2ae928892eL,0x152520d716fd78ecL,
            0x47ab005daed9b995L,0x3d1f25b08ea4d801L,0xfd622e7a3f1d5ed0L,0xf1cdaea3a18de833L,0xfaae019fee2d67f6L,0x5225af236340a00cL,0xfb45e996b5aa4bb2L,0xdc8f317f601226d0L,
            0xecd370f24a423e20L,0x625b70481e6df2fdL,0x19783741d11785faL,0xe710245fe515a86fL,0x61357476efef8072L,0x9336dbcabd87c015L,0x49e4f72a2349e348L,0x51124f33c93dd2a2L};
    public static final long[] BLACK_PAWN_HASH = {
            0x8b74d56cfd8158b5L,0x37fc96b12ddb7b36L,0x4411935b911f9040L,0x4290100cf2dcef08L,0x8df62b529a9f5a5aL,0x57e710cdc3f52f64L,0xd3c1cc67c5540908L,0xd441bf6c5abb0479L,
            0x37896e05e2d15734L,0xd3dd21d8db01fc5dL,0xd2d1bfb9613bbc4dL,0x1263195da0919c19L,0x41293c9d9f09e956L,0x85cd22b5bcf7b5c8L,0xb4d5b0c4f47788b7L,0xda0117a4bbb852cfL,
            0x38d71fa987c99dc0L,0x7adef43ffc04006fL,0x4256e5917fcef918L,0xcafb349bdf335631L,0x13bbce683b5f43f2L,0xd70a119367539910L,0x135ee9fd8be6ba8cL,0xf8c25b6414a020cfL,
            0x9116805a087f3f05L,0x3e027c61d8a46136L,0xb8971d3fb5b8f8a9L,0xbe044785a0b322c0L,0x9e95d8fe4a88206bL,0x975322b92475d01eL,0x6883832f522be9dcL,0x5d929b01d583ccc0L,
            0x58ee39c70eeb3513L,0xe42e873db24138d8L,0x0c508478f5ad10b9L,0x0798ef0caab63127L,0x735f2e01b6182c04L,0xb4c88e4f3221e3cfL,0xeb8f2c3dc9cf38e2L,0x76cb377e371a850bL,
            0x7fe9d146ea3347d6L,0xe0dc9d1132b5f9aaL,0x5be5dfd5c398d88cL,0xc3672fa591e948daL,0xc755d37ffdc76f54L,0x6f6bf19ab600785cL,0xd92de9ead73fe593L,0xa469ef81b89c273fL,
            0x3dcbd37dbfc47157L,0x6ac814b2f81990e7L,0x14004fb4963d1149L,0xc46d8497472f44caL,0x25812ef68b53138aL,0x128b549ba467f17cL,0xb2eb6e734e127efbL,0x9d508ed766955587L,
            0x59a569a34203180eL,0x0ef669c2c66aa364L,0x174129d11b0c3e7cL,0xbd4a5eed1694fd8bL,0xe95f21db835451e0L,0x4f7af461eb42d65cL,0x6e2df9b4eb9e5d1dL,0xab655ac1b187aed3L};

    public static final long WHITE_TO_MOVE_HASH = 0x5590e8c4a4ef01c1L;
    public static final long[] CASTLE_RIGHTS_HASH = {0x12e1ec96d87ac00cL,0x7b324b0deb17744eL,0xb497403011703c41L,0x2ebb30ba17b47079L};
    public static final long[] EN_PASSANT_COLUMN_HASH = {0x53060784ad20a4a8L,0x92459a124efc3ce0L,0x315589004b4af4d8L,0xd1188432a1deae28L,
            0xc80173e3c7818aa3L,0xc548fa50c1617d5bL,0x7fd7c06f024ccc07L,0x0b182d89ec268729L};


    public static final long A_FILE = 0x8080808080808080L;
    public static final long B_FILE = 0x4040404040404040L;
    public static final long C_FILE = 0x2020202020202020L;
    public static final long D_FILE = 0x1010101010101010L;
    public static final long E_FILE = 0x0808080808080808L;
    public static final long F_FILE = 0x0404040404040404L;
    public static final long G_FILE = 0x0202020202020202L;
    public static final long H_FILE = 0x0101010101010101L;

    public static final int[] QUEEN_TABLE = {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20};
    public static final int[] ROOK_TABLE = {
            0,  0,  0,  0,  0,  0,  0,  0,
            5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            0,  0,  0,  5,  5,  0,  0,  0};
    public static final int[] KNIGHT_TABLE = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50};
    public static final int[] BISHOP_TABLE = {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20};
    public static final int[] PAWN_TABLE = {
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0};
    public static final int[] KING_MIDDLE_TABLE = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 30, 10,  0,  0, 10, 30, 20};

    public static final int[] KING_END_TABLE = {
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-10,-20,-30,-30,-20,-10,-50};
}
