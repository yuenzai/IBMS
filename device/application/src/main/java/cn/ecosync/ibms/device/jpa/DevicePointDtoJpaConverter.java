package cn.ecosync.ibms.device.jpa;

import cn.ecosync.ibms.device.dto.DevicePointDto;
import cn.ecosync.ibms.jpa.ListJpaConverter;
import cn.ecosync.ibms.serde.JsonSerde;

import javax.persistence.Converter;

@Converter
public class DevicePointDtoJpaConverter extends ListJpaConverter<DevicePointDto> {
    public DevicePointDtoJpaConverter() {
    }

    public DevicePointDtoJpaConverter(JsonSerde jsonSerde) {
        super(jsonSerde);
    }
}
