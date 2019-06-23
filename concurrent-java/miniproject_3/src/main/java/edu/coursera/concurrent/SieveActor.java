package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor(2);
        finish(() -> {
            for (int i = 3; i <= limit; i += 2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });
        int primeCount = 0;
        SieveActorActor loopActor = sieveActor;
        while (loopActor != null) {
            primeCount += loopActor.numLocalPrime;
            loopActor = loopActor.nextActor;
        }
        return primeCount;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {

        private static final int MAX_LOCAL_PRIMES = 500;
        private List<Integer> primes;
        private int numLocalPrime;
        private SieveActorActor nextActor;

        public SieveActorActor(final int localPrime) {
            primes = new ArrayList<>();
            primes.add(localPrime);
            this.nextActor = null;
            this.numLocalPrime = 1;
        }
        /**
         * Process a single message sent to this actor.
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;
            if (candidate <= 0) {
                if (nextActor != null){
                    nextActor.send(msg);
                }
               // exit()
            } else {
                final boolean locallyPrime = isLocallyPrime(candidate);
                if (locallyPrime) {
                    if (primes.size() <= MAX_LOCAL_PRIMES) {
                        primes.add(candidate);
                        numLocalPrime++;
                    } else if (nextActor == null) {
                        nextActor = new SieveActorActor(candidate);
                    } else {
                        nextActor.send(msg);
                    }
                }
            }
        }

        private boolean isLocallyPrime(final Integer candidate) {
            return primes
                    .stream()
                    .noneMatch(prime -> candidate % prime == 0);
        }
    }
}
