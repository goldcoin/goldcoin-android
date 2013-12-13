package com.google.goldcoin.core;

import java.math.BigInteger;
import java.util.Date;
import java.util.Vector;

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

    public static final int PROTOCOL_VERSION = 60014;
    public static final int MIN_PROTOCOL_VERSION = 60005;
    public static final int Port    = 8121;
    public static final int TestPort = 18121;

    public static final int AddressHeader = 32;
    public static final long PacketMagic = 0xfdc2b4dd;
    public static final int newDifficultyProtocolFork = 21000;
    public static final boolean usingNewDifficultyProtocol(int height)
    { return height >= newDifficultyProtocolFork; /*&& height < novemberFork;*/}

    public static final boolean usingMedianDifficultyProtocol(int height)
    { return height >= novemberFork;}

    public static final boolean usingMedianDifficultyProtocol2(int height)
    { return height > novemberFork2;}

    public static final long julyFork = 45000;
    public static final long novemberFork = 103000;
    public static final long novemberFork2 = 118800;
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

    static class blockInfo {
        long timeStamp;
        String peerIp;

        blockInfo(long t, String pip)
        {
           timeStamp = t;
           peerIp = pip;
        }
    };

    static Vector<blockInfo> lastFiveBlocks = new Vector<blockInfo>(5);

    //Schedule CheckPoint Block
