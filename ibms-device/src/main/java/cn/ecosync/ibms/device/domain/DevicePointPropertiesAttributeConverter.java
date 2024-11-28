package cn.ecosync.ibms.device.domain;

import cn.ecosync.ibms.dto.DevicePointExtra;
import cn.ecosync.iframework.serde.JsonSerde;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DevicePointPropertiesAttributeConverter implements AttributeConverter<DevicePointExtra, String> {
    private final JsonSerde jsonSerde;

    public DevicePointPropertiesAttributeConverter(JsonSerde jsonSerde) {
        this.jsonSerde = jsonSerde;
    }

    @Override
    public String convertToDatabaseColumn(DevicePointExtra attribute) {
        return jsonSerde.serialize(attribute);
    }

    @Override
    public DevicePointExtra convertToEntityAttribute(String dbData) {
        return jsonSerde.deserialize(dbData, DevicePointExtra.class);
    }
}
