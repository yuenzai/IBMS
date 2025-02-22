package cn.ecosync.ibms.gateway.model;

import cn.ecosync.ibms.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@Getter
@ToString
public class PrometheusConfigurationProperties {
    @Getter
    @ToString
    public static class ScrapeConfigs {
        private List<ScrapeConfig> scrapeConfigs;

        protected ScrapeConfigs() {
        }

        public ScrapeConfigs(List<ScrapeConfig> scrapeConfigs) {
            this.scrapeConfigs = scrapeConfigs;
        }

        public List<ScrapeConfig> getScrapeConfigs() {
            return CollectionUtils.nullSafeOf(scrapeConfigs);
        }
    }

    @Getter
    @ToString
    public static class ScrapeConfig {
        private String jobName;
        private String metricsPath;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String scrapeInterval;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String scrapeTimeout;
        private List<RelabelConfig> relabelConfigs;
        private List<StaticConfig> staticConfigs;

        protected ScrapeConfig() {
        }

        public ScrapeConfig(String jobName, String metricsPath, Integer scrapeInterval, StaticConfig... staticConfigs) {
            this(jobName, metricsPath, scrapeInterval, null, null, staticConfigs);
        }

        public ScrapeConfig(String jobName, String metricsPath, Integer scrapeInterval, Integer scrapeTimeout, List<RelabelConfig> relabelConfigs, StaticConfig... staticConfigs) {
            this.jobName = jobName;
            this.metricsPath = metricsPath;
            this.scrapeInterval = Optional.ofNullable(scrapeInterval)
                    .map(in -> in + "s")
                    .orElse(null);
            this.scrapeTimeout = Optional.ofNullable(scrapeTimeout)
                    .map(in -> in + "s")
                    .orElse(null);
            this.relabelConfigs = relabelConfigs;
            this.staticConfigs = Arrays.asList(staticConfigs);
        }

        public List<RelabelConfig> getRelabelConfigs() {
            return CollectionUtils.nullSafeOf(relabelConfigs);
        }

        public List<StaticConfig> getStaticConfigs() {
            return CollectionUtils.nullSafeOf(staticConfigs);
        }
    }

    @Getter
    @ToString
    public static class StaticConfig {
        private Collection<String> targets;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private Map<String, String> labels;

        protected StaticConfig() {
        }

        public StaticConfig(String... targets) {
            this(Arrays.asList(targets), Collections.emptyMap());
        }

        public StaticConfig(Collection<String> targets, Map<String, String> labels) {
            this.targets = targets;
            this.labels = labels;
        }

        public Collection<String> getTargets() {
            return CollectionUtils.nullSafeOf(targets);
        }

        public Map<String, String> getLabels() {
            return CollectionUtils.nullSafeOf(labels);
        }
    }

    @Getter
    @ToString
    public static class RelabelConfig {
        private static final RelabelConfig RELABEL1 = new RelabelConfig(Collections.singletonList("__address__"), "__param_target", null);
        private static final RelabelConfig RELABEL2 = new RelabelConfig(Collections.singletonList("__param_target"), "instance", null);

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> sourceLabels;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String targetLabel;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String replacement;

        protected RelabelConfig() {
        }

        private RelabelConfig(List<String> sourceLabels, String targetLabel, String replacement) {
            this.sourceLabels = sourceLabels;
            this.targetLabel = targetLabel;
            this.replacement = replacement;
        }

        public static List<RelabelConfig> toRelabelConfigs(String replacement) {
            return Arrays.asList(RELABEL1, RELABEL2, new RelabelConfig(null, "__address__", replacement));
        }
    }
}
