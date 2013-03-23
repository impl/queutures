package com.invectorate.open.queutures;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.base.Throwables;
import com.google.common.collect.Queues;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

@RunWith(JUnit4.class)
public class QueutureTaskTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void resultsArePassedThroughBoxToQueuture() throws InterruptedException, ExecutionException {
        QueutureTask<String> queuture = QueutureTaskTest.newQueutureTaskWithBasicInformable();

        queuture.run();

        Assert.assertFalse(queuture.isCancelled());
        Assert.assertTrue(queuture.isDone());

        Assert.assertEquals("Hello!", queuture.next());
        Assert.assertEquals("Goodbye!", queuture.next());
        Assert.assertNull(queuture.next());
    }

    @Test
    public void cancellingBeforeRunGeneratesNoResults() throws InterruptedException, ExecutionException {
        QueutureTask<String> queuture = QueutureTaskTest.newQueutureTaskWithBasicInformable();

        queuture.cancel(false);
        queuture.run();

        Assert.assertTrue(queuture.isCancelled());
        Assert.assertTrue(queuture.isDone());

        this.exceptionRule.expect(CancellationException.class);
        queuture.next();
    }

    @Test
    public void cancellingAfterRunDoesNothing() throws InterruptedException, ExecutionException {
        QueutureTask<String> queuture = QueutureTaskTest.newQueutureTaskWithBasicInformable();

        queuture.run();
        queuture.cancel(false);

        Assert.assertFalse(queuture.isCancelled());
        Assert.assertTrue(queuture.isDone());

        Assert.assertEquals("Hello!", queuture.next());
    }

    @Test
    public void exceptionThrownWhileRunningIsPassedThroughToQueuture() throws InterruptedException, ExecutionException {
        QueutureTask<String> queuture = QueutureTaskTest.newQueutureTaskWithThrowingInformable();

        queuture.run();

        this.exceptionRule.expect(ExecutionException.class);
        this.exceptionRule.expectCause(CoreMatchers.isA(IllegalStateException.class));
        queuture.next();
    }

    @Test
    public void resultsArePassedThroughBoxToQueutureWithoutTimeouts() throws TimeoutException, InterruptedException, ExecutionException {
        QueutureTask<String> queuture = QueutureTaskTest.newQueutureTaskWithBasicInformable();

        queuture.run();

        Assert.assertFalse(queuture.isCancelled());
        Assert.assertTrue(queuture.isDone());

        Assert.assertEquals("Hello!", queuture.next(25, TimeUnit.MILLISECONDS));
        Assert.assertEquals("Goodbye!", queuture.next(25, TimeUnit.MILLISECONDS));
        Assert.assertNull(queuture.next(25, TimeUnit.MILLISECONDS));
    }

    @Test
    public void computationTimesOutIfQueueIsFull() throws InterruptedException {
        QueutureTask<String> queuture = QueutureTaskTest.newQueutureTaskWithComputationTimeoutInformable();

        queuture.run();

        Assert.assertFalse(queuture.isCancelled());
        Assert.assertTrue(queuture.isDone());

        try {
            queuture.next();
            Assert.fail("QueutureTask#next() must fail when the computation fails");
        } catch (ExecutionException ee) {
            Assert.assertTrue(RuntimeException.class.isInstance(ee.getCause()));
            Assert.assertTrue(TimeoutException.class.isInstance(ee.getCause().getCause()));
        }
    }

    /**
     * Container for multithreaded interaction with {@link QueutureTaskTest#threadWaitsUntilResultsAreAvailable()}.
     */
    protected class ThreadWaitsUntilResultsAreAvailableInteraction extends MultithreadedTestCase {

        private QueutureTask<String> queuture = null;

        @Override
        public void initialize() {
            this.queuture = QueutureTaskTest.newQueutureTaskWithWaitingInformable(this);
        }

        @Override
        public void finish() {
            this.queuture = null;
        }

        public void thread1() {
            this.queuture.run();
        }

        public void thread2() throws InterruptedException, ExecutionException {
            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertFalse(this.queuture.isDone());

            Assert.assertEquals("Hello!", this.queuture.next());

            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertFalse(this.queuture.isDone());

            Assert.assertEquals("Goodbye!", this.queuture.next());

            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertFalse(this.queuture.isDone());

            Assert.assertNull(this.queuture.next());

            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertTrue(this.queuture.isDone());
        }

    }

    @Test
    public void threadWaitsUntilResultsAreAvailable() throws Throwable {
        TestFramework.runOnce(new ThreadWaitsUntilResultsAreAvailableInteraction());
    }

    /**
     * Container for multithreaded interaction with {@link QueutureTaskTest#threadTimesOutAfterWaitingWithoutResults()}.
     */
    protected class ThreadTimesOutAfterWaitingWithoutResultsInteraction extends MultithreadedTestCase {

        private QueutureTask<String> queuture = null;

        @Override
        public void initialize() {
            this.queuture = QueutureTaskTest.newQueutureTaskWithWaitingInformable(this);
        }

        @Override
        public void finish() {
            this.queuture = null;
        }

        public void thread1() {
            this.queuture.run();
        }

        public void thread2() throws InterruptedException, ExecutionException, TimeoutException {
            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertFalse(this.queuture.isDone());

            this.freezeClock();
            try {
                this.queuture.next(25, TimeUnit.MILLISECONDS);
                Assert.fail("QueutureTask#next() must throw a TimeoutException when nothing has been generated in time");
            } catch (TimeoutException te) {}
            this.unfreezeClock();

            this.assertTick(0);
        }

    }

    @Test
    public void threadTimesOutAfterWaitingWithoutResults() throws Throwable {
        TestFramework.runOnce(new ThreadTimesOutAfterWaitingWithoutResultsInteraction());
    }

    /**
     * Container for multithreaded interaction with {@link QueutureTaskTest#multipleThreadsCanReceiveResults()}.
     */
    protected class MultipleThreadsCanReceiveResultsInteraction extends MultithreadedTestCase {

        private QueutureTask<String> queuture = null;

        @Override
        public void initialize() {
            this.queuture = QueutureTaskTest.newQueutureTaskWithWaitingInformable(this);
        }

        @Override
        public void finish() {
            this.queuture = null;
        }

        public void thread1() {
            this.queuture.run();
        }

        public void thread2() throws InterruptedException, ExecutionException {
            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertFalse(this.queuture.isDone());

            Assert.assertNotNull(this.queuture.next());

            this.waitForTick(1000);

            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertTrue(this.queuture.isDone());

            Assert.assertNull(this.queuture.next());
        }

        public void thread3() throws InterruptedException, ExecutionException {
            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertFalse(this.queuture.isDone());

            Assert.assertNotNull(this.queuture.next());

            this.waitForTick(1000);

            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertTrue(this.queuture.isDone());

            Assert.assertNull(this.queuture.next());
        }

    }

    @Test
    public void multipleThreadsCanReceiveResults() throws Throwable {
        TestFramework.runOnce(new MultipleThreadsCanReceiveResultsInteraction());
    }

    /**
     * Container for multithreaded interaction with {@link QueutureTaskTest#threadCanBeCancelledWhileRunning()}.
     */
    protected class ThreadCanBeCancelledWhileRunningInteraction extends MultithreadedTestCase {

        private QueutureTask<String> queuture = null;

        @Override
        public void initialize() {
            this.queuture = QueutureTaskTest.newQueutureTaskWithWaitingInformable(this);
        }

        @Override
        public void finish() {
            this.queuture = null;
        }

        public void thread1() {
            this.queuture.run();
        }

        public void thread2() throws InterruptedException, ExecutionException {
            Assert.assertFalse(this.queuture.isCancelled());
            Assert.assertFalse(this.queuture.isDone());

            Assert.assertEquals("Hello!", this.queuture.next());

            this.queuture.cancel(true);

            Assert.assertTrue(this.queuture.isCancelled());
            Assert.assertTrue(this.queuture.isDone());

            QueutureTaskTest.this.exceptionRule.expect(CancellationException.class);
            this.queuture.next();
        }

    }

    @Test
    public void threadCanBeCancelledWhileRunning() throws Throwable {
        TestFramework.runOnce(new ThreadCanBeCancelledWhileRunningInteraction());
    }

    protected static QueutureTask<String> newQueutureTaskWithBasicInformable() {
        Informable<QueutureBox<String>> informable = new Informable<QueutureBox<String>>() {

            @Override
            public void inform(final QueutureBox<String> box) {
                try {
                    box.put("Hello!");
                    box.put("Goodbye!");
                } catch (InterruptedException ie) {
                    Throwables.propagate(ie);
                }
            }

        };
        return QueutureTaskTest.newQueutureTask(informable);
    }

    protected static QueutureTask<String> newQueutureTaskWithThrowingInformable() {
        Informable<QueutureBox<String>> informable = new Informable<QueutureBox<String>>() {

            @Override
            public void inform(final QueutureBox<String> box) {
                throw new IllegalStateException("I AM A BANANA");
            }

        };
        return QueutureTaskTest.newQueutureTask(informable);
    }

    protected static QueutureTask<String> newQueutureTaskWithWaitingInformable(final MultithreadedTestCase mtc) {
        Informable<QueutureBox<String>> informable = new Informable<QueutureBox<String>>() {

            @Override
            public void inform(final QueutureBox<String> box) {
                try {
                    mtc.waitForTick(100);
                    box.put("Hello!");
                    mtc.waitForTick(200);
                    box.put("Goodbye!");
                    mtc.waitForTick(300);
                } catch (InterruptedException ie) {
                    Throwables.propagate(ie);
                }
            }
        };
        return QueutureTaskTest.newQueutureTask(informable);
    }

    protected static QueutureTask<String> newQueutureTaskWithComputationTimeoutInformable() {
        Informable<QueutureBox<String>> informable = new Informable<QueutureBox<String>>() {

            @Override
            public void inform(final QueutureBox<String> box) {
                try {
                    box.put("Hello!");
                    box.put("Goodbye!", 100, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | InterruptedException e) {
                    Throwables.propagate(e);
                }
            }
        };
        return QueutureTaskTest.newQueutureTaskWithTinyQueue(informable);
    }

    protected static <V> QueutureTask<V> newQueutureTask(final Informable<QueutureBox<V>> informable) {
        return new QueutureTask<V>(informable, Queues.<V> newLinkedBlockingQueue());
    }

    protected static <V> QueutureTask<V> newQueutureTaskWithTinyQueue(final Informable<QueutureBox<V>> informable) {
        return new QueutureTask<V>(informable, Queues.<V> newLinkedBlockingDeque(1));
    }

}
