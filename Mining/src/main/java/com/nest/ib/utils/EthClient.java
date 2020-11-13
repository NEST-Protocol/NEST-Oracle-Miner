package com.nest.ib.utils;


import com.nest.ib.constant.AddressEnum;
import com.nest.ib.constant.Constant;
import com.nest.ib.contract.*;
import com.nest.ib.model.OfferContractData;
import com.nest.ib.service.MiningService;
import com.nest.ib.service.serviceImpl.MiningServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;


/**
 * @author wll
 * @date 2020/7/16 13:25
 * Operate Ethereum, get data, send transactions
 */
@Component
public class EthClient {
    private static final Logger LOG = LoggerFactory.getLogger(EthClient.class);

    @Autowired
    MiningService miningService;

    public Web3j web3j;
    public Credentials credentials;
    public ERC20 erc20;
    public NestOfferPriceContract nestOfferPriceContract;
    public Nest3OfferMain nest3OfferMain;
    public NestMiningContract miningContract;
    NTokenContract nTokenContract;

    // Ethereum node
    public static String NODE;

    /**
     * Initializing node
     */
    public void initNode(String node) {
        web3j = Web3j.build(new HttpService(node));
        NODE = node;
    }


    /**
     * Private key update, initialize
     *
     * @param userPrivateKey
     */
    public void updateUserPrivateKey(String userPrivateKey) {
        credentials = Credentials.create(userPrivateKey);

        resetBean();
    }

