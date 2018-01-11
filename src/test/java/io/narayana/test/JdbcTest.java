package io.narayana.test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMRules;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

@RunWith(BMUnitRunner.class)
public class JdbcTest {
    private static final String THREAD1_WORK_DONE = "Thread1-work-done";
    private static final String THREAD2_EXIT_SIGNAL = "Thread2-exit";

    @Rule
    public TestName method = new TestName();

    @BeforeClass
    public static void setUp() {
        TestUtils.createTables();
    }

    @Test
    @BMRules(rules = {
        @BMRule(
                name = "Thread2 waits on Thread1 to get its part being done",
                targetClass = "io.narayana.test.Thread2",
                targetMethod = "run",
                targetLocation = "AT INVOKE FlowControl.thread2WaitingThread1()",
                action = "System.out.println(\"Byteman: waiting for Thread1\");" +
                         "waitFor(\"" + THREAD1_WORK_DONE + "\");" +
                         "System.out.println(\"Byteman: end of waiting for Thread1\");"
                ),
        @BMRule(
            name = "Thread1 wait on Thread2 to finish",
            targetClass = "io.narayana.test.Thread1",
            targetMethod = "call",
            targetLocation = "AT INVOKE FlowControl.thread1WaitingThread2()",
            action = "System.out.println(\"Byteman: signaling Thread 1 work done and waiting for Thread2\");" +
                     "signalWake(\"" + THREAD1_WORK_DONE + "\", true);" +
                     "waitFor(\"" + THREAD2_EXIT_SIGNAL + "\");" +
                     "System.out.println(\"Byteman: end of waiting for Thread2\");"
        ),
        @BMRule(
            name = "Thread2 emits that's finished",
            targetClass = "io.narayana.test.Thread2",
            targetMethod = "run",
            targetLocation = "AT EXIT",
            action = "System.out.println(\"Byteman: waking up Thread1\");" +
                     "signalWake(\"" + THREAD2_EXIT_SIGNAL + "\", true);"
        ),
    })
    public void bytemanOrdering() throws InterruptedException, ExecutionException {
        Set<Future<?>> futures = new HashSet<>();

        ExecutorService es = Executors.newFixedThreadPool(3);
        Future<Exception> fe = es.submit(new Thread1(method.getMethodName()));
        futures.add(es.submit(new Thread2(method.getMethodName())));
        futures.add(es.submit(new Thread3(method.getMethodName())));

        Exception e = fe.get();
        TestUtils.waitToEnd(futures, es);

        Assert.assertEquals("The thread ordering should give IllegalStateException here",
                IllegalStateException.class, e.getClass());
    }

    @Test
    public void oneByOne() throws Exception {
        Callable<Exception> t1 = new Thread1(method.getMethodName());
        Exception e = t1.call();
        Assert.assertNull(
            "Expecting no exception on retrieving saved node name random as threads ordered one by one", e);

        Runnable t2 = new Thread3(method.getMethodName());
        t2.run();

        Runnable t3 = new Thread3(method.getMethodName());
        t3.run();
    }

    @Test
    public void whatEverOrder1() {
        Set<Future<?>> futures = new HashSet<>();

        ExecutorService es = Executors.newFixedThreadPool(3);
        futures.add(es.submit(new Thread1(method.getMethodName())));
        futures.add(es.submit(new Thread2(method.getMethodName())));
        futures.add(es.submit(new Thread3(method.getMethodName())));

        TestUtils.waitToEnd(futures, es);
    }

    @Test
    public void whatEverOrder2() {
        Set<Future<?>> futures = new HashSet<>();

        ExecutorService es = Executors.newFixedThreadPool(3);
        futures.add(es.submit(new Thread3(method.getMethodName())));
        futures.add(es.submit(new Thread2(method.getMethodName())));
        futures.add(es.submit(new Thread1(method.getMethodName())));

        TestUtils.waitToEnd(futures, es);
    }

    @Test
    public void whatEverOrder3() throws InterruptedException {
        Set<Future<?>> futures = new HashSet<>();

        ExecutorService es = Executors.newFixedThreadPool(3);
        futures.add(es.submit(new Thread3(method.getMethodName())));
        futures.add(es.submit(new Thread1(method.getMethodName())));
        Thread.sleep(100);
        futures.add(es.submit(new Thread2(method.getMethodName())));

        TestUtils.waitToEnd(futures, es);
    }
}
