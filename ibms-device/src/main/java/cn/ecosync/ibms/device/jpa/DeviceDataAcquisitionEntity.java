package cn.ecosync.ibms.device.jpa;

import cn.ecosync.ibms.device.model.DeviceDataAcquisition;
import cn.ecosync.ibms.device.model.DeviceDataAcquisitionId;
import cn.ecosync.ibms.model.ConcurrencySafeEntity;
import jakarta.persistence.*;
import org.springframework.util.Assert;

import java.util.Objects;

@Entity
@Table(name = "device_data_acquisition")
public class DeviceDataAcquisitionEntity extends ConcurrencySafeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    protected Integer id;
    @Embedded
    private DeviceDataAcquisitionId dataAcquisitionId;
    @Convert(converter = DeviceDataAcquisitionConverter.class)
    @Column(name = "payload", nullable = false)
    private DeviceDataAcquisition payload;

    protected DeviceDataAcquisitionEntity() {
    }

    public DeviceDataAcquisitionEntity(DeviceDataAcquisition payload) {
        Assert.notNull(payload, "DataAcquisition payload must not be null");
        this.dataAcquisitionId = payload.getDataAcquisitionId();
        this.payload = payload;
    }

    public DeviceDataAcquisition getPayload() {
        return payload;
    }

    public void setPayload(DeviceDataAcquisition payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceDataAcquisitionEntity)) return false;
        DeviceDataAcquisitionEntity that = (DeviceDataAcquisitionEntity) o;
        return Objects.equals(this.dataAcquisitionId, that.dataAcquisitionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dataAcquisitionId);
    }
}
