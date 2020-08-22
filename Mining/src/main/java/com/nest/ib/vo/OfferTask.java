package com.nest.ib.vo;

import com.nest.ib.service.MiningService;

import com.nest.ib.utils.EthClient;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import sun.security.krb5.Credentials;

/**
 * ClassName:applicationBonusStorage
 * Description:
 */
@Component
public class OfferTask {

    @Autowired
    private MiningService miningService;
    @Autowired
    EthClient ethClient;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(50);
        return taskScheduler;
    }

    /**
     * quote price：ETH/ERC20:3 seconds
     */
    @Scheduled(fixedDelay = 3 * 1000)
    public void offer() {
        miningService.offer();
    }

    /**
     * Update huocoin exchange price:3 seconds
     */
    @Scheduled(fixedDelay = 3 * 1000)
    public void updatePrice() {
        miningService.updatePrice();
    }

    /**
     * Retrieve the assets:2 minutes
     */
    @Scheduled(fixedDelay = 2 * 60 * 1000)
    public void turnOut() {
        miningService.turnOut();
    }

}
