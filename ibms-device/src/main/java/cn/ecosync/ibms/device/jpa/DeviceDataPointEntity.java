package cn.ecosync.ibms.device.jpa;

import cn.ecosync.ibms.device.model.DeviceDataPointId;
import jakarta.persistence.*;
import org.springframework.util.Assert;

import java.util.Objects;

@Entity
@Table(name = "device_data_point")
public class DeviceDataPointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(name = "data_acquisition_id", nullable = false, updatable = false)
    private Integer dataAcquisitionId;
    @Embedded
    private DeviceDataPointId dataPointId;

    protected DeviceDataPointEntity() {
    }

    public DeviceDataPointEntity(DeviceDataPointId dataPointId) {
        Assert.notNull(dataPointId, "dataPointId must not be null");
        this.dataPointId = dataPointId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceDataPointEntity)) return false;
        DeviceDataPointEntity that = (DeviceDataPointEntity) o;
        return Objects.equals(this.dataPointId, that.dataPointId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dataPointId);
    }
}
