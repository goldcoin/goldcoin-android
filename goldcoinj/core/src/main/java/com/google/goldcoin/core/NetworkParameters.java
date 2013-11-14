/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.goldcoin.core;

import com.google.common.base.Objects;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.google.goldcoin.core.Utils.COIN;
import static com.google.common.base.Preconditions.checkState;


/**
 * <p>NetworkParameters contains the data needed for working with an instantiation of a goldcoin chain.</p>
 * <p/>
 * Currently there are only two, the production chain and the test chain. But in future as goldcoin
 * evolves there may be more. You can create your own as long as they don't conflict.
 */
public class NetworkParameters implements Serializable {
    private static final long serialVersionUID = 3L;

    /**
     * The protocol version this library implements.
     */
    public static final int PROTOCOL_VERSION = GoldcoinDefinition.PROTOCOL_VERSION;

    /**
     * The alert signing key originally owned by Akumaburn.
     */
    public static final byte[] SATOSHI_KEY = Hex.decode("04bd513df05d93e939fbd367fe2874c967980530334f43d1aa998e67ae71c13ce4aab48202d64ed8ee78b217094f17a5ce633e1f444950830aacc730677ef992db");

    /**
     * The string returned by getId() for the main, production network where people trade things.
     */
    public static final String ID_PRODNET = "org.goldcoin.production";
    /**
     * The string returned by getId() for the testnet.
     */
    public static final String ID_TESTNET = "org.goldcoin.test";
    /**
     * Unit test network.
     */
    static final String ID_UNITTESTNET = "com.google.goldcoin.unittest";

    // TODO: Seed nodes should be here as well.

    // TODO: Replace with getters and then finish making all these fields final.

    /**
     * <p>Genesis block for this chain.</p>
     * <p/>
     * <p>The first block in every chain is a well known constant shared between all goldcoin implemenetations. For a
     * block to be valid, it must be eventually possible to work backwards to the genesis block by following the
     * prevBlockHash pointers in the block headers.</p>
     * <p/>
     * <p>The genesis blocks for both test and prod networks contain the timestamp of when they were created,
     * and a message in the coinbase transaction. It says, <i>"The Times 03/Jan/2009 Chancellor on brink of second
     * bailout for banks"</i>.</p>
     */
    public final Block genesisBlock;
    /**
     * What the easiest allowable proof of work should be.
     */
    public /*final*/ BigInteger proofOfWorkLimit;
    /**
     * Default TCP port on which to connect to nodes.
     */
    public final int port;
    /**
     * The header bytes that identify the start of a packet on this network.
     */
    public final long packetMagic;
    /**
     * First byte of a base58 encoded address. See {@link Address}. This is the same as acceptableAddressCodes[0] and
     * is the one used for "normal" addresses. Other types of address may be encountered with version codes found in
     * the acceptableAddressCodes array.
     */
    public final int addressHeader;
    /**
     * First byte of a base58 encoded dumped private key. See {@link DumpedPrivateKey}.
     */
    public final int dumpedPrivateKeyHeader;
    /**
     * How many blocks pass between difficulty adjustment periods. goldcoin standardises this to be 59 or 60.
     */
    public /*final*/ int interval;
    public final int getInterval(int height) { return GoldcoinDefinition.GetInterval(height, id.equals(ID_TESTNET));}
    /**
     * How much time in seconds is supposed to pass between "interval" blocks. If the actual elapsed time is
     * significantly different from this value, the network difficulty formula will produce a different value. Both
     * test and production goldcoin networks use 2 weeks (1209600 seconds).
     */
    public final int targetTimespan;
    public final int getTargetTimespan(int height) { return GoldcoinDefinition.GetTargetTimespan(height, id.equals(ID_TESTNET));}
    /**
     * The key used to sign {@link AlertMessage}s. You can use {@link ECKey#verify(byte[], byte[], byte[])} to verify
     * signatures using it.
     */
    public /*final*/ byte[] alertSigningKey;

    /**
     * See getId(). This may be null for old deserialized wallets. In that case we derive it heuristically
     * by looking at the port number.
     */
    private final String id;

