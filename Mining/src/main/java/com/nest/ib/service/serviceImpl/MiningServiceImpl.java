package com.nest.ib.service.serviceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nest.ib.constant.Constant;
import com.nest.ib.model.OfferContractData;
import com.nest.ib.service.MiningService;
import com.nest.ib.utils.EthClient;
import com.nest.ib.utils.HttpClientUtil;
import com.nest.ib.utils.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple2;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ClassName:MiningServiceImpl
 * Description:
 */
@Service
public class MiningServiceImpl implements MiningService {
    private static final Logger LOG = LoggerFactory.getLogger(MiningServiceImpl.class);

    // Upper limit of block interval, validation period T0
    private static volatile BigInteger BLOCK_LIMIT;

    // Whether to start mining
    private static volatile boolean START_MINING = false;

    // Number of block intervals, default 3
    public static volatile int DEFAULT_BLOCK_INTERVAL = 3;

    // The quantity of ERC20 required is quoted
    private static volatile BigInteger OFFER_ERC20_AMOUNT = null;

    // ERC20 digits
    private static volatile BigInteger DECIMAL = null;

    // Huocoin EXCHANGE API
    private static volatile String HUOBI_API = null;

    // Erc20 symbol
    public static String SYMBOL;

    // Multiple of gasPrice when sending quotation transaction :1.2
    public static volatile BigDecimal OFFER_GAS_PRICE = new BigDecimal("1.2");

    // ERC20 address
    public static volatile String ERC20_TOKEN_ADDRESS;

    // Huocoin exchange trades pairs
    public static volatile String SYMBOLS;

    // The private key
    private String USER_PRIVATE_KEY;

    @Autowired
    private EthClient ethClient;

    /**
     * Open quotation
     */
    @Override
    public void offer() {
        // Check if the miner is set up
        if (!START_MINING) {
            LOG.info("The miner hasn't been turned on yet");
            return;
        }

        // Judge the exchange price
        if (OFFER_ERC20_AMOUNT == null) return;

        // Check the balance
        if (checkWalletBalance()) return;

        BigInteger defaultGasPrice = null;
        BigInteger nonce = null;
        BigInteger ethBlockNumber = null;
        try {
            defaultGasPrice = ethClient.ethGasPrice();
            nonce = ethClient.ethGetTransactionCount();
            ethBlockNumber = ethClient.ethBlockNumber();
        } catch (IOException e) {
            LOG.error("An exception occurred when the connection infura was quoted:{}", e.getMessage());
            e.printStackTrace();
        }

        /**
         *   Get the latest quoted block number through the price contract：
         *   1. In this way, the last quoted block number is obtained,
         *   which contains the quoted price after eating the order.
         *   When quoting the mining interval block,
         *   the quoted price after eating the order should not be calculated
         *   (the quoted price after eating the order does not participate in mining),
         *   so when someone eats the quoted price, the number of set interval blocks will increase.
         *   2. Suggestion: The developer USES the database to store all the quotation contracts on the chain and sift out the quotation after the order, so that the calculation is accurate
         */
        BigInteger miningBlockNumber = ethClient.checkLatestMining();
        if (miningBlockNumber == null) {
            LOG.error("Failed to obtain the last quoted block number");
            return;
        }

        // Quote and trade gasPrice
        BigInteger offerGasPrice = MathUtil.toBigInt(MathUtil.decMulInt(OFFER_GAS_PRICE, defaultGasPrice));
        String transactionHash = sendErc20Offer(offerGasPrice, nonce, ethBlockNumber, miningBlockNumber, defaultGasPrice);
        LOG.info("Quote price transaction hash:{}", transactionHash);
        if (transactionHash == null) {
            return;
        }

        // Check the status of quoted trades
        checkOfferTransaction(defaultGasPrice, nonce, miningBlockNumber);
    }


