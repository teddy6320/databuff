package com.databuff.apm.ingest.pipeline.task;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AsyncTask implements Task, EventHandler<MutableEvent> {

    private static final EventTranslatorTwoArg<MutableEvent, Object, Object> TRANSLATOR =
            (event, sequence, key, payload) -> event.set(key, payload);

    private final int bufferSize;
    private final int index;
    private final AtomicLong overflowCount = new AtomicLong();
    private Disruptor<MutableEvent> disruptor;
    private RingBuffer<MutableEvent> ringBuffer;

    protected AsyncTask(int bufferSize, int index) {
        this.bufferSize = bufferSize;
        this.index = index;
    }

    @Override
    public void init() {
        disruptor = new Disruptor<>(
                MutableEvent::new,
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new com.lmax.disruptor.BlockingWaitStrategy());
        ringBuffer = disruptor.getRingBuffer();
        disruptor.handleEventsWith(this);
        disruptor.start();
    }

    @Override
    public void onEvent(MutableEvent mutableEvent, long sequence, boolean endOfBatch) {
        Object key = mutableEvent.getKey();
        Object event = mutableEvent.getEvent();
        mutableEvent.clear();
        processEvent(key, event);
    }

    protected abstract void processEvent(Object key, Object event);

    @Override
    public void onShutdown() {
        // hook for subclasses
    }

    @Override
    public void close() {
        if (disruptor != null) {
            try {
                disruptor.shutdown(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                disruptor.halt();
            }
        }
        onShutdown();
    }

    @Override
    public boolean handleEvent(Object key, Object event) {
        if (ringBuffer == null) {
            return false;
        }
        if (ringBuffer.tryPublishEvent(TRANSLATOR, key, event)) {
            return true;
        }
        if (ringBuffer.tryPublishEvent(TRANSLATOR, key, event)) {
            return true;
        }
        overflowCount.incrementAndGet();
        return false;
    }

    public long overflowCount() {
        return overflowCount.get();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName() + "-" + index;
    }
}
