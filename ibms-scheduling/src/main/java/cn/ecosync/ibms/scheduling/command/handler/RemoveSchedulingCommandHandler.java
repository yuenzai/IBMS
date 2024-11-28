package cn.ecosync.ibms.scheduling.command.handler;

import cn.ecosync.ibms.scheduling.command.RemoveSchedulingCommand;
import cn.ecosync.ibms.scheduling.domain.Scheduling;
import cn.ecosync.ibms.scheduling.domain.SchedulingApplicationService;
import cn.ecosync.ibms.scheduling.domain.SchedulingId;
import cn.ecosync.ibms.scheduling.domain.SchedulingRepository;
import cn.ecosync.iframework.command.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class RemoveSchedulingCommandHandler implements CommandHandler<RemoveSchedulingCommand> {
    private final SchedulingRepository schedulingRepository;
    private final SchedulingApplicationService schedulingApplicationService;

    @Override
    @Transactional
    public void handle(RemoveSchedulingCommand command) {
        SchedulingId schedulingId = new SchedulingId(command.getSchedulingName());
        Scheduling scheduling = schedulingRepository.get(schedulingId).orElse(null);
        Assert.notNull(scheduling, "scheduling not found:" + schedulingId);
        schedulingApplicationService.cancel(schedulingId);
        schedulingRepository.remove(scheduling);
    }
}