    /**
     * Fetch quoted assets
     */
    @Override
    public void turnOut() {
        List<OfferContractData> offerContractAddresses = getOfferContractAddress();
        if (offerContractAddresses.size() == 0) {
            LOG.info("There is no quotation contract that needs to be retrieved at present");
            return;
        }

        System.out.println("Unretrieved assets：====" + offerContractAddresses.size());
        BigInteger gasPrice = null;
        try {
            gasPrice = ethClient.ethGasPrice();
        } catch (IOException e) {
            LOG.error("There is an exception to the gasPrice obtained during fetch：" + e);
        }

        gasPrice = gasPrice.multiply(Constant.TURNOUT_GAS_PRICE).divide(BigInteger.TEN);
        BigInteger nonce = null;
        try {
            nonce = ethClient.ethGetTransactionCount();
        } catch (IOException e) {
            LOG.error("An exception occurs when you get a nonce on a fetch：" + e);
        }

        for (OfferContractData contractData : offerContractAddresses) {
            List<Type> typeList = Arrays.<Type>asList(new Address(contractData.getUuid()));
            String transaction = null;
            try {
                transaction = ethClient.turnOut(nonce, typeList, gasPrice);
            } catch (Exception e) {
                LOG.error("An exception has occurred to fetch the quote", e.getMessage());
            }
            nonce = nonce.add(BigInteger.ONE);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            LOG.info("hash： " + transaction);
        }
    }


    /**
     * Check the wallet ETH and ERC20 balance for an adequate quote, and if not, the quote will not start
     *
     * @return
     */
    @Override
    public boolean checkWalletBalance() {
        // Check the ETH,ERC20 balance is sufficient
        BigInteger ethBalance = null;
        try {
            ethBalance = ethClient.ethGetBalance();
        } catch (IOException e) {
            LOG.error("The time to get the account ETH balance is abnormal:{}", e.getMessage());
            e.printStackTrace();
        }

        BigInteger erc20Balance = null;
        try {
            erc20Balance = ethClient.ethBalanceOfErc20();
        } catch (Exception e) {
            LOG.error("There is an exception to the balance of the account {} obtained when quoting :{}", SYMBOL, e.getMessage());
            e.printStackTrace();
        }

        if (erc20Balance == null || ethBalance == null) {
            return true;
        }

        LOG.info("Current account balance：ETH={}，{}={}", ethBalance, SYMBOL, erc20Balance);
        if (ethBalance.compareTo(Constant.PAYABLE_ETH_AMOUNT) < 0 || erc20Balance.compareTo(OFFER_ERC20_AMOUNT) < 0) {
            LOG.info("The balance of the account is insufficient, please check if there is any unretrieved contract !");
            return true;
        } else {
            return false;
        }
    }


    /**
     * Update the number of block intervals
     */
    @Override
    public void updateIntervalBlock(int blockInterval) {
        DEFAULT_BLOCK_INTERVAL = blockInterval;
    }

    /**
     * View the current number of block intervals
     */
    @Override
    public int selectIntervalBlock() {
        return DEFAULT_BLOCK_INTERVAL;
    }

    /**
     * On/off mining:True on,false off
     */
    @Override
    public void startMining() {
        START_MINING = START_MINING == true ? false : true;
    }

    /**
     * Check the status of miner
     */
    @Override
    public boolean selectStartMining() {
        return START_MINING;
    }

    /**
     * Update the private key
     */
    @Override
    public void updateUserPrivateKey(String privateKey) {
        USER_PRIVATE_KEY = privateKey;
        // Some initialization takes place after the private key is updated
        ethClient.updateUserPrivateKey(USER_PRIVATE_KEY);
    }

    /**
     * Check your wallet address
     */
    @Override
    public String selectUserWalletAddress() {
        return StringUtils.isEmpty(USER_PRIVATE_KEY) ? null : Credentials.create(USER_PRIVATE_KEY).getAddress();
    }

    /**
     * Set validation period
     *
     * @param blockLimit
     */
    @Override
    public void setBlockLimit(BigInteger blockLimit) {
        BLOCK_LIMIT = blockLimit;
    }

    /**
     * Set the number of ERC20 tokens
     *
     * @param decimal
     */
    @Override
    public void setErc20Decimal(BigInteger decimal) {
        DECIMAL = decimal;
    }

