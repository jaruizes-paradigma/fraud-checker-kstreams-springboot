package com.paradigma.rt.streaming.kstreams.fraudchecker.topologies;

import com.paradigma.rt.streaming.kstreams.fraudchecker.model.FraudCase;
import com.paradigma.rt.streaming.kstreams.fraudchecker.model.Movement;
import com.paradigma.rt.streaming.kstreams.fraudchecker.model.MovementsAggregation;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
@Slf4j
public class FraudCheckerUtils {

    public final static float ALLOWED_ONLINE_AMOUNT_IN_SHORT_PERIOD = 200;
    public final static int MULTIPLE_ONLINE_MOVEMENTS_IN_SHORT_PERIOD = 3;
    public final static int MULTIPLE_PHYSICAL_MOVEMENTS_IN_SHORT_PERIOD = 4;
    public final static int ALLOWED_PHYSICAL_DEVICES_IN_SHORT_PERIOD = 1;

    static MovementsAggregation aggMovement(Movement movement, MovementsAggregation movementsAggregation){
        movementsAggregation.addMovement(movement);
        return movementsAggregation;
    }

    static MovementsAggregation aggMerge(MovementsAggregation fraudA, MovementsAggregation fraudB){
        log.info("Merging aggregations: [A (ID: {}, Movs: {})}, B (ID: {}, Movs: {})] ", fraudA.getId(), fraudA.getMovements().size(), fraudB.getId(), fraudB.getMovements().size());
        return fraudB;
    }

    public static FraudCase movementsAggregationToFraudCase(MovementsAggregation movementsAggregation) {
        final FraudCase fraudCase = new FraudCase();
        movementsAggregation.getMovements().forEach((movement -> fraudCase.addMovement(movement)));

        return fraudCase;
    }

    static boolean isOnlineFraud(MovementsAggregation movementsAggregation) {
        if (movementsAggregation.getMovements().size() > MULTIPLE_ONLINE_MOVEMENTS_IN_SHORT_PERIOD) {
            final Set<Movement> movements = movementsAggregation.getMovements();
            double totalAmount = movements.stream().mapToDouble((movement) -> movement.getAmount()).sum();

            return totalAmount > ALLOWED_ONLINE_AMOUNT_IN_SHORT_PERIOD;
        }

        return false;
    }

    static boolean isPhysicalFraud(MovementsAggregation movementsAggregation){
        final Set<String> devices = new HashSet<>();
        movementsAggregation.getMovements().forEach((movement -> devices.add(movement.getDevice())));

        // Multiple devices during the session
        return devices.size() > ALLOWED_PHYSICAL_DEVICES_IN_SHORT_PERIOD || movementsAggregation.getMovements().size() > MULTIPLE_PHYSICAL_MOVEMENTS_IN_SHORT_PERIOD;
    }
    
}
