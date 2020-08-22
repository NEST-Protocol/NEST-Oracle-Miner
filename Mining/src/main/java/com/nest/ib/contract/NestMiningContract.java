package com.nest.ib.contract;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Mining contract: storage pool + mining logic
 */
public class NestMiningContract extends Contract {
    private static final String BINARY = "608060405262249f00600055605a600155606460025534801561002157600080fd5b50604051602080611c388339810180604052602081101561004157600080fd5b810190808051906020019092919050505080600660006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825260118152602001807f6e6573742e76332e6f666665724d61696e00000000000000000000000000000081525060200191505060206040518083038186803b15801561015357600080fd5b505afa158015610167573d6000803e3d6000fd5b505050506040513d602081101561017d57600080fd5b8101908080519060200190929190505050600860006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825260048152602001807f6e6573740000000000000000000000000000000000000000000000000000000081525060200191505060206040518083038186803b15801561028e57600080fd5b505afa1580156102a2573d6000803e3d6000fd5b505050506040513d60208110156102b857600080fd5b8101908080519060200190929190505050600760006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555043600381905550436004819055506815af1d78b58c4000006005600043815260200190815260200160002081905550506118f0806103486000396000f3fe6080604052600436106100ba576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806346bf7cf8146100bf5780638434d141146100ea578063920f5e3714610139578063a781e7f814610164578063b34ed412146101b5578063be9c375f146101f0578063cd0a6f631461021b578063d02ceeea1461026c578063d5c6347414610297578063e246ac39146102c9578063e38fd2ae146102f4578063fdd946851461031f575b600080fd5b3480156100cb57600080fd5b506100d4610364565b6040518082815260200191505060405180910390f35b3480156100f657600080fd5b506101236004803603602081101561010d57600080fd5b810190808035906020019092919050505061068a565b6040518082815260200191505060405180910390f35b34801561014557600080fd5b5061014e6106a7565b6040518082815260200191505060405180910390f35b34801561017057600080fd5b506101b36004803603602081101561018757600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506106b1565b005b3480156101c157600080fd5b506101ee600480360360208110156101d857600080fd5b8101908080359060200190929190505050610adc565b005b3480156101fc57600080fd5b50610205610c66565b6040518082815260200191505060405180910390f35b34801561022757600080fd5b5061026a6004803603602081101561023e57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610c6f565b005b34801561027857600080fd5b50610281610fd9565b6040518082815260200191505060405180910390f35b3480156102a357600080fd5b506102ac610ff6565b604051808381526020018281526020019250505060405180910390f35b3480156102d557600080fd5b506102de611007565b6040518082815260200191505060405180910390f35b34801561030057600080fd5b50610309611104565b6040518082815260200191505060405180910390f35b34801561032b57600080fd5b506103626004803603604081101561034257600080fd5b81019080803590602001909291908035906020019092919050505061110e565b005b6000600860009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561042b576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f4e6f20617574686f72697479000000000000000000000000000000000000000081525060200191505060405180910390fd5b60006104356113cd565b905080600760009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166370a08231306040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b1580156104f357600080fd5b505afa158015610507573d6000803e3d6000fd5b505050506040513d602081101561051d57600080fd5b8101908080519060200190929190505050101561053957600090505b600081111561068357600760009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a9059cbb33836040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182815260200192505050602060405180830381600087803b15801561060757600080fd5b505af115801561061b573d6000803e3d6000fd5b505050506040513d602081101561063157600080fd5b8101908080519060200190929190505050507fe562eb487d0f4f5d86dc1c1fc74820ea3006c9d180ea22a48404eed0dac4a6594382604051808381526020018281526020019250505060405180910390a15b8091505090565b600060056000838152602001908152602001600020549050919050565b6000600454905090565b60011515600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b15801561077057600080fd5b505afa158015610784573d6000803e3d6000fd5b505050506040513d602081101561079a57600080fd5b81019080805190602001909291905050501515141515610822576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f4e6f20617574686f72697479000000000000000000000000000000000000000081525060200191505060405180910390fd5b80600660006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825260118152602001807f6e6573742e76332e6f666665724d61696e00000000000000000000000000000081525060200191505060206040518083038186803b15801561092357600080fd5b505afa158015610937573d6000803e3d6000fd5b505050506040513d602081101561094d57600080fd5b8101908080519060200190929190505050600860006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001828103825260048152602001807f6e6573740000000000000000000000000000000000000000000000000000000081525060200191505060206040518083038186803b158015610a5e57600080fd5b505afa158015610a72573d6000803e3d6000fd5b505050506040513d6020811015610a8857600080fd5b8101908080519060200190929190505050600760006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b60011515600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b158015610b9b57600080fd5b505afa158015610baf573d6000803e3d6000fd5b505050506040513d6020811015610bc557600080fd5b81019080805190602001909291905050501515141515610c4d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f4e6f20617574686f72697479000000000000000000000000000000000000000081525060200191505060405180910390fd5b600081111515610c5c57600080fd5b8060008190555050565b60008054905090565b60011515600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b158015610d2e57600080fd5b505afa158015610d42573d6000803e3d6000fd5b505050506040513d6020811015610d5857600080fd5b81019080805190602001909291905050501515141515610de0576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f4e6f20617574686f72697479000000000000000000000000000000000000000081525060200191505060405180910390fd5b600760009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a9059cbb82600760009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166370a08231306040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b158015610eda57600080fd5b505afa158015610eee573d6000803e3d6000fd5b505050506040513d6020811015610f0457600080fd5b81019080805190602001909291905050506040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182815260200192505050602060405180830381600087803b158015610f9a57600080fd5b505af1158015610fae573d6000803e3d6000fd5b505050506040513d6020811015610fc457600080fd5b81019080805190602001909291905050505050565b600060056000600354815260200190815260200160002054905090565b600080600154600254915091509091565b6000600760009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166370a08231306040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b1580156110c457600080fd5b505afa1580156110d8573d6000803e3d6000fd5b505050506040513d60208110156110ee57600080fd5b8101908080519060200190929190505050905090565b6000600354905090565b60011515600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b1580156111cd57600080fd5b505afa1580156111e1573d6000803e3d6000fd5b505050506040513d60208110156111f757600080fd5b8101908080519060200190929190505050151514151561127f576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f4e6f20617574686f72697479000000000000000000000000000000000000000081525060200191505060405180910390fd5b60008211151561131d576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260248152602001807f506172616d65746572206e6565647320746f206265206772656174657220746881526020017f616e20300000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b6000811115156113bb576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260248152602001807f506172616d65746572206e6565647320746f206265206772656174657220746881526020017f616e20300000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b81600181905550806002819055505050565b600080600090505b436113ed60005460035461154d90919063ffffffff16565b1115156114ef576000611434600254611426600154600560006003548152602001908152602001600020546115d790919063ffffffff16565b6116a490919063ffffffff16565b905061144d60005460035461154d90919063ffffffff16565b60038190555060035460045410156114cf576114c36114b461147c6004546003546116ee90919063ffffffff16565b600560006114976000546003546116ee90919063ffffffff16565b8152602001908152602001600020546115d790919063ffffffff16565b8361154d90919063ffffffff16565b91506003546004819055505b8060056000600354815260200190815260200160002081905550506113d5565b61153d61152e61150a600454436116ee90919063ffffffff16565b600560006003548152602001908152602001600020546115d790919063ffffffff16565b8261154d90919063ffffffff16565b9050436004819055508091505090565b60008082840190508381101515156115cd576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252601b8152602001807f536166654d6174683a206164646974696f6e206f766572666c6f77000000000081525060200191505060405180910390fd5b8091505092915050565b6000808314156115ea576000905061169e565b600082840290508284828115156115fd57fe5b04141515611699576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260218152602001807f536166654d6174683a206d756c7469706c69636174696f6e206f766572666c6f81526020017f770000000000000000000000000000000000000000000000000000000000000081525060400191505060405180910390fd5b809150505b92915050565b60006116e683836040805190810160405280601a81526020017f536166654d6174683a206469766973696f6e206279207a65726f000000000000815250611738565b905092915050565b600061173083836040805190810160405280601e81526020017f536166654d6174683a207375627472616374696f6e206f766572666c6f770000815250611802565b905092915050565b6000808311829015156117e6576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825283818151815260200191508051906020019080838360005b838110156117ab578082015181840152602081019050611790565b50505050905090810190601f1680156117d85780820380516001836020036101000a031916815260200191505b509250505060405180910390fd5b50600083858115156117f457fe5b049050809150509392505050565b600083831115829015156118b1576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825283818151815260200191508051906020019080838360005b8381101561187657808201518184015260208101905061185b565b50505050905090810190601f1680156118a35780820380516001836020036101000a031916815260200191505b509250505060405180910390fd5b506000838503905080915050939250505056fea165627a7a72305820999a86b3db42b69fe0f4ac87ba999f4d6ed0f061792129854028bc824902c2f10029";