    /**
     * Set the fire coin API
     *
     * @param url
     */
    @Override
    public void setHuoBiApi(String url) {
        HUOBI_API = url;
    }

    /**
     * Set the ERC20 identity
     *
     * @param symbol
     */
    @Override
    public void setErc20Symbol(String symbol) {
        SYMBOL = symbol;
    }

    /**
     * Update the fire coin price
     */
    @Override
    public boolean updatePrice() {
        if (DECIMAL == null) {
            OFFER_ERC20_AMOUNT = null;
            LOG.info("The token number was not successfully obtained:{}", SYMBOL);
            return false;
        }

        BigDecimal exchangePrice = getExchangePrice();
        if (exchangePrice == null) {
            LOG.info("Failed to access huocoin exchange API");
            OFFER_ERC20_AMOUNT = null;
            return false;
        }

        // Depending on the transaction pair, it needs to be processed differently, or converted to ethXXX if it ends in the ETH
        if (SYMBOLS.toLowerCase().endsWith("eth")) {
            exchangePrice = BigDecimal.ONE.divide(exchangePrice, 18, BigDecimal.ROUND_DOWN);
        }

        BigDecimal erc20Amount = MathUtil.decMulInt(exchangePrice, DECIMAL).multiply(Constant.ETH_AMOUNT);

        // It is not allowed to exceed 20% with a price deviation
        if (OFFER_ERC20_AMOUNT != null) {
            // The price difference
            BigDecimal dValue = MathUtil.decSubInt(erc20Amount, OFFER_ERC20_AMOUNT);
            // Price offset
            BigDecimal offset = MathUtil.decDivInt(dValue, OFFER_ERC20_AMOUNT, 3).abs();
            if (offset.compareTo(Constant.HUOBI_PRICE_OFFERSET) > 0) {
                LOG.info("There is a deviation of more than 20% in the exchange price. It has stopped running. Please check it for errors immediately");
                START_MINING = false;
                return false;
            }
        }

        OFFER_ERC20_AMOUNT = MathUtil.toBigInt(erc20Amount);
        LOG.info("Update the price --> Quote required ETH quantity for ：" + Constant.OFFER_ETH_AMOUNT + "  {} number is:" + OFFER_ERC20_AMOUNT, SYMBOL);
        return true;
    }

    /**
     * Get the exchange price
     */
    public static BigDecimal getExchangePrice() {
        if (HUOBI_API == null) {
            LOG.error("The fire coin API failed to initialize, and ERC20's Symbol failed to obtain");
            return null;
        }

        String s = HttpClientUtil.sendHttpGet(HUOBI_API);
        if (s == null) {
            return null;
        }

        JSONObject jsonObject = JSONObject.parseObject(s);
        JSONArray data = jsonObject.getJSONArray("data");
        if (data == null) {
            return null;
        }

        BigDecimal totalPrice = new BigDecimal("0");
        BigDecimal n = new BigDecimal("0");
        if (data.size() == 0) {
            return null;
        }

        for (int i = 0; i < data.size(); i++) {
            Object o = data.get(i);
            JSONObject jsonObject1 = JSONObject.parseObject(String.valueOf(o));
            JSONArray data1 = jsonObject1.getJSONArray("data");

            if (data1 == null) {
                continue;
            }

            if (data1.size() == 0) {
                continue;
            }

            JSONObject jsonObject2 = JSONObject.parseObject(String.valueOf(data1.get(0)));
            BigDecimal price = jsonObject2.getBigDecimal("price");
            if (price == null) {
                continue;
            }

            totalPrice = totalPrice.add(price);
            n = n.add(new BigDecimal("1"));
        }

        if (n.compareTo(new BigDecimal("0")) > 0) {
            return totalPrice.divide(n, 18, BigDecimal.ROUND_DOWN);
        }
        return null;
    }

