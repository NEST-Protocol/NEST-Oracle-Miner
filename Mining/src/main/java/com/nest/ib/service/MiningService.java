package com.nest.ib.service;

import java.math.BigInteger;

/**
 * ClassName:MiningService
 * Description:
 */
public interface MiningService {

    /**
     *  quote price
     */
    void offer();

    /**
     *  Retrieve the assets
     */
    void turnOut();

    /**
     * Sets the number of block intervals
     * @param blockInterval
     */
    void updateIntervalBlock(int blockInterval);

    /**
     * Query the number of block intervals
     * @return
     */
    int selectIntervalBlock();

    /**
     * Update miner status
     */
    void startMining();

    /**
     * Check the status of miner
     * @return
     */
    boolean selectStartMining();

    /**
     * Update the private key
     * @param privateKey
     */
    void updateUserPrivateKey(String privateKey);

    /**
     * Check your wallet address
     * @return
     */
    String selectUserWalletAddress();

    /**
     * Set validation period
     * @param blockLimit
     */
    void setBlockLimit(BigInteger blockLimit);

    /**
     * Check your wallet balance
     * @return
     */
    boolean checkWalletBalance();

    /**
     * Set the number of ERC20 tokens
     * @param decimal
     */
    void setErc20Decimal(BigInteger decimal);

    /**
     * Set the fire coin API
     * @param url
     */
    void setHuoBiApi(String url);

    /**
     * Set the ERC20 identity
     * @param symbol
     */
    void setErc20Symbol(String symbol);

    /**
     * Update the fire coin price
     * @return
     */
    boolean updatePrice();
}
