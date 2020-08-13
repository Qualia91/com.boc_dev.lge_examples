package com.nick.wood.game_engine.examples;

import com.nick.wood.game_engine.event_bus.event_data.PickingResponseEventData;
import com.nick.wood.game_engine.event_bus.event_types.PickingEventType;
import com.nick.wood.game_engine.event_bus.events.PickingEvent;
import com.nick.wood.game_engine.event_bus.interfaces.Event;
import com.nick.wood.game_engine.event_bus.interfaces.Subscribable;

import java.util.HashSet;
import java.util.Set;

public class PrintPickingResult implements Subscribable {

	private final Set<Class<?>> supported = new HashSet<>();

	public PrintPickingResult() {
		supported.add(PickingEvent.class);
	}

	@Override
	public void handle(Event<?> event) {
		if (event.getType().equals(PickingEventType.RESPONSE)) {
			System.out.println(((PickingResponseEventData) event.getData()).getUuid());
		}
	}

	@Override
	public boolean supports(Class<? extends Event> aClass) {
		return supported.contains(aClass);
	}
}
