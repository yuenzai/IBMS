package cn.ecosync.ibms.orchestration;

import cn.ecosync.ibms.bacnet.BacnetMapper;
import cn.ecosync.ibms.bacnet.model.BacnetDeviceExtra;
import cn.ecosync.ibms.bacnet.model.ReadPropertyMultipleAck;
import cn.ecosync.ibms.bacnet.query.BacnetReadPropertyMultipleBatchQuery;
import cn.ecosync.ibms.bacnet.service.BacnetReadPropertyMultiple;
import cn.ecosync.ibms.device.event.DeviceStatusUpdatedEvent;
import cn.ecosync.ibms.device.model.DeviceDto;
import cn.ecosync.ibms.device.model.DeviceStatus;
import cn.ecosync.ibms.device.query.SearchDeviceListQuery;
import cn.ecosync.ibms.event.EventBus;
import cn.ecosync.ibms.query.QueryBus;
import cn.ecosync.ibms.scheduling.model.SchedulingTaskParams;
import cn.ecosync.ibms.util.CollectionUtils;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadDeviceStatusBatch {
    public static final String JOB_ID = "ReadDeviceStatusBatch";

    public static void run(QueryBus queryBus, EventBus eventBus) {
        List<DeviceDto> deviceDtoList = queryBus.execute(new SearchDeviceListQuery(true)).stream()
                .filter(in -> in.getDeviceExtra() instanceof BacnetDeviceExtra && CollectionUtils.notEmpty(in.getDevicePoints()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deviceDtoList)) {
            return;
        }

        List<BacnetReadPropertyMultiple> services = deviceDtoList.stream()
                .map(BacnetMapper::toReadPropertyMultipleService)
                .collect(Collectors.toList());

        List<ReadPropertyMultipleAck> acks = queryBus.execute(new BacnetReadPropertyMultipleBatchQuery(services));
        if (CollectionUtils.isEmpty(acks)) {
            return;
        }

        Map<Integer, DeviceDto> deviceInstanceMap = CollectionUtils.newHashMap(deviceDtoList.size());
        for (DeviceDto deviceDto : deviceDtoList) {
            Integer deviceInstance = BacnetMapper.toDeviceInstance(deviceDto).orElse(null);
            if (deviceInstance == null) {
                continue;
            }
            deviceInstanceMap.put(deviceInstance, deviceDto);
        }

        for (ReadPropertyMultipleAck ack : acks) {
            DeviceDto deviceDto = deviceInstanceMap.get(ack.getDeviceInstance());
            if (deviceDto == null) {
                continue;
            }
            DeviceStatus deviceStatus = BacnetMapper.toDeviceStatus(deviceDto, ack);
            eventBus.publish(new DeviceStatusUpdatedEvent(deviceStatus));
        }
    }

    @Getter
    @ToString
    public static class TaskParams implements SchedulingTaskParams {
        @Override
        public String type() {
            return JOB_ID;
        }

        @Override
        public Map<String, Object> toParams() {
            return Collections.emptyMap();
        }
    }
}
