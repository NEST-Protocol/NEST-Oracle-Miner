[toc]

***

### NEST3.0 automatic quotation program

***

#####  1. Create private key, node

>Since calling the contract to obtain relevant data and send transactions, you need to interact with the chain, and you need to prepare an Ethereum node URL and private key. The node can apply for free after registration through https://infura.io/.
>
>The assets after quotation need to be retrieved by triggering the contract. Currently, the quotation contract of the account is directly obtained through the contract find interface for retrieval operation.

```java
//Ethereum node
String ETH_NODE = "";
//Private key
String USER_PRIVATE_KEY = "";
Web3j web3j = Web3j.build(new HttpService(ETH_NODE));
Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
```

##### 2. Get Nest Protocol related contract address

>One of the functions of the voting contract in Nest Protocol is to manage all other contract addresses.
>
>The contracts involved in the quotation are: ERC20 token contract, mapping contract, quotation contract, mining pool contract, and price contract.


```java
// nToken mapping
if (!usdtAddress.equalsIgnoreCase(erc20TokenAddress)) {
    // nTokenQuotation contract address
    nTokenFactoryMapping(credentials, web3jFree);
} else { // NEST mining contract
    // Quotation factory address
    String nestFactoryAddress = mapping(credentials, web3jFree, "nest quotation factory", "nest.v3.offerMain");
    AddressEnum.OFFER_CONTRACT_FACTORY_ADDRESS.setValue(nestFactoryAddress);
}
// Quote price contract
String offerPriceAddress = mapping(credentials, web3jFree, "Quoted price", "nest.v3.offerPrice");
AddressEnum.OFFER_PRICE_CONTRACT_ADDRESS.setValue(offerPriceAddress);

// Mining pool contract address
String miningSaveAddress = mapping(credentials, web3jFree, "Mining pool contract", "nest.v3.miningSave");
AddressEnum.MINING_SAVE_CONTRACT_ADDRESS.setValue(miningSaveAddress);
```

##### 3. Authorized quotation contract ERC20

>Quotation needs to transfer ERC20 to the quotation contract. The transfer to ERC20 is executed by the quotation contract calling the ERC20 token contract, so ERC20 authorization is required for the quotation contract.

```java
// View approved amount
BigInteger approveValue = allowance();
BigInteger nonce = ethGetTransactionCount();
// 1.5 times gasPrice, adjustable
BigInteger gasPrice = ethGasPrice().multiply(BigInteger.valueOf(15)).divide(BigInteger.TEN);
// approving
if (approveValue.compareTo(new BigInteger("100000000000000")) <= 0) {

    List<Type> typeList = Arrays.<Type>asList(
            new Address(AddressEnum.OFFER_CONTRACT_FACTORY_ADDRESS.getValue()),
            new Uint256(new BigInteger("999999999999999999999999999999999999999999"))
    );

    Function function = new Function("approve", typeList, Collections.<TypeReference<?>>emptyList());
    String encode = FunctionEncoder.encode(function);
    String transaction = ethSendErc20RawTransaction(gasPrice, nonce, Constant.OFFER_GAS_LIMIT, BigInteger.ZERO, encode);
    LOG.info("One-time authorizationhash：" + transaction);
}
```

##### 4. Set mining block interval

* The first method

  >Get the block height of the last quote by calling the mining pool contract, get the latest block height through the link node, and subtract to get the number of interval blocks between the last quote.
  >
  >Advantages: Directly call the contract, simple and high timeliness.
  >
  >Disadvantages: Quotation will also be made while taking orders, but the quotation is not involved in mining. At this time, the calculated mining block interval is wrong.

  ```java
  // Mining pool contract address
  String miningSaveContractAddress = "";
  // Get mining pool contract object
  NestMiningContract nestMiningContract = NestMiningContract.load(miningSaveContractAddress,web3j,credentials, Contract.GAS_PRICE,Contract.GAS_LIMIT);
  // The block height of the last quote
  BigInteger latestOfferBlockNumber = nestMiningContract.checkLatestMining().sendAsync().get();
  // Current block height
  BigInteger nowBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
  // Get the number of mining block intervals
  BigInteger intervalBlockNumber = nowBlockNumber.subtract(latestOfferBlockNumber);
  ```

