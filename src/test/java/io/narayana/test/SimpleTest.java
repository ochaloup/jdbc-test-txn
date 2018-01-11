package io.narayana.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

@RunWith(BMUnitRunner.class)
public class SimpleTest {

    @Rule
    public TestName method = new TestName();

    @BeforeClass
    public static void setUp() {
        TestUtils.createTables();
    }

    @Test
    public void insertSelect() throws Exception {
        String name = method.getMethodName();

        ExecutorService es = Executors.newFixedThreadPool(1);

        Future<?> deleteAll = es.submit(new DeleteThread(name));
        deleteAll.get();

        Future<Integer> insert = es.submit(new InsertThread(name));
        int random = insert.get();

        Future<ResultSet> select = es.submit(new SelectThread(name));
        ResultSet result = select.get();

        assertThat(result.next()).isTrue();
        assertThat(result.getInt("random")).isEqualTo(random);

        System.out.printf("name: %s, random: %s",
            result.getString("node_name"), result.getInt("random"));

        assertThat(result.next()).isFalse();
    }
    
    @Test
    public void insertUpdate() throws Exception {
        String name = method.getMethodName();

        ExecutorService es = Executors.newFixedThreadPool(1);

        Future<Integer> insert = es.submit(new InsertThread(name));
        int random = insert.get();

        Future<Integer> update = es.submit(new UpdateThread(name));
        Integer updateRandom = update.get();

        Future<ResultSet> select = es.submit(new SelectThread(name));
        ResultSet result = select.get();

        assertThat(result.next()).isTrue();

        boolean isContinue = true;
        while(isContinue) {
            assertThat(result.getInt("random")).isEqualTo(updateRandom);
            isContinue = result.next();
        }
    }
}
