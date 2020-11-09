package com.base;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class ReadFileService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ScheduledExecutorService sendMailServiceExecutor;

    private final ScheduledExecutorService sendMailTimeServiceExecutor;

    private final MailService mailService;

    private final NatAppConfig natAppConfig;

    private final HeartbeatConfig heartbeatConfig;

    private final MailConfig mailConfig;

    private final AtomicReference<String> urlAtomicReference = new AtomicReference<>("");

    private final AtomicBoolean urlAtomicReferenceIsOk = new AtomicBoolean(false);

    public ReadFileService(@Qualifier("sendMailServiceExecutor") ScheduledExecutorService sendMailServiceExecutor,
                           @Qualifier("sendMailTimeServiceExecutor") ScheduledExecutorService sendMailTimeServiceExecutor,
                           MailService mailService,
                           NatAppConfig natAppConfig,
                           HeartbeatConfig heartbeatConfig,
                           MailConfig mailConfig) {
        this.sendMailServiceExecutor = sendMailServiceExecutor;
        this.sendMailTimeServiceExecutor = sendMailTimeServiceExecutor;
        this.mailService = mailService;
        this.natAppConfig = natAppConfig;
        this.heartbeatConfig = heartbeatConfig;
        this.mailConfig = mailConfig;
    }

    void asyncSendMail(){

        send();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        sendMailServiceExecutor.schedule(this::send, mailConfig.getDelay(), TimeUnit.SECONDS);

        sendMailTimeServiceExecutor.scheduleWithFixedDelay(this::heartbeat, heartbeatConfig.getInitialDelay(), heartbeatConfig.getDelay(), TimeUnit.SECONDS);
    }

    private void heartbeat(){
        try {
            if (!StringUtils.isEmpty(urlAtomicReference.get())) {
                urlAtomicReferenceIsOk.set(HttpRequest.get(urlAtomicReference.get()).execute().isOk());
                logger.warn("服务{}，心跳检测返回：{}", urlAtomicReference.get(), urlAtomicReferenceIsOk.get());
            }


            if (!urlAtomicReferenceIsOk.get()) {
                boolean localIsOk = HttpRequest.get(heartbeatConfig.getLocal()).execute().isOk();
                logger.warn("服务{}，心跳检测返回：{}", heartbeatConfig.getLocal(), localIsOk);

                if (localIsOk) {
                    Process proc = null;
                    try {
                        proc = Runtime.getRuntime().exec(natAppConfig.getPerformStart());
                        proc.waitFor();
                    }catch (Throwable t) {
                        logger.warn("心跳检测执行脚本: {} 失败: {}", natAppConfig.getPerformStart(), t.getMessage());
                    }finally {
                        if (proc != null) {
                            proc.destroy();
                            send();
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            logger.warn("心跳检测失败: {}", e.getMessage());
        }

    }

    private void shutdown(){
        sendMailServiceExecutor.shutdown();
        sendMailTimeServiceExecutor.shutdown();
    }

    private void send(){
        final String urls = redsFile();
        logger.warn("当前服务访问地址: " + urls);

        if (!StringUtils.isEmpty(urls) && !urlAtomicReference.get().equals(urls)) {
            urlAtomicReference.set(urls);
            mailService.sendHtmlMail(urls);
        }
    }

    MailApi sendApi(){
        final String urls = redsFile();
        logger.warn("当前服务访问地址: " + urls);

        if (!StringUtils.isEmpty(urls) && !urlAtomicReference.get().equals(urls)) {
            urlAtomicReference.set(urls);
            return new MailApi(0, "获取成功", urls);
        }
        return new MailApi(1, "获取失败", new Object());
    }

    private synchronized String redsFile(){
        try {
            final List<String> lineData = FileUtil.readLines(natAppConfig.getLog(), CharsetUtil.CHARSET_UTF_8, new LinkedList<>());
            logger.warn("lineData size: {}", lineData.size());
            final List<String> panList = lineData.stream().filter(x -> x.contains(natAppConfig.getPan())).collect(Collectors.toList());

            final AtomicReference<List<String>> atomicReference = new AtomicReference<>();
            panList
                    .stream()
                    .map(x -> {
                        final String strDate = x.substring(1, 20);
                        return DateUtil.parse(strDate, natAppConfig.getFormat());
                    })
                    .max(Date::compareTo)
                    .ifPresent(y -> {
                        final String dateTime = DateUtil.format(y, natAppConfig.getFormat());

                        logger.warn("最早日期：{}", dateTime);

                        atomicReference.set(
                                panList
                                        .stream()
                                        .filter(x -> x.contains(dateTime))
                                        .map(x -> {
                                            final Integer index = x.indexOf(natAppConfig.getPan());
                                            final Integer len = x.length();
                                            return x.substring(index, len);
                                        }).collect(Collectors.toList())
                        );

                    });

            List<String> result = atomicReference.get();
            if (CollectionUtils.isEmpty(result)) {
                return "";
            }

            return result.get(0);

        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }
}