    /**
     * Registered bean
     */
    public void resetBean() {
        if (credentials == null) {
            return;
        }
        // Mapping contract address
        mappingContractAddress();

        // Loading contract
        erc20 = ContractFactory.erc20(credentials, web3j);
        miningContract = ContractFactory.nestMiningContract(credentials, web3j);
        nestOfferPriceContract = ContractFactory.nestOfferPriceContract(credentials, web3j);
        nest3OfferMain = ContractFactory.nest3OfferMain(credentials, web3j);
        if (!AddressEnum.USDT_TOKEN_CONTRACT_ADDRESS.getValue().equalsIgnoreCase(MiningServiceImpl.ERC20_TOKEN_ADDRESS)) {
            nTokenContract = ContractFactory.nTokenContract(credentials, web3j);
        }


        // Initializes the least eth
        if (!initLeastEth()) return;


        // Get ERC20 token information
        getErc20Info();

        // Gets the upper limit of the block interval, which is the time required for the validation period
        getBlockLimit();

        // Check the authorization
        try {
            approveToOfferFactoryContract();
        } catch (Exception e) {
            LOG.error("ERC20 authorization for the quoted factory contract failed :{}", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean initLeastEth() {
        BigInteger leastEth = null;
        BigInteger miningEth = null;
        try {
            leastEth = nest3OfferMain.checkleastEth().send();
            miningEth = nest3OfferMain.checkMiningETH().send();
        } catch (Exception e) {
            LOG.error("Failed to obtain leastEth, unable to quote, please restart：{}", e.getMessage());
            return false;
        }
        if (leastEth == null) return false;

        if (miningEth == null) return false;

        MiningServiceImpl.SERVICE_CHARGE_RATE = miningEth;

        MiningServiceImpl.OFFER_ETH_AMOUNT = leastEth;

        MiningServiceImpl.PAYABLE_ETH_AMOUNT = leastEth.add(leastEth.multiply(miningEth).divide(BigInteger.valueOf(1000)));

        MiningServiceImpl.ETH_AMOUNT = MathUtil.intDivDec(leastEth, Constant.UNIT_ETH, 0);

        LOG.info("The leastEth obtains the success：{} ETH", MiningServiceImpl.ETH_AMOUNT);
        LOG.info("The Quotation commission ratio：{} ", MiningServiceImpl.SERVICE_CHARGE_RATE);
        return true;
    }

    /**
     * Get ERC20 token information
     */
    public void getErc20Info() {
        try {
            BigInteger decimal = erc20.decimals().send();
            long unit = (long) Math.pow(10, decimal.intValue());
            miningService.setErc20Decimal(BigInteger.valueOf(unit));

            String symbol = erc20.symbol().send();
            // HBTC special treatment should be replaced by BTC
            if (symbol.equalsIgnoreCase("HBTC")) {
                symbol = "BTC";
            }

            String huobiApi = "https://api.huobi.pro/market/history/trade?size=1&symbol=";
            String url = huobiApi + MiningServiceImpl.SYMBOLS.toLowerCase();
            System.out.println(url);
            miningService.setHuoBiApi(url);
            miningService.setErc20Symbol(symbol);
            LOG.info("ERC20 token decimal number: {} Name: {}", unit, symbol);
        } catch (Exception e) {
            LOG.error("Failed to get ERC20 token decimal} and mark {symbol} :{}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the validation block interval
     */
    public void getBlockLimit() {
        try {
            BigInteger blockLimit = nest3OfferMain.checkBlockLimit().send();
            miningService.setBlockLimit(blockLimit);
        } catch (Exception e) {
            LOG.error("Failed to get block interval upper limit, set default value 25:{}", e.getMessage());
            miningService.setBlockLimit(Constant.DEFAULT_BLOCK_LIMIT);
            e.printStackTrace();
        }
    }

    /**
     * Mapping contract address
     */
    public void mappingContractAddress() {
        // NToken mapping
        if (!AddressEnum.USDT_TOKEN_CONTRACT_ADDRESS.getValue().equalsIgnoreCase(MiningServiceImpl.ERC20_TOKEN_ADDRESS)) {
            // NToken offer contract address
            nTokenFactoryMapping(credentials, web3j);
        } else { // NEST mining contract
            // Address of quoted factory
            String nestFactoryAddress = mapping(credentials, web3j, "Nest Quote Factory ", "nest.v3.offerMain");
            AddressEnum.OFFER_CONTRACT_FACTORY_ADDRESS.setValue(nestFactoryAddress);
        }

        // Quotation contract
        String offerPriceAddress = mapping(credentials, web3j, "Quote the price", "nest.v3.offerPrice");
        AddressEnum.OFFER_PRICE_CONTRACT_ADDRESS.setValue(offerPriceAddress);

        // Mine pool contract address
        String miningSaveAddress = mapping(credentials, web3j, "Ore pool contract", "nest.v3.miningSave");
        AddressEnum.MINING_SAVE_CONTRACT_ADDRESS.setValue(miningSaveAddress);
    }

    /**
     * NToken contract mapping
     *
     * @param credentials
     * @param web3j
     */
    public void nTokenFactoryMapping(Credentials credentials, Web3j web3j) {
        // Address of quoted factory
        String nTokenFactoryAddress = mapping(credentials, web3j, "NToken quote factory", "nest.nToken.offerMain");
        AddressEnum.OFFER_CONTRACT_FACTORY_ADDRESS.setValue(nTokenFactoryAddress);

        // NToken mapping
        String nTokenMappingAddress = mapping(credentials, web3j, "NToken mapping", "nest.nToken.tokenMapping");
        AddressEnum.NTOKEN_MAPPING_CONTRACT_ADDRESS.setValue(nTokenMappingAddress);

        if (StringUtils.isEmpty(nTokenFactoryAddress)) {
            return;
        }

        // NToken contract address mapping
        String tokenAddress = null;
        try {
            NTokenMapping nTokenMapping = NTokenMapping.load(nTokenMappingAddress, web3j, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
            tokenAddress = nTokenMapping.checkTokenMapping(MiningServiceImpl.ERC20_TOKEN_ADDRESS).sendAsync().get();
        } catch (Exception e) {
            LOG.error("An exception occurred when the nToken contract address was updated:{}", e.getMessage());
            e.printStackTrace();
        }

        if (StringUtils.isEmpty(tokenAddress) || tokenAddress.equalsIgnoreCase(Address.DEFAULT.getValue())) {
            LOG.error("NToken contract address update failed");
            return;
        }
        AddressEnum.NTOKEN_CONTRACT_ADDRESS.setValue(tokenAddress);
        LOG.info("NToken contract address update：" + tokenAddress);

    }

    /**
     * Mapping the contract
     */
    public String mapping(Credentials credentials, Web3j web3j, String addressName, String mappingName) {
        String address = null;
        try {
            VoteContract mappingContract = VoteContract.load(AddressEnum.VOTE_CONTRACT_ADDRESS.getValue(), web3j, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
            address = mappingContract.checkAddress(mappingName).sendAsync().get();
        } catch (Exception e) {
            LOG.error("{}Contract address update failed:{}", addressName, e.getMessage());
            e.printStackTrace();
        }

        if (StringUtils.isEmpty(address) || address.equalsIgnoreCase(Address.DEFAULT.getValue())) {
            LOG.error("{}Contract address update failed", addressName);
            return null;
        }

        LOG.info("{}Contract address update:{}", addressName, address);
        return address;
    }


    /**
     * Checks whether a one-time authorization has been granted, and if not, a one-time authorization is granted
     */
    public void approveToOfferFactoryContract() throws ExecutionException, InterruptedException, IOException {
        // View the authorized amount
        BigInteger approveValue = allowance();
        BigInteger nonce = ethGetTransactionCount();
        // 1.5 times the authorization of gasPrice, which can be adjusted by itself
        BigInteger gasPrice = ethGasPrice().multiply(BigInteger.valueOf(15)).divide(BigInteger.TEN);
        // For authorization
        if (approveValue.compareTo(new BigInteger("100000000000000")) <= 0) {
            List<Type> typeList = Arrays.<Type>asList(
                    new Address(AddressEnum.OFFER_CONTRACT_FACTORY_ADDRESS.getValue()),
                    new Uint256(new BigInteger("999999999999999999999999999999999999999999"))
            );

            Function function = new Function("approve", typeList, Collections.<TypeReference<?>>emptyList());
            String encode = FunctionEncoder.encode(function);
            String transaction = ethSendErc20RawTransaction(gasPrice, nonce, Constant.OFFER_GAS_LIMIT, BigInteger.ZERO, encode);
            LOG.info("One-time authorization hash：" + transaction);
        }
    }

    /**
     * Get the default gasPrice
     *
     * @return
     * @throws IOException
     */
    public BigInteger ethGasPrice() throws IOException {
        return web3j.ethGasPrice().send().getGasPrice();
    }

    /**
     * View the authorized amount
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public BigInteger allowance() throws ExecutionException, InterruptedException {
        return erc20.allowance(credentials.getAddress(), AddressEnum.OFFER_CONTRACT_FACTORY_ADDRESS.getValue()).sendAsync().get();
    }

    /**
     * For the nonce value
     *
     * @return
     * @throws IOException
     */
    public BigInteger ethGetTransactionCount() throws IOException {
        return web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
    }

    /**
     * Get the latest ethereum block number
     *
     * @return
     * @throws IOException
     */
    public BigInteger ethBlockNumber() throws IOException {
        return web3j.ethBlockNumber().send().getBlockNumber();
    }

    /**
     * Obtain the block number of the last quoted contract
     *
     * @return
     */
    public BigInteger checkLatestMining() {
        BigInteger lastNum = null;
        try {
            String ntokenContractAddress = AddressEnum.NTOKEN_CONTRACT_ADDRESS.getValue();
            // USDT quotation
            if (StringUtils.isEmpty(ntokenContractAddress)
                    && MiningServiceImpl.ERC20_TOKEN_ADDRESS.equalsIgnoreCase(AddressEnum.USDT_TOKEN_CONTRACT_ADDRESS.getValue())) {
                lastNum = miningContract.checkLatestMining().sendAsync().get();
            } else if (!StringUtils.isEmpty(ntokenContractAddress)) {
                lastNum = nTokenContract.checkBlockInfo().sendAsync().get().getValue2();
            }
        } catch (Exception e) {
            LOG.error("Gets the last quoted block number, exception: {}", e.getMessage());
            e.printStackTrace();
        }
        return lastNum;
    }

    /**
     * Check the ETH balance in your wallet
     *
     * @return
     * @throws IOException
     */
    public BigInteger ethGetBalance() throws IOException {
        if (credentials == null) {
            return null;
        }

        return web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
    }

    /**
     * Query the ERC20 balance
     *
     * @return
     * @throws Exception
     */
    public BigInteger ethBalanceOfErc20() throws Exception {
        if (credentials == null) {
            return null;
        }

        return erc20.balanceOf(credentials.getAddress()).send();
    }

    /**
     * Find the contract for the target account (in reverse order)
     *
     * @param count Maximum number of records returned
     * @param start Query forward from the index corresponding to the given contract address (does not contain the record corresponding to start)
     * @return
     */
    public List<OfferContractData> find(BigInteger count, String start) throws Exception {
        if (nest3OfferMain == null || credentials == null) {
            return null;
        }

        String contracts = nest3OfferMain.find(start, count, Constant.MAX_FIND_COUNT, credentials.getAddress()).send();
        if (StringUtils.isEmpty(contracts)) {
            return null;
        }

        // The data returned by the Find interface is parsed
        List<OfferContractData> offerContractDataList = transformToOfferContractData(contracts);
        return offerContractDataList;
    }


    /**
     * Find contract order information in positive order
     *
     * @param offset Skip the initial OFFSET bar record
     * @param count  Maximum number of records returned
     * @return
     */
    public List<OfferContractData> list(BigInteger offset, BigInteger count) throws Exception {
        String contracts = nest3OfferMain.list(offset, count, BigInteger.ONE).send();
        if (StringUtils.isEmpty(contracts)) {
            return null;
        }

        return transformToOfferContractData(contracts);
    }


    /**
     * Send quotation transaction
     *
     * @param gasPrice
     * @param nonce
     * @param typeList   Parameter collection
     * @param payableEth Number of eth
     * @return
     */
    public String offer(BigInteger gasPrice, BigInteger nonce, List typeList, BigInteger payableEth) {
        Function function = new Function("offer", typeList, Collections.<TypeReference<?>>emptyList());
        String encode = FunctionEncoder.encode(function);

        String transaction = null;
        try {
            transaction = ethSendRawTransaction(gasPrice, nonce, Constant.OFFER_GAS_LIMIT, payableEth, encode);
        } catch (Exception e) {
            LOG.error("Send quote transaction failed", e.getMessage());
            e.printStackTrace();
        }
        return transaction;
    }

    /**
     * Retrieve the transaction
     */
    public String turnOut(BigInteger nonce, List typeList, BigInteger gasPrice) throws ExecutionException, InterruptedException {
        Function function = new Function("turnOut", typeList, Collections.<TypeReference<?>>emptyList());
        String encode = FunctionEncoder.encode(function);
        String transaction = null;
        transaction = ethSendRawTransaction(gasPrice, nonce, Constant.TURN_OUT_GAS_LIMIT, BigInteger.ZERO, encode);
        return transaction;
    }

    /**
     * Cancel the transaction (use the same nONCE as the quoted price, set the gasPrice higher than the quoted price, and make a transfer to yourself to cover the quoted transaction)
     */
    public String cancelTransaction(BigInteger nonce, BigInteger gasPrice) {
        BigInteger value = BigInteger.ZERO;
        String transactionHash = null;
        try {
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, Constant.CANEL_GAS_LIMIT, credentials.getAddress(), value);
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signMessage);

            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            transactionHash = ethSendTransaction.getTransactionHash();
            LOG.info("Cancel the transaction hash：" + transactionHash);
        } catch (Exception e) {
            System.out.println("Cancel the deal fail");
        }
        return transactionHash;
    }

    /**
     * Parse the data returned by the contract into an object
     *
     * @param contracts
     * @return
     */
    private List<OfferContractData> transformToOfferContractData(String contracts) {
        List<OfferContractData> dataList = new ArrayList<>();

        String[] split = contracts.split(",");
        //最后一个为空
        int n = split.length - 1;
        String prefix = "0x";
        for (int i = 0; i < n; i += 9) {
            OfferContractData contractData = new OfferContractData();
            contractData.setUuid(prefix + split[i]);
            contractData.setOwner(prefix + split[i + 1]);
            contractData.setTokenAddress(prefix + split[i + 2]);
            contractData.setEthAmount(split[i + 3]);
            contractData.setTokenAmount(split[i + 4]);
            contractData.setDealEthAmount(split[i + 5]);
            contractData.setDealTokenAmount(split[i + 6]);
            contractData.setBlockNum(split[i + 7]);
            contractData.setServiceCharge(split[i + 8]);
            dataList.add(contractData);
        }

        return dataList;
    }

    /**
     * Send a deal
     */
    private String ethSendRawTransaction(BigInteger gasPrice, BigInteger nonce, BigInteger gasLimit, BigInteger payableEth, String encode) throws ExecutionException, InterruptedException {
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                AddressEnum.OFFER_CONTRACT_FACTORY_ADDRESS.getValue(),
                payableEth,
                encode);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        String transactionHash = null;
        try {
            transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
        } catch (Exception e) {
            System.out.println("Offer abnormal：" + e.getMessage());
        }
        return transactionHash;
    }

    /**
     * Initiate the ERC20 transaction
     */
    private String ethSendErc20RawTransaction(BigInteger gasPrice, BigInteger nonce, BigInteger gasLimit, BigInteger payableEth, String encode) throws ExecutionException, InterruptedException {
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                MiningServiceImpl.ERC20_TOKEN_ADDRESS,
                payableEth,
                encode);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        String transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
        return transactionHash;
    }

}
