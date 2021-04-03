package com.github.hpchugo.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorStreamingClient {
    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext() //Disable ssl
                .build();

        System.out.println("Creating stub");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);


        CalculatorManyTimesRequest calculatorManyTimesRequest = CalculatorManyTimesRequest.newBuilder()
                .setCalculator(Calculator.newBuilder()
                        .setFactor(2000001))
                .build();

        calculatorClient.calculatorManyTime(calculatorManyTimesRequest)
                .forEachRemaining(calculatorManyTimesResponse -> System.out.println(calculatorManyTimesResponse.getResult()));

        System.out.println("Shutting down channel");
        channel.shutdown();

    }
}
