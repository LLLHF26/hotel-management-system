package com.lhf.hotel.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Properties;

public class KafkaLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String HOSTNAME = resolveHostname();

    private String topic = "logs-topic";
    private String bootstrapServers = "localhost:9091";
    private String appName = "unknown";
    private KafkaProducer<String, String> producer;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public void start() {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            addError("bootstrapServers 未配置");
            return;
        }
        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
            props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "5000");
            props.put(ProducerConfig.CLIENT_ID_CONFIG, appName + "-log-producer");
            producer = new KafkaProducer<>(props);
        } catch (Exception e) {
            addError("创建 KafkaProducer 失败", e);
            return;
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted() || producer == null) return;
        String json = toJson(event);
        producer.send(new ProducerRecord<>(topic, json), (metadata, ex) -> {
            if (ex != null) {
                addError("发送日志到 Kafka 失败", ex);
            }
        });
    }

    @Override
    public void stop() {
        if (producer != null) {
            try {
                producer.flush();
                producer.close(java.time.Duration.ofSeconds(5));
            } catch (Exception ignored) {
            }
            producer = null;
        }
        super.stop();
    }

    private String toJson(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{\"timestamp\":\"");
        sb.append(Instant.ofEpochMilli(event.getTimeStamp()).toString());
        sb.append("\",\"level\":\"");
        sb.append(event.getLevel().levelStr);
        sb.append("\",\"logger\":\"");
        sb.append(escapeJson(event.getLoggerName()));
        sb.append("\",\"message\":\"");
        sb.append(escapeJson(event.getFormattedMessage()));
        sb.append("\",\"thread\":\"");
        sb.append(escapeJson(event.getThreadName()));
        sb.append("\",\"app\":\"");
        sb.append(escapeJson(appName));
        sb.append("\",\"host\":\"");
        sb.append(HOSTNAME);
        sb.append('"');
        IThrowableProxy tp = event.getThrowableProxy();
        if (tp != null) {
            sb.append(",\"stackTrace\":\"");
            sb.append(escapeJson(ThrowableProxyUtil.asString(tp)));
            sb.append('"');
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
