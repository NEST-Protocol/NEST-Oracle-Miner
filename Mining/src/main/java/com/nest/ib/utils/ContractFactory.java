package com.nest.ib.utils;

import com.nest.ib.constant.AddressEnum;
import com.nest.ib.contract.*;
import com.nest.ib.service.serviceImpl.MiningServiceImpl;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;

/**
 * @author wll
 * @date 2020/7/16 13:29
 * Contract class creation
 */
public class ContractFactory {

    /**
     * ERC20 contracts
     */
    public static ERC20 erc20(Credentials credentials, Web3j web3j) {
        return ERC20.load(MiningServiceImpl.ERC20_TOKEN_ADDRESS, web3j, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
    }

    /**
     * Pit contract mining logic
     */
    public static NestMiningContract nestMiningContract(Credentials credentials, Web3j web3jFree) {
        return NestMiningContract.load(AddressEnum.MINING_SAVE_CONTRACT_ADDRESS.getValue(), web3jFree, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
    }

    /**
     * Contract price
     */
    public static NestOfferPriceContract nestOfferPriceContract(Credentials credentials, Web3j web3jFree) {
        return NestOfferPriceContract.load(AddressEnum.OFFER_PRICE_CONTRACT_ADDRESS.getValue(), web3jFree, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
    }

    /**
     * Offer factory
     */
    public static Nest3OfferMain nest3OfferMain(Credentials credentials, Web3j web3jFree) {
        return Nest3OfferMain.load(AddressEnum.OFFER_CONTRACT_FACTORY_ADDRESS.getValue(), web3jFree, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
    }

    /**
     * NToken contracts
     */
    public static NTokenContract nTokenContract(Credentials credentials, Web3j web3j){
        return NTokenContract.load(AddressEnum.NTOKEN_CONTRACT_ADDRESS.getValue(), web3j, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
    }

}
