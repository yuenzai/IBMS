package cn.ecosync.ibms.device.model;

import cn.ecosync.ibms.device.dto.DeviceSchema;
import cn.ecosync.ibms.metrics.InstrumentEnum;
import cn.ecosync.ibms.metrics.InstrumentKindEnum;
import cn.ecosync.ibms.metrics.MeasurementTypeEnum;
import cn.ecosync.iframework.util.CollectionUtils;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableMeasurement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public interface IDeviceSchemas {
    Collection<? extends DeviceSchema> getSchemaItems();

    default Map<String, ObservableMeasurement> toObservableMeasurements(Meter meter, Collection<DeviceId> deviceIds) {
        Collection<? extends DeviceSchema> schemas = getSchemaItems();
        Map<String, ObservableMeasurement> observableMeasurements = new LinkedHashMap<>();

        for (DeviceId deviceId : deviceIds) {
            for (DeviceSchema schema : schemas) {
                InstrumentKindEnum instrumentKind = schema.getInstrumentKind();
                MeasurementTypeEnum measurementType = schema.getMeasurementType();
                String instrumentName = deviceId.toString() + "." + schema.getName();
                ObservableMeasurement observableMeasurement;

                observableMeasurement = InstrumentEnum.getInstrumentEnum(instrumentKind, measurementType)
                        .toObservableMeasurement(meter, instrumentName);
                if (observableMeasurement != null)
                    observableMeasurements.put(instrumentName, observableMeasurement);
            }
        }
        return observableMeasurements;
    }

    default boolean isUniqueName() {
        return CollectionUtils.hasUniqueElement(getSchemaItems(), DeviceSchema::getName);
    }
}
