package com.airtel.scheduler.execution.actions;

import com.airtel.scheduler.execution.enums.ActionType;
import com.airtel.scheduler.execution.exception.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ActionFactory {

    private static Map<ActionType, Actions> actionsMap = new EnumMap<>(ActionType.class);

    @Autowired
    private ActionFactory(List<Actions> actions) {
        populateFactoryMap(actions.stream().collect(Collectors.toMap(Actions::getType, Function.identity())));
    }

    public static Actions getAction(ActionType actionType) {
        return Optional.ofNullable(actionsMap.get(actionType)).orElseThrow(() -> new SchedulerException("Action Not Found"));
    }

    public static void populateFactoryMap(Map<ActionType, Actions> factoryMap) {
        actionsMap = factoryMap;
    }
}