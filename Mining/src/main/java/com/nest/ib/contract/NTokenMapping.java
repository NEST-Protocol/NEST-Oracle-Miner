package com.nest.ib.contract;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
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
 * NToken mapping contract
 */
public class NTokenMapping extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b50604051610aad380380610aad8339818101604052602081101561003357600080fd5b810190808051906020019092919050505080600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050610a18806100956000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80630dc1c4b21461005157806364688586146100b5578063a781e7f814610139578063f90679311461017d575b600080fd5b6100b36004803603604081101561006757600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506101e1565b005b6100f7600480360360208110156100cb57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610578565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b61017b6004803603602081101561014f57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506105e0565b005b6101df6004803603604081101561019357600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610777565b005b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff1660e01b81526004018080602001828103825260188152602001807f6e6573742e6e546f6b656e2e746f6b656e41756374696f6e000000000000000081525060200191505060206040518083038186803b15801561028557600080fd5b505afa158015610299573d6000803e3d6000fd5b505050506040513d60208110156102af57600080fd5b810190808051906020019092919050505073ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610360576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f4e6f20617574686f72697479000000000000000000000000000000000000000081525060200191505060405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff166000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614610460576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260148152602001807f546f6b656e20616c72656164792065786973747300000000000000000000000081525060200191505060405180910390fd5b806000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055507f97496bc9e831832e571d1eb2950e1bf8e41a8519d4d0de8d40e988cbca2da2c88282604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390a15050565b60008060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050919050565b60011515600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b15801561068357600080fd5b505afa158015610697573d6000803e3d6000fd5b505050506040513d60208110156106ad57600080fd5b8101908080519060200190929190505050151514610733576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f4e6f20617574686f72697479000000000000000000000000000000000000000081525060200191505060405180910390fd5b80600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b60011515600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b15801561081a57600080fd5b505afa15801561082e573d6000803e3d6000fd5b505050506040513d602081101561084457600080fd5b81019080805190602001909291905050501515146108ca576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f4e6f20617574686f72697479000000000000000000000000000000000000000081525060200191505060405180910390fd5b806000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055507f97496bc9e831832e571d1eb2950e1bf8e41a8519d4d0de8d40e988cbca2da2c88282604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390a1505056fea26469706673582212202afd0be8dd209f6adb9e878decce3bdab63d96689a01016b1f92dc109d31475b64736f6c63430006000033";

    protected NTokenMapping(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected NTokenMapping(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<TokenMappingLogEventResponse> getTokenMappingLogEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("TokenMappingLog",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}));
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<TokenMappingLogEventResponse> responses = new ArrayList<TokenMappingLogEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            TokenMappingLogEventResponse typedResponse = new TokenMappingLogEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.token = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.nToken = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TokenMappingLogEventResponse> tokenMappingLogEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("TokenMappingLog",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TokenMappingLogEventResponse>() {
            @Override
            public TokenMappingLogEventResponse call(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                TokenMappingLogEventResponse typedResponse = new TokenMappingLogEventResponse();
                typedResponse.log = log;
                typedResponse.token = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.nToken = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public static RemoteCall<NTokenMapping> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String voteFactory) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(voteFactory)));
        return deployRemoteCall(NTokenMapping.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<NTokenMapping> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String voteFactory) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(voteFactory)));
        return deployRemoteCall(NTokenMapping.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public RemoteCall<TransactionReceipt> addTokenMapping(String token, String nToken) {
        final Function function = new Function(
                "addTokenMapping",
                Arrays.<Type>asList(new Address(token),
                new Address(nToken)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeMapping(String voteFactory) {
        final Function function = new Function(
                "changeMapping",
                Arrays.<Type>asList(new Address(voteFactory)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeTokenMapping(String token, String nToken) {
        final Function function = new Function(
                "changeTokenMapping",
                Arrays.<Type>asList(new Address(token),
                new Address(nToken)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> checkTokenMapping(String token) {
        final Function function = new Function(
                "checkTokenMapping",
                Arrays.<Type>asList(new Address(token)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public static NTokenMapping load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new NTokenMapping(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static NTokenMapping load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new NTokenMapping(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class TokenMappingLogEventResponse {
        public Log log;

        public String token;

        public String nToken;
    }
}
