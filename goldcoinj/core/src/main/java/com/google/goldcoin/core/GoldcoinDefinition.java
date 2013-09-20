package com.google.goldcoin.core;

import java.math.BigInteger;

import static com.google.goldcoin.core.Utils.COIN;

/**
 * Created with IntelliJ IDEA.
 * User: Eric
 * Date: 8/13/13
 * Time: 7:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoldcoinDefinition {

    //Original Values
    public static final int TARGET_TIMESPAN_1 = (int) (3.5 * 24 * 60 * 60);  // 3.5 days per difficulty cycle, on average.
    public static final int TARGET_SPACING_1 = (int) (2.5 * 60);  // 2.5 minutes per block.
    public static final int INTERVAL_1 = TARGET_TIMESPAN_1 / TARGET_SPACING_1;
    //21,000 Fork
    public static final int TARGET_TIMESPAN_21000 = (int) (7 * 24 * 60 * 60) / 8;  // 0.875 days per difficulty cycle, on average.
    public static final int TARGET_SPACING_21000 = (int) (2.5 * 60);  // 2.5 minutes per block.
    public static final int INTERVAL_21000 = TARGET_TIMESPAN_21000 / TARGET_SPACING_21000;
    //45,000 Fork
    public static final int TARGET_TIMESPAN_45000 = (int) (2 * 60 * 60);  // 2 hours per difficulty cycle, on average.
    public static final int TARGET_SPACING_45000 = (int) (2.0 * 60);  // 2.0 minutes per block.
    public static final int INTERVAL_45000 = TARGET_TIMESPAN_45000 / TARGET_SPACING_45000;

    public static final int GetInterval(int height, boolean testNet) {
        if(height < newDifficultyProtocolFork)
            return INTERVAL_1;
        else if(height >= newDifficultyProtocolFork && height < julyFork)
            return INTERVAL_21000;
        else //if(height >= julyFork)
            return INTERVAL_45000;
        //else return -1; //error
    }
    public static final int GetTargetTimespan(int height, boolean testNet) {
        if(height < newDifficultyProtocolFork)
            return TARGET_TIMESPAN_1;
        else if(height < julyFork)
            return TARGET_TIMESPAN_21000;
        else return TARGET_TIMESPAN_45000;
    }

    public static final int MAX_MONEY = 121399000;
    public static final String MAX_MONEY_STRING = "121399000";

    public static final BigInteger DEFAULT_MIN_TX_FEE = BigInteger.valueOf(10000000);

    public static final int PROTOCOL_VERSION = 60003;
    public static final int Port    = 8121;
    public static final int TestPort = 18121;

    public static final int AddressHeader = 32;
    public static final long PacketMagic = 0xfdc2b4dd;
    public static final int newDifficultyProtocolFork = 21000;
    public static final boolean usingNewDifficultyProtocol(int height)
    { return height >= newDifficultyProtocolFork;}
    public static final long julyFork = 45000;
    public static boolean hardForkedJuly = false;
    public static final int GetBlockReward(int nHeight)
    {
        int COIN = 1;
        int nFees = 0;
        int nSubsidy = 50 * COIN;//First block is worth a ceremonial 50 coins.

        if(nHeight > 0 && nHeight <= 200)
        {
            nSubsidy = 10000 * COIN;
        }
        else if(nHeight > 200 && nHeight <= 2200)
        {
            nSubsidy = 1000 * COIN;
        }
        else if(nHeight > 2200 && nHeight < julyFork)
        {
            nSubsidy = 500 * COIN;
        }
        else if(nHeight >= julyFork && nHeight <= 26325000)
        {
            hardForkedJuly = true;
            //nSubsidy = Calculate(400,julyFork,2,8,nHeight) * COIN;
            nSubsidy = (int)(50.0/(1.1 + 0.49*((nHeight-julyFork)/262800))) * COIN;
        } else {
            nSubsidy = 0;
        }
        return nSubsidy + nFees;
    }
}
