package cn.ecosync.ibms.device.jpa;

import cn.ecosync.ibms.device.model.DeviceDataAcquisition;
import cn.ecosync.iframework.serde.JsonSerde;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DeviceDataAcquisitionConverter implements AttributeConverter<DeviceDataAcquisition, String> {
    private final JsonSerde jsonSerde;

    public DeviceDataAcquisitionConverter(JsonSerde jsonSerde) {
        this.jsonSerde = jsonSerde;
    }

    @Override
    public String convertToDatabaseColumn(DeviceDataAcquisition attribute) {
        return jsonSerde.serialize(attribute);
    }

    @Override
    public DeviceDataAcquisition convertToEntityAttribute(String dbData) {
        return jsonSerde.deserialize(dbData, DeviceDataAcquisition.class);
    }
}