    // Send quotation
    private String sendErc20Offer(BigInteger gasPrice, BigInteger nonce,
                                  BigInteger ethBlockNumber, BigInteger miningBlockNumber, BigInteger defaultGasPrice) {
        // Check that the set block interval is satisfied
        boolean b = checkBlockInterval(ethBlockNumber, miningBlockNumber, gasPrice);
        if (!b) {
            return null;
        }

        // Get the latest effective price in the current contract and compare it with the current prepared price. If the deviation is more than 10%, then quote 10 times
        BigDecimal otherMinerOfferPrice = checkPriceNow();
        // Current exchange prices
        BigDecimal companyPrice = calPrice(OFFER_ERC20_AMOUNT, Constant.OFFER_ETH_AMOUNT);

        // The quotation needs to be entered into the contract for the number of ETH
        BigInteger payableEth = Constant.PAYABLE_ETH_AMOUNT;
        // Quoted ETH quantity
        BigInteger offerEthAmount = Constant.OFFER_ETH_AMOUNT;
        // Quoted ERC20 quantity
        BigInteger offerErc20Amount = OFFER_ERC20_AMOUNT;

        // Price offset
        BigDecimal offset = (otherMinerOfferPrice.subtract(companyPrice)).divide(companyPrice, 3, BigDecimal.ROUND_DOWN).abs();
        // If the deviation exceeds 10%, quote 10 times
        if (offset.compareTo(Constant.OFFER_PRICE_OFFERSET) > 0) {
            offerEthAmount = Constant.OFFER_ETH_AMOUNT.multiply(BigInteger.TEN);
            offerErc20Amount = OFFER_ERC20_AMOUNT.multiply(BigInteger.TEN);
            payableEth = offerEthAmount.add(offerEthAmount.divide(Constant.SERVICE_CHARGE_RATE));
        }
        LOG.info("Quoted ETH quantity：{}  Quoted {} quantity：{}  Number of credits to contract ETH：{}", offerEthAmount, SYMBOL, offerErc20Amount, payableEth);

        List<Type> typeList = Arrays.<Type>asList(
                new Uint256(offerEthAmount),
                new Uint256(new BigInteger(String.valueOf(offerErc20Amount))),
                new Address(ERC20_TOKEN_ADDRESS));
        // Send quotation
        String transactionHash = ethClient.offer(gasPrice, nonce, typeList, payableEth);
        return transactionHash;
    }

    /**
     * Get the latest valid price in the current contract
     */
    private BigDecimal checkPriceNow() {
        Tuple2<BigInteger, BigInteger> latestPrice = null;
        try {
            latestPrice = ethClient.nestOfferPriceContract.checkPriceNow(ERC20_TOKEN_ADDRESS).sendAsync().get();
        } catch (Exception e) {
            LOG.error("There is an exception for getting {} prices through the price contract:{}", SYMBOL, e.getMessage());
            e.printStackTrace();
        }
        BigInteger ethAmount = latestPrice.getValue1();
        BigInteger erc20Amount = latestPrice.getValue2();

        // The latest effective price in the price contract
        return calPrice(erc20Amount, ethAmount);
    }

