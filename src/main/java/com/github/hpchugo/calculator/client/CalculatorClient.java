package com.github.hpchugo.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;
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

        List<Integer> numbers = Arrays.asList(3, 21, 15, 9);
        numbers.forEach(number -> {
            System.out.println(String.format("Sending to server number %d", number));
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
}