    /**
     * The depth of blocks required for a coinbase transaction to be spendable.
     */
    private final int spendableCoinbaseDepth;

    /**
     * Returns the number of blocks between subsidy decreases
     */
    private final int subsidyDecreaseBlockCount;

    /**
     * If we are running in testnet-in-a-box mode, we allow connections to nodes with 0 non-genesis blocks
     */
    final boolean allowEmptyPeerChains;

    /**
     * The version codes that prefix addresses which are acceptable on this network. Although Satoshi intended these to
     * be used for "versioning", in fact they are today used to discriminate what kind of data is contained in the
     * address and to prevent accidentally sending coins across chains which would destroy them.
     */
    public final int[] acceptableAddressCodes;


    /**
     * Block checkpoints are a safety mechanism that hard-codes the hashes of blocks at particular heights. Re-orgs
     * beyond this point will never be accepted. This field should be accessed using
     * {@link NetworkParameters#passesCheckpoint(int, Sha256Hash)} and {@link NetworkParameters#isCheckpoint(int)}.
     */
    public Map<Integer, Sha256Hash> checkpoints = new HashMap<Integer, Sha256Hash>();

    private NetworkParameters(int type) {
        alertSigningKey = SATOSHI_KEY;
        if (type == 0 || type == 100) {
            // Production.
            genesisBlock = createGenesis(this);
            interval = GoldcoinDefinition.INTERVAL_1;
            targetTimespan = GoldcoinDefinition.TARGET_TIMESPAN_1;
            proofOfWorkLimit = Utils.decodeCompactBits(0x1e0fffffL);
            acceptableAddressCodes = new int[]{GoldcoinDefinition.AddressHeader};
            dumpedPrivateKeyHeader = 128;
            addressHeader = GoldcoinDefinition.AddressHeader;
            if (type == 100) port = 10333; //TODO::not sure what this is for
            else port = GoldcoinDefinition.Port;
            packetMagic = GoldcoinDefinition.PacketMagic;
            genesisBlock.setDifficultyTarget(0x1e0ffff0L); //good
            genesisBlock.setTime(1368560876L); //good
            genesisBlock.setNonce(3591624L); // good
            genesisBlock.setMerkleRoot(new Sha256Hash("a215e67ba165202f75b6458d22fedd1a3ec4f03449a4c6b2a4b8130bfebd3b15"));
            id = ID_PRODNET;
            subsidyDecreaseBlockCount = 840000;
            allowEmptyPeerChains = false;
            spendableCoinbaseDepth = 100;
            String genesisHash = genesisBlock.getHashAsString();
            checkState(genesisHash.equals("dced3542896ed537cb06f9cb064319adb0da615f64dd8c5e5bad974398f44b24"),
                    genesisHash);

            // This contains (at a minimum) the blocks which are not BIP30 compliant. BIP30 changed how duplicate
            // transactions are handled. Duplicated transactions could occur in the case where a coinbase had the same
            // extraNonce and the same outputs but appeared at different heights, and greatly complicated re-org handling.
            // Having these here simplifies block connection logic considerably.
            //checkpoints.put(91722, new Sha256Hash("00000000000271a2dc26e7667f8419f2e15416dc6955e5a6c6cdf3f2574dd08e"));
            //checkpoints.put(91812, new Sha256Hash("00000000000af0aed4792b1acee3d966af36cf5def14935db8de83d6f9306f2f"));
            //checkpoints.put(91842, new Sha256Hash("00000000000a4d0a398161ffc163c503763b1f4360639393e0e4c8e300e0caec"));
            //checkpoints.put(91880, new Sha256Hash("00000000000743f190a18c5577a3c2d2a1f610ae9601ac046a38084ccb7cd721"));
            //checkpoints.put(200000, new Sha256Hash("000000000000034a7dedef4a161fa058a2d67a173a90155f3a2fe6fc132e0ebf"));
            checkpoints.put(0, new Sha256Hash("dced3542896ed537cb06f9cb064319adb0da615f64dd8c5e5bad974398f44b24"));
            checkpoints.put( 1, new Sha256Hash("e39be079a4e57af79f63edb2726bdcb401ae520fa5b5328bbeab185b5b3d636e"));
            checkpoints.put( 50,new Sha256Hash("34b021706ae2b72e41d411a31ead78219087213a29fe338515926f055a4c5655")) ;
            checkpoints.put( 150,new Sha256Hash("c3565831a172cf737b0dd8bd47a81f2dfe99012c97362f397838e617d26ba99b"));
            checkpoints.put( 300,new Sha256Hash("52d9e6919828769b358db66f8793a06e5915948d0a0ee36f59465e91dabb97a6"));
            checkpoints.put( 500,new Sha256Hash("6ba9043e14339790c56181da3ef4d87ff58b8cd3c07b2d33e7d808e189136002"));
            checkpoints.put( 1000,new Sha256Hash("29607502895cf180b7b556f0bc70b2001eacb819be6b4be1e5f0092622e03cb3"));
            checkpoints.put( 6048, new Sha256Hash("e8eb14ac03f25fd52a46b51acba8af543f6b4baf1b85b62d6e6e09ba5f108ed7"));
            checkpoints.put( 12096, new Sha256Hash("d2bd87d677cced55584634d6d03434e041d5dbbaf5639c873a5f6a654d788ad1"));
            checkpoints.put( 16128, new Sha256Hash("290d05c1f5fb2cc75d5bc7657141e4e42d4ee7b4e78a73c96fac878a58d52057"));
            checkpoints.put( 18143, new Sha256Hash("996caca04cce6ffc7f1053abcd350742e2ecc3220663cfd3e3585e3442d7cf74"));
            checkpoints.put( 18144, new Sha256Hash("ed5124e191b92d5405374dc4018203a7e03f4251293b6d5f57a83d1e1ff4df30"));
            checkpoints.put( 20160, new Sha256Hash("e19b119f4a633d89320d502e7c05b88d083acdff3b4bd40efcdca54b25f6cb2c"));
            checkpoints.put( 20500, new Sha256Hash("23ab64ad71d7191c28010c7c1b0b35d32ac97ace893dbb20068a6abb617f80a4"));
            checkpoints.put( 24000, new Sha256Hash("c3cf2892cbaaf8b88565f027460bda831a428bf6ea76fafa870bf3586dd07c5f"));
            checkpoints.put( 26000, new Sha256Hash("906a76b3b36aa7f489ea0ee38c180d0eebaf489e4998e6aefa806fadc687e475"));
            checkpoints.put( 27000, new Sha256Hash("f7391f58e29d057f152b9c124af6153dadb62385d8728118e9cef728d9a4d16d"));
            checkpoints.put( 27000, new Sha256Hash("f7391f58e29d057f152b9c124af6153dadb62385d8728118e9cef728d9a4d16d"));
            checkpoints.put( 28000, new Sha256Hash("28adf712f2a7d9d7ab3836249c9e2beff8d0deb362a1991c61cb61c0fe9af10b"));
            checkpoints.put( 29000, new Sha256Hash("0aca7e1d7cebe224479db62d9887bba7e8dbf5cc295261b6b2e9b9bc76f58ab1"));
            checkpoints.put( 29000, new Sha256Hash("0aca7e1d7cebe224479db62d9887bba7e8dbf5cc295261b6b2e9b9bc76f58ab1"));
            checkpoints.put( 30000, new Sha256Hash("1ff80eac17ba7efc350d65d842cbedd5822b4bef3eae7b1c24424c0d5cc2af51"));
            checkpoints.put( 31000, new Sha256Hash("e9a24595526e9c67357e3a5962e8d489a867573eef1ea104de6be113d26512de"));
            checkpoints.put( 35000, new Sha256Hash("0d8f14bc84ed93490b8c2070de4b744085a4d661f7ef96c856fd32572bbd87fc"));
            checkpoints.put( 45000, new Sha256Hash("612461aaa0ca6a3bc07238ac86e67fa37ae1b8b083d0c1e23f396bbe5bd05896"));
            checkpoints.put( 50000, new Sha256Hash("e251895683ec1363344504b91d9899c29064afc786905c9a052d61ee88a95206"));
            checkpoints.put( 60000, new Sha256Hash("f03feaab75843a39be8cf0fbf8bdae3056aebd4817b89a99e4837db2bdd2659a"));
            checkpoints.put( 65000, new Sha256Hash("b635ce68527e8b777f68a71fe441faab285fa7aafd78259ddc24843539bba369"));
            checkpoints.put( 66000, new Sha256Hash("f619fc8b01c1aedcf4623cea7d85310db85174e27e1b3069dadf76e9bc2f6c99"));
            checkpoints.put(103000, new Sha256Hash("e41285ba8cd299b28f0dbfc60b28a9c3e5b6482079d4608ef0dad14390ce6da7"));

        } else if (type == 3) {
            //TODO::This code is not set up correctly for a goldcoin testnet
            // Testnet3
            genesisBlock = createTestGenesis(this);
            id = ID_TESTNET;
            // Genesis hash is 000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943
            packetMagic = 0xfcc1b7dc;
            interval = GoldcoinDefinition.INTERVAL_1;
            targetTimespan = GoldcoinDefinition.TARGET_TIMESPAN_1;
            proofOfWorkLimit = Utils.decodeCompactBits(0x1d00ffffL);
            port = GoldcoinDefinition.TestPort;
            addressHeader = 111;
            acceptableAddressCodes = new int[]{111};
            dumpedPrivateKeyHeader = 239;
            genesisBlock.setTime(1320884152L);
            genesisBlock.setDifficultyTarget(0x1d018ea7L);
            genesisBlock.setNonce(3562614017L);
            allowEmptyPeerChains = true;
            spendableCoinbaseDepth = 100;
            subsidyDecreaseBlockCount = 210000;
            String genesisHash = genesisBlock.getHashAsString();
            //checkState(genesisHash.equals("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943"),
            //        genesisHash);
        } else if (type == 2) {
            genesisBlock = createTestGenesis(this);
            id = ID_TESTNET;
            packetMagic = 0xfabfb5daL;
            port = 18333;
            addressHeader = 111;
            interval = GoldcoinDefinition.INTERVAL_1;
            targetTimespan = GoldcoinDefinition.TARGET_TIMESPAN_1;
            proofOfWorkLimit = Utils.decodeCompactBits(0x1d0fffffL);
            acceptableAddressCodes = new int[]{111};
            dumpedPrivateKeyHeader = 239;
            genesisBlock.setTime(1296688602L);
            genesisBlock.setDifficultyTarget(0x1d07fff8L);
            genesisBlock.setNonce(384568319);
            allowEmptyPeerChains = false;
            spendableCoinbaseDepth = 100;
            subsidyDecreaseBlockCount = 210000;
            String genesisHash = genesisBlock.getHashAsString();
            checkState(genesisHash.equals("00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008"),
                    genesisHash);
        } else if (type == -1) {
            genesisBlock = createGenesis(this);
            id = ID_UNITTESTNET;
            packetMagic = 0x0b110907;
            addressHeader = 111;
            proofOfWorkLimit = new BigInteger("00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
            genesisBlock.setTime(System.currentTimeMillis() / 1000);
            genesisBlock.setDifficultyTarget(Block.EASIEST_DIFFICULTY_TARGET);
            genesisBlock.solve();
            port = 18333;
            interval = 10;
            dumpedPrivateKeyHeader = 239;
            allowEmptyPeerChains = false;
            targetTimespan = 200000000;  // 6 years. Just a very big number.
            spendableCoinbaseDepth = 5;
            acceptableAddressCodes = new int[]{111};
            subsidyDecreaseBlockCount = 100;
        } else {
            throw new RuntimeException();
        }
    }

    private static Block createGenesis(NetworkParameters n) {
        Block genesisBlock = new Block(n);
        Transaction t = new Transaction(n);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 14/May/2013 Justice Dept. Opens Criminal Inquiry Into I.R.S. Audits"
            //TODO::This code doesn't look right, something is missing - may have fixed it
            byte[] bytes = Hex.decode
                    ("486604799 4 5468652054696d65732031342f4d61792f32303133204a75737469636520446570742e204f70656e73204372696d696e616c20496e717569727920496e746f20492e522e532e20417564697473");
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Hex.decode
                    ("040184710fa689ad5023690c80f3a49c8f13f8d45b8c857fbcbc8bc4a8e4d3eb4b10f4d4604fa08dce601aaf0f470216fe1b51850b4acf21b179c45070ac7b03a9"));
            scriptPubKeyBytes.write(Script.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, Utils.toNanoCoins(50, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }

    private static Block createTestGenesis(NetworkParameters n) {
        Block genesisBlock = new Block(n);
        Transaction t = new Transaction(n);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
            byte[] bytes = Hex.decode
                    ("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Hex.decode
                    ("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(Script.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, Utils.toNanoCoins(50, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }

    //public static final int TARGET_TIMESPAN = GoldcoinDefinition.TARGET_TIMESPAN;  // 3.5 days per difficulty cycle, on average.
    public static final int TARGET_SPACING = GoldcoinDefinition.TARGET_SPACING_1;  // 2.5 minutes per block.
    //public static final int INTERVAL = GoldcoinDefinition.INTERVAL;

    /**
     * Blocks with a timestamp after this should enforce BIP 16, aka "Pay to script hash". This BIP changed the
     * network rules in a soft-forking manner, that is, blocks that don't follow the rules are accepted but not
     * mined upon and thus will be quickly re-orged out as long as the majority are enforcing the rule.
     */
    public static final int BIP16_ENFORCE_TIME = 1333238400;

    /**
     * The maximum money to be generated
     */
    //public static final BigInteger MAX_MONEY = new BigInteger("121399000", 10).multiply(COIN);
    public static final BigInteger MAX_MONEY = new BigInteger(GoldcoinDefinition.MAX_MONEY_STRING).multiply(COIN);

    /**
     * Returns whatever the latest testNet parameters are.  Use this rather than the versioned equivalents.
     */
    public static NetworkParameters testNet() {
        return testNet3();
    }

    private static NetworkParameters tn2;

    public synchronized static NetworkParameters testNet2() {
        if (tn2 == null) {
            tn2 = new NetworkParameters(2);
        }
        return tn2;
    }

    private static NetworkParameters tn3;

    public synchronized static NetworkParameters testNet3() {
        if (tn3 == null) {
            tn3 = new NetworkParameters(3);
        }
        return tn3;
    }

    private static NetworkParameters pn;

    /**
     * The primary Litecoin chain created by Satoshi.
     */
    public synchronized static NetworkParameters prodNet() {
        if (pn == null) {
            pn = new NetworkParameters(0);
        }
        return pn;
    }

    private static NetworkParameters pnh;

    /**
     * The primary Litecoin chain created by Hank.
     */
    public synchronized static NetworkParameters prodNetHank() {
        if (pnh == null) {
            pnh = new NetworkParameters(100);
        }
        return pnh;
    }

    private static NetworkParameters ut;

    /**
     * Returns a testnet params modified to allow any difficulty target.
     */
    public synchronized static NetworkParameters unitTests() {
        if (ut == null) {
            ut = new NetworkParameters(-1);
        }
        return ut;
    }

    /**
     * A Java package style string acting as unique ID for these parameters
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NetworkParameters)) return false;
        NetworkParameters o = (NetworkParameters) other;
        return o.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * Returns the network parameters for the given string ID or NULL if not recognized.
     */
    public static NetworkParameters fromID(String id) {
        if (id.equals(ID_PRODNET)) {
            return prodNet();
        } else if (id.equals(ID_TESTNET)) {
            return testNet();
        } else if (id.equals(ID_UNITTESTNET)) {
            return unitTests();
        } else {
            return null;
        }
    }

    public int getSpendableCoinbaseDepth() {
        return spendableCoinbaseDepth;
    }

    /**
     * Returns true if the block height is either not a checkpoint, or is a checkpoint and the hash matches.
     */
    public boolean passesCheckpoint(int height, Sha256Hash hash) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash == null || checkpointHash.equals(hash);
    }

    /**
     * Returns true if the given height has a recorded checkpoint.
     */
    public boolean isCheckpoint(int height) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash != null;
    }

    public int getSubsidyDecreaseBlockCount() {
        return subsidyDecreaseBlockCount;
    }
}
