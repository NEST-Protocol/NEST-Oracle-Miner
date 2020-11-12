package com.nest.ib.constant;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author wll
 * @date 2020/7/16 13:22
 */
public interface Constant {

    /**
     * Multiple of gasPrice when initiating cancellation: 2 times
     */
    BigInteger CANCEL_GAS_PRICE = new BigInteger("2");

    /**
     * Default multiple of gasPrice at fetch transaction :12/10
     */
    BigInteger TURNOUT_GAS_PRICE = new BigInteger("12");

    /**
     * Default validation period block interval
     */
    BigInteger DEFAULT_BLOCK_LIMIT = new BigInteger("25");

    /**
     * Offer gasLimit
     */
    BigInteger OFFER_GAS_LIMIT = new BigInteger("600000");

    /**
     * Cancel trade gasLimit
     */
    BigInteger CANEL_GAS_LIMIT = new BigInteger("200000");

    /**
     * Fetch the transaction gasLimit
     */
    BigInteger TURN_OUT_GAS_LIMIT = new BigInteger("200000");

    /**
     * The ETH unit
     */
    BigDecimal UNIT_ETH = new BigDecimal("1000000000000000000");

    /**
     * Quotation commission ratio: 1%
     */
    // BigInteger SERVICE_CHARGE_RATE = new BigInteger("100");

    /**
     * If the price offset exceeds 10%, the minimum quotation amount should be multiplied by 10
     */
    BigDecimal OFFER_PRICE_OFFERSET = new BigDecimal("0.1");

    /**
     * Huobi currency price offset more than 20%, stop mining
     */
    BigDecimal HUOBI_PRICE_OFFERSET = new BigDecimal("0.2");

    /**
     * Gets the number of unretrieved asset contract queries
     */
    BigInteger FIRST_FIND_COUNT = BigInteger.valueOf(50L);

    /**
     * Find maxFindCount records up to 100 quote contracts at a time to find your own quote contract
     */
    BigInteger MAX_FIND_COUNT = BigInteger.valueOf(100L);

}