    private void checkOfferTransaction(BigInteger defaultGasPrice, BigInteger nonce, BigInteger miningBlockNumber) {
        while (true) {
            try {
                /**
                 *   Dormancy is to prevent frequent and excessive requests from quickly reaching the access limit of the node
                 *   (if it is a paid node of Infura, it is better to remove the dormancy if it is visited more times).
                 */
                Thread.sleep(2000);

                /**
                 *   If the block number of the last bid is changed, then either someone else has packaged it, or you have packaged it yourself, and the trade is cancelled.
                 *   1. If someone else succeeds, canceling the trade can avoid being blocked between blocks
                 *   2. If you succeed, because nonce is the same, then the transaction initiated is null and there is no loss
                 */
                BigInteger nowMiningBlockNumber = ethClient.checkLatestMining();
                if (miningBlockNumber == null) {
                    LOG.error("Failed to obtain the last quoted block number");
                    return;
                }

                if (nowMiningBlockNumber.compareTo(miningBlockNumber) > 0) {
                    // Determine if the nonce has changed
                    if (nonceIsChanged(nonce)) return;
                    BigInteger cancelGasPrice = defaultGasPrice.multiply(Constant.CANCEL_GAS_PRICE);
                    ethClient.cancelTransaction(nonce, cancelGasPrice);
                    return;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean nonceIsChanged(BigInteger nonce) throws IOException {
        BigInteger nowNonce = ethClient.ethGetTransactionCount();
        if (nowNonce.compareTo(nonce) > 0) {
            LOG.info("Nonce value change");
            return true;
        }

        return false;
    }

    /**
     * Checks that the current block conforms to the set block interval
     *
     * @param ethBlockNumber    The current block
     * @param miningBlockNumber The latest quote block number
     * @param gasPrice
     * @return
     */
    private boolean checkBlockInterval(BigInteger ethBlockNumber, BigInteger miningBlockNumber, BigInteger gasPrice) {
        BigDecimal n = new BigDecimal(String.valueOf(MiningServiceImpl.DEFAULT_BLOCK_INTERVAL - 1));
        // Checks to see if the number of blocks in the interval satisfies
        boolean b = false;
        try {
            BigInteger setBlockNumber = ethBlockNumber.subtract(new BigInteger(String.valueOf(n)));
            if (setBlockNumber.compareTo(miningBlockNumber) > 0) {
                b = true;
                LOG.info("Height of last quoted block: {} Current block height: {} gasPrice: {}", miningBlockNumber, ethBlockNumber, gasPrice);
            } else {
                LOG.info("Current block interval: {}, does not meet the price interval condition", ethBlockNumber.subtract(miningBlockNumber));
            }
        } catch (Exception e) {
            b = false;
        }

        return b;
    }

    /**
     * Get the quote contract address that needs to be retrieved through the Find interface
     *
     * @return
     */
    private List<OfferContractData> getOfferContractAddress() {
        // All quotation data queried
        List<OfferContractData> contractList = new ArrayList<>();
        try {
            contractList = ethClient.find(Constant.FIRST_FIND_COUNT, Address.DEFAULT.getValue());
        } catch (Exception e) {
            LOG.error("The Find interface failed to get the quote contract:{}", e.getMessage());
            e.printStackTrace();
        }

        if (CollectionUtils.isEmpty(contractList)) {
            return Collections.EMPTY_LIST;
        }

        BigInteger nowBlockNumber = null;
        try {
            nowBlockNumber = ethClient.ethBlockNumber();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Unretrieved contract
        List<OfferContractData> list = new ArrayList<>();
        int index = contractList.size() - 1;
        for (int i = index; i >= 0; i--) {
            OfferContractData contractData = contractList.get(i);
            BigInteger tokenAmount = new BigInteger(contractData.getTokenAmount());
            BigInteger serviceCharge = new BigInteger(contractData.getServiceCharge());
            BigInteger ethAmount = new BigInteger(contractData.getEthAmount());
            BigInteger blockNumber = new BigInteger(contractData.getBlockNum());

            // Fetch is not available until after the validation period
            if (nowBlockNumber.subtract(blockNumber).compareTo(BLOCK_LIMIT) < 0) {
                continue;
            }

            // If the balance or service fee is greater than 0, the assets have not been retrieved
            if (tokenAmount.compareTo(BigInteger.ZERO) > 0
                    || serviceCharge.compareTo(BigInteger.ZERO) > 0
                    || ethAmount.compareTo(BigInteger.ZERO) > 0) {
                list.add(contractData);
            }
        }

        return list;
    }

    /**
     * The price is calculated by the amount of ERC20 and ETH
     *
     * @return
     */
    private BigDecimal calPrice(BigInteger erc20Amount, BigInteger ethAmount) {
        BigDecimal erc20Max = MathUtil.intDivDec(erc20Amount, MathUtil.toDecimal(DECIMAL), 18);
        BigDecimal ethMax = MathUtil.intDivDec(ethAmount, Constant.UNIT_ETH, 18);
        BigDecimal price = erc20Max.divide(ethMax, 18, BigDecimal.ROUND_DOWN);
        return price;
    }

}
