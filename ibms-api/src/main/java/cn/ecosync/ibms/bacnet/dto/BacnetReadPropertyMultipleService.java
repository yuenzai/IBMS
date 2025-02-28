package cn.ecosync.ibms.bacnet.dto;

import cn.ecosync.ibms.JsonSerdeContextHolder;
import cn.ecosync.ibms.serde.JsonSerde;
import cn.ecosync.ibms.util.CollectionUtils;
import cn.ecosync.ibms.util.StringUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
@ToString
public class BacnetReadPropertyMultipleService {
    private static final Logger log = LoggerFactory.getLogger(BacnetReadPropertyMultipleService.class);
    private static final String SEGMENTATION_NOT_SUPPORTED = "BACnet Abort: Segmentation Not Supported";

    @NotNull
    private Integer deviceInstance;
    @NotEmpty
    private Collection<@Valid BacnetObjectProperties> bacnetDataPoints;

    protected BacnetReadPropertyMultipleService() {
    }

    public BacnetReadPropertyMultipleService(Integer deviceInstance, BacnetObjectProperties... bacnetDataPoints) {
        this(deviceInstance, Arrays.asList(bacnetDataPoints));
    }

    public BacnetReadPropertyMultipleService(Integer deviceInstance, Collection<BacnetObjectProperties> bacnetDataPoints) {
        Assert.notNull(deviceInstance, "deviceInstance must not be null");
        Assert.notEmpty(bacnetDataPoints, "bacnetDataPoints must not be null");
        this.deviceInstance = deviceInstance;
        this.bacnetDataPoints = bacnetDataPoints;
    }

    public Collection<BacnetObjectProperties> getBacnetDataPoints() {
        return Collections.unmodifiableCollection(bacnetDataPoints);
    }

    public List<String> toCommand() {
        List<String> commands = new ArrayList<>();
        commands.add("readpropm");
        commands.add(String.valueOf(getDeviceInstance()));

        for (BacnetObjectProperties dataPoint : bacnetDataPoints) {
            commands.add(String.valueOf(dataPoint.getBacnetObject().getObjectType().getCode()));
            commands.add(dataPoint.getBacnetObject().getObjectInstance().toString());
            String propCmdArg = dataPoint.getProperties().stream()
                    .map(this::commandArgOf)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(","));
            commands.add(propCmdArg);
        }
        return commands;
    }

    public String toCommandString() {
        if (CollectionUtils.isEmpty(toCommand())) return "";
        return String.join(" ", toCommand());
    }

    private String commandArgOf(BacnetProperty property) {
        String prop = String.valueOf(property.getPropertyIdentifier().getCode());
        Integer index = property.getPropertyArrayIndex().orElse(null);
        if (index != null) {
            prop += "[" + index + "]";
        }
        return prop;
    }

    private static final AtomicInteger portIncrementer = new AtomicInteger(47900);
    private static final Map<Integer, Integer> deviceInstanceAndPortBindMap = new ConcurrentHashMap<>();

    public static ReadPropertyMultipleAck execute(BacnetReadPropertyMultipleService service) throws IOException, InterruptedException {
        Integer deviceInstance = service.getDeviceInstance();
        Integer port = deviceInstanceAndPortBindMap.computeIfAbsent(deviceInstance, in -> portIncrementer.getAndIncrement());
        List<String> command = service.toCommand();
        log.atInfo().addKeyValue("BACNET_IP_PORT", port).addKeyValue("command", String.join(" ", command)).log("执行命令");
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Map<String, String> environment = processBuilder.environment();
        environment.put("BACNET_IP_PORT", String.valueOf(port));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        String stdout = StreamUtils.copyToString(process.getInputStream(), StandardCharsets.UTF_8)
                .trim();
        int exitCode = process.waitFor();
        log.atInfo().addKeyValue("exitCode", exitCode).addKeyValue("stdout", stdout).log("命令执行完毕");
        if (!StringUtils.hasText(stdout)) return null;
        if (exitCode != 0) {
            if (stdout.startsWith(SEGMENTATION_NOT_SUPPORTED))
                throw new SegmentationNotSupportedException();
            else
                throw new RuntimeException(stdout);
        }
        JsonSerde jsonSerde = JsonSerdeContextHolder.get();
        return jsonSerde.deserialize(stdout, ReadPropertyMultipleAck.class);
    }

    public static class SegmentationNotSupportedException extends RuntimeException {
    }
}
