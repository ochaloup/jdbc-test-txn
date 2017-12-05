package io.narayana.test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMRules;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BMUnitRunner.class)
public class JdbcTest {
    private static final String THREAD2_EXIT_SIGNAL = "thread2";

    @Test
    @BMRules(rules = {
        @BMRule(
            name = "Thread1 wait on Thread2 to finish",
            targetClass = "io.narayana.test.Thread1",
            targetMethod = "run",
            targetLocation = "AT INVOKE FlowControl.point2()",
            action = "System.out.println(\"Waiting for Thread2\"); waitFor(\"" + THREAD2_EXIT_SIGNAL + "\"); System.out.println(\"End of waiting for Thread2\");"
        ),
        @BMRule(
            name = "Thred2 emits that finished",
            targetClass = "io.narayana.test.Thread2",
            targetMethod = "run",
            targetLocation = "AT EXIT",
            action = "System.out.println(\"Waking up Thread1\"); signalWake(\"" + THREAD2_EXIT_SIGNAL + "\", true);"
        ),
    })
    public void go() throws InterruptedException, ExecutionException {
        Set<Future<?>> futures = new HashSet<>();

        ExecutorService es = Executors.newFixedThreadPool(3);
        futures.add(es.submit(new Thread1("go-thread1")));
        futures.add(es.submit(new Thread2("go-thread2")));
        futures.add(es.submit(new Thread3("go-thread3")));

        for(Future<?> f: futures) {
            f.get();
        }
        es.awaitTermination(10, TimeUnit.SECONDS);
    }

    // ignore
    public void oneByOne() {
        Runnable t1 = new Thread1("1");
        t1.run();

        Runnable t2 = new Thread3("2");
        t2.run();

        Runnable t3 = new Thread3("3");
        t3.run();
    }
}
