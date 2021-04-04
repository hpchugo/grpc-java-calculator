package com.github.hpchugo.calculator.server;

import com.proto.calculator.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;


public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void calculatorManyTime(CalculatorManyTimesRequest request, StreamObserver<CalculatorManyTimesResponse> responseObserver) {
        var number = request.getCalculator().getNumber();
        int divisor = 2;
        while (number > 1) {
            if (number % divisor == 0) {
                number = number / divisor;
                responseObserver.onNext(CalculatorManyTimesResponse.newBuilder()
                        .setResult(divisor)
                        .build());
            } else {
                divisor = divisor + 1;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        List<Integer> result = new ArrayList<>();


        return new StreamObserver<>() {

            @Override
            public void onNext(ComputeAverageRequest value) {
                result.add(value.getNumber());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(ComputeAverageResponse
                        .newBuilder()
                        .setAverage(result.stream().mapToDouble(a -> a).average().orElse(0))
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<>() {

            List<Integer> numbers = new ArrayList<>();

            @Override
            public void onNext(FindMaximumRequest value) {
                numbers.add(value.getNumber());
                responseObserver.onNext(FindMaximumResponse.newBuilder()
                        .setMaximum(getMaxValue())
                        .build());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        FindMaximumResponse.newBuilder()
                                .setMaximum(getMaxValue())
                                .build());
                responseObserver.onCompleted();
            }

            private int getMaxValue() {
                return numbers.stream().mapToInt(a -> a).max().getAsInt();
            }

        };
    }
}