* The second method

  >By polling all transactions in the latest block, find the successfully packaged quotation transaction. The current block height minus the height of the latest quote block found is the number of blocks in the mining interval.
  >
  >Advantages: The number of blocks obtained between mining intervals is always correct. When storing the data in the database and performing the operation of retrieving assets, you can quickly find the contract address that needs to be retrieved.
  >
  >Disadvantages: Constant polling increases the frequency of access to nodes and increases node pressure. The current block height and the last quote block height found may not be the data at the same point in time, causing the new quote to be packaged and not detected.

  ```java
  // Quotation contract address
  String offerContractAddress = "";
  // Get the current block height
  BigInteger nowBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
  // Obtain block data according to block height
  EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(nowBlockNumber), true).sendAsync().get().getBlock();
  // Get all transaction data in the block
  List<EthBlock.TransactionResult> transactions = block.getTransactions();
  // Traverse all transactions to find the quoted transaction
  for (EthBlock.TransactionResult tx : transactions) {
  	EthBlock.TransactionObject transaction = (EthBlock.TransactionObject) tx.get();
  	// Get the name of the contract method called by the transaction
  	String input = transaction.getInput();
  	// If the input length is less than 10, it means that it is just a normal transfer and does not involve calling the contract
      if(input.length() < 10){
      	continue;
      }
      // The first 10 digits of the input string are the name of the contract method called
      String substring = input.substring(0, 10);
      if(substring.equalsIgnoreCase("0xf6a4932f")) {
          String to = transaction.getTo();
          // Since the contract method name may be repeated, check whether the callee is the quotation contract address
  		if(to.equalsIgnoreCase(offerContractAddress)){
  			System.out.println("The latest quote block height is：" + nowBlockNumber);
  		}
  	}
  }
  ```

##### 5. Set the quoted ERC20 and ETH quantity

>Obtain the ERC20-ETH price through the exchange API with a high trading frequency, and calculate the amount of ERC20 and ETH required to obtain a 10ETH quote. If the price deviates by more than 10%, a 10-fold quotation is required, or a multiple ETH quotation is required, just multiply the corresponding multiple when quoting.
>
>Some exchange APIs require overseas nodes to access, the following is the Huobi Exchange API:

```java
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
```

##### 6. Initiate a quote transaction

>If the ETH/ERC20 price currently quoted deviates from the latest effective price in the price contract by more than 10%, then the quoted asset needs to be 10 times, and the fee paid for the quoted price remains unchanged (10 times is not required).
>
>When the set interval block meets the requirements and the ETH and ERC20 quantities required for the quotation have been calculated, then the quotation can be initiated directly at this time.

```java
// Check whether the set block interval is met
boolean b = checkBlockInterval(ethBlockNumber, miningBlockNumber, gasPrice);
if (!b) {
    return null;
}

// Take the latest effective price in the current contract and compare it with the price currently prepared for quotation. If the deviation exceeds 10%, then 10 times the quotation
BigDecimal otherMinerOfferPrice = checkPriceNow();

// Current exchange price
BigDecimal companyPrice = calPrice(OFFER_ERC20_AMOUNT, Constant.OFFER_ETH_AMOUNT);

// The quotation needs to be entered into the amount of ETH in the contract
BigInteger payableEth = Constant.PAYABLE_ETH_AMOUNT;
// Quotation ETH quantity
BigInteger offerEthAmount = Constant.OFFER_ETH_AMOUNT;
// Quotation ERC20 quantity
BigInteger offerErc20Amount = OFFER_ERC20_AMOUNT;

// Price offset
BigDecimal offset = (otherMinerOfferPrice.subtract(companyPrice)).divide(companyPrice, 3, BigDecimal.ROUND_DOWN).abs();
// The price deviation exceeds 10%, 10 times the quotation
if (offset.compareTo(Constant.OFFER_PRICE_OFFERSET) > 0) {
    offerEthAmount = Constant.OFFER_ETH_AMOUNT.multiply(BigInteger.TEN);
    offerErc20Amount = OFFER_ERC20_AMOUNT.multiply(BigInteger.TEN);
    payableEth = offerEthAmount.add(offerEthAmount.divide(Constant.SERVICE_CHARGE_RATE));
}
LOG.info("Quotation ETH quantity：{} Quote{}Quantity：{}  The amount of ETH transferred into the contract：{}", offerEthAmount, SYMBOL, offerErc20Amount, payableEth);

List<Type> typeList = Arrays.<Type>asList(
        new Uint256(offerEthAmount),
        new Uint256(new BigInteger(String.valueOf(offerErc20Amount))),
        new Address(ERC20_TOKEN_ADDRESS));
String transactionHash = ethClient.offer(gasPrice, nonce, typeList, payableEth);
```

