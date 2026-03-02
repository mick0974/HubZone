package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.output.kafka;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.persistency.Reservation;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.ReservationRepository;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.specification.ReservationSpecification;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.ChargerMetricsChangedEvent;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.ChargerOperationalStateChangedEvent;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.output.kafka.message.HubStateMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class ChargerStateKafkaPublisher {

    private final KafkaTemplate<String, HubStateMessage<?>> kafkaTemplate;
    private final ReservationRepository reservationRepository;
    @Value("${hub.init.hubTarget}")
    private String targetHubId;
    @Value("${kafka.topic}")
    private String topic = "template";
    @Value("${kafka.key}")
    private String key = "key";

    public ChargerStateKafkaPublisher(KafkaTemplate<String, HubStateMessage<?>> kafkaTemplate, ReservationRepository reservationRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.reservationRepository = reservationRepository;
    }

    @EventListener
    public void onChargerOperationalStateChanged(ChargerOperationalStateChangedEvent event) {
        log.info("[ChargerStateKafkaPublisher] Ricevuto cambio stato operativo connettore: {}", event);

        Specification<Reservation> spec =
                ReservationSpecification.hasDate(LocalDate.now())
                        .or(ReservationSpecification.hasDateAfter(LocalDate.now()));

        HubStateMessage<ChargerOperationalStateChangedEvent> message = new HubStateMessage<>(
                targetHubId,
                HubStateMessage.ChangeType.CHARGER_OPERATIONAL_STATE_CHANGED,
                event,
                reservationRepository.findAll(spec)
        );

        sendMessage(message);
    }

    @EventListener
    public void onChargerStateChanged(ChargerMetricsChangedEvent event) {
        log.info("[ChargerStateKafkaPublisher] Ricevuto cambio stato metriche hub: {}", event);

        Specification<Reservation> spec =
                ReservationSpecification.hasDate(LocalDate.now())
                        .or(ReservationSpecification.hasDateAfter(LocalDate.now()));

        HubStateMessage<ChargerMetricsChangedEvent> message = new HubStateMessage<>(
                targetHubId,
                HubStateMessage.ChangeType.CHARGER_METRICS_CHANGED,
                event,
                reservationRepository.findAll(spec)
        );

        sendMessage(message);
    }

    @AsyncPublisher(operation = @AsyncOperation(
            channelName = "hub_state",
            description = "Lo stato di uno dei connettori dell'hub è cambiato"
    ))
    private void sendMessage(@Payload HubStateMessage<?> event) {
        // log.info("Evento inviato: {}", event);

        kafkaTemplate.send(topic, targetHubId, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[KafkaPublisher] Errore invio evento {}", event, ex);
                    } else {
                        log.info(
                                "[KafkaPublisher] Evento inviato (partition={}, offset={})",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
}
