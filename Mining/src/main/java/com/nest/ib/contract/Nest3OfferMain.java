package com.nest.ib.contract;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Quotation contract: quotation + order
 */
public class Nest3OfferMain extends Contract {
    private static final String BINARY = "";

    protected Nest3OfferMain(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Nest3OfferMain(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<OfferContractAddressEventResponse> getOfferContractAddressEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("OfferContractAddress",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<OfferContractAddressEventResponse> responses = new ArrayList<OfferContractAddressEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            OfferContractAddressEventResponse typedResponse = new OfferContractAddressEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.contractAddress = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.tokenAddress = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.ethAmount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.erc20Amount = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.continued = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.serviceCharge = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OfferContractAddressEventResponse> offerContractAddressEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("OfferContractAddress",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, OfferContractAddressEventResponse>() {
            @Override
            public OfferContractAddressEventResponse call(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                OfferContractAddressEventResponse typedResponse = new OfferContractAddressEventResponse();
                typedResponse.log = log;
                typedResponse.contractAddress = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.tokenAddress = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.ethAmount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.erc20Amount = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
                typedResponse.continued = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
                typedResponse.serviceCharge = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
                return typedResponse;
            }
        });
    }

    public List<OfferTranEventResponse> getOfferTranEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("OfferTran",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}));
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<OfferTranEventResponse> responses = new ArrayList<OfferTranEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            OfferTranEventResponse typedResponse = new OfferTranEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.tranSender = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.tranToken = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.tranAmount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.otherToken = (String) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.otherAmount = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.tradedContract = (String) eventValues.getNonIndexedValues().get(5).getValue();
            typedResponse.tradedOwner = (String) eventValues.getNonIndexedValues().get(6).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OfferTranEventResponse> offerTranEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("OfferTran",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, OfferTranEventResponse>() {
            @Override
            public OfferTranEventResponse call(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                OfferTranEventResponse typedResponse = new OfferTranEventResponse();
                typedResponse.log = log;
                typedResponse.tranSender = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.tranToken = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.tranAmount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.otherToken = (String) eventValues.getNonIndexedValues().get(3).getValue();
                typedResponse.otherAmount = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
                typedResponse.tradedContract = (String) eventValues.getNonIndexedValues().get(5).getValue();
                typedResponse.tradedOwner = (String) eventValues.getNonIndexedValues().get(6).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<BigInteger> checkDelayedAgain() {
        final Function function = new Function("checkDelayedAgain",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkOfferBlockMining(BigInteger blockNum) {
        final Function function = new Function("checkOfferBlockMining",
                Arrays.<Type>asList(new Uint256(blockNum)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> changeTokenAllow(String token, Boolean allow) {
        final Function function = new Function(
                "changeTokenAllow",
                Arrays.<Type>asList(new Address(token),
                new Bool(allow)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeDeviationFromScale(BigInteger num) {
        final Function function = new Function(
                "changeDeviationFromScale",
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> checkDelayedAgain(BigInteger num) {
        final Function function = new Function(
                "checkDelayedAgain",
                Arrays.<Type>asList(new Uint32(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> checkTranAddition() {
        final Function function = new Function("checkTranAddition",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> list(BigInteger offset, BigInteger count, BigInteger order) {
        final Function function = new Function("list",
                Arrays.<Type>asList(new Uint256(offset),
                new Uint256(count),
                new Uint256(order)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> changeTranEth(BigInteger num) {
        final Function function = new Function(
                "changeTranEth",
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> checkTokenAllow(String token) {
        final Function function = new Function("checkTokenAllow",
                Arrays.<Type>asList(new Address(token)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<BigInteger> checkTranEth() {
        final Function function = new Function("checkTranEth",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> changeDelayed(BigInteger num) {
        final Function function = new Function(
                "changeDelayed",
                Arrays.<Type>asList(new Uint32(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeBlockLimit(BigInteger num) {
        final Function function = new Function(
                "changeBlockLimit",
                Arrays.<Type>asList(new Uint32(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> checkDelayed() {
        final Function function = new Function("checkDelayed",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkOfferEthLimit() {
        final Function function = new Function("checkOfferEthLimit",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> changeInitialRatio(BigInteger coderNum, BigInteger NNNum, BigInteger otherNum) {
        final Function function = new Function(
                "changeInitialRatio",
                Arrays.<Type>asList(new Uint256(coderNum),
                new Uint256(NNNum),
                new Uint256(otherNum)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> checkDeviationFromScale() {
        final Function function = new Function("checkDeviationFromScale",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkDeviate() {
        final Function function = new Function("checkDeviate",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkNNAmount() {
        final Function function = new Function("checkNNAmount",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkCoderAmount() {
        final Function function = new Function("checkCoderAmount",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkOfferSpan() {
        final Function function = new Function("checkOfferSpan",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> sendEthBuyErc(BigInteger ethAmount, BigInteger tokenAmount, String contractAddress, BigInteger tranEthAmount, BigInteger tranTokenAmount, String tranTokenAddress, BigInteger weiValue) {
        final Function function = new Function(
                "sendEthBuyErc",
                Arrays.<Type>asList(new Uint256(ethAmount),
                new Uint256(tokenAmount),
                new Address(contractAddress),
                new Uint256(tranEthAmount),
                new Uint256(tranTokenAmount),
                new Address(tranTokenAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<BigInteger> toIndex(String contractAddress) {
        final Function function = new Function("toIndex",
                Arrays.<Type>asList(new Address(contractAddress)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkContractState(BigInteger delayedBlock, BigInteger createBlock) {
        final Function function = new Function("checkContractState",
                Arrays.<Type>asList(new Uint256(delayedBlock),
                new Uint256(createBlock)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkOtherAmount() {
        final Function function = new Function("checkOtherAmount",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> writeUInt(BigInteger iv, byte[] buf, BigInteger index) {
        final Function function = new Function("writeUInt",
                Arrays.<Type>asList(new Uint256(iv),
                new org.web3j.abi.datatypes.DynamicBytes(buf),
                new Uint256(index)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> changeMapping(String voteFacory) {
        final Function function = new Function(
                "changeMapping",
                Arrays.<Type>asList(new Address(voteFacory)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeMiningETH(BigInteger num) {
        final Function function = new Function(
                "changeMiningETH",
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> sendErcBuyEth(BigInteger ethAmount, BigInteger tokenAmount, String contractAddress, BigInteger tranEthAmount, BigInteger tranTokenAmount, String tranTokenAddress, BigInteger weiValue) {
        final Function function = new Function(
                "sendErcBuyEth",
                Arrays.<Type>asList(new Uint256(ethAmount),
                new Uint256(tokenAmount),
                new Address(contractAddress),
                new Uint256(tranEthAmount),
                new Uint256(tranTokenAmount),
                new Address(tranTokenAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<TransactionReceipt> changekDeviate(BigInteger num) {
        final Function function = new Function(
                "changekDeviate",
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeTranAddition(BigInteger num) {
        final Function function = new Function(
                "changeTranAddition",
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeOfferEthLimit(BigInteger num) {
        final Function function = new Function(
                "changeOfferEthLimit",
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> checkOfferBlockEth(BigInteger blockNum) {
        final Function function = new Function("checkOfferBlockEth",
                Arrays.<Type>asList(new Uint256(blockNum)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> getPriceCount() {
        final Function function = new Function("getPriceCount",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkBlockLimit() {
        final Function function = new Function("checkBlockLimit",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> find(String start, BigInteger count, BigInteger maxFindCount, String owner) {
        final Function function = new Function("find",
                Arrays.<Type>asList(new Address(start),
                new Uint256(count),
                new Uint256(maxFindCount),
                new Address(owner)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<BigInteger> checkMiningETH() {
        final Function function = new Function("checkMiningETH",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> toAddress(BigInteger index) {
        final Function function = new Function("toAddress",
                Arrays.<Type>asList(new Uint256(index)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> changeLeastEth(BigInteger num) {
        final Function function = new Function(
                "changeLeastEth",
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> getPrice(BigInteger priceIndex) {
        final Function function = new Function("getPrice",
                Arrays.<Type>asList(new Uint256(priceIndex)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> turnOut(String contractAddress) {
        final Function function = new Function(
                "turnOut",
                Arrays.<Type>asList(new Address(contractAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeOfferSpan(BigInteger num) {
        final Function function = new Function(
                "changeOfferSpan",
                Arrays.<Type>asList(new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> offer(BigInteger ethAmount, BigInteger erc20Amount, String erc20Address, BigInteger weiValue) {
        final Function function = new Function(
                "offer",
                Arrays.<Type>asList(new Uint256(ethAmount),
                new Uint256(erc20Amount),
                new Address(erc20Address)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<BigInteger> checkleastEth() {
        final Function function = new Function("checkleastEth",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkOfferMining(BigInteger blockNum, BigInteger serviceCharge) {
        final Function function = new Function("checkOfferMining",
                Arrays.<Type>asList(new Uint256(blockNum),
                new Uint256(serviceCharge)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static RemoteCall<Nest3OfferMain> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String voteFacory) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(voteFacory)));
        return deployRemoteCall(Nest3OfferMain.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<Nest3OfferMain> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String voteFacory) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(voteFacory)));
        return deployRemoteCall(Nest3OfferMain.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static Nest3OfferMain load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Nest3OfferMain(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Nest3OfferMain load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Nest3OfferMain(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class OfferContractAddressEventResponse {
        public Log log;

        public String contractAddress;

        public String tokenAddress;

        public BigInteger ethAmount;

        public BigInteger erc20Amount;

        public BigInteger continued;

        public BigInteger serviceCharge;
    }

    public static class OfferTranEventResponse {
        public Log log;

        public String tranSender;

        public String tranToken;

        public BigInteger tranAmount;

        public String otherToken;

        public BigInteger otherAmount;

        public String tradedContract;

        public String tradedOwner;
    }
}
