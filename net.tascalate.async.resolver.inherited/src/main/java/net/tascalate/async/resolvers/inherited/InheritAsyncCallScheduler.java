package net.tascalate.async.resolvers.inherited;

import org.kohsuke.MetaInfServices;

import net.tascalate.async.api.Scheduler;
import net.tascalate.async.spi.ActiveAsyncCall;
import net.tascalate.async.spi.SchedulerResolver;

@MetaInfServices
public class InheritAsyncCallScheduler implements SchedulerResolver {

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public Scheduler resolve(Object owner, Class<?> ownerDeclaringClass) {
        return ActiveAsyncCall.scheduler();
    }
    
}
