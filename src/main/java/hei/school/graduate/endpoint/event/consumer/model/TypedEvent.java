package hei.school.graduate.endpoint.event.consumer.model;

import hei.school.graduate.PojaGenerated;
import hei.school.graduate.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
