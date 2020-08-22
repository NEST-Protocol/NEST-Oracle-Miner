package com.nest.ib.model;

import java.io.Serializable;

/**
 * @author wll
 * @date 2020/7/17 12:13
 * Quotation contract data
 */
public class OfferContractData implements Serializable {

    private static final long serialVersionUID = 5920753423098691391L;

    /**
     * Address of quotation contract list
     */
    private String uuid;

    /**
     * Quotation owner
     */
    private String owner;

    /**
     * The ERC20 contract address of the target quote Token
     */
    private String tokenAddress;

    /**
     * Eth asset book in quotation sheet
     */
    private String ethAmount;

    /**
     * Token assets in the quotation book
     */
    private String tokenAmount;

    /**
     * Number of eth remaining tradable
     */
    private String dealEthAmount;

    /**
     * The number of tradable tokens remaining
     */
    private String dealTokenAmount;

    /**
     * The block number of the quotation
     */
    private String blockNum;

    /**
     * Charges for mining
     */
    private String serviceCharge;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getEthAmount() {
        return ethAmount;
    }

    public void setEthAmount(String ethAmount) {
        this.ethAmount = ethAmount;
    }

    public String getTokenAmount() {
        return tokenAmount;
    }

    public void setTokenAmount(String tokenAmount) {
        this.tokenAmount = tokenAmount;
    }

    public String getDealEthAmount() {
        return dealEthAmount;
    }

    public void setDealEthAmount(String dealEthAmount) {
        this.dealEthAmount = dealEthAmount;
    }

    public String getDealTokenAmount() {
        return dealTokenAmount;
    }

    public void setDealTokenAmount(String dealTokenAmount) {
        this.dealTokenAmount = dealTokenAmount;
    }

    public String getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(String blockNum) {
        this.blockNum = blockNum;
    }

    public String getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(String serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    @Override
    public String toString() {
        return "OfferContractData{" +
                "uuid='" + uuid + '\'' +
                ", owner='" + owner + '\'' +
                ", tokenAddress='" + tokenAddress + '\'' +
                ", ethAmount='" + ethAmount + '\'' +
                ", tokenAmount='" + tokenAmount + '\'' +
                ", dealEthAmount='" + dealEthAmount + '\'' +
                ", dealTokenAmount='" + dealTokenAmount + '\'' +
                ", blockNum='" + blockNum + '\'' +
                ", serviceCharge='" + serviceCharge + '\'' +
                '}';
    }
}