    protected NestMiningContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected NestMiningContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<OreDrawingLogEventResponse> getOreDrawingLogEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("OreDrawingLog",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<OreDrawingLogEventResponse> responses = new ArrayList<OreDrawingLogEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            OreDrawingLogEventResponse typedResponse = new OreDrawingLogEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.nowBlock = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.blockAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OreDrawingLogEventResponse> oreDrawingLogEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("OreDrawingLog",
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, OreDrawingLogEventResponse>() {
            @Override
            public OreDrawingLogEventResponse call(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                OreDrawingLogEventResponse typedResponse = new OreDrawingLogEventResponse();
                typedResponse.log = log;
                typedResponse.nowBlock = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.blockAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<TransactionReceipt> oreDrawing() {
        final Function function = new Function(
                "oreDrawing",
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> checkBlockAmountList(BigInteger blockNum) {
        final Function function = new Function("checkBlockAmountList",
                Arrays.<Type>asList(new Uint256(blockNum)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkLatestMining() {
        final Function function = new Function("checkLatestMining",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> changeMapping(String voteFactory) {
        final Function function = new Function(
                "changeMapping",
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(voteFactory)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeBlockAttenuation(BigInteger blockNum) {
        final Function function = new Function(
                "changeBlockAttenuation",
                Arrays.<Type>asList(new Uint256(blockNum)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> checkBlockAttenuation() {
        final Function function = new Function("checkBlockAttenuation",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> takeOutNest(String target) {
        final Function function = new Function(
                "takeOutNest",
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(target)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> checkBlockAmountListLatest() {
        final Function function = new Function("checkBlockAmountListLatest",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<Tuple2<BigInteger, BigInteger>> checkAttenuation() {
        final Function function = new Function("checkAttenuation",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteCall<BigInteger> checkNestBalance() {
        final Function function = new Function("checkNestBalance",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkLatestBlock() {
        final Function function = new Function("checkLatestBlock",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> changeAttenuation(BigInteger top, BigInteger bottom) {
        final Function function = new Function(
                "changeAttenuation",
                Arrays.<Type>asList(new Uint256(top),
                new Uint256(bottom)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<NestMiningContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String voteFactory) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(voteFactory)));
        return deployRemoteCall(NestMiningContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<NestMiningContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String voteFactory) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(voteFactory)));
        return deployRemoteCall(NestMiningContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static NestMiningContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new NestMiningContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static NestMiningContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new NestMiningContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class OreDrawingLogEventResponse {
        public Log log;

        public BigInteger nowBlock;

        public BigInteger blockAmount;
    }
}
