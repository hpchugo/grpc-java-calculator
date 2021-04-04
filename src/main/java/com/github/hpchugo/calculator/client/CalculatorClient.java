package com.github.hpchugo.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

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
        //doAverageClientStreamingCall(channel);
        doFindMaximumBiDirectionalStreamingCall(channel);
        System.out.println("Shutting down channel");
        channel.shutdown();

    }

    private void doFactorStreamingServerCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        CalculatorManyTimesRequest calculatorManyTimesRequest = CalculatorManyTimesRequest.newBuilder()
                .setCalculator(Calculator.newBuilder()
                        .setNumber(2000001))
                .build();

        calculatorClient.calculatorManyTime(calculatorManyTimesRequest)
                .forEachRemaining(calculatorManyTimesResponse -> System.out.println(calculatorManyTimesResponse.getResult()));
    }

    private void doAverageClientStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ComputeAverageRequest> requestObserver = asyncClient.computeAverage(new StreamObserver<>() {
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

        List<Integer> numbers = Arrays.asList(3, 21, 15, 9);
        numbers.forEach(number -> {
            System.out.printf("Sending to server number %d%n", number);
            requestObserver.onNext(ComputeAverageRequest.newBuilder()
                    .setNumber(number)
                    .build());
        });

        requestObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doFindMaximumBiDirectionalStreamingCall(ManagedChannel channel) {
        //created a greet service client (blocking - asynchronous)
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<FindMaximumRequest> requestObserver = asyncClient.findMaximum(new StreamObserver<FindMaximumResponse>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                out.printf("Maximum value at the moment: %d\n", value.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                out.println("Server has completed something");
            }
        });


        Arrays.asList(1,5,3,6,2,20).forEach(
                number -> {
                    out.printf("Sending: %d\n", number);
                    requestObserver.onNext(FindMaximumRequest.newBuilder()
                            .setNumber(number)
                            .build());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
        );
        requestObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
