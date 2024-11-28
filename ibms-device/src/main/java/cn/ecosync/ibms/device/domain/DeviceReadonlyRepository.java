package cn.ecosync.ibms.device.domain;

import cn.ecosync.ibms.device.dto.DeviceDto;
import cn.ecosync.ibms.device.dto.DeviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface DeviceReadonlyRepository {
    Optional<DeviceDto> findByDeviceCode(String deviceCode);

    List<DeviceDto> findAll();

    Page<DeviceDto> findAll(Pageable pageable);

    void save(DeviceDto deviceDto);

    void remove(String deviceCode);

    void update(DeviceStatus deviceStatus);
}
