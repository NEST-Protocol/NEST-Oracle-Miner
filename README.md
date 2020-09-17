### NEST3.0 Automatic quotation program operating instructions

[toc]


#### Introduction
The NEST3.0 automatic quotation program is a sample pair pricing program that submits prices to the NEST oracle automatically. Parameters such as the block interval and quotation gasPrice multiples are not necassarily optimal and should be adjusted according to your own requirements. 

#### Main functions
   *  Check ETH and ERC20 balances
   *  ERC20 token approve for oracle service 
   *  Exchange price update（The example exchange is Huobi）
   *  Initiate a quotation 
   *  Cancel a quotation (automatically determine whether the latest quotation block number of the contract has changed, if the latest block number is greater than the block number when you quote, then cancel the quotation to avoid being stuck in one block with other miner)
   *  Retrieve staking assets


#### Preparation before starting

You must have:

1. A private key with a sufficient balance to provide quotes for a pair. For example, if using the ETH:USDT pair you will need to provide 10.2 ETH  (10 eth plus 0.2 ETH commission) and 10 ETH worth of USDT.  Your private key can be generated via the nestDapp. 
2. An ethereum node URL
3. The ERC20 Token contract address for your pair, e.g. if you choose ETH/USDT then the trading pair requires the contract address `0xdac17f958d2ee523a2206206994597c13d831ec7` for USDT. 
4. The exchange trading pair API to get the market prices, if you are using huobi's API (`https://api.huobi.pro/v1/common/symbols`) and using ETH:USDT you will need to provide that API URL and write `ethusdt` into the pair field so it can get the prices for the ETH:USDT pair

#### Startup and shutdown

1. Run the quotation program：
   * Go to [releases](https://github.com/NEST-Protocol/NEST-oracle-V3-miner/releases) and download the lastest release NEST-oracle-V3-miner.zip file.
   * Double-click start.bat under the root path of the quotation program (from the zip) to run the quotation program, and a window will pop up. Please do not close it. You can view the take-order quotation and retrieve information in the window.

2. Log in：
   * Enter http://127.0.0.1:8088/offer/miningData in the browser, and you will enter the login page with the default user name `nest` and password `nest`.
   * If you need to modify the password, you can modify `nest.user.name` (user name) and `nest.user.password` (password) in `Mining/src/main/resources/application.properties`.

3. Close the quotation process:：
   * Stop mining before closing the quotation program, then wait 10 minutes, and close the window after the quotation asset is retrieved.

#### Settings

1. Ethereum node (required)：
   * The node address must be set first.

2. Set ERC20 address and Huobi trading pair (required):
* For ETH/USDT quotes, fill in `0xdac17f958d2ee523a2206206994597c13d831ec7` for ERC20 address and `ethusdt` for Huobi trading pair. ethusdt corresponds to the huobi API.
   * For nToken quotation, please fill in the corresponding ERC20 address and Huobi trading pair.

3. Set the number of block intervals and multiples of gasPrice (not required):：
   * Can be adjusted according to the situation.

4. Set the agent address and port (not required)：
   * The agent address, which defaults to the native address of 127.0.0.1, if a network agent is used, the agent port must be configured.

5. Set private key (required)：
   * Fill in the private key, some initialization work will be carried out, such as mapping the address of each contract, obtaining the ERC20 token ID, the number of bits, etc. Please be patient

6. Start mining:
   * After the above configuration is completed, click the Modify Status button to modify the status of the miner to open. You can view the block height, the number of quotations, and the hash of the quotation transaction in the background (the window that pops up when you click start.bat).

#### Test quotation

```
1. Comment on the transaction code of the quotation:：
LOG.info("Quotation ETH quantity：{} Quote{}Quantity：{}  The amount of ETH transferred into the contract：{}", offerEthAmount, SYMBOL, offerErc20Amount, payableEth);
List<Type> typeList = Arrays.<Type>asList(
       new Uint256(offerEthAmount),
       new Uint256(new BigInteger(String.valueOf(offerErc20Amount))),
       new Address(ERC20_TOKEN_ADDRESS));
// Initiate transaction
String transactionHash = ethClient.offer(gasPrice, nonce, typeList, payableEth);

2. open http://127.0.0.1:8080/offer/miningData ,After the relevant configuration, modify the startup state。

3. Check the number of quoted ETH and quoted ERC20 in the printed log, and check the data
```



#### Contract interaction

[Contract interaction description](./Mining/README.md)

