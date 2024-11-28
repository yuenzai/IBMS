package cn.ecosync.ibms.orchestration;

import cn.ecosync.ibms.bacnet.dto.BacnetDeviceExtra;
import cn.ecosync.ibms.bacnet.dto.BacnetReadPropertyMultipleService;
import cn.ecosync.ibms.bacnet.query.BacnetReadPropertyMultipleQuery;
import cn.ecosync.ibms.device.dto.DeviceDto;
import cn.ecosync.ibms.device.event.DeviceStatusUpdatedEvent;
import cn.ecosync.ibms.device.query.GetDeviceQuery;
import cn.ecosync.iframework.event.EventBus;
import cn.ecosync.iframework.query.QueryBus;
import cn.ecosync.iframework.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class ReadDeviceStatus {
    public static final String JOB_ID = "ReadDeviceStatus";

    public static void run(QueryBus queryBus, EventBus eventBus, String deviceCode) {
        DeviceDto device = queryBus.execute(new GetDeviceQuery(deviceCode, true));
        if (device == null) {
            log.info("Device not found: {}", deviceCode);
            return;
        }
        if (device.getDeviceExtra() instanceof BacnetDeviceExtra && CollectionUtils.notEmpty(device.getDevicePoints())) {
            BacnetReadPropertyMultipleService readPropertyMultipleService = BacnetMapper.toReadPropertyMultipleService(device);
            BacnetReadPropertyMultipleQuery query = new BacnetReadPropertyMultipleQuery(readPropertyMultipleService);
            Optional.ofNullable(queryBus.execute(query))
                    .map(ack -> BacnetMapper.toDeviceStatus(device, ack))
                    .map(DeviceStatusUpdatedEvent::new)
                    .ifPresent(eventBus::publish);
        }
    }
}
