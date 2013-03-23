# Queutures

Queutures are like futures, but better!

With a typical Java [Future](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html), you're limited to just one result (accessible via the `Future.get()` methods). But with a Queuture, you have access to many, many more results.

## Use case

Imagine you have a task that you want to run asynchronously, such as crawling a Website. The task itself belongs in a single thread (because it's iterative), but it will produce many results during the course of its execution, and those results will be produced at abitrary times, potentially with large delays between them.

With a typical `Future`, you'd have to wait until all of your crawling was complete before producing a large data set as the result of your computation. On the other hand, you could use a `BlockingQueue` to pass information on in pieces, but that requires a lot of overhead and can get messy if there are tons of queues floating around.

When you use a `Queuture`, you get the best of both worlds: a simple object that represents many results of an asynchronous computation using a queue-like interface, but without the code overhead of having to build queues manually. It's sort of like a three-inch-tall version of the actor model.

## How do I use it?

For a computation that produces many results, implement the `Informable` interface as follows:

```java
public class AReallyHardWorker implements Informable<QueutureBox<Integer>> {

    @Override
    public void inform(QueutureBox<Integer> box) {
        try {
            /* Work hard! */
            Thread.sleep(100);

            /* Add the result to the queuture. */
            box.put(1);

            /* Work even harder! */
            Thread.sleep(5000);

            /* Add another result. */
            box.put(2);
        } catch (InterruptedException ie) {}
    }

}
```

A consumer looks suspiciously similar to a `Future` and integrates through any `ExecutorService`:

```java
ExecutorService myService = new DelegatingQueutureExecutorService(Executors.newCachedThreadPool());
Queuture<Integer> queuture = myService.submit(new AReallyHardWorker());

Integer v;
while ((v = queuture.next()) != null) {
    System.out.println(v);
}
```

## License

This library is licensed under the GNU GPL (version 2). It uses source code from OpenJDK that is licensed equivalently.

## Documentation

We have made available the [API documentation](http://invectorate.github.com/queutures/apidocs/) for easy consumption.

## FAQ

<dl>
    <dt>I don't like the name "Queutures," and you should change it.</dt>
    <dd>Well I happen to like the name. Feel free to fork it.</dd>
    <dt>I don't like the license.</dt>
    <dd>Me neither, but we're stuck with it unless someone feels like rewriting <tt>QueutureTask</tt> without using any existing code from the Java class library.</dd>
    <dt>Neither of those were questions, and this isn't either.</dt>
    <dd>For some reason "Frequently Stated Statements" just doesn't have the same ring to it.</dd>
</dl>