//-1 if no checkpoint is to be done.
    static int checkpointBlockNum = -1;

    //Delay block-transmittance by 14 minutes flag (51% defence)
    static boolean defenseDelayActive = false;
    static long defenseStartTime;

    static boolean Defense(Peer peer, Block block) throws VerificationException
    {
        return true;
    }

    static boolean Defense1(Peer peer, Block block) throws VerificationException
    {

        blockInfo newBlock = new blockInfo(block.getTimeSeconds(), peer.getAddress().toString());
        if(lastFiveBlocks.size() < 5) {
            //Add this block's information to the last five blocks

                   // pblock->GetBlockTime(),pfrom?pfrom->addr.ToString():"local");
            lastFiveBlocks.add(newBlock);// push_back(newBlock);
            //printf("====BLOCK's Recorded START====\n");
			/*for(int x = 0; x < lastFiveBlocks.size(); x++) {
                printf("Time:\n");
                printf("%lld\n",lastFiveBlocks.at(x).timeStamp);
                printf("Qtime:\n");
                printf("%s\n", QDateTime::fromTime_t(lastFiveBlocks.at(x).timeStamp).toString().toStdString().c_str());
                printf("IP:\n");
                printf("%s\n",lastFiveBlocks.at(x).peerIp.c_str());
			}*/
            //printf("====BLOCK's Recorded END====\n");
            //-- akumaburn (GoldCoin Lead Dev -Sept 2013)
        } else {
			/*printf("Stage 0 Entered\n");
			printf("Our current time is:\n");
			printf("QDateTime::currentDateTime()\n");
            printf("The block time of the current block is: \n");
            printf("%s\n",QDateTime::fromTime_t(pblock->GetBlockTime()).toString().toStdString().c_str());

            if(pfrom) {
                printf("The peer ip of the current block is: \n");
                printf("%s\n",pfrom->addr.ToString().c_str());
            }*/


			/*printf("====BLOCK's Recorded INSTAGE START====\n");
			for(int x = 0; x < lastFiveBlocks.size(); x++) {
                printf("Time:\n");
                printf("%lld\n",lastFiveBlocks.at(x).timeStamp);
                printf("Qtime:\n");
                printf("%s\n", QDateTime::fromTime_t(lastFiveBlocks.at(x).timeStamp).toString().toStdString().c_str());
                printf("IP:\n");
                printf("%s\n",lastFiveBlocks.at(x).peerIp.c_str());
			}
			printf("====BLOCK's Recorded INSTAGE END====\n");*/

            //We have 5 blocks
            //First we check whether or not this peer is the same as the peer that transmitted the last five blocks
            if(lastFiveBlocks.elementAt(0).peerIp.compareTo(peer.getAddress().toString()) == 0 &&
                    lastFiveBlocks.elementAt(1).peerIp.compareTo(peer.getAddress().toString()) == 0 &&
                    lastFiveBlocks.elementAt(2).peerIp.compareTo(peer.getAddress().toString()) == 0 &&
                    lastFiveBlocks.elementAt(3).peerIp.compareTo(peer.getAddress().toString()) == 0 &&
                    lastFiveBlocks.elementAt(4).peerIp.compareTo(peer.getAddress().toString()) == 0) {
                //printf("Stage 1 Entered\n");
                //-- akumaburn (GoldCoin Lead Dev -Sept 2013)
                //Make sure not to detect our own blocks..
                //Unless we've hit 100K, in which case we will stop accepting blocks in general to avoid triggering this defence on other nodes
                //	if(!lastFiveBlocks.at(0).peerIp.compare("local") == 0 || nBestHeight > 100000) {

                //If so then we go on to check the block's time stamp
                //First we check whether it is within 10 minutes of the first block in our array
                //if(QDateTime::fromTime_t(lastFiveBlocks.front().timeStamp).secsTo(QDateTime::fromTime_t(pblock->GetBlockTime())) < (60*10))
                if((block.getTimeSeconds() - lastFiveBlocks.elementAt(0).timeStamp) < (60*60)) {   //TODO: 60*10
                    //printf("Stage 2 Entered\n");

                    //Now we check whether the first block we recorded was within 10 minutes of our time
                    //Or if we are past block 100K and it should work anyhow...
                    Date currentDate = new Date();
                    //if((QDateTime::fromTime_t(lastFiveBlocks.front().timeStamp).secsTo(QDateTime::currentDateTime()) < (60*10)) || nBestHeight > 100000) {
                    if((currentDate.getTime() - lastFiveBlocks.elementAt(0).timeStamp) < (60*60) /*|| nBestHeight > 100000*/)  //TODO:10*60
                    {
                        //printf("Stage 3 Entered\n");

                        //If so then we check if the current block is within 2 minutes of our time
                        //We don't want to ban peers for transmitting old blocks that were accepted prior to this change!
                        //if((QDateTime::fromTime_t(pblock->GetBlockTime()).secsTo(QDateTime::currentDateTime()) <= (60*2)) || nBestHeight > 100000)
                        if((currentDate.getTime() - block.getTimeSeconds()) < (60*2) /*|| bBestHeight > 100000*/)
                        {
                            //printf("Stage 4 Entered\n");
                            //We must delay the transmittance of the next block(good or bad) for 14 minutes,
                            //in order to not get banned ourselves! (there is a small probability we will also mine/receive a block whilst
                            //the 51% attack is going on that is not from the 51%er)
                            //Delay block-transmittance by 14 minutes flag (51% defence)

                            //The app does not transmit block information, no need to delay

                            defenseDelayActive = true;
                            defenseStartTime = currentDate.getTime();

                            //If the block being accepted isn't local
                            if(true /*lastFiveBlocks.at(0).peerIp.compare("local") != 0*/) {
                                //Now we schedule a checkpoint 12 blocks from now!
                                //TODO: checkpointBlockNum = nBestHeight + 12;

                                //If so then we ban them locally for 4 hours
                                //if (pfrom)
                                //    pfrom->Misbehaving(50);
                                //error("\n ProcessBlock() : 51% attempt detected and TERMINATED O_O \n");
                                throw new VerificationException("51% attempt detected and TERMINATED O_O");
                                //return false;
                            } else {
                                //Otherwise we simply delay our own generation of blocks for 10 minutes
                               // return error("\n ProcessBlock() : Mining too fast! 10 minute delay active! Own 51% of the network.. SLOW DOWN! \n");
                            }

                        } else {
                            //Otherwise we simply ignore this event
                        }
                    }
                }
                //}
            }
            //We want to clear the vector to allow for the next five blocks to be checked
            //lastFiveBlocks.clear();
            //lastFiveBlocks.clear();
            lastFiveBlocks.remove(0);
            lastFiveBlocks.add(newBlock);
        }

        //stop accepting blocks.. including our own, for ten minutes
        //to avoid a "ban-chain"
        if(defenseDelayActive) {
            Date currentDate = new Date();

            if((currentDate.getTime() - defenseStartTime) < 600) {
                throw new VerificationException("51% defence delay active.");
                //return false;//error("\n ProcessBlock() : 51% defence delay active. \n");
            }
            if((currentDate.getTime() - defenseStartTime) >= 600) {//If 10 minutes has passed
                defenseDelayActive = false;
            }
        }

        //throw new VerificationException("51% defence delay active.");
        return true;
    }
}
