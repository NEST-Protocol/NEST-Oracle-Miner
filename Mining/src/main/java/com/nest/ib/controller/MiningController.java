package com.nest.ib.controller;

import com.nest.ib.service.MiningService;

import com.nest.ib.service.serviceImpl.MiningServiceImpl;
import com.nest.ib.utils.EthClient;
import com.nest.ib.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;

/**
 * ClassName:MiningController
 * Description:
 */
@RestController
@RequestMapping("/offer")
public class MiningController {

    @Autowired
    private MiningService miningService;

    @Autowired
    private EthClient ethClient;


    /**
     * On/off mining:True on,false off
     */
    @PostMapping("/startMining")
    public boolean startMining() {
        miningService.startMining();
        return miningService.selectStartMining();
    }

    /**
     * Sets the number of block intervals
     */
    @PostMapping("/updateIntervalBlock")
    public int updateIntervalBlock(@RequestParam(name = "intervalBlock") int intervalBlock) {
        miningService.updateIntervalBlock(intervalBlock);
        return miningService.selectIntervalBlock();
    }

    /**
     * Set the account private key
     */
    @PostMapping("/updatePrivateKey")
    public String updateUserPrivatekey(@RequestParam(name = "privateKey") String privateKey) {
        miningService.updateUserPrivateKey(privateKey);
        return miningService.selectUserWalletAddress();
    }

    /**
     * Set the quote gasPrice
     */
    @PostMapping("/updateOfferGasPrice")
    public BigDecimal updateOfferGasPrice(@RequestParam(name = "offerGasPrice") BigDecimal gasPrice) {
        MiningServiceImpl.OFFER_GAS_PRICE = gasPrice;
        return MiningServiceImpl.OFFER_GAS_PRICE;
    }

    /**
     * Set the node
     */
    @PostMapping("/updateNode")
    public String updateNode(@RequestParam(name = "node") String node) {
        ethClient.initNode(node);

        // If the key has been filled in, the address and transaction pairs that have been reset need to be re-registered
        ethClient.resetBean();
        return node;
    }

    /**
     * Set token addresses and transaction pairs
     */
    @PostMapping("/updateErc20")
    public String updateErc20(@RequestParam(name = "erc20Addr") String erc20Addr, @RequestParam(name = "symbols") String symbols) {
        MiningServiceImpl.ERC20_TOKEN_ADDRESS = erc20Addr;
        MiningServiceImpl.SYMBOLS = symbols.toLowerCase();

        // If the key has been filled in, the address and transaction pairs that have been reset need to be re-registered
        ethClient.resetBean();
        return "ok";
    }

    /**
     * Set the network agent address and port
     * @param proxyIp
     * @param proxyPort
     * @return
     */
    @PostMapping("/updateProxy")
    public String updateProxy(@RequestParam(name = "proxyIp") String proxyIp, @RequestParam(name = "proxyPort") int proxyPort) {
        HttpClientUtil.updateProxy(proxyIp, proxyPort);
        return "ok";
    }

    /**
     * View miner details
     */
    @GetMapping("/miningData")
    public ModelAndView miningData() {
        String address = miningService.selectUserWalletAddress();
        if (address == null) address = "Please fill in the correct private key first";
        int intervalBlock = miningService.selectIntervalBlock();
        boolean b = miningService.selectStartMining();

        ModelAndView mav = new ModelAndView("miningData");
        mav.addObject("address", address);
        mav.addObject("node", EthClient.NODE);
        mav.addObject("erc20Addr", MiningServiceImpl.ERC20_TOKEN_ADDRESS);
        mav.addObject("symbols", MiningServiceImpl.SYMBOLS);
        mav.addObject("intervalBlock", intervalBlock);
        mav.addObject("startMining", b == true ? "Mill state: open" : "Mill state: close");
        mav.addObject("offerGasPrice", MiningServiceImpl.OFFER_GAS_PRICE);
        mav.addObject("proxyIp", HttpClientUtil.getProxyIp());
        mav.addObject("proxyPort", HttpClientUtil.getProxyPort());

        return mav;
    }

}
