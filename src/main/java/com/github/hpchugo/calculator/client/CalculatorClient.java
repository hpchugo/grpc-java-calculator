package com.github.hpchugo.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {
    public static void main(String[] args) {
        CalculatorClient calculatorClient = new CalculatorClient();
        calculatorClient.run();
    }

    private void run() {
        System.out.println("Hello I'm a gRPC client");
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext() //Disable ssl
                .build();

        System.out.println("Creating stub");

        //doFactorStreamingServerCall(channel);
        doAverageClientStreamingCall(channel);
        System.out.println("Shutting down channel");
        channel.shutdown();

    }

    private void doFactorStreamingServerCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        CalculatorManyTimesRequest calculatorManyTimesRequest = CalculatorManyTimesRequest.newBuilder()
                .setCalculator(Calculator.newBuilder()
                        .setFactor(2000001))
                .build();

        calculatorClient.calculatorManyTime(calculatorManyTimesRequest)
                .forEachRemaining(calculatorManyTimesResponse -> System.out.println(calculatorManyTimesResponse.getResult()));
    }

    private void doAverageClientStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ComputeAverageRequest> requestObserver = asyncClient.computeAverage(new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                System.out.println("Received a response from the server");
                System.out.println(value.getAverage());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed");
                latch.countDown();
            }
        });

        System.out.println("Sending number #1");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(3)
                .build());

        System.out.println("Sending number #2");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(21)
                .build());

        System.out.println("Sending number #4");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(15)
                .build());

        System.out.println("Sending number #3");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(9)
                .build());

        requestObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
