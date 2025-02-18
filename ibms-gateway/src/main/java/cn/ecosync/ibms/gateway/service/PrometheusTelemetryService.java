package cn.ecosync.ibms.gateway.service;

import cn.ecosync.ibms.gateway.model.DeviceDataAcquisition;
import cn.ecosync.ibms.gateway.model.DeviceDataAcquisitionRepository;
import cn.ecosync.ibms.gateway.model.DeviceInfos.DeviceInfo;
import cn.ecosync.ibms.gateway.model.PrometheusConfigurationProperties.RelabelConfig;
import cn.ecosync.ibms.gateway.model.PrometheusConfigurationProperties.ScrapeConfig;
import cn.ecosync.ibms.gateway.model.PrometheusConfigurationProperties.ScrapeConfigs;
import cn.ecosync.ibms.gateway.model.PrometheusConfigurationProperties.StaticConfig;
import cn.ecosync.ibms.util.CollectionUtils;
import cn.ecosync.ibms.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cn.ecosync.ibms.Constants.PATH_METRICS;
import static cn.ecosync.ibms.Constants.PATH_METRICS_DEVICES;

public class PrometheusTelemetryService implements TelemetryService, MultiCollector, ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PrometheusTelemetryService.class);

    private final DeviceDataAcquisitionRepository dataAcquisitionRepository;
    private final ObjectMapper yamlSerde;
    private final String gatewayHost;
    private final String gatewayCode;
    private final File scrapeConfigFile;
    private final AtomicReference<Map<String, DeviceInfo>> deviceInfosRef = new AtomicReference<>(new HashMap<>());
    private final AtomicReference<Map<String, MultiCollector>> instrumentsRef = new AtomicReference<>(new HashMap<>());

    public PrometheusTelemetryService(DeviceDataAcquisitionRepository dataAcquisitionRepository, Environment environment) {
        this.dataAcquisitionRepository = dataAcquisitionRepository;
        this.yamlSerde = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.gatewayHost = environment.getRequiredProperty("IBMS_HOST");
        this.gatewayCode = environment.getRequiredProperty("IBMS_GATEWAY_CODE");
        this.scrapeConfigFile = new File("scrape_config_file.yml");
    }

    @Override
    public void reload() {
        log.info("reload...");
        Map<String, DeviceInfo> deviceInfos = new HashMap<>();
        Map<String, MultiCollector> instruments = new HashMap<>();
        List<ScrapeConfig> scrapeConfigs = new ArrayList<>();
        ScrapeConfig gatewayScrapeConfig = new ScrapeConfig(gatewayCode, "/ibms" + PATH_METRICS, 30, new StaticConfig(gatewayHost));
        scrapeConfigs.add(gatewayScrapeConfig);

        List<DeviceDataAcquisition> dataAcquisitions = dataAcquisitionRepository.search(Pageable.unpaged()).getContent();
        for (DeviceDataAcquisition dataAcquisition : dataAcquisitions) {
            dataAcquisition.getDeviceInfos()
                    .getDeviceInfos()
                    .forEach(in -> deviceInfos.put(in.getDeviceCode(), in));
            dataAcquisition.getDataPoints().newInstruments(instruments::put);
            ScrapeConfig scrapeConfig = toScrapeConfig(dataAcquisition);
            scrapeConfigs.add(scrapeConfig);
        }

        try {
            yamlSerde.writeValue(scrapeConfigFile, new ScrapeConfigs(scrapeConfigs));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        deviceInfosRef.set(deviceInfos);
        instrumentsRef.set(instruments);
    }

    @Override
    public MetricSnapshots collect(PrometheusScrapeRequest scrapeRequest) {
        String deviceCode = CollectionUtils.firstElement(Arrays.asList(scrapeRequest.getParameterValues("target")));
        log.info("collect(requestPath={}, target={})", scrapeRequest.getRequestPath(), deviceCode);

        Map<String, DeviceInfo> deviceInfos = deviceInfosRef.get();
        Info deviceInfo = Optional.ofNullable(deviceInfos.get(deviceCode))
                .map(DeviceInfo::toDeviceInfo)
                .orElse(null);

        Map<String, MultiCollector> instruments = instrumentsRef.get();
        MetricSnapshots metricSnapshots = Optional.ofNullable(deviceCode)
                .filter(StringUtils::hasText)
                .map(instruments::get)
                .map(MultiCollector::collect)
                .orElseGet(this::collect);
        if (deviceInfo != null) {
            MetricSnapshots.Builder metricsBuilder = MetricSnapshots.builder();
            metricsBuilder.metricSnapshot(deviceInfo.collect());
            metricSnapshots.stream()
                    .forEach(metricsBuilder::metricSnapshot);
            return metricsBuilder.build();
        }
        return metricSnapshots;
    }

    @Override
    public MetricSnapshots collect() {
        return new MetricSnapshots();
    }

    private ScrapeConfig toScrapeConfig(DeviceDataAcquisition dataAcquisition) {
        Set<String> deviceCodes = dataAcquisition.getDataPoints()
                .toCollection().stream()
                .map(in -> in.getDataPointId().getDeviceCode())
                .collect(Collectors.toSet());
        String jobName = dataAcquisition.getDataAcquisitionId().toString();
        StaticConfig staticConfig = new StaticConfig(deviceCodes, Collections.singletonMap("target_type", "device"));
        List<RelabelConfig> relabelConfigs = RelabelConfig.toRelabelConfigs(gatewayHost);
        return new ScrapeConfig(jobName, "/ibms" + PATH_METRICS_DEVICES, dataAcquisition.getScrapeInterval(), relabelConfigs, staticConfig);
    }

    @Override
    public void run(ApplicationArguments args) {
        reload();
    }
}
