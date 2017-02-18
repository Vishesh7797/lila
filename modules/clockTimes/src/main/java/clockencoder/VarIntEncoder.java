package org.lila.clockencoder;

public class VarIntEncoder {
    static int zigzagEncode(int n) {
        return (n << 1) ^ (n >> 31);
    }

    static int zigzagDecode(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public static void encode(int[] values, BitWriter writer) {
        for (int n : values) {
            n = zigzagEncode(n);
            do {
                if ((n & ~0x07) == 0) {
                    writer.writeBits(n, 4);
                } else {
                    writer.writeBits((n | 0x08), 4);
                }
                n >>>= 3;
            } while (n != 0);
        }
    }

    public static int[] decode(BitReader reader, int numMoves) {
        int[] values = new int[numMoves];

        int[] numBuffer = new int[16];
        
        for (int curMove = 0; curMove < numMoves; curMove++) {
            int idx = -1;
            do {
                numBuffer[++idx] = reader.readBits(4);
            } while ((numBuffer[idx] & 0x08) != 0);

            int unsignedNum = numBuffer[idx--];
            for (; idx >= 0 ; idx--) {
                unsignedNum = (unsignedNum << 3) + numBuffer[idx] & 0x07;
            }

            values[curMove] = zigzagDecode(unsignedNum);
        }

        return values;
    }
}