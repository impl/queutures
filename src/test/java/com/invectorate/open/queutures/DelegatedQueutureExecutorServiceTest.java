package com.invectorate.open.queutures;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.base.Throwables;

@RunWith(JUnit4.class)
public class DelegatedQueutureExecutorServiceTest {

    @Test
    public void submittedInformableIsExecutedAsQueuture() throws InterruptedException, ExecutionException {
        DelegatedQueutureExecutorService executorService = new DelegatedQueutureExecutorService(Executors.newSingleThreadExecutor());
        Queuture<String> queuture = executorService.submit(new Informable<QueutureBox<String>>() {

            @Override
            public void inform(final QueutureBox<String> box) {
                try {
                    box.put("Hello!");
                    box.put("Goodbye!");
                } catch (InterruptedException ie) {
                    Throwables.propagate(ie);
                }
            }

        });

        Assert.assertEquals("Hello!", queuture.next());
        Assert.assertEquals("Goodbye!", queuture.next());
        Assert.assertNull(queuture.next());

        Assert.assertTrue(queuture.isDone());
    }

}
