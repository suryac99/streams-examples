package org.apache.streams.sysomos.example;

import com.google.common.collect.Maps;
import com.sysomos.SysomosConfiguration;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.mongo.MongoPersistWriter;
import org.apache.streams.sysomos.SysomosProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Creates a Sysomos to Mongo Stream
 */
public class SysomosMongo {

    private final static Logger LOGGER = LoggerFactory.getLogger(SysomosMongo.class);

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        Config sysomos = StreamsConfigurator.config.getConfig("sysomos");
        Config mongo = StreamsConfigurator.config.getConfig("mongo");

        SysomosConfiguration config = new SysomosConfiguration();
        config.setHeartbeatIds(sysomos.getStringList("heartbeatIds"));
        config.setApiBatchSize(sysomos.getLong("apiBatchSize"));
        config.setApiKey(sysomos.getString("apiKey"));
        config.setMinDelayMs(sysomos.getLong("minDelayMs"));
        config.setScheduledDelayMs(sysomos.getLong("scheduledDelayMs"));
        config.setMaxBatchSize(sysomos.getLong("maxBatchSize"));


        SysomosProvider provider = new SysomosProvider(config);
        MongoPersistWriter writer = new MongoPersistWriter();

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 10 * 60 * 1000);
        StreamBuilder builder = new LocalStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(1000), streamConfig);

        builder.newPerpetualStream("SysomosProvider", provider);
        builder.addStreamsPersistWriter("mongo", writer, 1, "SysomosProvider");
        builder.start();
    }
}