##### 7. find interface to get your own quotation contract


```java
// Get your own quotation contract directly through the find interface
String data = nest3OfferMain.find(start, count, Constant.MAX_FIND_COUNT, credentials.getAddress()).send();
if (StringUtils.isEmpty(contracts)) {
    return null;
}
// Analyze the data returned by the find interface
List<OfferContractData> offerContractDataList = transformToOfferContractData(contracts);

// Traverse the contract sheet to see if it is retrieved

//Unclaimed contract
List<OfferContractData> list = new ArrayList<>();
int index = offerContractDataList.size() - 1;
boolean set = false;
for (int i = index; i >= 0; i--) {

    OfferContractData contractData = offerContractDataList.get(i);

    BigInteger tokenAmount = new BigInteger(contractData.getTokenAmount());
    BigInteger serviceCharge = new BigInteger(contractData.getServiceCharge());
    BigInteger ethAmount = new BigInteger(contractData.getEthAmount());
    BigInteger blockNumber = new BigInteger(contractData.getBlockNum());

    // Can be retrieved after T0 time
    if (nowBlockNumber.subtract(blockNumber).compareTo(BLOCK_LIMIT) < 0) {
        continue;
    }

    // The balance or service fee is greater than 0, and there are assets that have not been retrieved
    if (tokenAmount.compareTo(BigInteger.ZERO) > 0
            || serviceCharge.compareTo(BigInteger.ZERO) > 0
            || ethAmount.compareTo(BigInteger.ZERO) > 0) {
        list.add(contractData);
    }
}
```

##### 8. Contract asset

>”7“Quotation contracts that have been obtained but not retrieved but have passed 25 blocks can be retrieved directly according to the quotation contract.

```java
// Quotation contract address
String offerContractAddress = "";
// The quotation contract address that needs to be retrieved
String contractAddress = "";
// Set to retrieve gasPrice, you can set it according to your needs
BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice().multiply(new BigInteger("2"));
// setting nonce
BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
// Set up retrieval gasLimit(If the account ETH balance is not large, it can be obtained by estimation)
BigInteger gasLimit = new BigInteger("500000");
// Send and retrieve transaction
Function function = new Function(
	"turnOut",
	Arrays.<Type>asList(new Address(offerContractAddress)),
	Collections.<TypeReference<?>>emptyList();
String encode = FunctionEncoder.encode(function);
RawTransaction rawTransaction = RawTransaction.createTransaction(
	nonce,
	gasPrice,
	gasLimit,
	offerContractAddress,
	encode);
byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,credentials);
String hexValue = Numeric.toHexString(signedMessage);
String transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
```

##### 9. Cancel offer

>When the quotation transaction is sent out, but the mining interval block becomes insufficient, you can call the cancel quotation operation.

```java
// Record the nonce value when quoting, and specify the transaction to be cancelled according to the nonce value
BigInteger nonce = "";
// Transfer ETH amount
BigInteger value = new BigInteger("0");
// setting gasLimit
BigInteger gasLimit = new BigInteger("200000");
// Cancel the quotation operation by transferring money to yourself
RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, credentials.getAddress(), value);
byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
String hexValue = Numeric.toHexString(signMessage);
EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
String transactionHash = ethSendTransaction.getTransactionHash();
```

